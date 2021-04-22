package com.vikingelectronics.softphone.legacy.settings;

/*
AudioSettingsFragment.java
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
import android.media.AudioManager;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.vikingelectronics.softphone.LegacyFragmentDependencyProvider;
import com.vikingelectronics.softphone.MainActivity;
import com.vikingelectronics.softphone.R;
import com.vikingelectronics.softphone.legacy.settings.widget.BasicSetting;
import com.vikingelectronics.softphone.legacy.settings.widget.ListSetting;
import com.vikingelectronics.softphone.legacy.settings.widget.SettingListenerBase;
import com.vikingelectronics.softphone.legacy.settings.widget.SwitchSetting;
import com.vikingelectronics.softphone.legacy.settings.widget.TextSetting;

import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.EcCalibratorStatus;
import org.linphone.core.PayloadType;

import java.util.ArrayList;


import kotlin.Unit;

public class AudioSettingsFragment extends Fragment {
    protected View mRootView;

    protected LegacyFragmentDependencyProvider provider;

    private SwitchSetting mEchoCanceller, mAdaptiveRateControl;
    private TextSetting mMicGain, mSpeakerGain;
    private ListSetting mCodecBitrateLimit;
    private BasicSetting mEchoCalibration, mEchoTester;
    private LinearLayout mAudioCodecs;
    private ArrayList<String> audioCodecs = new ArrayList<String>();


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        provider = (MainActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.settings_audio, container, false);

        audioCodecs.add("PCMU");
        audioCodecs.add("PCMA");
        audioCodecs.add("G722");

        loadSettings();

        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        updateValues();
    }

    protected void loadSettings() {
        mEchoCanceller = mRootView.findViewById(R.id.pref_echo_cancellation);

        mAdaptiveRateControl = mRootView.findViewById(R.id.pref_adaptive_rate_control);

        mMicGain = mRootView.findViewById(R.id.pref_mic_gain_db);
        mMicGain.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        mSpeakerGain = mRootView.findViewById(R.id.pref_playback_gain_db);
        mSpeakerGain.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        mCodecBitrateLimit = mRootView.findViewById(R.id.pref_codec_bitrate_limit);

        mEchoCalibration = mRootView.findViewById(R.id.pref_echo_canceller_calibration);

        mEchoTester = mRootView.findViewById(R.id.pref_echo_tester);

        mAudioCodecs = mRootView.findViewById(R.id.pref_audio_codecs);
    }

    protected void setListeners() {
        mEchoCanceller.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onBoolValueChanged(boolean newValue) {
                        provider.getLinphonePreferences().setEchoCancellation(newValue);
                    }
                });

        mAdaptiveRateControl.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onBoolValueChanged(boolean newValue) {
                        provider.getLinphonePreferences().enableAdaptiveRateControl(newValue);
                    }
                });

        mMicGain.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onTextValueChanged(String newValue) {
                        provider.getLinphonePreferences().setMicGainDb(Float.valueOf(newValue));
                    }
                });

        mSpeakerGain.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onTextValueChanged(String newValue) {
                        provider.getLinphonePreferences().setPlaybackGainDb(Float.valueOf(newValue));
                    }
                });

        mCodecBitrateLimit.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onListValueChanged(int position, String newLabel, String newValue) {
                        int bitrate = Integer.valueOf(newValue);
                        provider.getLinphonePreferences().setCodecBitrateLimit(bitrate);

                        for (final PayloadType pt : provider.getCore().getAudioPayloadTypes()) {
                            if (pt.isVbr()) {
                                pt.setNormalBitrate(bitrate);
                            }
                        }
                    }
                });

        mEchoCalibration.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onClicked() {
                        mEchoCalibration.setSubtitle(getString(R.string.ec_calibrating));

                        provider.getPermissionsManager().requestPermissionsForAudio( () -> {
                            startEchoCancellerCalibration();
                            return Unit.INSTANCE;
                        });
                    }
                });

        mEchoTester.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onClicked() {
                        provider.getPermissionsManager().requestPermissionsForAudio( () -> {
                            if (provider.getLinphoneManager().getEchoTesterStatus()) {
                                stopEchoTester();
                            } else {
                                startEchoTester();
                            }
                            return Unit.INSTANCE;
                        });
                    }
                });
    }

    protected void updateValues() {
        mEchoCanceller.setChecked(provider.getLinphonePreferences().echoCancellationEnabled());

        mAdaptiveRateControl.setChecked(provider.getLinphonePreferences().adaptiveRateControlEnabled());

        mMicGain.setValue(provider.getLinphonePreferences().getMicGainDb());

        mSpeakerGain.setValue(provider.getLinphonePreferences().getPlaybackGainDb());

        mCodecBitrateLimit.setValue(provider.getLinphonePreferences().getCodecBitrateLimit());

        if (provider.getLinphonePreferences().echoCancellationEnabled()) {
            mEchoCalibration.setSubtitle(
                    String.format(
                            getString(R.string.ec_calibrated),
                            String.valueOf(provider.getLinphonePreferences().getEchoCalibration())));
        }

        populateAudioCodecs();

        setListeners();
    }

    private void populateAudioCodecs() {
        mAudioCodecs.removeAllViews();

        for (final PayloadType pt : provider.getCore().getAudioPayloadTypes()) {
            String mimeType = pt.getMimeType();
            Boolean enabled = pt.enabled();
            if (audioCodecs.contains(mimeType)) {
                audioCodecs.remove(mimeType);
                final SwitchSetting codec = new SwitchSetting(getActivity());
                codec.setTitle(pt.getMimeType());
                /* Special case */
                if (pt.getMimeType().equals("mpeg4-generic")) {
                    codec.setTitle("AAC-ELD");
                }

                codec.setSubtitle(pt.getClockRate() + " Hz");
                if (pt.enabled()) {
                    // Never use codec.setChecked(pt.enabled) !
                    codec.setChecked(true);
                }
                codec.setListener(
                        new SettingListenerBase() {
                            @Override
                            public void onBoolValueChanged(boolean newValue) {
                                pt.enable(newValue);
                            }
                        });

                mAudioCodecs.addView(codec);
            } else {
                pt.enable(false);
            }
        }

    }

    public void startEchoTester() {
        if (provider.getLinphoneManager().startEchoTester() > 0) {
            mEchoTester.setSubtitle("Is running");
        }
    }

    private void stopEchoTester() {
        if (provider.getLinphoneManager().stopEchoTester() > 0) {
            mEchoTester.setSubtitle("Is stopped");
        }
    }

    public void startEchoCancellerCalibration() {
        if (provider.getLinphoneManager().getEchoTesterStatus()) { stopEchoTester(); }
        provider.getCore().addListener(
                new CoreListenerStub() {
                    @Override
                    public void onEcCalibrationResult(
                            Core core, EcCalibratorStatus status, int delayMs) {
                        if (status == EcCalibratorStatus.InProgress) return;
                        core.removeListener(this);
                        provider.getLinphoneManager().routeAudioToSpeaker(false);

                        if (status == EcCalibratorStatus.DoneNoEcho) {
                            mEchoCalibration.setSubtitle(getString(R.string.no_echo));
                        } else if (status == EcCalibratorStatus.Done) {
                            mEchoCalibration.setSubtitle(
                                    String.format(
                                            getString(R.string.ec_calibrated),
                                            String.valueOf(delayMs)));
                        } else if (status == EcCalibratorStatus.Failed) {
                            mEchoCalibration.setSubtitle(getString(R.string.failed));
                        }
                        mEchoCanceller.setChecked(status != EcCalibratorStatus.DoneNoEcho);
                        ((AudioManager)
                                        getActivity()
                                                .getSystemService(Context.AUDIO_SERVICE))
                                .setMode(AudioManager.MODE_NORMAL);
                    }
                });
        provider.getLinphoneManager().startEcCalibration();
    }
}
