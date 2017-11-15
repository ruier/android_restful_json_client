package com.kemov.xdong.jsonclientapplication;

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

public class MainActivity extends AppCompatActivity {
    private EditText et_modid;
    private EditText et_name;
    private EditText et_status;
    private EditText et_remark;
    private EditText et_addr;
    private String host="127.0.0.1";//同一个局域网内作为服务端的手机的IP，使用端口8155
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

    };

    public void submit(View v) throws JSONException {
        if( TextUtils.isEmpty(et_modid.getText().toString().trim())||
        TextUtils.isEmpty( et_name.getText().toString().trim())||
        TextUtils.isEmpty(et_status.getText().toString().trim())||
                TextUtils.isEmpty(et_remark.getText().toString().trim())) {
            Toast.makeText(MainActivity.this, "信息不能为空!!!", 0).show();
            return;
        }
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("modid", et_modid.getText().toString().trim());
        jsonObject.put("name", et_name.getText().toString().trim());
        jsonObject.put("status", et_status.getText().toString().trim());
        jsonObject.put("remark", et_remark.getText().toString().trim());
        final String  result=jsonObject.toString();
        Log.i("jSON字符串", result);
        new Thread(new  Runnable() {
            @Override
            public void run() {

                try {
                    host = et_addr.getText().toString();
                    Log.i("kemov", "kemovlog host="+host);
                    Socket socket=new Socket(InetAddress.getByName(host), 8155);
                    OutputStream os=socket.getOutputStream();
                    os.write(result.getBytes());
                    os.flush();
                    //防止服务端read方法读阻塞
                    socket.shutdownOutput();
                } catch (UnknownHostException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }).start();
    }
}
