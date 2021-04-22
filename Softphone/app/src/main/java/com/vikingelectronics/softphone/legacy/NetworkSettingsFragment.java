package com.vikingelectronics.softphone.legacy;

/*
NetworkSettingsFragment.java
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
import android.os.Bundle;
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

import org.linphone.core.tools.Log;

public class NetworkSettingsFragment extends Fragment {
    protected View mRootView;
    protected LegacyFragmentDependencyProvider provider;

    private SwitchSetting mWifiOnly, mIpv6, mPush, mRandomPorts, mIce, mTurn;
    private TextSetting mSipPort, mStunServer, mTurnUsername, mTurnPassword;
    private BasicSetting mAndroidBatterySaverSettings;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        provider = (MainActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.settings_network, container, false);

        loadSettings();

        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        updateValues();
    }

    protected void loadSettings() {
        mWifiOnly = mRootView.findViewById(R.id.pref_wifi_only);

        mPush = mRootView.findViewById(R.id.pref_push_notification);

        mRandomPorts = mRootView.findViewById(R.id.pref_transport_use_random_ports);

        mIce = mRootView.findViewById(R.id.pref_ice_enable);

        mTurn = mRootView.findViewById(R.id.pref_turn_enable);

        mSipPort = mRootView.findViewById(R.id.pref_sip_port);
        mSipPort.setInputType(InputType.TYPE_CLASS_NUMBER);

        mStunServer = mRootView.findViewById(R.id.pref_stun_server);
        mStunServer.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);

        mTurnUsername = mRootView.findViewById(R.id.pref_turn_username);

        mTurnPassword = mRootView.findViewById(R.id.pref_turn_passwd);
        mTurnPassword.setInputType(
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        mAndroidBatterySaverSettings =
                mRootView.findViewById(R.id.pref_android_battery_protected_settings);
    }

    protected void setListeners() {
        mWifiOnly.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onBoolValueChanged(boolean newValue) {
                        provider.getLinphonePreferences().setWifiOnlyEnabled(newValue);
                    }
                });

        mPush.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onBoolValueChanged(boolean newValue) {
                        provider.getLinphonePreferences().setPushNotificationEnabled(newValue);
                    }
                });

        mRandomPorts.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onBoolValueChanged(boolean newValue) {
                        provider.getLinphonePreferences().useRandomPort(newValue);
                        mSipPort.setVisibility(
                            provider.getLinphonePreferences().isUsingRandomPort() ? View.GONE : View.VISIBLE);
                    }
                });

        mIce.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onBoolValueChanged(boolean newValue) {
                        provider.getLinphonePreferences().setIceEnabled(newValue);
                    }
                });

        mTurn.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onBoolValueChanged(boolean newValue) {
                        provider.getLinphonePreferences().setTurnEnabled(newValue);
                        mTurnUsername.setEnabled(provider.getLinphonePreferences().isTurnEnabled());
                        mTurnPassword.setEnabled(provider.getLinphonePreferences().isTurnEnabled());
                    }
                });

        mSipPort.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onTextValueChanged(String newValue) {
                        try {
                            provider.getLinphonePreferences().setSipPort(Integer.valueOf(newValue));
                        } catch (NumberFormatException nfe) {
                            Log.e(nfe);
                        }
                    }
                });

        mStunServer.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onTextValueChanged(String newValue) {
                        provider.getLinphonePreferences().setStunServer(newValue);
                        mIce.setEnabled(
                            provider.getLinphonePreferences().getStunServer() != null
                                        && !provider.getLinphonePreferences().getStunServer().isEmpty());
                        mTurn.setEnabled(
                            provider.getLinphonePreferences().getStunServer() != null
                                        && !provider.getLinphonePreferences().getStunServer().isEmpty());
                        if (newValue == null || newValue.isEmpty()) {
                            mIce.setChecked(false);
                            mTurn.setChecked(false);
                        }
                    }
                });

        mTurnUsername.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onTextValueChanged(String newValue) {
                        provider.getLinphonePreferences().setTurnUsername(newValue);
                    }
                });

        mTurnPassword.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onTextValueChanged(String newValue) {
                        provider.getLinphonePreferences().setTurnPassword(newValue);
                    }
                });

        mAndroidBatterySaverSettings.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onClicked() {
                        provider.getLinphonePreferences().powerSaverDialogPrompted(true);
                        Intent intent =
                                DeviceUtils.getDevicePowerManagerIntent(requireActivity());
                        if (intent != null) {
                            startActivity(intent);
                        }
                    }
                });
    }

    protected void updateValues() {
        mWifiOnly.setChecked(provider.getLinphonePreferences().isWifiOnlyEnabled());

//        mIpv6.setChecked(mPrefs.isUsingIpv6());

        //TODO: deal with these setting when we implement push
//        mPush.setChecked(provider.getLinphonePreferences().isPushNotificationEnabled());
//        mPush.setVisibility(
//                PushNotificationUtils.isAvailable(getActivity()) ? View.VISIBLE : View.GONE);

        mRandomPorts.setChecked(provider.getLinphonePreferences().isUsingRandomPort());

        mIce.setChecked(provider.getLinphonePreferences().isIceEnabled());
        mIce.setEnabled(provider.getLinphonePreferences().getStunServer() != null && !provider.getLinphonePreferences().getStunServer().isEmpty());

        mTurn.setChecked(provider.getLinphonePreferences().isTurnEnabled());
        mTurn.setEnabled(provider.getLinphonePreferences().getStunServer() != null && !provider.getLinphonePreferences().getStunServer().isEmpty());

        mSipPort.setValue(provider.getLinphonePreferences().getSipPort());
        mSipPort.setVisibility(provider.getLinphonePreferences().isUsingRandomPort() ? View.GONE : View.VISIBLE);

        mStunServer.setValue(provider.getLinphonePreferences().getStunServer());

        mTurnUsername.setValue(provider.getLinphonePreferences().getTurnUsername());
        mTurnUsername.setEnabled(provider.getLinphonePreferences().isTurnEnabled());
        mTurnPassword.setEnabled(provider.getLinphonePreferences().isTurnEnabled());

        mAndroidBatterySaverSettings.setVisibility(
                DeviceUtils.hasDevicePowerManager(requireActivity())
                        ? View.VISIBLE
                        : View.GONE);

        setListeners();
    }
}
