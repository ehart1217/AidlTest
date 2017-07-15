package com.android.jv.ink.aidltest.aidltest;

import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcel;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.jv.ink.launcherink.aidllib.AccountUtils;
import com.android.jv.ink.launcherink.aidllib.CyBalanceInfo;
import com.android.jv.ink.launcherink.aidllib.CyPayCallback;
import com.android.jv.ink.launcherink.aidllib.CyPayHelper;
import com.android.jv.ink.launcherink.aidllib.CyUserCallback;
import com.android.jv.ink.launcherink.aidllib.CyUserHelper;
import com.android.jv.ink.launcherink.aidllib.CyUserInfo;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    public static final String ACCOUNT_TYPE = "com.android.by.user";
    public static final String AUTH_TOKEN_TYPE = "com.android.by.user.token";

    private CyUserHelper mUserHelper;
    private CyPayHelper mPayHelper;
    private AccountManager mAccountManager;
    private String mToken;

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(CyUserHelper.ACTION_LOGOUT)) {
                Toast.makeText(context, "收到了退出登录的广播！", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onReceive: 收到了退出登录的广播");
            } else if (action.equals(CyUserHelper.ACTION_LOGIN)) {
                Toast.makeText(context, "收到了登录的广播", Toast.LENGTH_SHORT).show();
                CyUserInfo userInfo = intent.getParcelableExtra(CyUserHelper.KEY_USER_INFO);
                Log.d(TAG, "onReceive: 收到了登录的广播,userInfo:" + userInfo);
            }
        }
    };
    private String sign;
    private String signType;
    private String timestamp;
    private String transData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        initTestPayParams();

        mUserHelper = new CyUserHelper(this);
        mPayHelper = new CyPayHelper(this);
        mUserHelper.bind(mUserCallback);
        mPayHelper.bind(mPayCallback);
        Log.d(TAG, "bind service");
        mAccountManager = AccountManager.get(this);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CyUserHelper.ACTION_LOGOUT);
        intentFilter.addAction(CyUserHelper.ACTION_LOGIN);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        mUserHelper.unbind();
        mPayHelper.unbind();
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    /**
     * 请求登录
     *
     * @param view
     */
    public void onRequestLogin(View view) {
        try {
            mUserHelper.requestLogin(this);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 请求退出登录
     *
     * @param view
     */
    public void onRequestLogout(View view) {
        if (TextUtils.isEmpty(mToken)) {
            Toast.makeText(this, "请先登录获得token", Toast.LENGTH_SHORT).show();
        } else {
            try {
                mUserHelper.requestLogout(mToken);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 请求阅读权益状态
     *
     * @param view
     */
    public void onRequestReadStatus(View view) {
        try {
            mUserHelper.requestReadRightStatus();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 请求余额支付
     *
     * @param view
     */
    public void onRequestBalancePay(View view) {
        //   "sign": "hiZN+4356O0FXJUqcCcVGvl9s1c/2iR/E8p9ZT8UVrIf4qMLxK8oHA+UTTrfVfvPbbyTm6W7J3rGCSNFNHT+3UlIwoSgOqcCZ0fSTtU9LwTs3+9CYUcWe7iNF7NIS8HNqWnY4NPpebaFoagk2O+U5gFsTfwOzjdt7Pcua+GVwMI=",
//        "signType": "RSA",
//                "sourceIp": "1.1.1.1",
//                "timestamp": "2017-07-12 21:52:51",
        mPayHelper.requestBalancePay(this, sign, signType, timestamp, transData);
    }

    /**
     * 请求查询余额
     *
     * @param view
     */
    public void onRequestBalanceInfo(View view) {
        mPayHelper.requestBalanceInfo(this);
    }

    /**
     * 微信直接支付
     *
     * @param view
     */
    public void wechatPay(View view) {

        mPayHelper.requestWechatPay(this, sign, signType, timestamp, transData);
    }

    /**
     * 支付宝直接支付
     *
     * @param view
     */
    public void aliPay(View view) {
        mPayHelper.requestAliPay(this, sign, signType, timestamp, transData);
    }

    /**
     * 请求余额充值
     *
     * @param view
     */
    public void requestRechargeBalance(View view) {
        mPayHelper.requestBalanceRecharge(MainActivity.this, 1);
    }


    private void initTestPayParams() {
        sign = "mDmpvcF94Fos/hIs1ojJ7nOunf6dpQS5mQD0khV47gII+cMnQZ9JoXDWNnpjehsvIU+4lLbPkDAYyRhyreIKB/9wkJxfZNB8FMPwMocVQ8EzsgTXN/aDe9M66bypTG+Ep257zFEJa17yri+Dibm4wvnPI6EXc5PONkz7Q6ATS8A=";
        signType = "RSA";
        timestamp = "2017-07-14 15:35:14";
        transData = "{\"appId\":4001,\"outerTradeNo\":\"12345267892641\",\"subject\":\"测试test!!\",\"totalAmount\":1,\"waresCount\":1,\"waresId\":\"1\"}";
    }

    private CyUserCallback mUserCallback = new CyUserCallback.Stub() {

        @Override
        public void onLoginResult(final CyUserInfo cyUserInfo, final String s) throws RemoteException {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (cyUserInfo == null) {
                        // 登录失败
                        Toast.makeText(MainActivity.this, "result,error:" + s, Toast.LENGTH_SHORT).show();
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
            if (b) {
                mToken = "";
            }
            Toast.makeText(MainActivity.this, "result:" + b, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onGetReadRightStatus(final String s) throws RemoteException {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (TextUtils.isEmpty(s)) {
                        // 获取失败
                        Toast.makeText(MainActivity.this, "result:fail", Toast.LENGTH_SHORT).show();
                    } else {
                        // 获取成功
                        Toast.makeText(MainActivity.this, "result:" + s, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        @Override
        public void onTokenInvalid() throws RemoteException {

        }

        @Override
        public void onBind() throws RemoteException {
            Log.d(TAG, "onBind");
            Toast.makeText(MainActivity.this, "onb", Toast.LENGTH_SHORT).show();
        }

        @Override
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            return super.onTransact(code, data, reply, flags);
        }
    };

    private CyPayCallback mPayCallback = new CyPayCallback.Stub() {
        @Override
        public void onPayResult(final int code, final String s, final String errorInfo) throws RemoteException {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (code == AccountUtils.CODE_SUCCESS) {
                        Log.i(TAG, "onPayResult:支付成功:result: " + s);
                        Toast.makeText(MainActivity.this, "支付成功:result: " + s, Toast.LENGTH_SHORT).show();
                    } else if (code == AccountUtils.CODE_INSUFFICIENT_BALANCE) {
                        Log.i(TAG, "onPayResult:余额不足:info: " + s);
                        Toast.makeText(MainActivity.this, "余额不足:info: " + s + s, Toast.LENGTH_SHORT).show();
                        mPayHelper.requestBalanceRecharge(MainActivity.this, 1);
                    } else {
                        Toast.makeText(MainActivity.this, "余额支付结果:error: " + errorInfo, Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "onPayResult:余额支付结果:error: " + errorInfo);
                    }
                }
            });

        }

        @Override
        public void onGetBalanceInfo(final boolean b, final CyBalanceInfo cyBalanceInfo, final String s) throws RemoteException {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (b) {
                        Log.i(TAG, "onGetBalanceInfo:获余额信息成功:result: " + cyBalanceInfo);
                        Toast.makeText(MainActivity.this, "获余额信息成功:result:" + cyBalanceInfo, Toast.LENGTH_SHORT).show();
                    } else {
                        Log.i(TAG, "onGetBalanceInfo: 获取余额信息失败:error: " + s);
                        Toast.makeText(MainActivity.this, "获取余额信息失败:error:" + s, Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }

        @Override
        public void onTokenInvalid() throws RemoteException {
            Toast.makeText(MainActivity.this, "onTokenInvalid", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onBind() throws RemoteException {

        }
    };
}
