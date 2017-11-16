package com.kemov.xdong.jsonclientapplication;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private EditText et_modid;
    private EditText et_name;
    private EditText et_status;
    private EditText et_remark;
    private EditText et_addr;
    private String host="127.0.0.1";//同一个局域网内作为服务端的手机的IP，使用端口8155

    String modid;
    String name;
    String status;
    String remark;

    Handler uiHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            if(msg.what==0x123)
            {
                et_modid.setText(modid);
                et_name.setText(name);
                et_status.setText(status);
                et_remark.setText(remark);
            }
        };
    };

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    OkHttpClient client = new OkHttpClient();

    String post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    String run(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        et_modid=(EditText) findViewById(R.id.EModID);
        et_name=(EditText) findViewById(R.id.EName);
        et_status=(EditText) findViewById(R.id.EStatus);
        et_remark=(EditText) findViewById(R.id.EReMark);
        et_addr = (EditText) findViewById(R.id.et_addr);

        Button b_send = (Button) findViewById(R.id.button_send);
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Bundle data = msg.getData();
                String val = data.getString("value");
                Log.i("mylog", "请求结果为-->" + val);
                // TODO
                // UI界面的更新等相关操作
            }
        };
    };

    public void submit(View v) throws JSONException  {

        new Thread()  {
            @Override
            public void run()
            {
                try {
                    String response = MainActivity.this.run("http://10.56.56.236:65500/todo/api/v1.0/tasks/3");
                    Log.i("kemov", "respond" + response);
                    JSONObject jsonObject=new JSONObject(response);

                    modid=jsonObject.getString("modid");
                    name=jsonObject.getString("name");
                    status=jsonObject.getString("status");
                    remark=jsonObject.getString("remark");
                    Log.i("kemov", "we get " + modid + name + status + remark);
                    uiHandler.sendEmptyMessage(0x123);

                }catch (Exception e) {
                }

            }
        }.start();


    }
}
