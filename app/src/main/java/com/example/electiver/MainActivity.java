package com.example.electiver;

import android.content.Intent;
import android.content.SharedPreferences;
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
    }
    public void init(){
        Log.d("checkMain","enter init");
        SharedPreferences firstuse = getSharedPreferences("loginInfo", MODE_PRIVATE);
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
        }
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
        //super.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
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
                case 0x0001:
                    break;
                default:
                    break;
            }
        }
    };
}
