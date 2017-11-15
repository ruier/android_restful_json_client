package com.kemov.xdong.jsonserverapp;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class JsonServerApp extends AppCompatActivity {
    private EditText et_modid;
    private EditText et_name;
    private EditText et_status;
    private EditText et_remark;
    private String host="127.0.0.1";//同一个局域网内作为服务端的手机的IP，使用端口8155
    private Button b_recv;
    volatile Socket mSocket;
    ServerSocket server;
    private Handler mHandler=new Handler(){


        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            if(msg.what==0x01){
                Toast.makeText(JsonServerApp.this,(String) msg.obj, 500).show();
            }
            if(msg.what==0x02){
                new Thread(new  Runnable() {
                    @Override
                    public void run() {

                        try {
                            Log.i("客户端连接", "读取客户端发来的数据");
                            InputStream ins=mSocket.getInputStream();
                            ByteArrayOutputStream os=new ByteArrayOutputStream();
                            int len=0;
                            byte[] buffer=new byte[1024];
                            while((len=ins.read(buffer))!=-1){
                                os.write(buffer);
                            }
                            //第一步，生成Json字符串格式的JSON对象
                            JSONObject jsonObject=new JSONObject(os.toString());
                            //第二步，从JSON对象中取值如果JSON 对象较多，可以用json数组
                            String modid="modid："+jsonObject.getString("modid");
                            String name="name："+jsonObject.getString("name");
                            String status="status："+jsonObject.getString("status");
                            String remark="remark："+jsonObject.getString("remark");

                            Looper.prepare();
                            Message message=Message.obtain();
                            message.what=0X01;
                            message.obj= modid + name + status + remark;
                            mHandler.sendMessage(message);
                            Looper.loop();


                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }finally{

                            if(mSocket!=null){
                                try {
                                    mSocket.close();
                                    mSocket=null;
                                } catch (IOException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }

                            }
                        }

                    }
                }).start();
            }

        }


    };


        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_json_server_app);

        et_modid=(EditText) findViewById(R.id.EModID);
        et_name=(EditText) findViewById(R.id.EName);
        et_status=(EditText) findViewById(R.id.EStatus);
        et_remark=(EditText) findViewById(R.id.EReMark);

    }

    public void recv(View v) throws JSONException, IOException{

        new Thread(new  Runnable() {

            @Override
            public void run() {

                try {
                    Log.i("阻塞，等待客户端连接", "<<<<<<<<<");
                    if(server==null){
                        server=new ServerSocket(8155);
                    }
                    mSocket=server.accept();
                    Log.i("客户端连接成功", "<<<<<<<<<客户端连接成功");
                    Looper.prepare();
                    Message message=Message.obtain();
                    message.what=0X02;
                    mHandler.sendMessage(message);
                    Looper.loop();


                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }



            }
        }).start();
    }
}
