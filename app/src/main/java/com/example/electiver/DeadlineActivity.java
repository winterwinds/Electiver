package com.example.electiver;

//@Author 王泽楷

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Bundle;
import android.content.Intent;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

public class DeadlineActivity extends AppCompatActivity {
    //------本页面需要修改get_ddlList()
    //------使用ctrl+f寻找以便修改
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_deadline);

        //初始化内部变量
        get_course_info();
        try {
            get_ddlList();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (ddl_id == null) return ;
        find_View();
        dynamic_create_View();
    }
    //--------全局变量
    //--参数
    private String course_id;//保存这个ddl界面的课程id
    private String course_name;//保存这个课程id的课程名
    private int[] ddl_id;//ddl编号
    private String[] ddl_time;//ddl时间列表
    private String[] ddl_content;//ddl内容列表
    private Boolean[] ddl_state;//ddl完成状态

    //--控件
    private TextView textView_courseName;
    private Button button_addDDL;
    private ScrollView scrollView_scroll;
    private ConstraintLayout constraintLayout_ddlList;
    private ConstraintSet constraintSet_ddlList;
    private TextView[] textViews_date;
    private TextView[] textViews_time;
    private Button[] buttons_content;

    //--------该界面与其他界面的接口
    private void get_course_info() {
        Intent intent = getIntent();
        course_id =  intent.getStringExtra("course_id");
        course_name = intent.getStringExtra("course_name");
    }//希望能从课程表界面同时获得课程名称

    //--------前端与后端的接口
    /* Test only
    private void get_ddlList() {
        ddl_id = new int[8];
        ddl_time = new String[8];
        ddl_content = new String[8];
        ddl_state = new Boolean[8];

        String temp = "2020-12-2";
        int cnt = 0;
        for (int i = 0; i < 8; i++) {
            cnt += i % 2;
            ddl_id[i] = i;
            ddl_time[i] = temp + String.valueOf(cnt) + "-00-00";
            ddl_content[i] = "这是一条ddl这是一条ddl这是一条ddl";
            ddl_state[i] = (i % 2 == 0);
        }
    }
     */
    private void get_ddlList() throws JSONException {
        SharedPreferences getToken = getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
        String Token = getToken.getString("Token","null");
        final String[] result = new String[1];
        HttpThread thread = new HttpThread(){
            @Override
            public void run(){
                result[0] = doQueryDDL(Token, course_id);
                Log.d("QueryDDL", result[0]);
            }
        };
        thread.start();

        try {
            Thread.sleep( 1000 );
        } catch (Exception e){
            System.exit( 0 ); //退出程序
        }
        JSONObject json = new JSONObject(result[0]);
        Iterator iterator = json.keys();
        Vector<JSONArray> DDL_JSON = new Vector<JSONArray>();
        while(iterator.hasNext()){
            String key = (String) iterator.next();
            DDL_JSON.add(json.getJSONArray(key));
        }

        int size = DDL_JSON.size();
        ddl_id = new int[size];
        ddl_time = new String[size];
        ddl_content = new String[size];
        ddl_state = new Boolean[size];
        for (int i = 0; i < size; i++) {
            ddl_id[i] = (int) DDL_JSON.get(i).get(0);
            ddl_time[i] = (String) DDL_JSON.get(i).get(3);
            ddl_content[i] = (String) DDL_JSON.get(i).get(2);
            if ((int)DDL_JSON.get(i).get(4) == 0) {
                ddl_state[i] = false;
            }else {ddl_state[i] = true;}
        }
    }

    //--------具体的逻辑实现
    //--找到页面控件并初始化
    private void find_View() {
        textView_courseName = (TextView)findViewById(R.id.courseName);
        textView_courseName.setText(course_name);

        button_addDDL = (Button)findViewById(R.id.addDDL);
        button_addDDL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DeadlineActivity.this, EditDeadlineActivity.class);
                intent.putExtra("edit_add", "add");
                intent.putExtra("cid", course_id);
                intent.putExtra("course_name", course_name);
                startActivity(intent);
                DeadlineActivity.this.finish();
            }
        });//-添加DDL界面

        scrollView_scroll = (ScrollView)findViewById(R.id.scroll);
        constraintLayout_ddlList = (ConstraintLayout)scrollView_scroll.findViewById(R.id.ddlList);

        constraintSet_ddlList = new ConstraintSet();
        textViews_date = new TextView[ddl_id.length];
        textViews_time = new TextView[ddl_id.length];
        buttons_content = new Button[ddl_id.length];
    }

    //--动态生成控件
    private void dynamic_create_View(){
        if (ddl_id.length == 0) return ;//非空才动态生成
        //创建顺序不可更改，分成三个函数仅为了便于阅读
        create_button_content();//创建一个内容按钮列表
        create_textView_time();//根据内容按钮定位时间控件的位置，同时修改内容按钮的宽度
        create_textView_date();//创建日期列表，按照日期整合控件
        constraintSet_ddlList.applyTo(constraintLayout_ddlList);
    }
    //-生成内容按钮列表
    private void create_button_content(){
        //定位第一个控件的位置
        buttons_content[0] = new Button(this);
        //属性
        if (ddl_content[0].length() > 15) {
            buttons_content[0].setText(Html.fromHtml(ddl_content[0].substring(0,15)+"..."));
        }//只显示前15个字
        else buttons_content[0].setText(ddl_content[0]);
        buttons_content[0].setTextColor(Color.WHITE);
        buttons_content[0].setId(IDUtils.generateViewId());
        //定位
        constraintLayout_ddlList.addView(buttons_content[0]);
        constraintSet_ddlList.constrainHeight(buttons_content[0].getId(), ConstraintSet.WRAP_CONTENT);
        constraintSet_ddlList.constrainWidth(buttons_content[0].getId(), ConstraintSet.MATCH_CONSTRAINT);
        constraintSet_ddlList.connect(buttons_content[0].getId(), ConstraintSet.TOP,
                ConstraintSet.PARENT_ID, ConstraintSet.TOP,0);
        constraintSet_ddlList.connect(buttons_content[0].getId(), ConstraintSet.LEFT,
                ConstraintSet.PARENT_ID, ConstraintSet.LEFT,0);
        constraintSet_ddlList.connect(buttons_content[0].getId(), ConstraintSet.RIGHT,
                ConstraintSet.PARENT_ID, ConstraintSet.RIGHT,0);

        for (int i = 1; i < ddl_content.length; i ++) {
            //以第一个为准编写剩余控件
            buttons_content[i] = new Button(this);
            //属性
            if (ddl_content[i].length() > 15) {
                buttons_content[i].setText(Html.fromHtml(ddl_content[i].substring(0,15)+"..."));
            }//只显示前10个字
            else buttons_content[i].setText(ddl_content[i]);
            buttons_content[i].setTextColor(Color.WHITE);
            buttons_content[i].setId(IDUtils.generateViewId());
            //定位
            constraintLayout_ddlList.addView(buttons_content[i]);
            constraintSet_ddlList.constrainHeight(buttons_content[i].getId(), ConstraintSet.WRAP_CONTENT);
            constraintSet_ddlList.constrainWidth(buttons_content[i].getId(), ConstraintSet.MATCH_CONSTRAINT);
            constraintSet_ddlList.connect(buttons_content[i].getId(), ConstraintSet.TOP,
                    buttons_content[i-1].getId(), ConstraintSet.BOTTOM,2);
            constraintSet_ddlList.connect(buttons_content[i].getId(), ConstraintSet.LEFT,
                    buttons_content[0].getId(), ConstraintSet.LEFT,0);
            constraintSet_ddlList.connect(buttons_content[i].getId(), ConstraintSet.RIGHT,
                    buttons_content[0].getId(), ConstraintSet.RIGHT,0);
        }
    }
    //生成时间列表
    private void create_textView_time(){
        for (int i = 0; i < ddl_time.length; i ++) {
            String[] split_time;
            textViews_time[i] = new TextView(this);
            //属性
            split_time = ddl_time[i].split("-");
            textViews_time[i].setText(Html.fromHtml(split_time[3]+":"+split_time[4]));
            textViews_time[i].setId(IDUtils.generateViewId());
            //设置按钮的监听
            int finalI = i;
            buttons_content[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(DeadlineActivity.this, EditDeadlineActivity.class);
                    intent.putExtra("edit_add", "edit");
                    intent.putExtra("year", split_time[0]);
                    intent.putExtra("month", split_time[1]);
                    intent.putExtra("day", split_time[2]);
                    intent.putExtra("hour", split_time[3]);
                    intent.putExtra("minute", split_time[4]);
                    intent.putExtra("content", ddl_content[finalI]);
                    intent.putExtra("DDLid", String.valueOf(ddl_id[finalI]));
                    intent.putExtra("state", String.valueOf(ddl_state[finalI]));
                    intent.putExtra("cid", course_id);
                    intent.putExtra("course_name", course_name);
                    intent.putExtra("rough_data", ddl_time[finalI]);
                    startActivity(intent);
                    DeadlineActivity.this.finish();
                }
            });//-添加DDL界面
            //定位
            constraintLayout_ddlList.addView(textViews_time[i]);
            constraintSet_ddlList.constrainHeight(textViews_time[i].getId(), ConstraintSet.WRAP_CONTENT);
            constraintSet_ddlList.constrainWidth(textViews_time[i].getId(), ConstraintSet.WRAP_CONTENT);
            constraintSet_ddlList.connect(textViews_time[i].getId(), ConstraintSet.TOP,
                    buttons_content[i].getId(), ConstraintSet.TOP,0);
            constraintSet_ddlList.connect(textViews_time[i].getId(), ConstraintSet.BOTTOM,
                    buttons_content[i].getId(), ConstraintSet.BOTTOM,0);
            constraintSet_ddlList.connect(textViews_time[i].getId(), ConstraintSet.LEFT,
                    ConstraintSet.PARENT_ID, ConstraintSet.LEFT,0);
            constraintSet_ddlList.connect(buttons_content[i].getId(), ConstraintSet.LEFT,
                    textViews_time[i].getId(), ConstraintSet.RIGHT,0);
        }
    }
    //生成日期列表
    private void create_textView_date(){
        String[] split_time;
        String temp_date = ddl_time[0].substring(0,10);
        //初始化第一天的日期
        textViews_date[0] = new TextView(this);
        //属性
        split_time = ddl_time[0].split("-");
        textViews_date[0].setText(Html.fromHtml(split_time[0]+"年"+split_time[1]+"月"+split_time[2]+"日"));
        textViews_date[0].setId(IDUtils.generateViewId());
        //定位
        constraintLayout_ddlList.addView(textViews_date[0]);
        constraintSet_ddlList.constrainHeight(textViews_date[0].getId(), ConstraintSet.WRAP_CONTENT);
        constraintSet_ddlList.constrainWidth(textViews_date[0].getId(), ConstraintSet.WRAP_CONTENT);
        constraintSet_ddlList.connect(textViews_date[0].getId(), ConstraintSet.TOP,
                ConstraintSet.PARENT_ID, ConstraintSet.TOP,5);
        constraintSet_ddlList.connect(textViews_date[0].getId(), ConstraintSet.LEFT,
                ConstraintSet.PARENT_ID, ConstraintSet.LEFT,0);
        constraintSet_ddlList.connect(buttons_content[0].getId(), ConstraintSet.TOP,
                textViews_date[0].getId(), ConstraintSet.BOTTOM,0);

        for (int i = 1; i < ddl_time.length; i ++) {
            if (! temp_date.equals(ddl_time[i].substring(0, 10))) {
                temp_date = ddl_time[i].substring(0,10);

                textViews_date[i] = new TextView(this);
                //属性
                split_time = ddl_time[i].split("-");
                textViews_date[i].setText(Html.fromHtml(split_time[0]+"年"+split_time[1]+"月"+split_time[2]+"日"));
                textViews_date[i].setId(IDUtils.generateViewId());
                //定位
                constraintLayout_ddlList.addView(textViews_date[i]);
                constraintSet_ddlList.constrainHeight(textViews_date[i].getId(), ConstraintSet.WRAP_CONTENT);
                constraintSet_ddlList.constrainWidth(textViews_date[i].getId(), ConstraintSet.WRAP_CONTENT);
                constraintSet_ddlList.connect(textViews_date[i].getId(), ConstraintSet.TOP,
                        buttons_content[i-1].getId(), ConstraintSet.BOTTOM,10);
                constraintSet_ddlList.connect(textViews_date[i].getId(), ConstraintSet.LEFT,
                        ConstraintSet.PARENT_ID, ConstraintSet.LEFT,0);
                constraintSet_ddlList.connect(buttons_content[i].getId(), ConstraintSet.TOP,
                        textViews_date[i].getId(), ConstraintSet.BOTTOM,0);
            }
        }
    }
}
