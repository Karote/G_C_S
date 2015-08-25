package com.coretronic.drone;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.coretronic.drone.utility.AppConfig;

public class LoginFragment extends Fragment {

    private EditText edUserId;
    private EditText edUserPw;
    private CheckBox stayLogged;
    private SharedPreferences sharedPreferences;
    public static final String ARG_USER_ID = "user_id";
    public static final String ARG_USER_PW = "user_passwd";
    public static final String ARG_STAY_LOGGED = "stay_logged";


    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getActivity().getSharedPreferences(AppConfig.SHAREDPREFERENCE_ID, Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean(ARG_STAY_LOGGED, false)) {
            replaceFragment();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_login, container, false);
        initView(v);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void initView(View v) {
        edUserId = (EditText) v.findViewById(R.id.ed_user_id);
        edUserId.setText(sharedPreferences.getString(ARG_USER_ID,""));
        edUserPw = (EditText) v.findViewById(R.id.ed_user_pwd);
        stayLogged = (CheckBox) v.findViewById(R.id.chk_stay_logged);
        Button btnOk = (Button) v.findViewById(R.id.btn_login_ok);
        btnOk.setOnClickListener(btnListener);
    }


    View.OnClickListener btnListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            String uid = edUserId.getText().toString();
            String upw = edUserPw.getText().toString();

            if (checkID(uid, upw)) {
                sharedPreferences.edit().putString(ARG_USER_ID, uid)
                        .putString(ARG_USER_PW, upw)
                        .putBoolean(ARG_STAY_LOGGED, stayLogged.isChecked())
                        .apply();
                replaceFragment();
            }
        }
    };


    private void replaceFragment() {
        MainFragment fragment = new MainFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_view, fragment, LoginFragment.class.getSimpleName());
        transaction.commit();
    }


    private boolean checkID(String uid, String pw) {
        if (uid.trim().length() == 0) {
            Toast.makeText(getActivity(), "Email is null", Toast.LENGTH_SHORT).show();
            return false;
        } else if (pw.trim().length() == 0) {
            Toast.makeText(getActivity(), "Passwd is null", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(!isValidEmail(uid)){
            Toast.makeText(getActivity(), "Email format error", Toast.LENGTH_SHORT).show();
            return false;
        }

        String oldUserId = sharedPreferences.getString(ARG_USER_ID, "");
        String oldUserPw = sharedPreferences.getString(ARG_USER_PW, "");

        if ((oldUserId.length() == 0) || (oldUserPw.length() == 0)) {
            return true;
        }

        if (!uid.equals(oldUserId)) {
            Toast.makeText(getActivity(), "Email error", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!pw.equals(sharedPreferences.getString(ARG_USER_PW, ""))) {
            Toast.makeText(getActivity(), "Password error", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    public final static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }
}
