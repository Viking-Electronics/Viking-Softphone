package com.vikingelectronics.softphone.accounts.add.legacy;
/*
LoginFragment.java
Copyright (C) 2017  Belledonne Communications, Grenoble, France

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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.vikingelectronics.softphone.R;
import com.vikingelectronics.softphone.util.LinphoneManager;
import com.vikingelectronics.softphone.util.PermissionsManager;

import org.json.JSONArray;
import org.json.JSONObject;
import org.linphone.assistant.AssistantActivity;
import org.linphone.core.TransportType;

import java.util.ArrayList;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import kotlin.Unit;

@AndroidEntryPoint
public class LoginFragment extends Fragment implements OnClickListener, TextWatcher {
    private EditText mLogin, mUserid, mPassword, mDomain, mDisplayName;
    private RadioGroup mTransports;
    private Button mApply;
    private ImageButton mScanner;
    private NavController navController;

    @Inject
    PermissionsManager permissionsManager;
    @Inject
    LinphoneManager linphoneManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_legacy_login, container, false);


        mLogin = view.findViewById(R.id.assistant_username);
        mLogin.addTextChangedListener(LoginFragment.this);

        mDisplayName = view.findViewById(R.id.assistant_display_name);
        mDisplayName.addTextChangedListener(LoginFragment.this);
        mUserid = view.findViewById(R.id.assistant_userid);
        mUserid.addTextChangedListener(LoginFragment.this);
        mPassword = view.findViewById(R.id.assistant_password);
        mPassword.addTextChangedListener(LoginFragment.this);

        mDomain = view.findViewById(R.id.assistant_domain);
        mDomain.addTextChangedListener(LoginFragment.this);

        mTransports = view.findViewById(R.id.assistant_transports);
        mApply = view.findViewById(R.id.assistant_apply);
        mApply.setEnabled(false);
        mApply.setOnClickListener(LoginFragment.this);
        mScanner = view.findViewById(R.id.qr_button);
        mScanner.setOnClickListener(LoginFragment.this);
        String logstr = "QR Scanner";
        Bundle args = getArguments();
        try{
            Log.i(logstr, args.toString());
            mDomain.setText(args.getString("url", ""));
            if(args.getBoolean("multi")){
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("Choose a user");
                JSONArray creds = new JSONArray(args.getString("passes", ""));
                final ArrayList<String> users = new ArrayList<>();
                final ArrayList<String> passes = new ArrayList<>();
                boolean end = false;
                int i = 1;
                while(!end){
                    try{
                        JSONObject obj = creds.getJSONObject(i);
                        users.add(args.getString("num", "user") + 'u' + i);
                        passes.add(obj.getString(Integer.toString(i)));
                        i++;
                    }
                    catch (Exception e){
                        end = true;
                    }
                }
                builder.setSingleChoiceItems(users.toArray(new String[i-2]), 0, null);
                builder.setPositiveButton("OK", (dialog, which) -> {
                    int pos = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
                    mLogin.setText(users.get(pos));
                    mUserid.setText(users.get(pos));
                    mPassword.setText(passes.get(pos));
                });
                builder.setNegativeButton("Cancel", null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
            else{
                mPassword.setText(args.getString("pass", ""));
                mLogin.setText(args.getString("num", ""));
                mUserid.setText(args.getString("num", ""));
            }
        }
        catch (Exception e){
            Log.e(logstr, e.toString());
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        mLogin.setText("5514255221u1");
        mPassword.setText("salabledba4602");
        mDomain.setText("sip.myviking.com:5799");

        navController = Navigation.findNavController(requireView());
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.assistant_apply) {
            if (mLogin.getText() == null
                    || mLogin.length() == 0
                    || mPassword.getText() == null
                    || mPassword.length() == 0
                    || mDomain.getText() == null
                    || mDomain.length() == 0) {
                Toast.makeText(
                                getActivity(),
                                getString(R.string.first_launch_no_login_password),
                                Toast.LENGTH_LONG)
                        .show();
                return;
            }

            TransportType transport;
            if (mTransports.getCheckedRadioButtonId() == R.id.transport_tls) {
                transport = TransportType.Tls;
            } else if (mTransports.getCheckedRadioButtonId() == R.id.transport_tcp) {
                transport = TransportType.Tcp;
            } else {
                transport = TransportType.Udp;
            }

            linphoneManager.login(
                mLogin.getText().toString(),
                mPassword.getText().toString(),
                mDomain.getText().toString(),
                transport,
                mUserid.getText().toString(),
                mDisplayName.getText().toString()
            );

//            navController.navigateUp();

        }
        else if(id == R.id.qr_button){
            Log.i("QR Scanner", "Starting");
            permissionsManager.requestPermissionsForQRReading(() -> {
                navController.navigate(R.id.qrFragment);
                return Unit.INSTANCE;
            });

        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        mApply.setEnabled(
                !mLogin.getText().toString().isEmpty()
                        && !mPassword.getText().toString().isEmpty()
                        && !mDomain.getText().toString().isEmpty());
    }

    @Override
    public void afterTextChanged(Editable s) {}
}