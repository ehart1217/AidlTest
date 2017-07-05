package com.android.jv.ink.aidltest.aidltest;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.jv.ink.launcherink.aidllib.CyUserCallback;
import com.android.jv.ink.launcherink.aidllib.CyUserHelper;
import com.android.jv.ink.launcherink.aidllib.CyUserInfo;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    public static final String ACCOUNT_TYPE = "com.android.by.user";
    public static final String AUTH_TOKEN_TYPE = "com.android.by.user.token";

    private CyUserHelper mUserHelper;
    private AccountManager mAccountManager;
    private String mToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mUserHelper = new CyUserHelper(this);
        mUserHelper.bind(mUserCallback);
        mAccountManager = AccountManager.get(this);
    }

    @Override
    protected void onDestroy() {
        mUserHelper.unbind();
        super.onDestroy();
    }

    private CyUserCallback mUserCallback = new CyUserCallback.Stub() {

        @Override
        public void onLoginResult(final CyUserInfo cyUserInfo) throws RemoteException {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (cyUserInfo == null) {
                        // 登录失败
                        Toast.makeText(MainActivity.this, "result:fail", Toast.LENGTH_SHORT).show();
                    } else {
                        // 登录成功
                        Toast.makeText(MainActivity.this, "result:" + cyUserInfo, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onLoginResult: " + cyUserInfo);
                        mToken = cyUserInfo.getToken();
                    }
                }
            });
        }

        @Override
        public void onLogoutResult(boolean b) throws RemoteException {
            Toast.makeText(MainActivity.this, "result:" + b, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onGetReadRightStatus(String s) throws RemoteException {
            if (TextUtils.isEmpty(s)) {
                // 获取失败
                Toast.makeText(MainActivity.this, "result:fail", Toast.LENGTH_SHORT).show();
            } else {
                // 获取成功
                Toast.makeText(MainActivity.this, "result:" + s, Toast.LENGTH_SHORT).show();
            }
        }
    };

    public void onRequestLogin(View view) {
        try {
            mUserHelper.requestLogin(this);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
//        mAccountManager.getAuthTokenByFeatures(ACCOUNT_TYPE, AUTH_TOKEN_TYPE,
//                null, this, null, null, new GetAuthTokenCallback(this), null);

    }

    private class GetAuthTokenCallback implements AccountManagerCallback<Bundle> {

        private Activity activity;

        GetAuthTokenCallback(Activity activity) {
            this.activity = activity;
        }

        @Override
        public void run(AccountManagerFuture<Bundle> result) {
            Bundle bundle;

            try {
                bundle = result.getResult();
                final Intent intent = (Intent) bundle.get(AccountManager.KEY_INTENT);
                if (null != intent) {
                    activity.startActivityForResult(intent, 1);
                } else {
                    // 不需要登录,直接获取token
                    String authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);

                    final String accountName = bundle.getString(AccountManager.KEY_ACCOUNT_NAME);
                    // If the logged account didn't exist, we need to create it on the device
                    Account account = getAccount(activity, accountName);
                    if (null == account) {
                        account = new Account(accountName, ACCOUNT_TYPE);
                        mAccountManager.addAccountExplicitly(account, null, null);
                        mAccountManager.setAuthToken(account, AUTH_TOKEN_TYPE, authToken);
                    }
                    Toast.makeText(MainActivity.this, "accountName:" + accountName, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private Account getAccount(Activity activity, String accountName) {
        AccountManager accountManager = AccountManager.get(activity);
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.GET_ACCOUNTS}, 2);
            return null;
        }
        Account[] accounts = accountManager.getAccountsByType(ACCOUNT_TYPE);
        for (Account account : accounts) {
            if (account.name.equalsIgnoreCase(accountName)) {
                return account;
            }
        }
        return null;
    }

    public void onRequestLogout(View view) {
        if (TextUtils.isEmpty(mToken)) {
            Toast.makeText(this, "请先登录获得token", Toast.LENGTH_SHORT).show();
        } else {
//            try {
//                mUserHelper.requestLogout(mToken);
//            } catch (RemoteException e) {
//                e.printStackTrace();
//            }
            mAccountManager.invalidateAuthToken(ACCOUNT_TYPE, mToken);
            try {
                mUserHelper.requestLogin(this);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mToken = "";
        }

    }

    public void onRequestReadStatus(View view) {
        try {
            mUserHelper.requestReadRightStatus();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
