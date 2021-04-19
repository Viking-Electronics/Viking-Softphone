package com.vikingelectronics.softphone.legacy.settings;

/*
SettingsFragment.java
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
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptionsBuilder;
import androidx.navigation.compose.NavHostControllerKt;


import com.vikingelectronics.softphone.R;
import com.vikingelectronics.softphone.navigation.Screen;
import org.linphone.core.Core;
import org.linphone.core.ProxyConfig;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class SettingsFragment extends Fragment {
    protected View mRootView;
    private BasicSetting mTunnel, mAudio, mVideo, mCall, mChat, mNetwork, mAdvanced;
    private LinearLayout mAccounts;
    private TextView mAccountsHeader;
    public NavController navController;

    private final Function1<NavOptionsBuilder, Unit> builderOptions = navOptionsBuilder -> Unit.INSTANCE;

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.settings, container, false);

        loadSettings();
        setListeners();

        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();

//        if (LinphoneActivity.isInstantiated()) {
//            LinphoneActivity.instance().selectMenu(FragmentsAvailable.SETTINGS);
//        }

        updateValues();
    }

    protected void loadSettings() {
        mAccounts = mRootView.findViewById(R.id.accounts_settings_list);
        mAccountsHeader = mRootView.findViewById(R.id.accounts_settings_list_header);

        mTunnel = mRootView.findViewById(R.id.pref_tunnel);

        mAudio = mRootView.findViewById(R.id.pref_audio);

        mVideo = mRootView.findViewById(R.id.pref_video);

        mCall = mRootView.findViewById(R.id.pref_call);

        mChat = mRootView.findViewById(R.id.pref_chat);

        mNetwork = mRootView.findViewById(R.id.pref_network);

        mAdvanced = mRootView.findViewById(R.id.pref_advanced);
    }

    protected void setListeners() {
        mTunnel.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onClicked() {
                        NavHostControllerKt.navigate(navController, Screen.Primary.Settings.Tunnel.INSTANCE.getRoute(), builderOptions);
                    }
                });

        mAudio.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onClicked() {
                        NavHostControllerKt.navigate(navController, Screen.Primary.Settings.Audio.INSTANCE.getRoute(), builderOptions);
                    }
                });

        mVideo.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onClicked() {
                        NavHostControllerKt.navigate(navController, Screen.Primary.Settings.Video.INSTANCE.getRoute(), builderOptions);
                    }
                });

        mCall.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onClicked() {
                        NavHostControllerKt.navigate(navController, Screen.Primary.Settings.Call.INSTANCE.getRoute(), builderOptions);
                    }
                });

//        mChat.setListener(
//                new SettingListenerBase() {
//                    @Override
//                    public void onClicked() {
//                        LinphoneActivity.instance().displaySubSettings(new ChatSettingsFragment());
//                    }
//                });

        mNetwork.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onClicked() {
                        NavHostControllerKt.navigate(navController, Screen.Primary.Settings.Network.INSTANCE.getRoute(), builderOptions);
                    }
                });

        mAdvanced.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onClicked() {
                        NavHostControllerKt.navigate(navController, Screen.Primary.Settings.Advanced.INSTANCE.getRoute(), builderOptions);
                    }
                });
    }

    protected void updateValues() {
//        Core core = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
//        if (core != null) {
//            mTunnel.setVisibility(core.tunnelAvailable() ? View.VISIBLE : View.GONE);
//            initAccounts(core);
//        }
    }

    private void initAccounts(Core core) {
        mAccounts.removeAllViews();
        ProxyConfig[] proxyConfigs = core.getProxyConfigList();

        if (proxyConfigs == null || proxyConfigs.length == 0) {
            mAccountsHeader.setVisibility(View.GONE);
        } else {
            mAccountsHeader.setVisibility(View.VISIBLE);
            int i = 0;
            for (ProxyConfig proxyConfig : proxyConfigs) {
//                final LedSetting account = new LedSetting(getActivity());
//                account.setTitle(
//                        LinphoneUtils.getDisplayableAddress(proxyConfig.getIdentityAddress()));
//
//                if (proxyConfig.equals(core.getDefaultProxyConfig())) {
//                    account.setSubtitle(getString(R.string.default_account_flag));
//                }
//
//                switch (proxyConfig.getState()) {
//                    case Ok:
//                        account.setColor(LedSetting.Color.GREEN);
//                        break;
//                    case Failed:
//                        account.setColor(LedSetting.Color.RED);
//                        break;
//                    case Progress:
//                        account.setColor(LedSetting.Color.ORANGE);
//                        break;
//                    case None:
//                    case Cleared:
//                        account.setColor(LedSetting.Color.GRAY);
//                        break;
//                }

//                final int accountIndex = i;
//                account.setListener(
//                        new SettingListenerBase() {
//                            @Override
//                            public void onClicked() {
//                                LinphoneActivity.instance().displayAccountSettings(accountIndex);
//                            }
//                        });
//
//                mAccounts.addView(account);
//                i += 1;
            }
        }
    }

    public class BasicSetting extends LinearLayout {
        protected Context mContext;
        protected View mView;
        protected TextView mTitle, mSubtitle;
        protected SettingListener mListener;

        public BasicSetting(Context context) {
            super(context);
            mContext = context;
            init(null, 0, 0);
        }

        public BasicSetting(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
            mContext = context;
            init(attrs, 0, 0);
        }

        public BasicSetting(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            mContext = context;
            init(attrs, defStyleAttr, 0);
        }

        public BasicSetting(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
            mContext = context;
            init(attrs, defStyleAttr, defStyleRes);
        }

        protected void inflateView() {
            mView = LayoutInflater.from(mContext).inflate(R.layout.settings_widget_basic, this, true);
        }

        public void setListener(SettingListener listener) {
            mListener = listener;
        }

        protected void init(@Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            inflateView();

            mTitle = mView.findViewById(R.id.setting_title);
            mSubtitle = mView.findViewById(R.id.setting_subtitle);

            RelativeLayout rlayout = mView.findViewById(R.id.setting_layout);
            rlayout.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mTitle.isEnabled() && mListener != null) {
                            mListener.onClicked();
                        }
                    }
                });

            if (attrs != null) {
                TypedArray a =
                    mContext.getTheme()
                        .obtainStyledAttributes(
                            attrs, R.styleable.Settings, defStyleAttr, defStyleRes);
                try {
                    String title = a.getString(R.styleable.Settings_title);
                    if (title != null) {
                        mTitle.setText(title);
                    } else {
                        mTitle.setVisibility(GONE);
                    }

                    String subtitle = a.getString(R.styleable.Settings_subtitle);
                    if (subtitle != null) {
                        mSubtitle.setText(subtitle);
                    } else {
                        mSubtitle.setVisibility(GONE);
                    }
                } finally {
                    a.recycle();
                }
            }
        }

        public void setTitle(String title) {
            mTitle.setText(title);
            mTitle.setVisibility(title == null || title.isEmpty() ? GONE : VISIBLE);
        }

        public void setSubtitle(String subtitle) {
            mSubtitle.setText(subtitle);
            mSubtitle.setVisibility(subtitle == null || subtitle.isEmpty() ? GONE : VISIBLE);
        }

        public void setEnabled(boolean enabled) {
            mTitle.setEnabled(enabled);
            mSubtitle.setEnabled(enabled);
        }
    }

    public interface SettingListener {
        void onClicked();

        void onTextValueChanged(String newValue);

        void onBoolValueChanged(boolean newValue);

        void onListValueChanged(int position, String newLabel, String newValue);
    }

    public class SettingListenerBase implements SettingListener {
        public void onClicked() {}

        public void onTextValueChanged(String newValue) {}

        public void onBoolValueChanged(boolean newValue) {}

        public void onListValueChanged(int position, String newLabel, String newValue) {}
    }
}
