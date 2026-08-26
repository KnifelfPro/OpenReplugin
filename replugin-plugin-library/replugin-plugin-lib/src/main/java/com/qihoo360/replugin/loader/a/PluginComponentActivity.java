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
 */

package com.qihoo360.replugin.loader.a;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.ComponentActivity;

import com.qihoo360.replugin.RePluginInternal;
import com.qihoo360.replugin.helper.LogRelease;
import com.qihoo360.replugin.loader.PluginResource;

/**
 * Plugin base for {@link ComponentActivity}, including Jetpack Compose
 * activities that call {@code setContent}.
 */
public abstract class PluginComponentActivity extends ComponentActivity {

    private PluginResource pluginResource;

    @Override
    protected void attachBaseContext(Context newBase) {
        newBase = RePluginInternal.createActivityContext(this, newBase);
        pluginResource = new PluginResource(newBase);
        super.attachBaseContext(newBase);
    }

    @Override
    public Resources getResources() {
        if (pluginResource != null) {
            return pluginResource;
        }
        return super.getResources();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        RePluginInternal.handleActivityCreateBefore(this, savedInstanceState);

        super.onCreate(savedInstanceState);

        RePluginInternal.handleActivityCreate(this, savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        RePluginInternal.handleActivityDestroy(this);

        super.onDestroy();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        RePluginInternal.handleRestoreInstanceState(this, savedInstanceState);

        try {
            super.onRestoreInstanceState(savedInstanceState);
        } catch (Throwable e) {
            if (LogRelease.LOGR) {
                LogRelease.e("PluginComponentActivity", "o r i s: p=" + getPackageCodePath() + "; " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void startActivity(Intent intent) {
        if (RePluginInternal.startActivity(this, intent)) {
            return;
        }

        super.startActivity(intent);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        startActivityForResult(intent, requestCode, null);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode, Bundle options) {
        if (RePluginInternal.startActivityForResult(this, intent, requestCode, options)) {
            return;
        }

        if (Build.VERSION.SDK_INT >= 16) {
            super.startActivityForResult(intent, requestCode, options);
        } else {
            super.startActivityForResult(intent, requestCode);
        }
    }
}
