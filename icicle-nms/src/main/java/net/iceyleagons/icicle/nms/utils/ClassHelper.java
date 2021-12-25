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

package net.iceyleagons.icicle.nms.utils;

import org.bukkit.Bukkit;

import java.util.logging.Logger;

/**
 * @author TOTHTOMI
 * @version 1.0.0
 * @since Dec. 23, 2021
 */
public class ClassHelper {

    private static final String cbString;
    private static final String mcString;
    private static final Logger logger = Logger.getLogger("ClassHelper");

    static {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        cbString = "org.bukkit.craftbukkit." + version + ".";
        mcString = "net.minecraft.server." + version + ".";
    }

    public static AdvancedClass<?> getBukkitClass(String name) {
        return new AdvancedClass<>(getClass(cbString + name));
    }

    public static AdvancedClass<?> getNMSClass(String name) {
        return new AdvancedClass<>(getClass(mcString + name));
    }

    public static Class<?> getClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            logger.warning("Class " + name + " was not found due to: " + e.getMessage());
            return null;
        }
    }
}