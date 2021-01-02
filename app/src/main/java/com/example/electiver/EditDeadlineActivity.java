package com.example.electiver;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class EditDeadlineActivity extends AppCompatActivity {
    //------本页面需要修改add_ddl_to_back, deleteDDL, changeDDL
    //------使用ctrl+f寻找以便修改
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_deadline);

        SharedPreferences getToken = getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
        Token = getToken.getString("Token","null");

        init();
    }
    //--------参数
    String Token;
    Intent intent;
    String state;

    //根据edit_add的不同初始化界面
    private void init(){
        state = "0";
        intent = getIntent();
        Button btn_state = (Button)findViewById(R.id.state);
        btn_state.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (state.equals("1")) {
                    btn_state.setBackgroundColor(0xFF939393);
                    btn_state.setText("未完成");
                    state = "0";
                }
                else if (state.equals("0")) {
                    btn_state.setBackgroundColor(0xFF8B0012);
                    btn_state.setText("已完成");
                    state = "1";
                }
            }
        });

        String edit_add = intent.getStringExtra("edit_add");
        if (edit_add.equals("add")){
            Button btn = (Button)findViewById(R.id.deleteDDL);
            btn.setVisibility(View.GONE);//添加则隐藏删除按钮
            btn = (Button)findViewById(R.id.submit);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EditText textView;
                    String ddl_content = "";
                    textView = (EditText)findViewById((R.id.contentInput));
                    ddl_content = textView.getText().toString();

                    add_ddl_to_back(get_time(), ddl_content, state);
                    try {
                        Thread.sleep( 1000 );
                    } catch (Exception e){
                        System.exit( 0 ); //退出程序
                    }

                    Intent intent_back = new Intent(EditDeadlineActivity.this, DeadlineActivity.class);
                    intent_back.putExtra("course_id", intent.getStringExtra("cid"));
                    intent_back.putExtra("course_name", intent.getStringExtra("course_name"));
                    startActivity(intent_back);
                    EditDeadlineActivity.this.finish();
                }
            });
        }
        else if (edit_add.equals("edit")){
            TextView textView;
            textView = (TextView)findViewById((R.id.Year));
            textView.setText(intent.getStringExtra("year"));
            textView = (TextView)findViewById((R.id.Month));
            textView.setText(intent.getStringExtra("month"));
            textView = (TextView)findViewById((R.id.Day));
            textView.setText(intent.getStringExtra("day"));
            textView = (TextView)findViewById((R.id.Hour));
            textView.setText(intent.getStringExtra("hour"));
            textView = (TextView)findViewById((R.id.Minute));
            textView.setText(intent.getStringExtra("minute"));
            textView = (TextView)findViewById((R.id.contentInput));
            textView.setText(intent.getStringExtra("content"));

            state = intent.getStringExtra("state");
            Log.d("getstate", state);
            if (state.equals("true")) {
                btn_state.setBackgroundColor(0xFF8B0012);
                btn_state.setText("已完成");
                state = "1";
            }
            else if (state.equals("false")) {
                btn_state.setBackgroundColor(0xFF939393);
                btn_state.setText("未完成");
                state = "0";
            }

            Button btn = (Button)findViewById(R.id.deleteDDL);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteDDL(Token, intent.getStringExtra("DDLid"));
                    try {
                        Thread.sleep( 1000 );
                    } catch (Exception e){
                        System.exit( 0 ); //退出程序
                    }
                    Intent intent_back = new Intent(EditDeadlineActivity.this, DeadlineActivity.class);
                    intent_back.putExtra("course_id", intent.getStringExtra("cid"));
                    intent_back.putExtra("course_name", intent.getStringExtra("course_name"));
                    startActivity(intent_back);
                    EditDeadlineActivity.this.finish();
                }
            });
            btn = (Button)findViewById(R.id.submit);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EditText textView;
                    String ddl_content = "";
                    textView = (EditText)findViewById((R.id.contentInput));
                    ddl_content = textView.getText().toString();

                    changeDDL(Token, intent.getStringExtra("DDLid"), ddl_content, get_time(), state);
                    try {
                        Thread.sleep( 1000 );
                    } catch (Exception e){
                        System.exit( 0 ); //退出程序
                    }
                    Intent intent_back = new Intent(EditDeadlineActivity.this, DeadlineActivity.class);
                    intent_back.putExtra("course_id", intent.getStringExtra("cid"));
                    intent_back.putExtra("course_name", intent.getStringExtra("course_name"));
                    startActivity(intent_back);
                    EditDeadlineActivity.this.finish();
                }
            });
        }
    }

    private String get_time(){
        EditText textView;
        String ddl_time = "";
        textView = (EditText)findViewById((R.id.Year));
        ddl_time = ddl_time + textView.getText().toString() + "-";
        textView = (EditText)findViewById((R.id.Month));
        ddl_time = ddl_time + textView.getText().toString() + "-";
        textView = (EditText)findViewById((R.id.Day));
        ddl_time = ddl_time + textView.getText().toString() + "-";
        textView = (EditText)findViewById((R.id.Hour));
        ddl_time = ddl_time + textView.getText().toString() + "-";
        textView = (EditText)findViewById((R.id.Minute));
        ddl_time = ddl_time + textView.getText().toString();
        return ddl_time;
    }

    private void add_ddl_to_back(String ddl_time, String ddl_content, String ddl_state) {
        HttpThread thread = new HttpThread(){
            @Override
            public void run(){
                int i = doInsertDDL(Token, intent.getStringExtra("cid"), ddl_content, ddl_time, ddl_state);
                Log.d("Insert", String.valueOf(i));
            }
        };
        thread.start();
    }
    private void deleteDDL(String token, String DDLid) {
        HttpThread thread = new HttpThread(){
            @Override
            public void run(){
                int i = doDeleteDDL(token, DDLid);
                Log.d("Delete", String.valueOf(i));
            }
        };
        thread.start();
    }
    private void changeDDL(String token, String ddlid, String ddlcontent, String ddltime, String ddlstate) {
        //doUpdateDDLInfo(String token, String ddlid,String ddlcontent, String ddltime)
        //doUpdateDDLState(String token, String ddlid, String ddlstate)
        HttpThread thread = new HttpThread(){
            @Override
            public void run(){
                int a1 = doUpdateDDLInfo(token, ddlid, ddlcontent, ddltime);
                int a2 = doUpdateDDLState(token, ddlid, ddlstate);
                Log.d("Info", String.valueOf(a1));
                Log.d("State", String.valueOf(a2));
            }
        };
        thread.start();
    }
}