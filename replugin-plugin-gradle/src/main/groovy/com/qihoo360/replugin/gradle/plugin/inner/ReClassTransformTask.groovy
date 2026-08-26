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

import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/**
 * AGP 8+/9 replacement for the removed Transform API.
 */
@DisableCachingByDefault(because = "bytecode rewrite over the whole class set, not worth caching")
abstract class ReClassTransformTask extends DefaultTask {

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract ListProperty<RegularFile> getAllJars()

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract ListProperty<Directory> getAllDirectories()

    @OutputFile
    abstract RegularFileProperty getOutput()

    @Input
    abstract Property<String> getVariantName()

    @Input
    abstract Property<String> getAppPackage()

    @Internal
    Object pluginConfig

    @TaskAction
    void taskAction() {
        if (appPackage.isPresent()) {
            CommonData.appPackage = appPackage.get()
            println ">>> APP_PACKAGE ${CommonData.appPackage}"
        }
        def workDir = new File(project.layout.buildDirectory.get().asFile,
                "intermediates/replugin-reclass/${variantName.get()}")
        ReClassProcessor.process(
                project,
                allJars.get().collect { it.asFile },
                allDirectories.get().collect { it.asFile },
                output.get().asFile,
                variantName.get(),
                workDir,
                pluginConfig)
    }
}
