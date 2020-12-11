package com.example.electiver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.reflect.Array;

public class RegisterActivity extends AppCompatActivity  {
    private EditText et_user_name, et_psw, et_psw_again;
    private String userName, psw, pswAgain;
    private Spinner department;
    private Spinner major;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        init();
    }

    private void init(){
        Button btn_register = (Button) findViewById(R.id.btn_register);
        et_user_name = (EditText) findViewById(R.id.et_user_name);
        et_psw = (EditText) findViewById(R.id.et_psw);
        et_psw_again = (EditText) findViewById(R.id.et_psw_again);
        department = (Spinner) findViewById(R.id.spin_department);
        major = (Spinner) findViewById(R.id.spin_major);

        String[] dp_all = {"数学科学学院","化学与分子工程学院","心理与认知学院",
                            "城市与环境学院","物理学院","生命科学学院","国际关系学院",
                            "政府管理学院","法学院","社会学系","新闻与传播学院",
                            "信息科学技术学院","中国语言文学系","外国语学院",
                            "历史学系","考古文博学院","艺术学院","哲学系"};
        String[][] major_all = {{"数学"},{"化学"},{"心理"},
                             {"城环"},{"物理"},{"生物"},{"国关"},
                        {"政管"},{"法院"},{"社系"},{"新传"},
                {"软件工程","计算机科学与技术（科学方向）","计算机科学与技术（技术方向）","电子系","微电子系"},
                {"中文"},{"外院"},
                {"历史"},{"考古"},{"艺术"},{"哲学"}};

        Context context = this;
        ArrayAdapter<String> adapter_dp = new ArrayAdapter<String>(context,
                android.R.layout.simple_list_item_1, dp_all);
        department.setAdapter(adapter_dp);

        department.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int pos = department.getSelectedItemPosition();
                ArrayAdapter<String> adapter_major = new ArrayAdapter<String>(context,
                        android.R.layout.simple_list_item_1, major_all[pos]);
                major.setAdapter(adapter_major);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        btn_register.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                getEditString();

                if(TextUtils.isEmpty(userName)){
                    Toast.makeText(RegisterActivity.this, "请输入用户名",
                            Toast.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(psw)){
                    Toast.makeText(RegisterActivity.this, "请输入密码",
                            Toast.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(pswAgain)){
                    Toast.makeText(RegisterActivity.this, "请再次输入密码",
                            Toast.LENGTH_SHORT).show();
                }else if(!psw.equals(pswAgain)){
                    Toast.makeText(RegisterActivity.this, "两次输入的密码不一致",
                            Toast.LENGTH_SHORT).show();
                }else if(isExistUserName(userName)){
                    Toast.makeText(RegisterActivity.this, "此用户名已存在",
                            Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(RegisterActivity.this, "注册成功",
                            Toast.LENGTH_SHORT).show();
                    saveRegisterInfo(userName, psw);
                    Intent data = new Intent();
                    data.putExtra("userName", userName);
                    setResult(RESULT_OK,data);
                    RegisterActivity.this.finish();
                }
            }
        });
    }

    private void getEditString(){
        userName = et_user_name.getText().toString().trim();
        psw = et_psw.getText().toString().trim();
        pswAgain = et_psw_again.getText().toString().trim();
    }

    private boolean isExistUserName(String userName){
        boolean has_userName = false;

        SharedPreferences sp = getSharedPreferences("loginInfo", MODE_PRIVATE);
        String spPsw = sp.getString(userName, "");
        if(!TextUtils.isEmpty(spPsw)){
            has_userName = true;
        }

        return has_userName;
    }

    private void saveRegisterInfo(String userName, String psw){
        String md5Psw = MD5Utils.md5(psw);
        SharedPreferences sp = getSharedPreferences("loginInfo", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(userName, md5Psw);
        editor.apply();
    }
}
