package com.vikingelectronics.softphone.legacy;

/*
AdvancedSettingsFragment.java
Copyright (C) 2019 Belledonne Communications, Grenoble, France

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.vikingelectronics.softphone.LegacyFragmentDependencyProvider;
import com.vikingelectronics.softphone.MainActivity;
import com.vikingelectronics.softphone.R;
import com.vikingelectronics.softphone.legacy.settings.widget.BasicSetting;
import com.vikingelectronics.softphone.legacy.settings.widget.SettingListenerBase;
import com.vikingelectronics.softphone.legacy.settings.widget.SwitchSetting;
import com.vikingelectronics.softphone.legacy.settings.widget.TextSetting;


public class AdvancedSettingsFragment extends Fragment {
    protected View mRootView;
    protected LegacyFragmentDependencyProvider provider;

    private SwitchSetting mDebug, mJavaLogger, mFriendListSubscribe, mBackgroundMode, mStartAtBoot, mDarkMode;
    private TextSetting mRemoteProvisioningUrl, mDisplayName, mUsername, mDeviceName;
    private BasicSetting mAndroidAppSettings;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        provider = (MainActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.settings_advanced, container, false);

        loadSettings();

        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        updateValues();
    }

    protected void loadSettings() {
        mDebug = mRootView.findViewById(R.id.pref_debug);

        mJavaLogger = mRootView.findViewById(R.id.pref_java_debug);
        // This is only required for blackberry users for all we know
        mJavaLogger.setVisibility(
                Build.MANUFACTURER.equals("BlackBerry") ? View.VISIBLE : View.GONE);

        mFriendListSubscribe = mRootView.findViewById(R.id.pref_friendlist_subscribe);

        mBackgroundMode = mRootView.findViewById(R.id.pref_background_mode);

        mStartAtBoot = mRootView.findViewById(R.id.pref_autostart);

        mDarkMode = mRootView.findViewById(R.id.pref_dark_mode);

        mRemoteProvisioningUrl = mRootView.findViewById(R.id.pref_remote_provisioning);
        mRemoteProvisioningUrl.setInputType(
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);

        mDisplayName = mRootView.findViewById(R.id.pref_display_name);

        mUsername = mRootView.findViewById(R.id.pref_user_name);

        mAndroidAppSettings = mRootView.findViewById(R.id.pref_android_app_settings);

        mDeviceName = mRootView.findViewById(R.id.pref_device_name);
    }

    protected void setListeners() {
        mDebug.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onBoolValueChanged(boolean newValue) {
                        provider.getLinphonePreferences().setDebugEnabled(newValue);
                    }
                });

        mJavaLogger.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onBoolValueChanged(boolean newValue) {
                        provider.getLinphonePreferences().setJavaLogger(newValue);
                    }
                });

        mFriendListSubscribe.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onBoolValueChanged(boolean newValue) {
                        provider.getLinphonePreferences().enabledFriendlistSubscription(newValue);
                    }
                });

//        mBackgroundMode.setListener(
//                new SettingListenerBase() {
//                    @Override
//                    public void onBoolValueChanged(boolean newValue) {
//                        mPrefs.setServiceNotificationVisibility(newValue);
//                        if (newValue) {
////                            LinphoneService.instance().getNotificationManager().startForeground();
//                        } else {
////                            LinphoneService.instance().getNotificationManager().stopForeground();
//                        }
//                    }
//                });

//        mStartAtBoot.setListener(
//                new SettingListenerBase() {
//                    @Override
//                    public void onBoolValueChanged(boolean newValue) {
//                        mPrefs.setAutoStart(newValue);
//                    }
//                });

//        mDarkMode.setListener(
//                new SettingListenerBase() {
//                    @Override
//                    public void onBoolValueChanged(boolean newValue) {
//                        mPrefs.enableDarkMode(newValue);
//                    }
//                });

        mRemoteProvisioningUrl.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onTextValueChanged(String newValue) {
                        provider.getLinphonePreferences().setRemoteProvisioningUrl(newValue);
                    }
                });

        mDisplayName.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onTextValueChanged(String newValue) {
                        provider.getLinphonePreferences().setDefaultDisplayName(newValue);
                    }
                });

        mUsername.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onTextValueChanged(String newValue) {
                        provider.getLinphonePreferences().setDefaultUsername(newValue);
                    }
                });

        mAndroidAppSettings.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onClicked() {
                        Context context = requireContext();
                        Intent i = new Intent();
                        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        i.addCategory(Intent.CATEGORY_DEFAULT);
                        i.setData(Uri.parse("package:" + context.getPackageName()));
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
//                        startActivityForResult(i, LinphoneActivity.ANDROID_APP_SETTINGS_ACTIVITY);
                    }
                });

        mDeviceName.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onTextValueChanged(String newValue) {
                        provider.getLinphonePreferences().setDeviceName(newValue);
                    }
                });
    }

    protected void updateValues() {
        mDebug.setChecked(provider.getLinphonePreferences().isDebugEnabled());

        mJavaLogger.setChecked(provider.getLinphonePreferences().useJavaLogger());

        mFriendListSubscribe.setChecked(provider.getLinphonePreferences().isFriendlistsubscriptionEnabled());

        mBackgroundMode.setChecked(provider.getLinphonePreferences().getServiceNotificationVisibility());

        mStartAtBoot.setChecked(provider.getLinphonePreferences().isAutoStartEnabled());

        mDarkMode.setChecked(provider.getLinphonePreferences().isDarkModeEnabled());

        mRemoteProvisioningUrl.setValue(provider.getLinphonePreferences().getRemoteProvisioningUrl());

        mDisplayName.setValue(provider.getLinphonePreferences().getDefaultDisplayName());

        mUsername.setValue(provider.getLinphonePreferences().getDefaultUsername());

        mDeviceName.setValue(provider.getLinphonePreferences().getDeviceName(requireContext()));

        setListeners();
    }
}
