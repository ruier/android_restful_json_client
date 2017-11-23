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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

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

    String net_user = "";
    String net_pwd = "";
    String local_user = "";
    String local_pwd = "";

    private AlertDialog mDialog;
    private ListView taskList;
    private ArrayAdapter<String> mtaskListAdapter;

    Handler netHandler = new Handler() {
        public void handleMessage(final android.os.Message msg) {
            if(msg.what == 0x5) {
                final String text = (String) msg.obj;
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            String response = MainActivity.this.post("http://10.56.56.236:65500/appname/module/rest/task",text);

                        } catch (Exception e) {
                            Log.i("kemov", e.toString());
                            Looper.prepare();
                            Toast.makeText(MainActivity.this, "网络连接失败", 0).show();
                            Looper.loop();
                        }
                    }
                }.start();
            }
            if(msg.what == 0x4) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            String response = MainActivity.this.get("http://10.56.56.236:65500/appname/module/rest/task");
                            JSONObject jsonObject = new JSONObject(response);
                            Message message = Message.obtain();
                            message.what = 0X124;
                            JSONArray tasks=jsonObject.getJSONArray("tasks");
                            message.obj = "";
                            for (int i = 0; i < tasks.length(); i++) {
                                message.obj += tasks.getString(i) + ",";
                            }
                            uiHandler.sendMessage(message);

                        } catch (Exception e) {
                            Log.i("kemov", e.toString());
                            Looper.prepare();
                            Toast.makeText(MainActivity.this, "网络连接失败", 0).show();
                            Looper.loop();
                        }
                    }
                }.start();
            }
            if(msg.what == 0x3) {
                final int taskid = msg.arg1;
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Log.i("kemov", "msg arg1:" + msg.arg1);
                            String response = MainActivity.this.get("http://10.56.56.236:65500/appname/module/rest/task/" + taskid);

                            JSONObject jsonObject = new JSONObject(response);
                            Message message = Message.obtain();
                            message.what = 0X123;
                            message.obj = jsonObject.get("modid").toString() + " " + jsonObject.getString("name".toString())
                                            + " " + jsonObject.getString("status") + " " + jsonObject.getString("remark");
                            uiHandler.sendMessage(message);

                        } catch (Exception e) {
                            Log.i("kemov", e.toString());
                            Looper.prepare();
                            Toast.makeText(MainActivity.this, "网络连接失败", 0).show();
                            Looper.loop();
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
                            Looper.prepare();
                            Toast.makeText(MainActivity.this, "网络连接失败", 0).show();
                            Looper.loop();
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

            if (msg.what == 0x124)
            {
                final String[] msg_str = ((String) msg.obj).split(",");
                mtaskListAdapter.clear();
                for (String title : msg_str)
                mtaskListAdapter.add(title);
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

        Button b_send = (Button) findViewById(R.id.button_send);
        taskList = (ListView) findViewById(R.id.tasklist);
        mtaskListAdapter = new ArrayAdapter<String>(this, R.layout.task_list);
        taskList.setAdapter(mtaskListAdapter);
        taskList.setOnItemClickListener(mtaskClickListener);
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

    public void get_tasklist (View v) throws JSONException  {
        Log.i("kemov", "click");
        netHandler.sendEmptyMessage(0x4);
    }

    public void new_task (View v) throws JSONException  {
        Log.i("kemov", "click");
        Message msg = Message.obtain();

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("modid", et_modid.getText().toString());
        jsonObject.put("name", et_name.getText().toString());
        jsonObject.put("status", et_status.getText().toString());
        jsonObject.put("remark", et_remark.getText().toString());

        msg.what = 0x05;
        msg.obj = jsonObject.toString();
        netHandler.sendMessage(msg);
    }

    private AdapterView.OnItemClickListener mtaskClickListener
            = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            Message message = netHandler.obtainMessage();
            message.what = 0x3;
            message.arg1 = (arg2 + 1);
            netHandler.sendMessage(message);
        }
    };
}
