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

package com.qihoo360.replugin.gradle.plugin.manifest

import org.gradle.api.GradleException
import org.gradle.api.Project

import java.util.regex.Pattern

/**
 * @author RePlugin Team
 */
public class ManifestAPI {

    def IManifest sManifestAPIImpl

    def getActivities(Project project, String variantDir) {
        if (sManifestAPIImpl == null) {
            sManifestAPIImpl = new ManifestReader(manifestPath(project, variantDir))
        }
        sManifestAPIImpl.activities
    }

    /**
     * 获取 AndroidManifest.xml 路径
     */
    def static manifestPath(Project project, String variantDir) {
        def variantDirArray = variantDir.split(Pattern.quote(File.separator))
        String variantName = ""
        variantDirArray.each {
            variantName += it.capitalize()
        }
        println ">>> variantName:${variantName}"

        File result = fromProcessManifestTask(project, variantName)
        if (result != null && result.exists()) {
            println " AndroidManifest.xml 路径：$result"
            return result.absolutePath
        }

        result = searchMergedManifest(project, variantDir)
        if (result != null && result.exists()) {
            println " AndroidManifest.xml 路径：$result"
            return result.absolutePath
        }

        throw new GradleException("can't get manifest file for variant ${variantDir}")
    }

    private static File fromProcessManifestTask(Project project, String variantName) {
        def names = [
                "process${variantName}Manifest",
                "process${variantName}MainManifest",
                "processApplicationManifest${variantName}"
        ]
        for (def name : names) {
            try {
                def processManifestTask = project.tasks.findByName(name)
                if (processManifestTask == null) {
                    continue
                }
                try {
                    def dir = processManifestTask.multiApkManifestOutputDirectory.get().asFile
                    File manifestOutputFile = new File(dir, "AndroidManifest.xml")
                    if (manifestOutputFile.exists()) {
                        return manifestOutputFile
                    }
                } catch (Exception ignored) {
                }
                try {
                    def file = processManifestTask.outputs.files.files.find {
                        it.name.equalsIgnoreCase('AndroidManifest.xml')
                    }
                    if (file != null && file.exists()) {
                        return file
                    }
                } catch (Exception ignored) {
                }
            } catch (Exception ignored) {
            }
        }
        return null
    }

    private static File searchMergedManifest(Project project, String variantDir) {
        File intermediates = new File(project.buildDir, 'intermediates')
        if (!intermediates.exists()) {
            return null
        }
        def variantToken = variantDir.replace(File.separator, '').toLowerCase()
        File best = null
        intermediates.eachFileRecurse { File f ->
            if (f.isFile() && f.name.equalsIgnoreCase('AndroidManifest.xml')) {
                def path = f.absolutePath.toLowerCase()
                if (path.contains('merged_manifest') && path.contains(variantToken)) {
                    best = f
                } else if (best == null && path.contains(variantToken)) {
                    best = f
                }
            }
        }
        return best
    }
}
