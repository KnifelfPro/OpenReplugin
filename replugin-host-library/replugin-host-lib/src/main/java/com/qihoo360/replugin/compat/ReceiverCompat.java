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

package com.qihoo360.replugin.compat;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

/**
 * registerReceiver helper for API 19-37. RECEIVER_EXPORTED flags exist only on API 33+.
 */
public final class ReceiverCompat {

    private static final int TIRAMISU = 33;

    private ReceiverCompat() {
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    public static Intent registerReceiver(Context context, BroadcastReceiver receiver,
                                          IntentFilter filter, boolean exported) {
        if (Build.VERSION.SDK_INT >= TIRAMISU) {
            int flags = exported ? Context.RECEIVER_EXPORTED : Context.RECEIVER_NOT_EXPORTED;
            return context.registerReceiver(receiver, filter, flags);
        }
        return context.registerReceiver(receiver, filter);
    }
}
