/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.keyguard;

import android.content.Context;
import com.android.systemui.Dependency;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;

import com.android.internal.util.EmergencyAffordanceManager;
import com.android.internal.widget.LockPatternUtils;
import com.android.settingslib.Utils;
import com.android.systemui.R;

import java.util.List;

/**
 * This class implements a smart emergency button that updates itself based
 * on telephony state.  When the phone is idle, it is an emergency call button.
 * When there's a call in progress, it presents an appropriate message and
 * allows the user to return to the call.
 */
public class EmergencyButton extends Button {

    private final EmergencyAffordanceManager mEmergencyAffordanceManager;

    private int mDownX;
    private int mDownY;
    private boolean mLongPressWasDragged;

    private LockPatternUtils mLockPatternUtils;

    private final boolean mEnableEmergencyCallWhileSimLocked;
    private boolean mIsCellAvailable;
    private ServiceState mServiceState;

    public EmergencyButton(Context context) {
        this(context, null);
    }

    public EmergencyButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mEnableEmergencyCallWhileSimLocked = mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_enable_emergency_call_while_sim_locked);
        mEmergencyAffordanceManager = new EmergencyAffordanceManager(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mLockPatternUtils = new LockPatternUtils(mContext);
        if (mEmergencyAffordanceManager.needsEmergencyAffordance()) {
            setOnLongClickListener(v -> {
                if (!mLongPressWasDragged
                        && mEmergencyAffordanceManager.needsEmergencyAffordance()) {
                    mEmergencyAffordanceManager.performEmergencyCall();
                    return true;
                }
                return false;
            });
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int x = (int) event.getX();
        final int y = (int) event.getY();
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            mDownX = x;
            mDownY = y;
            mLongPressWasDragged = false;
        } else {
            final int xDiff = Math.abs(x - mDownX);
            final int yDiff = Math.abs(y - mDownY);
            int touchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();
            if (Math.abs(yDiff) > touchSlop || Math.abs(xDiff) > touchSlop) {
                mLongPressWasDragged = true;
            }
        }
        return super.onTouchEvent(event);
    }

    /**
     * Reload colors from resources.
     **/
    public void reloadColors() {
        int color = Utils.getColorAttrDefaultColor(getContext(),
                android.R.attr.textColorPrimaryInverse);
        setTextColor(color);
        setBackground(getContext()
                .getDrawable(com.android.systemui.R.drawable.kg_emergency_button_background));
    }

    @Override
    public boolean performLongClick() {
        return super.performLongClick();
    }

    public void updateEmergencyCallButton(boolean isInCall, boolean isVoiceCapable, boolean simLocked) {
        boolean visible = false;
        if (isVoiceCapable) {
            // Emergency calling requires voice capability.
            if (isInCall) {
                visible = true; // always show "return to call" if phone is off-hook
            } else {
                if (simLocked) {
                    // Some countries can't handle emergency calls while SIM is locked.
                    visible = mEnableEmergencyCallWhileSimLocked;
                } else {
                    // Show if there is a secure screen (pin/pattern/SIM pin/SIM puk) or config set
                    visible = mLockPatternUtils.isSecure(KeyguardUpdateMonitor.getCurrentUser()) ||
                            mContext.getResources().getBoolean(R.bool.config_showEmergencyButton);
                }

                if (mContext.getResources().getBoolean(R.bool.kg_hide_emgcy_btn_when_oos)) {
                    visible = visible && isEmergencyCapable();
                }
            }
        }
        if (visible) {
            setVisibility(View.VISIBLE);

            int textId;
            if (isInCall) {
                textId = com.android.internal.R.string.lockscreen_return_to_call;
            } else {
                textId = com.android.internal.R.string.lockscreen_emergency_call;
            }
            setText(textId);
        } else {
            setVisibility(View.GONE);
        }
    }

    private boolean isEmergencyCapable() {
        KeyguardUpdateMonitor monitor = Dependency.get(KeyguardUpdateMonitor.class);
        return (!monitor.isOOS()
                || mIsCellAvailable
                || (mServiceState !=null && mServiceState.isEmergencyOnly()));
    }
}
