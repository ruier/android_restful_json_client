package com.kemov.xdong.jsonclientapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.EOFException;
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
    String net_user = "";
    String net_pwd = "";
    String local_user = "";
    String local_pwd = "";
    String modid;
    String name;
    String status;
    String remark;
    private String m_user;
    private String m_pwd;
    private AlertDialog mDialog;
    Handler netHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {

            Log.i("kemov", "msg" + msg.what);

            if(msg.what == 0x3) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            String response = MainActivity.this.get("http://10.56.56.236:65500/appname/module/rest/task");
                            JSONObject jsonObject = new JSONObject(response);
                            Message message = Message.obtain();
                            message.what = 0X123;
                            message.obj = jsonObject.get("modid").toString() + " " + jsonObject.getString("name".toString())
                                            + " " + jsonObject.getString("status") + " " + jsonObject.getString("remark");
                            uiHandler.sendMessage(message);

                        } catch (Exception e) {
                            Log.i("kemov", e.toString());
                        }
                    }
                }.start();
            }

            if (msg.what == 0x2) {
                final String[] msg_net = ((String) msg.obj).split(" ");
                net_user = msg_net[0];
                net_pwd = msg_net[1];

                if (net_user.equals(local_user) && net_pwd.equals(local_pwd))
                    mDialog.dismiss();
                else
                    Toast.makeText(MainActivity.this, "用户名或密码错误", 0).show();
            }

            if (msg.what == 0x1) {
                final String[] msg_local = ((String) msg.obj).split(" ");
                local_user = msg_local[0];
                local_pwd = msg_local[1];

                new Thread() {
                    @Override
                    public void run() {
                        try {
                            String response = MainActivity.this.get(" http://10.56.56.236:65500/appname/module/rest/user");
                            JSONObject jsonObject = new JSONObject(response);
                            Message message = Message.obtain();
                            message.what = 0X2;
                            message.obj = jsonObject.get("user").toString() + " " + jsonObject.getString("pwd".toString());
                            netHandler.sendMessage(message);

                        } catch (Exception e) {
                            Log.i("kemov", e.toString());
                        }
                    }
                }.start();
            }
            Log.i("kemov", "net:" + net_user + net_pwd + "local:" + local_user + local_pwd);
        }

    };

    Handler uiHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            Log.i("kemov", "msg" + msg.toString());
            if(msg.what==0x123)
            {
                final String[] msg_str = ((String) msg.obj).split(" ");

                et_modid.setText(msg_str[0]);
                et_name.setText(msg_str[1]);
                et_status.setText(msg_str[2]);
                et_remark.setText(msg_str[3]);

            }
        };
    };

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    OkHttpClient client = new OkHttpClient();

    String get(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

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

        mDialog = new AlertDialog.Builder(this)
                .setTitle("用户管理")
                .setPositiveButton("登陆", null)
                .setNegativeButton("注册", null)
                .setCancelable(false)
                .create();

        LayoutInflater inflater = getLayoutInflater();
        final View layout = inflater.inflate(R.layout.login_main, null);
        final EditText et_relogin_name = (EditText) layout.findViewById(R.id.et_relogin_name);
        final EditText et_relogin_pwd = (EditText) layout.findViewById(R.id.et_relogin_pwd);
        mDialog.setView(layout);
        mDialog.setOnShowListener(new DialogInterface.OnShowListener(){
            @Override
            public void onShow(DialogInterface dialog) {
                Button positionButton=mDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Button negativeButton=mDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                positionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String name = et_relogin_name.getText().toString();
                        String pwd = et_relogin_pwd.getText().toString();
                        Message message;
                        if(name.isEmpty() || pwd.isEmpty())
                            Toast.makeText(MainActivity.this, "请输入用户名和密码",0).show();
                        else {

                            message = Message.obtain();
                            message.what = 0X1;
                            message.obj = name.toString() + " " + pwd.toString();
                            netHandler.sendMessage(message);
                        }

                    }
                });
                negativeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(MainActivity.this, "目前不支持",0).show();
                        mDialog.dismiss();
                    }
                });
            }
        });

        mDialog.show();
    };

    public void submit(View v) throws JSONException  {
        Log.i("kemov", "click");
        netHandler.sendEmptyMessage(0x3);
    }
}
