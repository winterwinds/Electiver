package com.example.electiver;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ForgetPswActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forget_psw);
        EditText et_username = (EditText)findViewById(R.id.change_user_name);
        EditText et_email = (EditText)findViewById(R.id.change_email);
        EditText et_newpsw = (EditText)findViewById(R.id.change_newpsw);
        EditText et_newpsw_again = (EditText)findViewById(R.id.change_psw_again);
        Button btn_change = (Button)findViewById(R.id.change_makesure);



        btn_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String username = et_username.getText().toString().trim();
                String email = et_email.getText().toString().trim();
                String newpsw = et_newpsw.getText().toString().trim();
                String newpsw_again = et_newpsw_again.getText().toString().trim();

                if(TextUtils.isEmpty(username)){
                    Toast.makeText(ForgetPswActivity.this, "请输入用户名",
                            Toast.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(email)){
                    Toast.makeText(ForgetPswActivity.this, "请输入邮箱",
                            Toast.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(newpsw)){
                    Toast.makeText(ForgetPswActivity.this, "请输入新密码",
                            Toast.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(newpsw_again)){
                    Toast.makeText(ForgetPswActivity.this, "请再次输入新密码",
                            Toast.LENGTH_SHORT).show();
                }else if(!newpsw.equals(newpsw_again)){
                    Toast.makeText(ForgetPswActivity.this, "两次输入的密码不一致",
                            Toast.LENGTH_SHORT).show();
                }else{
                    String md5newPsw = MD5Utils.md5(newpsw);
                    new HttpThread(){
                        @Override
                        public void run(){
                            doAlterPassword(username, email, md5newPsw);
                        }
                    }.start();
                    finish();
                }
            }
        });

    }
}
