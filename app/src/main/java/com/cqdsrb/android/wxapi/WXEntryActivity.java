package com.cqdsrb.android.wxapi;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author fantasychong
 * @date 2018/11/5
 */
public class WXEntryActivity extends AppCompatActivity implements IWXAPIEventHandler {
    private IWXAPI iwxapi;
    private String unionid;
    private String openid;
    private ProgressBar progressBar;
    private WXEntryActivity mContext;
    private ProgressDialog mProgressDialog;
    private static final String APP_ID = "wxa25161b2bbe40540"
            ;private static final String APP_ECRET = "294aad898644a29925d7d698da04891c";//私钥
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //接收到分享以及登录的intent传递handleIntent方法，处理结果
        iwxapi = WXAPIFactory.createWXAPI(this, APP_ID, false);
        iwxapi.handleIntent(getIntent(), this); // 第三步

    }

    private void createProgressDialog() {
        mContext=this;
        mProgressDialog=new ProgressDialog(mContext);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);//转盘
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setTitle("提示");
        mProgressDialog.setMessage("登录中，请稍后");
        mProgressDialog.show();
    }

    @Override
    public void onReq(BaseReq baseReq) {

    }

    //请求回调结果处理
    @Override
    public void onResp(BaseResp baseResp) {
        //登录回调 第四步
//        baseResp.getType() == ConstantsAPI.COMMAND_SENDAUTH 登录
        switch (baseResp.errCode) {
            case BaseResp.ErrCode.ERR_OK:

                String code = ((SendAuth.Resp) baseResp).code;
                getAccessToken(code);
                Log.d("fantasychongwxlogin", code.toString()+ "");
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED://用户拒绝授权
                finish();
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL://用户取消
                finish();
                break;
            default:
                finish();
                break;
        }
    }

    private void getAccessToken(String code) {
        createProgressDialog();
        //获取授权 第5步
        StringBuffer loginUrl = new StringBuffer();
        loginUrl.append("https://api.weixin.qq.com/sns/oauth2/access_token")
                .append("?appid=")
                .append(APP_ID)
                .append("&secret=")
                .append(APP_ECRET)
                .append("&code=")
                .append(code)
                .append("&grant_type=authorization_code");
        Log.d("urlurl", loginUrl.toString());

        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(loginUrl.toString())
                .get()//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("fan12", "onFailure: ");
                mProgressDialog.dismiss(); //第6步
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseInfo= response.body().string();
                Log.d("fan12", "onResponse: " +responseInfo);//第6步

                String access = null;
                String openId = null;
                try {
// 获取到的数据 这个时候已经算是登录成功了
//                    {"access_token":
//                        "30_9brj6JUiLXGglYj7XZqHme4T
//                        eYmHL66ytLIo7FA-ztOlANFvBQVc
//                        dkBQTqFqW3MnB4cSKP2voG9lW0Bl
//                        qJrXKbBcuHBG5TQEKmsxCnpaS_4",
//                        "expires_in":
//                        7200,
//                                "refresh_token":"
//                        30_EKYUJuC5UsXjyHPrV5BMRZx5p
//                            KeOrtQCFpg6v8_m3tuFRM4SW7158
//                        wZpKIZjeI7j5FxWQoAA4xTTSBZf4
//                        MurM5frt_KixlNBuvQyGlvPXRE",
//                        "openid":
//                        "o3P-o0h7AtyQNzOUTE1HisR9FYBk"
//                                ,"scope":
//                        "snsapi_userinfo",
//                                "unionid":
//                        "o-hLKwyy-LH1MghTTm3ZLb9Kiyjs"}

                    JSONObject jsonObject = new JSONObject(responseInfo);
                    access = jsonObject.getString("access_token");
                    openId = jsonObject.getString("openid");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                getUserInfo(access, openId);
            }
        });


    }

    private void getUserInfo(String access, String openid) {
        //第7步
        String getUserInfoUrl = "https://api.weixin.qq.com/sns/userinfo?access_token=" + access + "&openid=" + openid;

        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(getUserInfoUrl)
                .get()//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("fan12", "onFailure: ");          //第八步
                mProgressDialog.dismiss();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //第八步
                String responseInfo= response.body().string();
                Log.d("fan123", "onResponse: " + responseInfo);
                SharedPreferences.Editor editor= getSharedPreferences("userInfo", MODE_PRIVATE).edit();
                editor.putString("responseInfo", responseInfo);
                editor.commit();
                finish();
                mProgressDialog.dismiss();
            }
        });
    }

}

