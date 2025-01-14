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

plugins {
    java
    id("net.iceyleagons.icicle-gradle") version "1.5-SNAPSHOT"
}

group = "net.iceyleagons"
version = "0.1-SNAPSHOT"

val spigotVersion = "1.17.1"

repositories {
    mavenCentral()
    spigot()
    jitpack()
}

dependencies {
    implementation(project(":icicle-core"))
    implementation(project(":icicle-utilities"))
    implementation("org.jetbrains:annotations:20.1.0")
    spigotApi(spigotVersion)
    lombok()
}

icicle {
    name = "Minecraft Commands"

    dependencyNotation = "net.iceyleagons:icicle-addon-commands:$version"
    version = project.version.toString()
    description = "A complete command framework ready-to-use in your project!"
    developers = listOf("TOTHTOMI", "Gabe")

    dependencies += "net.iceyleagons:icicle-addon-core:0.1-SNAPSHOT"
    dependencies += "net.iceyleagons:icicle-addon-utilities:0.1-SNAPSHOT"
}

tasks.test {
    useJUnitPlatform()
}