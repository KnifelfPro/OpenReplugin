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

package com.qihoo360.replugin.gradle.plugin.debugger

import com.qihoo360.replugin.gradle.plugin.AppConstant
import com.qihoo360.replugin.gradle.plugin.util.CmdUtil
import org.gradle.api.Project

/**
 * @author RePlugin Team
 */
class PluginDebugger {

    def project
    def config
    def variant
    File apkFile
    File adbFile

    public PluginDebugger(Project project, def config, def variant) {
        this.project = project
        this.config = config
        this.variant = variant

        apkFile = findApk(project, variant)
        adbFile = findAdb(project)
    }

    private static File findApk(Project project, def variant) {
        def variantName = ''
        try {
            variantName = variant.name
        } catch (Throwable ignored) {
        }

        File apkDir = new File(project.getBuildDir(), "outputs" + File.separator + "apk")
        def apks = []
        if (apkDir.exists()) {
            apkDir.eachFileRecurse { File f ->
                if (f.isFile() && f.name.endsWith('.apk') && !f.name.contains('androidTest')) {
                    apks << f
                }
            }
        }
        if (variantName) {
            def match = apks.find { it.path.toLowerCase().contains(variantName.toLowerCase()) }
            if (match != null) {
                return match
            }
        }
        return apks.max { it.lastModified() }
    }

    private static File findAdb(Project project) {
        try {
            def android = project.extensions.findByName('android')
            if (android != null && android.hasProperty('adbExecutable')) {
                return android.adbExecutable
            }
        } catch (Throwable ignored) {
        }
        def sdk = System.getenv('ANDROID_HOME') ?: System.getenv('ANDROID_SDK_ROOT')
        if (sdk) {
            return new File(sdk, "platform-tools/adb")
        }
        return null
    }

    /**
     * 安装插件
     */
    public boolean install() {

        if (isConfigNull()) {
            return false
        }
        if (apkFile == null || !apkFile.exists()) {
            apkFile = findApk(project, variant)
        }
        if (apkFile == null || !apkFile.exists()) {
            System.err.println "${AppConstant.TAG} Could not find the plugin apk !!!"
            return false
        }

        String pushCmd = "${adbFile.absolutePath} push ${apkFile.absolutePath} ${config.phoneStorageDir}"
        if (0 != CmdUtil.syncExecute(pushCmd)) {
            return false
        }

        String apkPath = "${config.phoneStorageDir}"
        if (!apkPath.endsWith("/")) {
            apkPath += "/"
        }
        apkPath += "${apkFile.name}"

        String grantWriteStorageCmd = "${adbFile.absolutePath} shell pm grant ${config.hostApplicationId} android.permission.WRITE_EXTERNAL_STORAGE "
        if (0 != CmdUtil.syncExecute(grantWriteStorageCmd)) {
            return false
        }

        String grantReadStorageCmd = "${adbFile.absolutePath} shell pm grant ${config.hostApplicationId} android.permission.READ_EXTERNAL_STORAGE "
        if (0 != CmdUtil.syncExecute(grantReadStorageCmd)) {
            return false
        }

        String sleepCmd = "${adbFile.absolutePath} shell sleep 2 "
        if (0 != CmdUtil.syncExecute(sleepCmd)) {
            return false
        }

        String installBrCmd = "${adbFile.absolutePath} shell am broadcast -a ${config.hostApplicationId}.replugin.install -e path ${apkPath} -e immediately true "
        if (0 != CmdUtil.syncExecute(installBrCmd)) {
            return false
        }

        return true
    }

    public boolean uninstall() {
        if (isConfigNull()) {
            return false
        }
        String cmd = "${adbFile.absolutePath} shell am broadcast -a ${config.hostApplicationId}.replugin.uninstall -e plugin ${config.pluginName}"
        if (0 != CmdUtil.syncExecute(cmd)) {
            return false
        }
        return true
    }

    public boolean forceStopHostApp() {
        if (isConfigNull()) {
            return false
        }
        String cmd = "${adbFile.absolutePath} shell am force-stop ${config.hostApplicationId}"
        if (0 != CmdUtil.syncExecute(cmd)) {
            return false
        }
        return true
    }

    public boolean startHostApp() {
        if (isConfigNull()) {
            return false
        }
        String cmd = "${adbFile.absolutePath} shell am start -n \"${config.hostApplicationId}/${config.hostAppLauncherActivity}\" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER"
        if (0 != CmdUtil.syncExecute(cmd)) {
            return false
        }
        return true
    }

    public boolean run() {
        if (isConfigNull()) {
            return false
        }
        String installBrCmd = "${adbFile.absolutePath} shell am broadcast -a ${config.hostApplicationId}.replugin.start_activity -e plugin ${config.pluginName}"
        if (0 != CmdUtil.syncExecute(installBrCmd)) {
            return false
        }
        return true
    }

    private boolean isConfigNull() {
        if (null == adbFile || !adbFile.exists()) {
            System.err.println "${AppConstant.TAG} Could not find the adb file !!!"
            return true
        }
        if (null == config) {
            System.err.println "${AppConstant.TAG} the config object can not be null!!!"
            System.err.println "${AppConstant.CONFIG_EXAMPLE}"
            return true
        }
        if (null == config.hostApplicationId) {
            System.err.println "${AppConstant.TAG} the config hostApplicationId can not be null!!!"
            System.err.println "${AppConstant.CONFIG_EXAMPLE}"
            return true
        }
        if (null == config.hostAppLauncherActivity) {
            System.err.println "${AppConstant.TAG} the config hostAppLauncherActivity can not be null!!!"
            System.err.println "${AppConstant.CONFIG_EXAMPLE}"
            return true
        }
        return false
    }
}
