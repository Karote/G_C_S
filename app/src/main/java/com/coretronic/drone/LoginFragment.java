package com.coretronic.drone;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.coretronic.drone.util.AppConfig;

public class LoginFragment extends Fragment {

    private EditText mUserMailEditText;
    private EditText mUserPasswordEditText;
    private CheckBox mIsStayLoginCheckBox;
    private SharedPreferences mSharedPreferences;
    private MainActivity mMainActivity;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mSharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        mMainActivity = (MainActivity) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUserMailEditText = (EditText) view.findViewById(R.id.ed_user_id);
        mUserPasswordEditText = (EditText) view.findViewById(R.id.ed_user_pwd);
        mIsStayLoginCheckBox = (CheckBox) view.findViewById(R.id.chk_stay_logged);
        view.findViewById(R.id.login_ok_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail = mUserMailEditText.getText().toString();
                String userPassword = mUserPasswordEditText.getText().toString();

                if (checkInputDataValid(userEmail, userPassword)) {
                    mSharedPreferences.edit().putString(AppConfig.SHARED_PREFERENCE_USER_MAIL_KEY, userEmail)
                            .putString(AppConfig.SHARED_PREFERENCE_USER_STAY_LOGIN_KEY, userPassword)
                            .putBoolean(AppConfig.SHARED_PREFERENCE_USER_STAY_LOGIN_KEY, mIsStayLoginCheckBox.isChecked())
                            .apply();
                    mMainActivity.switchToMainFragment();
                }
            }
        });
    }

    private boolean checkInputDataValid(String mail, String password) {
        if (mail.trim().length() == 0) {
            showWarningToast("Email is null");
            return false;
        }
        if (password.trim().length() == 0) {
            showWarningToast("Passwd is null");
            return false;
        }

        if (!isValidEmail(mail)) {
            showWarningToast("Email format error");
            return false;
        }

        String oldUserMail = mSharedPreferences.getString(AppConfig.SHARED_PREFERENCE_USER_MAIL_KEY, "");
        String oldUserPassword = mSharedPreferences.getString(AppConfig.SHARED_PREFERENCE_USER_PASSWORD_KEY, "");

        if ((oldUserMail.length() == 0) || (oldUserPassword.length() == 0)) {
            return true;
        }

        if (!mail.equals(oldUserMail)) {
            showWarningToast("Email error");
            return false;
        }
        if (!password.equals(oldUserPassword)) {
            showWarningToast("Password error");
            return false;
        }

        return true;
    }

    private void showWarningToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    private boolean isValidEmail(String mail) {
        return mail != null && android.util.Patterns.EMAIL_ADDRESS.matcher(mail).matches();
    }
}
