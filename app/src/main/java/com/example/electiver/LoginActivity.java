package com.example.electiver;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class LoginActivity extends AppCompatActivity {
    private String userName,psw,spPsw;//获取的用户名，密码，加密密码
    private EditText et_user_name,et_psw;//编辑框
    public String UserName;
    public String Grade, Department, Major, Token;

    LoginActivity.MyHandler myHandler = new LoginActivity.MyHandler(this);
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_in);
        //设置此界面为竖屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        init();
    }

    //获取界面控件
    private void init() {
        //从main_title_bar中获取的id
        //从activity_login.xml中获取的
        TextView tv_register = (TextView) findViewById(R.id.tv_register);
        TextView tv_forgetpsw = (TextView) findViewById(R.id.forget_psw);
        Button btn_login = (Button) findViewById(R.id.btn_login);
        et_user_name= (EditText) findViewById(R.id.et_user_name);
        et_psw= (EditText) findViewById(R.id.et_psw);
        //立即注册控件的点击事件
        tv_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //为了跳转到注册界面，并实现注册功能
                Intent intent=new Intent(LoginActivity.this,RegisterActivity.class);
                startActivityForResult(intent, 1);
            }
        });
        tv_forgetpsw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgetPswActivity.class);
                startActivity(intent);
            }
        });
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userName = et_user_name.getText().toString().trim();
                psw = et_psw.getText().toString().trim();
                String md5Psw = MD5Utils.md5(psw);

                Intent data = new Intent();
                data.putExtra("UserName", userName);
                data.putExtra("Grade",Grade);
                data.putExtra("Department", Department);
                data.putExtra("Major", Major);
                setResult(1,data);

                //保存用户名
                SharedPreferences saveinfo = getSharedPreferences("loginInfo", MODE_PRIVATE);
                SharedPreferences.Editor editor = saveinfo.edit();
                editor.putString("UserName",userName);
                editor.commit();

                if (TextUtils.isEmpty(userName)) {
                    Toast.makeText(LoginActivity.this, "请输入用户名", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(psw)) {
                    Toast.makeText(LoginActivity.this, "请输入密码", Toast.LENGTH_SHORT).show();
                } else{
                    final Message[] msg = new Message[1];
                    HttpThread thread = new HttpThread(){
                        @Override
                        public void run(){
                            msg[0] = Message.obtain();
                            try{
                                String res=doLogin(userName,md5Psw);
                                if(res.equals("password wrong")){
                                    msg[0].what=0x01;
                                    msg[0].obj=res;
                                }else if(res.equals("this username not exist")){
                                    msg[0].what=0x02;
                                    msg[0].obj=res;
                                }else{
                                    msg[0].what=0x03;
                                    msg[0].obj=res;

                                    editor.putString("roughInfo",res);
                                    editor.commit();
                                    Log.d("LoginOutput", res);
                                }
                            }catch(IOException e){
                                e.printStackTrace();
                                msg[0].what=0x04;
                                msg[0].obj="error happen";
                            }
                            myHandler.sendMessage(msg[0]);
                        }
                    };
                    thread.start();
                }
            }
        });
    }

    /**
     * 注册成功的数据返回至此
     * @param requestCode 请求码
     * @param resultCode 结果码
     * @param data 数据
     */
    @Override
    //显示数据， onActivityResult
    //startActivityForResult(intent, 1); 从注册界面中获取数据
    //int requestCode , int resultCode , Intent data
    // LoginActivity -> startActivityForResult -> onActivityResult();
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("bundle","loginActivityResult");
        if(data!=null){
            Log.d("bundle","logingetdata");
            //是获取注册界面回传过来的用户名
            // getExtra().getString("***");
            UserName = data.getStringExtra("userName");
            Department = data.getStringExtra("Department");
            Major = data.getStringExtra("Major");
            Grade = data.getStringExtra("Grade");

            if(!TextUtils.isEmpty(UserName)){
                //设置用户名到 et_user_name 控件
                et_user_name.setText(UserName);
                //et_user_name控件的setSelection()方法来设置光标位置
                et_user_name.setSelection(UserName.length());
            }
            //保存用户信息
            SharedPreferences saveinfo = getSharedPreferences("loginInfo", MODE_PRIVATE);
            SharedPreferences.Editor editor = saveinfo.edit();
            editor.putString("Department",Department);
            editor.putString("Major",Major);
            editor.putString("Grade",Grade);
            editor.commit();

        }else{
            Log.d("bundle","nodatatologin");
        }
    }

    static class MyHandler extends Handler {
        WeakReference<LoginActivity> mActivity;
        MyHandler(LoginActivity activity){
            mActivity = new WeakReference<LoginActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg){
            LoginActivity theActivity = mActivity.get();
            switch(msg.what){
                case 0x01:
                    Toast.makeText(theActivity, "密码错误",
                            Toast.LENGTH_SHORT).show();
                    break;
                case 0x02:
                    Toast.makeText(theActivity, "该用户名不存在",
                            Toast.LENGTH_SHORT).show();
                    break;
                case 0x03:
                    Toast.makeText(theActivity,"登陆成功",
                            Toast.LENGTH_SHORT).show();
                    theActivity.finish();
                    break;
                default:
                    break;
            }
        }
    };
}