/*
 * Copyright (C) 2005-2017 Qihoo 360 Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed To in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package com.qihoo360.replugin.gradle.plugin.inner

import org.gradle.api.Project

import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * @author RePlugin Team
 */
public class Util {

    /**
     * 获取 App Project 目录
     */
    def static appModuleDir(Project project) {
        appProject(project).projectDir.absolutePath
    }

    /**
     * 获取 App Project
     */
    def static appProject(Project project) {
        def modelName = CommonData.appModule.trim()
        if ('' == modelName || ':' == modelName) {
            return project
        }
        project.project(modelName)
    }

    /**
     * 将字符串的某个字符转换成 小写
     */
    def public static lowerCaseAtIndex(String str, int index) {
        def len = str.length()
        if (index > -1 && index < len) {
            return str.substring(0, index) + str.substring(index, index + 1).toLowerCase() + str.substring(index + 1)
        }
        str
    }

    def static newSection() {
        50.times {
            print '--'
        }
        println()
    }

    def static boolean isZipEmpty(String zipFilePath) {
        ZipFile z = null
        try {
            z = new ZipFile(zipFilePath)
            return z.size() == 0
        } catch (Throwable ignored) {
            return true
        } finally {
            if (z != null) {
                z.close()
            }
        }
    }

    static void copyDirectory(File src, File dest) {
        dest.mkdirs()
        src.eachFileRecurse { File f ->
            def rel = src.toPath().relativize(f.toPath())
            File out = dest.toPath().resolve(rel).toFile()
            if (f.isDirectory()) {
                out.mkdirs()
            } else {
                out.parentFile.mkdirs()
                Files.copy(f.toPath(), out.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }
        }
    }

    static void unzip(File zipFile, File destDir) {
        destDir.mkdirs()
        def zis = new ZipInputStream(new FileInputStream(zipFile))
        try {
            ZipEntry entry
            byte[] buf = new byte[8192]
            while ((entry = zis.nextEntry) != null) {
                if (entry.name.contains('..')) {
                    continue
                }
                File out = new File(destDir, entry.name)
                if (entry.isDirectory()) {
                    out.mkdirs()
                    continue
                }
                out.parentFile.mkdirs()
                def os = new FileOutputStream(out)
                try {
                    int n
                    while ((n = zis.read(buf)) > 0) {
                        os.write(buf, 0, n)
                    }
                } finally {
                    os.close()
                }
                zis.closeEntry()
            }
        } finally {
            zis.close()
        }
    }

    static void zipDirs(List<File> dirs, File outputJar) {
        outputJar.parentFile.mkdirs()
        def zos = new ZipOutputStream(new FileOutputStream(outputJar))
        def names = new HashSet<String>()
        try {
            dirs.each { dir ->
                if (dir == null || !dir.exists()) {
                    return
                }
                dir.eachFileRecurse { File f ->
                    if (!f.isFile()) {
                        return
                    }
                    def rel = dir.toPath().relativize(f.toPath()).toString().replace('\\', '/')
                    if (!names.add(rel)) {
                        return
                    }
                    zos.putNextEntry(new ZipEntry(rel))
                    def is = new FileInputStream(f)
                    try {
                        byte[] buf = new byte[8192]
                        int n
                        while ((n = is.read(buf)) > 0) {
                            zos.write(buf, 0, n)
                        }
                    } finally {
                        is.close()
                    }
                    zos.closeEntry()
                }
            }
        } finally {
            zos.close()
        }
    }
}
