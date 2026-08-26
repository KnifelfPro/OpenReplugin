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

package com.qihoo360.replugin.gradle.plugin

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts
import com.android.build.gradle.AppPlugin
import com.qihoo360.replugin.gradle.compat.VariantCompat
import com.qihoo360.replugin.gradle.plugin.debugger.PluginDebugger
import com.qihoo360.replugin.gradle.plugin.inner.CommonData
import com.qihoo360.replugin.gradle.plugin.inner.ReClassTransformTask
import kotlin.jvm.functions.Function1
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @author RePlugin Team
 */
public class ReClassPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {

        println "${AppConstant.TAG} Welcome to replugin world ! "

        project.extensions.create(AppConstant.USER_CONFIG, ReClassConfig)

        def isApp = project.plugins.hasPlugin(AppPlugin)
        if (!isApp) {
            return
        }

        def config = project.extensions.getByName(AppConstant.USER_CONFIG)
        def androidComponents = project.extensions.getByType(ApplicationAndroidComponentsExtension)

        androidComponents.onVariants(androidComponents.selector().all()) { variant ->
            def taskName = "repluginReClass${captureName(variant.name)}"
            def taskProvider = project.tasks.register(taskName, ReClassTransformTask) { t ->
                t.variantName.set(variant.name)
                t.appPackage.set(variant.applicationId)
                t.pluginConfig = config
            }
            variant.artifacts
                    .forScope(ScopedArtifacts.Scope.ALL)
                    .use(taskProvider)
                    .toTransform(
                            ScopedArtifact.CLASSES.INSTANCE,
                            { ReClassTransformTask t -> t.allJars } as Function1,
                            { ReClassTransformTask t -> t.allDirectories } as Function1,
                            { ReClassTransformTask t -> t.output } as Function1
                    )
        }

        def android = project.extensions.findByName('android')
        if (android != null && android.metaClass.respondsTo(android, 'getApplicationVariants')) {
            android.applicationVariants.all { variant ->
                registerDebuggerTasks(project, config, variant)
            }
            try {
                def ns = android.namespace
                if (ns instanceof org.gradle.api.provider.Provider) {
                    ns = ns.orNull
                }
                CommonData.appPackage = (ns ?: android.defaultConfig.applicationId) as String
                println ">>> APP_PACKAGE " + CommonData.appPackage
            } catch (Throwable ignored) {
                try {
                    CommonData.appPackage = android.defaultConfig.applicationId as String
                    println ">>> APP_PACKAGE " + CommonData.appPackage
                } catch (Throwable ignored2) {
                }
            }
        }
    }

    private static void registerDebuggerTasks(Project project, def config, def variant) {
        PluginDebugger pluginDebugger = new PluginDebugger(project, config, variant)

        def assembleTask = VariantCompat.getAssembleTask(variant)
        def variantName = captureName(variant.name)
        def tasks = project.tasks

        def installPluginTask = tasks.asMap.get(AppConstant.TASK_INSTALL_PLUGIN + variantName)
        if (installPluginTask == null) {
            installPluginTask = tasks.create(AppConstant.TASK_INSTALL_PLUGIN + variantName)
        }
        installPluginTask.doLast {
            pluginDebugger.startHostApp()
            pluginDebugger.uninstall()
            pluginDebugger.forceStopHostApp()
            pluginDebugger.startHostApp()
            pluginDebugger.install()
        }
        installPluginTask.group = AppConstant.TASKS_GROUP

        def uninstallPluginTask = tasks.asMap.get(AppConstant.TASK_UNINSTALL_PLUGIN)
        if (uninstallPluginTask == null) {
            uninstallPluginTask = tasks.create(AppConstant.TASK_UNINSTALL_PLUGIN)
        }
        uninstallPluginTask.doLast {
            pluginDebugger.uninstall()
        }
        uninstallPluginTask.group = AppConstant.TASKS_GROUP

        def forceStopHostAppTask = tasks.asMap.get(AppConstant.TASK_FORCE_STOP_HOST_APP)
        if (forceStopHostAppTask == null) {
            forceStopHostAppTask = tasks.create(AppConstant.TASK_FORCE_STOP_HOST_APP)
            forceStopHostAppTask.doLast {
                pluginDebugger.forceStopHostApp()
            }
            forceStopHostAppTask.group = AppConstant.TASKS_GROUP
        }

        def startHostAppTask = tasks.asMap.get(AppConstant.TASK_START_HOST_APP)
        if (startHostAppTask == null) {
            startHostAppTask = tasks.create(AppConstant.TASK_START_HOST_APP)
            startHostAppTask.doLast {
                pluginDebugger.startHostApp()
            }
            startHostAppTask.group = AppConstant.TASKS_GROUP
        }

        def restartHostAppTask = tasks.asMap.get(AppConstant.TASK_RESTART_HOST_APP)
        if (restartHostAppTask == null) {
            restartHostAppTask = tasks.create(AppConstant.TASK_RESTART_HOST_APP)
            restartHostAppTask.doLast {
                pluginDebugger.startHostApp()
            }
            restartHostAppTask.group = AppConstant.TASKS_GROUP
            restartHostAppTask.dependsOn(forceStopHostAppTask)
        }

        if (assembleTask) {
            installPluginTask.dependsOn assembleTask
        }

        def runPluginTask = tasks.asMap.get(AppConstant.TASK_RUN_PLUGIN + variantName)
        if (runPluginTask == null) {
            runPluginTask = tasks.create(AppConstant.TASK_RUN_PLUGIN + variantName)
        }
        runPluginTask.doLast {
            pluginDebugger.run()
        }
        runPluginTask.group = AppConstant.TASKS_GROUP

        def installAndRunPluginTask = tasks.asMap.get(AppConstant.TASK_INSTALL_AND_RUN_PLUGIN + variantName)
        if (installAndRunPluginTask == null) {
            installAndRunPluginTask = tasks.create(AppConstant.TASK_INSTALL_AND_RUN_PLUGIN + variantName)
        }
        installAndRunPluginTask.doLast {
            pluginDebugger.run()
        }
        installAndRunPluginTask.group = AppConstant.TASKS_GROUP
        installAndRunPluginTask.dependsOn installPluginTask
    }

    private static String captureName(String name) {
        if (name == null || name.isEmpty()) {
            return name
        }
        return name.substring(0, 1).toUpperCase() + name.substring(1)
    }
}

class ReClassConfig {

    /** 编译的 App Module 的名称 */
    def appModule = ':app'

    /** 用户声明要忽略的注入器 */
    def ignoredInjectors = []

    /** 执行 LoaderActivity 替换时，用户声明不需要替换的 Activity */
    def ignoredActivities = []

    /** 自定义的注入器 */
    def customInjectors = []

    /** 插件名字,默认null */
    def pluginName = null

    /** 手机存储目录,默认"/sdcard/" */
    def phoneStorageDir = "/sdcard/"

    /** 宿主包名,默认null */
    def hostApplicationId = null

    /** 宿主launcherActivity,默认null */
    def hostAppLauncherActivity = null
}
