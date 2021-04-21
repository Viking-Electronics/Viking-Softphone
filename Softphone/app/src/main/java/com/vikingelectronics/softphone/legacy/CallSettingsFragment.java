package com.vikingelectronics.softphone.legacy;

/*
CallSettingsFragment.java
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
import android.content.SharedPreferences;
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
import com.vikingelectronics.softphone.legacy.settings.widget.ListSetting;
import com.vikingelectronics.softphone.legacy.settings.widget.SettingListenerBase;
import com.vikingelectronics.softphone.legacy.settings.widget.SwitchSetting;
import com.vikingelectronics.softphone.legacy.settings.widget.TextSetting;
import com.vikingelectronics.softphone.legacy.LinphonePreferences;

import org.linphone.core.Core;
import org.linphone.core.MediaEncryption;
import org.linphone.core.tools.Log;

import java.util.ArrayList;
import java.util.List;

public class CallSettingsFragment extends Fragment {
    protected View mRootView;

    protected LegacyFragmentDependencyProvider provider;

    private SwitchSetting mDeviceRingtone,
            mVibrateIncomingCall,
            mDtmfSipInfo,
            mDtmfRfc2833,
            mAutoAnswer;
    private ListSetting mMediaEncryption;
    private TextSetting mAutoAnswerTime, mIncomingCallTimeout, mVoiceMailUri, mSnoozeTime;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        provider = (MainActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.settings_call, container, false);

        loadSettings();

        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();


        updateValues();
    }

    protected void loadSettings() {
        mDeviceRingtone = mRootView.findViewById(R.id.pref_device_ringtone);

        mVibrateIncomingCall = mRootView.findViewById(R.id.pref_vibrate_on_incoming_calls);

        mDtmfSipInfo = mRootView.findViewById(R.id.pref_sipinfo_dtmf);

        mDtmfRfc2833 = mRootView.findViewById(R.id.pref_rfc2833_dtmf);

        mAutoAnswer = mRootView.findViewById(R.id.pref_auto_answer);

        mMediaEncryption = mRootView.findViewById(R.id.pref_media_encryption);
        initMediaEncryptionList();

        mAutoAnswerTime = mRootView.findViewById(R.id.pref_auto_answer_time);
        mAutoAnswerTime.setInputType(InputType.TYPE_CLASS_NUMBER);

        mIncomingCallTimeout = mRootView.findViewById(R.id.pref_incoming_call_timeout);
        mAutoAnswerTime.setInputType(InputType.TYPE_CLASS_NUMBER);

        mVoiceMailUri = mRootView.findViewById(R.id.pref_voice_mail);
        mAutoAnswerTime.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);

        mSnoozeTime = mRootView.findViewById(R.id.pref_snooze_time);
        mSnoozeTime.setInputType(InputType.TYPE_CLASS_NUMBER);
    }

    protected void setListeners() {
        mDeviceRingtone.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onBoolValueChanged(boolean newValue) {
                        provider.getLinphonePreferences().enableDeviceRingtone(newValue);
                    }
                });

        mVibrateIncomingCall.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onBoolValueChanged(boolean newValue) {
                        provider.getLinphonePreferences().enableIncomingCallVibration(newValue);
                    }
                });

        mDtmfSipInfo.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onBoolValueChanged(boolean newValue) {
                        if (newValue) mDtmfRfc2833.setChecked(false);
                        provider.getLinphonePreferences().sendDTMFsAsSipInfo(newValue);
                    }
                });

        mDtmfRfc2833.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onBoolValueChanged(boolean newValue) {
                        if (newValue) mDtmfSipInfo.setChecked(false);
                        provider.getLinphonePreferences().sendDtmfsAsRfc2833(newValue);
                    }
                });

        mAutoAnswer.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onBoolValueChanged(boolean newValue) {
                        provider.getLinphonePreferences().enableAutoAnswer(newValue);
                        mAutoAnswerTime.setVisibility(
                            provider.getLinphonePreferences().isAutoAnswerEnabled() ? View.VISIBLE : View.GONE);
                    }
                });

        mMediaEncryption.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onListValueChanged(int position, String newLabel, String newValue) {
                        try {
                            provider.getLinphonePreferences().setMediaEncryption(
                                    MediaEncryption.fromInt(Integer.parseInt(newValue)));
                        } catch (NumberFormatException nfe) {
                            Log.e(nfe);
                        }
                    }
                });

        mAutoAnswerTime.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onTextValueChanged(String newValue) {
                        try {
                            provider.getLinphonePreferences().setAutoAnswerTime(Integer.parseInt(newValue));
                        } catch (NumberFormatException nfe) {
                            Log.e(nfe);
                        }
                    }
                });

        mIncomingCallTimeout.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onTextValueChanged(String newValue) {
                        try {
                            provider.getLinphonePreferences().setIncTimeout(Integer.parseInt(newValue));
                        } catch (NumberFormatException nfe) {
                            Log.e(nfe);
                        }
                    }
                });

        mVoiceMailUri.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onTextValueChanged(String newValue) {
                        provider.getLinphonePreferences().setVoiceMailUri(newValue);
                    }
                });

        mSnoozeTime.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onTextValueChanged(String newValue) {
                        try{
                            int newInterval = Integer.parseInt(newValue);
                            SharedPreferences prefs = getActivity().getSharedPreferences("snooze", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putInt("snoozeInterval", newInterval);
                            editor.commit();
                        }
                        catch (NumberFormatException e){

                        }
                    }
                }
        );
    }

    protected void updateValues() {
        mDeviceRingtone.setChecked(provider.getLinphonePreferences().isDeviceRingtoneEnabled());

        mVibrateIncomingCall.setChecked(provider.getLinphonePreferences().isIncomingCallVibrationEnabled());

        mDtmfSipInfo.setChecked(provider.getLinphonePreferences().useSipInfoDtmfs());

        mDtmfRfc2833.setChecked(provider.getLinphonePreferences().useRfc2833Dtmfs());

        mAutoAnswer.setChecked(provider.getLinphonePreferences().isAutoAnswerEnabled());

        mMediaEncryption.setValue(provider.getLinphonePreferences().getMediaEncryption().toInt());

        mAutoAnswerTime.setValue(provider.getLinphonePreferences().getAutoAnswerTime());
        mAutoAnswerTime.setVisibility(provider.getLinphonePreferences().isAutoAnswerEnabled() ? View.VISIBLE : View.GONE);

        mIncomingCallTimeout.setValue(provider.getLinphonePreferences().getIncTimeout());

        mVoiceMailUri.setValue(provider.getLinphonePreferences().getVoiceMailUri());

        SharedPreferences prefs = getActivity().getSharedPreferences("snooze", Context.MODE_PRIVATE);
        mSnoozeTime.setValue(prefs.getInt("snoozeInterval",10));

        setListeners();
    }

    private void initMediaEncryptionList() {
        List<String> entries = new ArrayList<>();
        List<String> values = new ArrayList<>();

        entries.add(getString(R.string.pref_none));
        values.add(String.valueOf(MediaEncryption.None.toInt()));

        if (!getResources().getBoolean(R.bool.disable_all_security_features_for_markets)) {
            boolean hasZrtp = provider.getCore().mediaEncryptionSupported(MediaEncryption.ZRTP);
            boolean hasSrtp = provider.getCore().mediaEncryptionSupported(MediaEncryption.SRTP);
            boolean hasDtls = provider.getCore().mediaEncryptionSupported(MediaEncryption.DTLS);

            if (!hasSrtp && !hasZrtp && !hasDtls) {
                mMediaEncryption.setEnabled(false);
            } else {
                if (hasSrtp) {
                    entries.add("SRTP");
                    values.add(String.valueOf(MediaEncryption.SRTP.toInt()));
                }
                if (hasZrtp) {
                    entries.add("ZRTP");
                    values.add(String.valueOf(MediaEncryption.ZRTP.toInt()));
                }
                if (hasDtls) {
                    entries.add("DTLS");
                    values.add(String.valueOf(MediaEncryption.DTLS.toInt()));
                }
            }
        }

        mMediaEncryption.setItems(entries, values);
    }
}
