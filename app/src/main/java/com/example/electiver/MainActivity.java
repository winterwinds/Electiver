package com.example.electiver;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.electiver.ui.account.AccountFragment;
import com.example.electiver.ui.schedule.ScheduleFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {

    private Boolean isFirstUse;
    private TextView mTextMessage;
    private FragmentTransaction transaction;
    private FragmentManager fragmentManager;

    public String UserName, Grade, Department, Major, Token;

    MyHandler myHandler = new MyHandler(this);

    private void setDefaultFragment(){
        fragmentManager = getSupportFragmentManager();
        transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.content, new ScheduleFragment());
        transaction.commit();
    }
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            fragmentManager = getSupportFragmentManager();
            transaction = fragmentManager.beginTransaction();
            switch(item.getItemId()){
                case R.id.navigation_schedule:
                    mTextMessage.setText(R.string.title_schedule);
                    return true;
                case R.id.navigation_elective_assistant:
                    mTextMessage.setText(R.string.title_elective_assistant);
                    return true;
                case R.id.navigation_account:
                    mTextMessage.setText(R.string.title_account);
                    return true;
            }
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTextMessage = (TextView) findViewById(R.id.message);
        setContentView(R.layout.activity_main);

        Log.d("checkMain", "enter onCreate");

        if(savedInstanceState!=null){
            SharedPreferences firstuse = getSharedPreferences("loginInfo", MODE_PRIVATE);
            UserName = firstuse.getString("UserName","none");
            Grade = firstuse.getString("Grade","none");
            Department = firstuse.getString("Department", "none");
            Major = firstuse.getString("Major", "none");
            Token = firstuse.getString("Token","none");
            return;
        }


        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_schedule, R.id.navigation_elective_assistant, R.id.navigation_account)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        init();
       // initTimeOccupy();
    }
    public void init(){

        SharedPreferences checkToken = getSharedPreferences("loginInfo", MODE_PRIVATE);
        String getToken = checkToken.getString("Token","null");
        Log.d("checkToken",getToken);

        final Message[] msg = new Message[1];
        if(!getToken.equals("null")){
            HttpThread thread = new HttpThread(){
                @Override
                public void run(){
                    msg[0] = Message.obtain();
                    String ifTokenOK = doQueryComment(getToken);
                    if(ifTokenOK.equals("Signature expired. Please log in again.")){
                        msg[0].what=0x01;
                    }else if(ifTokenOK.equals("Invalid token. Please log in again.")){
                        msg[0].what=0x01;
                    }else{
                        msg[0].what=0x02;
                    }
                    myHandler.sendMessage(msg[0]);
                }
            };
            thread.start();
            try{
                thread.join(4000);
                if(msg[0]==null || msg[0].what==0x01){
                    reLogin();
                }
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }


       /* SharedPreferences firstuse = getSharedPreferences("loginInfo", MODE_PRIVATE);
        isFirstUse = firstuse.getBoolean("isLogin", false);
        if(!isFirstUse) {
            SharedPreferences.Editor editor = firstuse.edit();
            editor.putBoolean("isLogin",false);
            editor.commit();
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, LoginActivity.class);
            startActivityForResult(intent, 1);
            UserName = firstuse.getString("UserName","none");
            Grade = firstuse.getString("Grade","none");
            Department = firstuse.getString("Department", "none");
            Major = firstuse.getString("Major", "none");
            Token = firstuse.getString("Token","none");
        }else{
            UserName = firstuse.getString("UserName","none");
            Grade = firstuse.getString("Grade","none");
            Department = firstuse.getString("Department", "none");
            Major = firstuse.getString("Major", "none");
            Token = firstuse.getString("Token","none");
        }*/
    }

    public void initTimeOccupy(){
        SharedPreferences occupyTime=getSharedPreferences("timeAvail",MODE_PRIVATE);
        SharedPreferences.Editor editor = occupyTime.edit();
        String possibledays[] ={"mon","tue","wed","thu","fri","sat","sun"};
        String possibletime[] ={"1-2","3-4","5-6","7-8","10-11"};
        for(int i=0;i<possibledays.length;i++){
            for(int j=0;j<possibletime.length;j++){
                String timetag = possibledays[i]+possibletime[j];
                editor.putString(timetag,"false");
            }
        }
        editor.commit();
    }
    /*
        重新登陆，清空本地存储的所有信息，并弹出登录界面。
     */
    public void reLogin(){
        SharedPreferences getUserInfo = getSharedPreferences("loginInfo",MODE_PRIVATE);
        SharedPreferences.Editor editor=getUserInfo.edit();
        editor.clear();
        editor.commit();
        SharedPreferences getCourseInfo = getSharedPreferences("courseInfo",MODE_PRIVATE);
        editor=getCourseInfo.edit();
        editor.clear();
        editor.commit();

        initTimeOccupy();

        Intent intent = new Intent();
        intent.setClass(MainActivity.this, LoginActivity.class);
        startActivityForResult(intent, 1);

      /*  SharedPreferences saveinfo = getSharedPreferences("loginInfo", MODE_PRIVATE);
        try{
            JSONObject json = new JSONObject(saveinfo.getString("roughInfo","{}"));

            String getToken=json.getString("token");
            String getGrade = json.getString("grade");
            String getDepart = json.getString("department");
            String getMajor = json.getString("major");
            editor = saveinfo.edit();
            editor.putString("Token", getToken);
            editor.putString("Grade", getGrade);
            editor.putString("Department",getDepart);
            editor.putString("Major", getMajor);
            editor.commit();

        }catch(JSONException e){
            e.printStackTrace();
        }*/
    }

    public String getUserName(){ return UserName; }
    public String getGrade(){ return Grade; }
    public String getDepartment(){ return Department; }
    public String getMajor(){ return Major; }
    public void SetToken(String t){ Token = t; }
    public String getToken(){
        SharedPreferences info = getSharedPreferences("loginInfo", MODE_PRIVATE);
        String str = info.getString("Token","");
        return str;
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data!=null){
            SharedPreferences saveinfo = getSharedPreferences("loginInfo", MODE_PRIVATE);
            String roughInfo = saveinfo.getString("roughInfo","null");
            if(!roughInfo.equals("null")){
                try{
                    JSONObject json = new JSONObject(roughInfo);
                    Token = json.getString("token");
                }catch(JSONException e){
                    e.printStackTrace();
                }
            }
            SharedPreferences.Editor editor = saveinfo.edit();
            editor.putString("Token",Token);
            editor.commit();

        }else{
            Log.d("bundle","nodatatomain");
        }
    }

    static class MyHandler extends Handler{
        WeakReference<MainActivity> mActivity;
        MyHandler(MainActivity activity){
            mActivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg){
            MainActivity theActivity = mActivity.get();
            switch(msg.what){
                case 0x01:

                    break;
                case 0x02:
                    break;
                default:
                    break;
            }
        }
    };
}
