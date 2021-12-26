/*
 * Copyright (C) 2019 BaikalOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.android.server;

import com.android.internal.baikalos.Actions;
import com.android.internal.baikalos.Bluetooth;
import com.android.internal.baikalos.Telephony;
import com.android.internal.baikalos.Torch;
import com.android.internal.baikalos.Sensors;
import com.android.internal.baikalos.Runtime;
import com.android.internal.baikalos.AppProfileManager;
import com.android.internal.baikalos.DevProfileManager;

import com.android.internal.baikalos.BaikalSettings;

import android.util.Slog;

import android.content.Context;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;


import android.os.Binder;
import android.os.IBinder;
import android.os.SystemProperties;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManagerInternal;
import android.os.UserHandle;
import android.os.Process;
import android.os.SystemClock;

import android.app.AlarmManager;


public class BaikalStaticService {

    private static final String TAG = "BaikalService";

    private static final boolean DEBUG = true;

    private static boolean mSystemReady = false;

    private static Context mContext;

    static com.android.internal.baikalos.Actions mBaikalActions;
    static com.android.internal.baikalos.Bluetooth mBaikalBluetooth;
    static com.android.internal.baikalos.Telephony mBaikalTelephony;
    static com.android.internal.baikalos.Torch mBaikalTorch;
    static com.android.internal.baikalos.Sensors mBaikalSensors;
    static com.android.internal.baikalos.AppProfileManager mBaikalAppProfileManager;
    static com.android.internal.baikalos.DevProfileManager mBaikalDevProfileManager;

    static com.android.internal.baikalos.BaikalSettings mBaikalSettings;


    BaikalStaticService() {
    }

    static void Initialize(Context context,
	    com.android.internal.baikalos.Actions baikalActions,
	    com.android.internal.baikalos.Bluetooth baikalBluetooth,
	    com.android.internal.baikalos.Telephony baikalTelephony,
	    com.android.internal.baikalos.Torch baikalTorch,
	    com.android.internal.baikalos.Sensors baikalSensors,
	    com.android.internal.baikalos.AppProfileManager baikalAppProfileManager,
	    com.android.internal.baikalos.DevProfileManager baikalDevProfileManager,
	    com.android.internal.baikalos.BaikalSettings baikalSettings) {

        mContext = context;
        if( DEBUG ) {
            Slog.i(TAG,"BaikalStatic()");
        }

	    mBaikalActions = baikalActions;
	    mBaikalBluetooth = baikalBluetooth;
	    mBaikalTelephony = baikalTelephony;
	    mBaikalTorch = baikalTorch;
	    mBaikalSensors = baikalSensors;
        mBaikalAppProfileManager = baikalAppProfileManager;
        mBaikalDevProfileManager = baikalDevProfileManager;
	    mBaikalSettings = baikalSettings;
    }

    static void setSystemReady(boolean ready) {
        if( DEBUG ) {
            Slog.i(TAG,"setSystemReady(" + ready + ")");
        }
	    mSystemReady = ready;
    }

    private static Object mStaticMembersLock = new Object();

    private static boolean mHideIdleFromGms;
    private static boolean mUnrestrictedNetwork;
	
    private static int mGmsUid = -1;
    static void setGmsUid(int uid) {
        synchronized(mStaticMembersLock) {
            mGmsUid = uid;
        }
    }


    public static boolean isGmsUid(int uid) {
        synchronized(mStaticMembersLock) {
            return mGmsUid == uid;
        }
    }



    public static boolean isGmsAppid(int appid) {
        synchronized(mStaticMembersLock) {
            return UserHandle.getAppId(mGmsUid) == appid;
        }
    }

    public static int gmsAppid() {
        synchronized(mStaticMembersLock) {
            return UserHandle.getAppId(mGmsUid);
        }
    }

    public static int gmsUid() {
        synchronized(mStaticMembersLock) {
            return mGmsUid;
        }
    }

    public static boolean gmsHideIdle() {
        synchronized(mStaticMembersLock) {
            return BaikalSettings.getHideGmsEnabled();
        }
    }

    public static boolean idleUnrestrictedNetwork() {
        synchronized(mStaticMembersLock) {
            return BaikalSettings.getUnrestrictedNetEnabled();
        }
    }

    public static boolean isEnergySaveMode() {
        synchronized(mStaticMembersLock) {
	    return BaikalSettings.getAggressiveIdleEnabled() ||
		   BaikalSettings.getExtremeIdleEnabled();
        }
    }


    private static int mDolbyUid = -1;
    static void setDolbyUid(int uid) {
        synchronized(mStaticMembersLock) {
            mDolbyUid = uid;
        }
    }


    public static boolean isDolbyUid(int uid) {
        synchronized(mStaticMembersLock) {
            return mDolbyUid == uid;
        }
    }

}
