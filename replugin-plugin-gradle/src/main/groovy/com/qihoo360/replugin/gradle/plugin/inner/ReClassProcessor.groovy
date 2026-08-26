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

import com.qihoo360.replugin.gradle.plugin.injector.IClassInjector
import com.qihoo360.replugin.gradle.plugin.injector.Injectors
import javassist.ClassPool
import org.gradle.api.Project

import java.nio.file.Files
import java.nio.file.Paths

/**
 * Javassist class rewrite used by {@link ReClassTransformTask}.
 */
class ReClassProcessor {

    static void process(Project project,
                        List<File> jars,
                        List<File> dirs,
                        File outputJar,
                        String variantName,
                        File workDir,
                        Object config) {
        welcome()

        if (workDir.exists()) {
            workDir.deleteDir()
        }
        workDir.mkdirs()

        CommonData.appModule = config.appModule
        CommonData.ignoredActivities = config.ignoredActivities
        CommonData.classAndPath = [:]

        def processedDirs = []
        def visitor = new ClassFileVisitor()
        int index = 0

        dirs.each { File dir ->
            if (dir == null || !dir.exists()) {
                return
            }
            File dest = new File(workDir, "dir-${index++}")
            Util.copyDirectory(dir, dest)
            processedDirs << dest
            visitor.setBaseDir(dest.absolutePath)
            Files.walkFileTree(Paths.get(dest.absolutePath), visitor)
        }

        jars.each { File jar ->
            if (jar == null || !jar.exists() || Util.isZipEmpty(jar.absolutePath)) {
                return
            }
            File dest = new File(workDir, "jar-${index++}")
            Util.unzip(jar, dest)
            processedDirs << dest
            visitor.setBaseDir(dest.absolutePath)
            Files.walkFileTree(Paths.get(dest.absolutePath), visitor)
        }

        def pool = new ClassPool(true)
        def androidJar = resolveAndroidJar(project)
        if (androidJar != null && androidJar.exists()) {
            println ">>> android.jar: ${androidJar.absolutePath}"
            pool.insertClassPath(androidJar.absolutePath)
        }
        processedDirs.each {
            println ">>> classpath: ${it.absolutePath}"
            pool.insertClassPath(it.absolutePath)
        }

        def injectors = includedInjectors(project, config, variantName)
        Util.newSection()
        Injectors.values().each {
            if (it.nickName in injectors) {
                println ">>> Do: ${it.nickName}"
                def configPre = Util.lowerCaseAtIndex(it.nickName, 0)
                doInject(processedDirs, pool, it.injector, config.properties["${configPre}Config"])
            } else {
                println ">>> Skip: ${it.nickName}"
            }
        }

        if (config.customInjectors != null) {
            config.customInjectors.each {
                doInject(processedDirs, pool, it, null)
            }
        }

        Util.zipDirs(processedDirs, outputJar)
        Util.newSection()
        println ">>> ReClass output: ${outputJar.absolutePath}"
    }

    private static List includedInjectors(Project project, def cfg, String variantDir) {
        def injectors = []
        Injectors.values().each {
            it.injector.setProject(project)
            it.injector.setVariantDir(variantDir)
            if (!(it.nickName in cfg.ignoredInjectors)) {
                injectors << it.nickName
            }
        }
        injectors
    }

    private static void doInject(List<File> dirs, ClassPool pool, IClassInjector injector, Object config) {
        try {
            dirs.each {
                injector.injectClass(pool, it.absolutePath, config)
            }
        } catch (Throwable t) {
            println t.toString()
        }
    }

    private static File resolveAndroidJar(Project project) {
        try {
            def android = project.extensions.findByName('android')
            def boot = android?.bootClasspath
            def hit = boot?.find { it.name.contains('android.jar') }
            if (hit != null) {
                return hit
            }
        } catch (Throwable ignored) {
        }
        def sdk = System.getenv('ANDROID_HOME') ?: System.getenv('ANDROID_SDK_ROOT')
        if (sdk) {
            def platforms = new File(sdk, 'platforms')
            for (def api : ['android-37', 'android-36', 'android-35', 'android-34']) {
                def f = new File(platforms, api + '/android.jar')
                if (f.exists()) {
                    return f
                }
            }
        }
        return null
    }

    private static void welcome() {
        println '\n'
        60.times { print '=' }
        println '\n                    replugin-plugin-gradle'
        60.times { print '=' }
        println("""
Add repluginPluginConfig to your build.gradle to enable this plugin:

repluginPluginConfig {
    // Name of 'App Module'，use '' if root dir is 'App Module'. ':app' as default.
    appModule = ':app'

    // Injectors ignored
    // LoaderActivityInjector: Replace Activity to LoaderActivity
    // ProviderInjector: Inject provider method call.
    ignoredInjectors = ['LoaderActivityInjector']
}""")
        println('\n')
    }
}
