package com.vikingelectronics.softphone.accounts.login;

/*
QrCodeFragment.java
Copyright (C) 2018  Belledonne Communications, Grenoble, France

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

import android.util.Log;
import android.view.TextureView;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavBackStackEntry;

import com.vikingelectronics.softphone.accounts.login.LoginViewModel;

import org.json.JSONException;
import org.json.JSONObject;
import org.linphone.R;
import org.linphone.assistant.AssistantActivity;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;

import javax.inject.Inject;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.hilt.android.AndroidEntryPoint;

//@AndroidEntryPoint
public class QrCodeFragment extends Fragment {
    public QrCodeFragment() {
        super(R.layout.qrcode);
    }


//    @Inject
    public Core core;

    public LoginViewModel viewModel;

    private TextureView mQrcodeView;
    private CoreListenerStub mListener =
        new CoreListenerStub() {
            @Override
            public void onQrcodeFound(Core lc, String result) {
                enableQrcodeReader(false);
                try {
                    JSONObject json = new JSONObject(result);
                    AssistantActivity.instance().displayLoginGeneric(json);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };


    private void enableQrcodeReader(boolean enable) {
        core.setNativePreviewWindowId(enable ? mQrcodeView : null);
        core.enableQrcodeVideoPreview(enable);
        core.enableVideoPreview(enable);

        if (enable) {
            core.addListener(mListener);
        } else {
            core.removeListener(mListener);
        }
    }

    private void setBackCamera() {
        String firstDevice = null;
        for (String camera : core.getVideoDevicesList()) {
            if (firstDevice == null) {
                firstDevice = camera;
            }

            if (camera.contains("Back")) {
                Log.i("QR", "[QR Code] Found back facing camera: " + camera);
                core.setVideoDevice(camera);
                return;
            }
        }

        Log.i("QR","[QR Code] Using first camera available: " + firstDevice);
        core.setVideoDevice(firstDevice);
    }

    private void launchQrcodeReader() {
        setBackCamera();
        mQrcodeView = requireView().findViewById(R.id.qrcodeCaptureSurface);
        enableQrcodeReader(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        launchQrcodeReader();
    }

    @Override
    public void onPause() {
        enableQrcodeReader(false);
        super.onPause();
    }
}
