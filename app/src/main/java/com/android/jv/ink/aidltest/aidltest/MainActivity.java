package com.android.jv.ink.aidltest.aidltest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.jv.ink.launcherink.aidllib.CyBalanceInfo;
import com.android.jv.ink.launcherink.aidllib.CyContract;
import com.android.jv.ink.launcherink.aidllib.CyPayCallback;
import com.android.jv.ink.launcherink.aidllib.CyPayHelper;
import com.android.jv.ink.launcherink.aidllib.CyUserCallback;
import com.android.jv.ink.launcherink.aidllib.CyUserHelper;
import com.android.jv.ink.launcherink.aidllib.CyUserInfo;

import static android.util.Log.i;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    private static final int REQUEST_CODE_BALANCE_PAY = 0;
    private static final int REQUEST_CODE_BALANCE_INFO = 1;
    private static final int REQUEST_CODE_WECHAT_PAY = 2;
    private static final int REQUEST_CODE_ALIPAY = 3;
    private static final int REQUEST_CODE_RECHARGE_BALANCE = 4;
    private static final int REQUEST_CODE_BUY_MOYUE_CARD = 5;

    private static final int REQUEST_CODE_LOGIN = 6;
    private static final int REQUEST_CODE_USER_INFO = 7;
    private static final int REQUEST_CODE_READ_INFO = 8;

    private CyUserHelper mUserHelper;
    private CyPayHelper mPayHelper;
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

        // 创建对象
        mUserHelper = new CyUserHelper(this);
        mPayHelper = new CyPayHelper(this);

        // 绑定aidl服务
        mUserHelper.bind(mUserCallback);
        mPayHelper.bind(mPayCallback);
        Log.d(TAG, "bind service");

        // 接收全局登入登出的广播
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
     * @param view btn clicked callback
     */
    public void onRequestLogin(View view) {
        try {
            mUserHelper.requestLogin(REQUEST_CODE_LOGIN, this);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 请求退出登录
     *
     * @param view btn clicked callback
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
     * @param view btn clicked callback
     */
    public void onRequestReadStatus(View view) {
        try {
            mUserHelper.requestReadRightStatus(REQUEST_CODE_READ_INFO, this);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 请求余额支付
     *
     * @param view btn clicked callback
     */
    public void onRequestBalancePay(View view) {
        //   "sign": "hiZN+4356O0FXJUqcCcVGvl9s1c/2iR/E8p9ZT8UVrIf4qMLxK8oHA+UTTrfVfvPbbyTm6W7J3rGCSNFNHT+3UlIwoSgOqcCZ0fSTtU9LwTs3+9CYUcWe7iNF7NIS8HNqWnY4NPpebaFoagk2O+U5gFsTfwOzjdt7Pcua+GVwMI=",
//        "signType": "RSA",
//                "sourceIp": "1.1.1.1",
//                "timestamp": "2017-07-12 21:52:51",
        mPayHelper.requestBalancePay(REQUEST_CODE_BALANCE_PAY, this, sign, signType, timestamp, transData);
    }

    /**
     * 请求查询余额
     *
     * @param view btn clicked callback
     */
    public void onRequestBalanceInfo(View view) {
        mPayHelper.requestBalanceInfo(REQUEST_CODE_BALANCE_INFO, this);
    }

    /**
     * 微信直接支付
     *
     * @param view btn clicked callback
     */
    public void wechatPay(View view) {

        mPayHelper.requestWechatPay(REQUEST_CODE_WECHAT_PAY, this, sign, signType, timestamp, transData);
    }

    /**
     * 支付宝直接支付
     *
     * @param view btn clicked callback
     */
    public void aliPay(View view) {
        mPayHelper.requestAliPay(REQUEST_CODE_ALIPAY, this, sign, signType, timestamp, transData);
    }

    /**
     * 请求余额充值
     *
     * @param view btn clicked callback
     */
    public void requestRechargeBalance(View view) {
        // 第二个参数，金额，单位为分。
        mPayHelper.requestBalanceRecharge(REQUEST_CODE_RECHARGE_BALANCE, MainActivity.this, 1, "4002");
    }

    public void requestBuyMoyueCard(View view) {
        mPayHelper.requestBuyMoyueCard(REQUEST_CODE_BUY_MOYUE_CARD, this);
    }


    // 测试数据
    private void initTestPayParams() {
        sign = "j2iX3+klU7yUkbG08Y/z5ExHOeZL8In448h8IL74vsMtil0CYYXhztmeeOL6SMtEX79H9Up1Z1zW8LkmB8YlPT4BhZpuImi51vAG1YxTwy7LiYZ2msJ1pAkOPqyq77Hh41lCdBLxFiphK8C2YqCRI5n4Pd6UJjSGaJjYXsYf4M0=";
        signType = "RSA";
        timestamp = "2017-07-15 17:41:36";
        transData = "{\"appId\":4001,\"outerTradeNo\":\"123452678941\",\"subject\":\"测试test!!\",\"totalAmount\":1,\"waresCount\":1,\"waresId\":\"1\"}";
    }

    /**
     * 用户登录等信息的统一回调
     */
    private CyUserCallback mUserCallback = new CyUserCallback.Stub() {

        /**
         * 登录结果
         * @param cyUserInfo 用户信息，当这个值为null的时候，代表请求失败了
         * @param s 失败的信息，失败才有这个值。
         * @throws RemoteException
         */
        @Override
        public void onLoginResult(final int requestCode, final int resultCode, final CyUserInfo cyUserInfo, final String s) throws RemoteException {
            // 注意这里可能在非主线程
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "requestCode:" + requestCode);
                    Log.d(TAG, "resultCode:" + resultCode);
                    Log.d(TAG, "errorInfo:" + s);

                    if (resultCode == CyContract.CODE_TOKEN_INVALID) {

                    }
                    if (resultCode == CyContract.CODE_POST_ERROR) {

                    }
                    if (resultCode == CyContract.CODE_SUCCESS) {
                        Toast.makeText(MainActivity.this, "result:" + cyUserInfo, Toast.LENGTH_SHORT).show();
                        mToken = cyUserInfo.getToken();
                    } else {
                        Toast.makeText(MainActivity.this, "失败：" + resultCode, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        /**
         * 退出登录结果
         * @param b 成功与否
         * @throws RemoteException
         */
        @Override
        public void onLogoutResult(boolean b) throws RemoteException {
            if (b) {
                mToken = "";
            }
            Toast.makeText(MainActivity.this, "result:" + b, Toast.LENGTH_SHORT).show();
        }

        /**
         * 得到阅读状态信息
         * @param code 阅读状态码，{@link CyContract#READ_CODE_VALID}等。
         * @param s 失败才有的信息
         * @throws RemoteException
         */
        @Override
        public void onGetReadRightStatus(final int requestCode, final int code, final String s) throws RemoteException {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "requestCode:" + requestCode);
                    Log.d(TAG, "resultCode:" + code);
                    Log.d(TAG, "info:" + s);
                    i(TAG, "getReadRightStatus,code:" + code + " desc:" + s);
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
        public void onBind() throws RemoteException {
            Log.d(TAG, "onBind");
            Toast.makeText(MainActivity.this, "onb", Toast.LENGTH_SHORT).show();
        }
    };

    /**
     * 支付的统一回调
     */
    private CyPayCallback mPayCallback = new CyPayCallback.Stub() {

        /**
         * 支付的统一回调
         * @param requestCode 用来标记对清的请求。
         * @param resultCode  结果code，{@link CyContract#CODE_SUCCESS}等。
         * @param successInfo 成功才有的信息。
         * @param errorInfo 失败才有的信息
         * @throws RemoteException
         */
        @Override
        public void onPayResult(final int requestCode, final int resultCode, final String successInfo, final String errorInfo) throws RemoteException {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    Log.i(TAG, "run: requestCode:" + requestCode);
                    Log.i(TAG, "run: resultCode:" + resultCode);

                    if (resultCode == CyContract.CODE_SUCCESS) {
                        i(TAG, "onPayResult:支付成功:result: " + successInfo);
                        Toast.makeText(MainActivity.this, "支付成功:result: " + successInfo, Toast.LENGTH_SHORT).show();
                    } else if (resultCode == CyContract.CODE_INSUFFICIENT_BALANCE) {
                        i(TAG, "onPayResult:余额不足:info: " + successInfo);
                        Toast.makeText(MainActivity.this, "余额不足:info: " + successInfo + successInfo, Toast.LENGTH_SHORT).show();
                        mPayHelper.requestBalanceRecharge(REQUEST_CODE_RECHARGE_BALANCE, MainActivity.this, 1, "4002");
                    } else {
                        Toast.makeText(MainActivity.this, "余额支付结果:error: " + resultCode + "," + errorInfo, Toast.LENGTH_SHORT).show();
                        i(TAG, "onPayResult:余额支付结果:error: " + errorInfo);
                    }
                }
            });

        }

        /**
         * 得到余额信息
         * @param requestCode 请求对应的code
         * @param success 成功或者失败
         * @param cyBalanceInfo 余额信息，成功才有
         * @param errorInfo 失败信息，失败才有
         * @throws RemoteException
         */
        @Override
        public void onGetBalanceInfo(final int requestCode, final boolean success, final CyBalanceInfo cyBalanceInfo, final String errorInfo) throws RemoteException {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    i(TAG, "onGetBalanceInfo, requestCode:" + requestCode);
                    if (success) {
                        i(TAG, "onGetBalanceInfo:获余额信息成功:result: " + cyBalanceInfo);
                        Toast.makeText(MainActivity.this, "获余额信息成功:result:" + cyBalanceInfo, Toast.LENGTH_SHORT).show();
                    } else {
                        i(TAG, "onGetBalanceInfo: 获取余额信息失败:error: " + errorInfo);
                        Toast.makeText(MainActivity.this, "获取余额信息失败:error:" + errorInfo, Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }

        /**
         * token过期
         * @param requestCode 请求对应的code
         * @throws RemoteException
         */
        @Override
        public void onTokenInvalid(final int requestCode) throws RemoteException {
            i(TAG, "onTokenInvalid, requestCode:" + requestCode);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "onTokenInvalid", Toast.LENGTH_SHORT).show();
                }
            });
        }

        /**
         * 服务绑定成功的回调
         */
        @Override
        public void onBind() throws RemoteException {

        }
    };

    public void onRequestUserInfo(View view) {
        mUserHelper.requestUserInfo(REQUEST_CODE_USER_INFO, this);
    }
}
