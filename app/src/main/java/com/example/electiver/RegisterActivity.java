package com.example.electiver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;

public class RegisterActivity extends AppCompatActivity  {
    private EditText et_user_name, et_psw, et_psw_again, et_email;
    private String userName, psw, pswAgain, Grade, Major, Department, Email;
    private Spinner department;
    private Spinner major;
    private Spinner grade;
    private int RegisterResult;
    MyHandler myHandler = new MyHandler(this);
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        init();
    }

    public void SetRegisterResult(int r){
        Log.d("bundle","someone called setregister");
        RegisterResult = r;
    }

    private void init(){
        Button btn_register = (Button) findViewById(R.id.btn_register);
        et_user_name = (EditText) findViewById(R.id.et_user_name);
        et_psw = (EditText) findViewById(R.id.et_psw);
        et_psw_again = (EditText) findViewById(R.id.et_psw_again);
        et_email = (EditText)findViewById(R.id.et_email);
        department = (Spinner) findViewById(R.id.spin_department);
        major = (Spinner) findViewById(R.id.spin_major);
        grade = (Spinner) findViewById(R.id.spin_grade);
        RegisterResult = -1;

        String[] grade_all={"大一","大二","大三","大四"};
        String[] dp_all = {"数学科学学院","化学与分子工程学院","心理与认知学院",
                            "城市与环境学院","物理学院","生命科学学院","国际关系学院",
                            "政府管理学院","法学院","社会学系","新闻与传播学院",
                            "信息科学技术学院","中国语言文学系","外国语学院",
                            "历史学系","考古文博学院","艺术学院","哲学系","地球与空间科学学院","工学院"};
        String[][] major_all = {
                {"数学与应用","统计学","信息与计算科学","数据科学与大数据"},
                {"化学","材料化学","化学生物学","应用化学"},
                {"心理学","应用心理学"},
                {"自然地理与资源环境","人文地理与城乡规划","城乡规划","生态学","环境科学"},
                {"物理学","天文学","大气科学","核物理"},
                {"生物科学","生物技术","生物信息学","生态学"},
                {"国际政治","外交学","国际政治专业"},
                {"政治学与行政学专业","行政管理专业","城市管理专业"},
                {"法学专业"},
                {"社会学","社会工作专业","人类学专业"},
                {"新闻学","广播电视学","广告学"},
                {"计算机科学与技术（科学）","计算机科学与技术（技术）","软件工程","电子信息科学与技术","微电子科学与技术"},
                {"汉语言文学","汉语言","古典文献","应用语言学","汉语言文学（留学生）"},
                {"阿拉伯语","波斯语","朝鲜语","德语","俄语","法语","菲律宾语","蒙古语","葡萄牙语","日语","西班牙语","英语"},
                {"历史学（中国史）","世界史","外国语言与外国历史","历史学 古典语文学","世界史 古典语文学"},
                {"考古学","文物与博物馆学","文物保护技术","考古学","外国语言与外国历史"},
                {"艺术史论","广播电视编导","艺术史论（文化产业）"},
                {"哲学","哲学（逻辑学与科学技术）","宗教学"},
                {"地质学","地球化学","地球物理学","空间科学与技术","地理信息科学","物理学","化学"},
                {"理论与应用力学","工程力学","能源与环境系统工程","航空航天","生物医学","材料科学与工程","机器人工程"}
        };

        Context context = this;
        ArrayAdapter<String> adapter_grade = new ArrayAdapter<String>(context,
                android.R.layout.simple_list_item_1, grade_all);
        grade.setAdapter(adapter_grade);
        grade.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Grade =(String)grade.getItemAtPosition(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        ArrayAdapter<String> adapter_dp = new ArrayAdapter<String>(context,
                android.R.layout.simple_list_item_1, dp_all);
        department.setAdapter(adapter_dp);

        department.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int pos = department.getSelectedItemPosition();
                Department = (String)department.getItemAtPosition(position);
                ArrayAdapter<String> adapter_major = new ArrayAdapter<String>(context,
                        android.R.layout.simple_list_item_1, major_all[pos]);
                major.setAdapter(adapter_major);
                major.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        Major = (String) major.getItemAtPosition(position);
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


      //  Major = major_all[department.getSelectedItemPosition()][major.getSelectedItemPosition()];
        btn_register.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                getEditString();
                Intent data = new Intent();
                data.putExtra("userName", userName);
                data.putExtra("Grade",Grade);
                data.putExtra("Department",Department);
                data.putExtra("Major", Major);
                setResult(1, data);

                if(TextUtils.isEmpty(userName)){
                    Toast.makeText(RegisterActivity.this, "请输入用户名",
                            Toast.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(Email)){
                    Toast.makeText(RegisterActivity.this, "请输入邮箱",
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
                }else{
                    //对密码加密后再送到服务器
                    String md5Psw = MD5Utils.md5(psw);


                    String url="http://47.92.240.179:5001/user/register";
                    new HttpThread(url, userName, md5Psw, Grade, Department, Major, Email){
                        @Override
                        public void run(){
                            Message msg = Message.obtain();
                            try{
                                String res=doPost();
                                if(res.equals("register successs")){
                                    msg.what=0x01;
                                    msg.obj=res;

                                }else if(res.equals("this name have existed")){
                                    msg.what=0x02;
                                    msg.obj=res;
                                }else{
                                    msg.what=0x03;
                                    msg.obj=res;
                                }
                            }catch(IOException e){
                                e.printStackTrace();
                                msg.what=0x03;
                                msg.obj="error happen";
                            }
                            myHandler.sendMessage(msg);
                        }
                    }.start();

                }
            }
        });
    }

    private void getEditString(){
        userName = et_user_name.getText().toString().trim();
        psw = et_psw.getText().toString().trim();
        pswAgain = et_psw_again.getText().toString().trim();
        Email = et_email.getText().toString().trim();
    }

    static class MyHandler extends Handler{
        WeakReference<RegisterActivity> mActivity;
        MyHandler(RegisterActivity activity){
            mActivity = new WeakReference<RegisterActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg){
            RegisterActivity theActivity = mActivity.get();
            switch(msg.what){
                case 0x01:
                    Toast.makeText(theActivity, "注册成功",
                            Toast.LENGTH_SHORT).show();
                    theActivity.finish();
                    break;
                case 0x02:
                    Toast.makeText(theActivity, "该用户名已存在",
                            Toast.LENGTH_SHORT).show();
                    break;
                case 0x03:
                    Toast.makeText(theActivity,msg.obj.toString(),
                            Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };
}
