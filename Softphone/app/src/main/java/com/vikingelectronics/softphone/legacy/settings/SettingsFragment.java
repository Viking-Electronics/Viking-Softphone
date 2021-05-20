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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavControllerKt;
import androidx.navigation.NavOptionsBuilder;
import androidx.navigation.compose.NavHostControllerKt;


import com.vikingelectronics.softphone.LegacyFragmentDependencyProvider;
import com.vikingelectronics.softphone.MainActivity;
import com.vikingelectronics.softphone.R;
import com.vikingelectronics.softphone.legacy.LinphoneUtils;
import com.vikingelectronics.softphone.legacy.settings.widget.BasicSetting;
import com.vikingelectronics.softphone.legacy.settings.widget.LedSetting;
import com.vikingelectronics.softphone.legacy.settings.widget.SettingListenerBase;
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

    protected LegacyFragmentDependencyProvider provider;

    private final Function1<NavOptionsBuilder, Unit> builderOptions = navOptionsBuilder -> Unit.INSTANCE;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        provider = (MainActivity) context;
    }

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
                        provider.getNavController().navigate(Screen.Primary.Settings.Tunnel.INSTANCE.getRoute());
//                        NavHostControllerKt.navigate(provider.getNavController(), Screen.Primary.Settings.Tunnel.INSTANCE.getRoute(), builderOptions);
                    }
                });

        mAudio.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onClicked() {
                        provider.getNavController().navigate(Screen.Primary.Settings.Audio.INSTANCE.getRoute());
//                        NavHostControllerKt.navigate(provider.getNavController(), Screen.Primary.Settings.Audio.INSTANCE.getRoute(), builderOptions);
                    }
                });

        mVideo.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onClicked() {
                        provider.getNavController().navigate(Screen.Primary.Settings.Video.INSTANCE.getRoute());

//                        NavHostControllerKt.navigate(provider.getNavController(), Screen.Primary.Settings.Video.INSTANCE.getRoute(), builderOptions);
                    }
                });

        mCall.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onClicked() {
                        provider.getNavController().navigate(Screen.Primary.Settings.Call.INSTANCE.getRoute());

//                        NavHostControllerKt.navigate(provider.getNavController(), Screen.Primary.Settings.Call.INSTANCE.getRoute(), builderOptions);
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
                        provider.getNavController().navigate(Screen.Primary.Settings.Network.INSTANCE.getRoute());

//                        NavHostControllerKt.navigate(provider.getNavController(), Screen.Primary.Settings.Network.INSTANCE.getRoute(), builderOptions);
                    }
                });

        mAdvanced.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onClicked() {
                        provider.getNavController().navigate(Screen.Primary.Settings.Advanced.INSTANCE.getRoute());

//                        NavHostControllerKt.navigate(provider.getNavController(), Screen.Primary.Settings.Advanced.INSTANCE.getRoute(), builderOptions);
                    }
                });
    }

    protected void updateValues() {
        mTunnel.setVisibility(provider.getCore().tunnelAvailable() ? View.VISIBLE : View.GONE);
        initAccounts(provider.getCore());
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
                final LedSetting account = new LedSetting(getActivity());
                account.setTitle(
                        LinphoneUtils.getDisplayableAddress(proxyConfig.getIdentityAddress()));

                if (proxyConfig.equals(core.getDefaultProxyConfig())) {
                    account.setSubtitle(getString(R.string.default_account_flag));
                }

                switch (proxyConfig.getState()) {
                    case Ok:
                        account.setColor(LedSetting.Color.GREEN);
                        break;
                    case Failed:
                        account.setColor(LedSetting.Color.RED);
                        break;
                    case Progress:
                        account.setColor(LedSetting.Color.ORANGE);
                        break;
                    case None:
                    case Cleared:
                        account.setColor(LedSetting.Color.GRAY);
                        break;
                }

                final int accountIndex = i;
                account.setListener(
                        new SettingListenerBase() {
                            @Override
                            public void onClicked() {
//                                LinphoneActivity.instance().displayAccountSettings(accountIndex);
                            }
                        });

                mAccounts.addView(account);
                i += 1;
            }
        }
    }
}
