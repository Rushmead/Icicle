/*
 * MIT License
 *
 * Copyright (c) 2021 IceyLeagons and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.iceyleagons.gradle;

import com.amihaiemil.eoyaml.Yaml;
import com.amihaiemil.eoyaml.YamlMapping;
import lombok.Setter;
import lombok.val;
import lombok.var;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.file.Deleter;

import javax.inject.Inject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;

public class IcicleConfigTask extends DefaultTask {

    private final DirectoryProperty outputDirectory;
    @Setter
    private IcicleAddonData data;

    public IcicleConfigTask() {
        ObjectFactory objectFactory = getProject().getObjects();
        outputDirectory = objectFactory.directoryProperty();
    }

    @OutputDirectory
    public DirectoryProperty getOutputDirectory() {
        return outputDirectory;
    }

    @Inject
    protected Deleter getDeleter() {
        throw new UnsupportedOperationException("Decorator takes care of injection");
    }

    private YamlMapping createYaml() {
        var yamlBuilder = Yaml.createYamlMappingBuilder().add("name", data.getName()).add("dependency-notation", data.getDependencyNotation()).add("description", data.getDescription()).add("version", data.getVersion());
        if (!data.getDevelopers().isEmpty()) {
            var sequenceBuilder = Yaml.createYamlSequenceBuilder();
            for (String developer : data.getDevelopers())
                sequenceBuilder = sequenceBuilder.add(developer);
            yamlBuilder = yamlBuilder.add("developers", sequenceBuilder.build());
        }

        if (!data.getDependencies().isEmpty()) {
            var sequenceBuilder = Yaml.createYamlSequenceBuilder();
            for (String dependency : data.getDependencies())
                sequenceBuilder = sequenceBuilder.add(dependency);
            yamlBuilder = yamlBuilder.add("dependencies", sequenceBuilder.build());
        }

        if (!data.getRuntimeDownloads().isEmpty()) {
            var sequenceBuilder = Yaml.createYamlSequenceBuilder();
            for (String dependency : data.getRuntimeDownloads())
                sequenceBuilder = sequenceBuilder.add(dependency);
            yamlBuilder = yamlBuilder.add("runtime-downloads", sequenceBuilder.build());
        }

        return yamlBuilder.build();
    }

    @TaskAction
    public void generateIcicleConfig() {
        // Clean output directory
        val outputDir = outputDirectory.get().getAsFile();
        clearOutputDirectory(outputDir);

        // Write contents of the icicle.yml file.
        try (FileWriter fw = new FileWriter(new File(outputDir, "icicle.yml"))) {
            fw.write(createYaml().toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void clearOutputDirectory(File directoryToClear) {
        try {
            getDeleter().ensureEmptyDirectory(directoryToClear);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
