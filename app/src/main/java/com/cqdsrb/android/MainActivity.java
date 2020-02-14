package com.cqdsrb.android;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private Button btn;
    private TextView tv;
    //填写自己项目的appid值即可（本处已删除）
    private static final String APP_ID = "wxa25161b2bbe40540";
    private IWXAPI api;
    private String nickname;
    private String headimgurl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = findViewById(R.id.main_btn);
        tv = findViewById(R.id.main_tv);

        regToWx();
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!api.isWXAppInstalled()) {
                    Toast.makeText(MainActivity.this, "您的设备未安装微信客户端", Toast.LENGTH_SHORT).show();
                } else {
                    final SendAuth.Req req = new SendAuth.Req();
                    req.scope = "snsapi_userinfo";
                    req.state = "wechat_sdk_demo_test";
                    api.sendReq(req); // 第二步 登录

                }
            }
        });
    }

    private void regToWx() {
        api = WXAPIFactory.createWXAPI(this, APP_ID, false);// 第一步绑定威信api
        api.registerApp(APP_ID);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sp= getSharedPreferences("userInfo", MODE_PRIVATE);
        String responseInfo= sp.getString("responseInfo", "");

        if (!responseInfo.isEmpty()){
            try {
                JSONObject jsonObject = new JSONObject(responseInfo);
                nickname = jsonObject.getString("nickname");
                headimgurl = jsonObject.getString("headimgurl");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            tv.setText("昵称："+ nickname+ "\n"+ "头像："+ headimgurl);
            SharedPreferences.Editor editor= getSharedPreferences("userInfo", MODE_PRIVATE).edit();
            editor.clear();
            editor.commit();
        }
    }
}
