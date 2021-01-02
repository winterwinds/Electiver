package com.example.electiver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Vector;

public class MyCommentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_comment);

        get_comment_info();

        find_View();
        dynamic_create_View();
    }

    //--------全局变量
    //--参数
    String Token;
    Vector<String> comment_key;
    Vector<String> my_comment;
    private String[] class_comment;

    //--控件
    private ScrollView scrollView_scroll_list;
    private ConstraintLayout constraintLayout_list;
    private ConstraintSet constraintSet_list_set;
    private boolean []deleted;
    private TextView[] comments;
    private TextView[] textViews_comment;
    private Button[] buttons_delete;
    private Button[] buttons_edit;

    //--------该界面与后端的接口
    private void get_comment_info() {
        /*
        String Token;
        SharedPreferences getToken = getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
        Token = getToken.getString("Token","null");
        HttpThread thread = new HttpThread(){
            @Override
            public void run(){
                String res=doQueryMyComment(Token);
                TextView temp = (TextView)findViewById(R.id.titleComment);
                temp.setText(res);
            }
        };
        thread.start();*///测试http thread

        SharedPreferences getToken = this.getSharedPreferences("loginInfo", this.MODE_PRIVATE);
        Token = getToken.getString("Token","null");
        Log.d("tokenC", Token);
        my_comment = new Vector<String>();
        comment_key = new Vector<String>();
        HttpThread thread = new HttpThread(){
            @Override
            public void start(){
                String res_query_comment = doQueryMyComment(Token);
                Log.d("resComment", res_query_comment);
                try {
                    JSONObject json = new JSONObject(res_query_comment);
                    Iterator iterator = json.keys();
                    while(iterator.hasNext()){
                        String key = (String) iterator.next();
                        comment_key.add(String.valueOf((int)json.getJSONArray(key).get(0)));
                        my_comment.add((String)json.getJSONArray(key).get(1));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
        try {
            Thread.sleep( 1000 );
        } catch (Exception e){
            System.exit( 0 ); //閫�鍑虹▼搴�
        }

        class_comment = new String[my_comment.size()];

        for (int i = 0; i < my_comment.size(); i ++) {
            class_comment[i] = comment_key.get(i) + ":" + my_comment.get(i);
        }
    }

    //--------具体的逻辑实现
    private void find_View() {
        scrollView_scroll_list = (ScrollView)findViewById(R.id.scrollList);
        constraintLayout_list = (ConstraintLayout)scrollView_scroll_list.findViewById(R.id.commentList);

        deleted = new boolean[class_comment.length];
        constraintSet_list_set = new ConstraintSet();
        comments = new TextView[class_comment.length];
        textViews_comment = new TextView[class_comment.length];
        buttons_delete = new Button[class_comment.length];
        buttons_edit = new Button[class_comment.length];
    }

    @SuppressLint("ResourceAsColor")
    private void dynamic_create_View() {
        //设置按钮格式
        for (int i = 0; i < class_comment.length; i ++) {

            textViews_comment[i] = new TextView(this);
            // textViews_comment[i].setText(class_comment[i]);
            textViews_comment[i].setMinLines(5);
            textViews_comment[i].setBackgroundResource(R.drawable.comment_text_filled);
            textViews_comment[i].setId(IDUtils.generateViewId());
            constraintLayout_list.addView(textViews_comment[i]);
            constraintSet_list_set.constrainHeight(textViews_comment[i].getId(), ConstraintSet.MATCH_CONSTRAINT);
            constraintSet_list_set.constrainWidth(textViews_comment[i].getId(), ConstraintSet.MATCH_CONSTRAINT);
            constraintSet_list_set.connect(textViews_comment[i].getId(), ConstraintSet.LEFT,
                    ConstraintSet.PARENT_ID, ConstraintSet.LEFT,0);
            constraintSet_list_set.connect(textViews_comment[i].getId(), ConstraintSet.RIGHT,
                    ConstraintSet.PARENT_ID, ConstraintSet.RIGHT,0);

            if (i == 0) {
                constraintSet_list_set.connect(textViews_comment[i].getId(), ConstraintSet.TOP,
                        ConstraintSet.PARENT_ID, ConstraintSet.TOP,0); }
            else {
                constraintSet_list_set.connect(textViews_comment[i].getId(), ConstraintSet.TOP,
                        buttons_delete[i-1].getId(), ConstraintSet.BOTTOM,15); }

            comments[i] = new TextView(this);
            comments[i].setId(IDUtils.generateViewId());
            comments[i].setText(class_comment[i] + "\n\n");
            comments[i].setMinLines(4);
            comments[i].setTextSize(18);
            comments[i].setPadding(28, 15, 28, 15);
            constraintLayout_list.addView(comments[i]);
            constraintSet_list_set.constrainHeight(comments[i].getId(), ConstraintSet.WRAP_CONTENT);
            constraintSet_list_set.constrainWidth(comments[i].getId(), ConstraintSet.MATCH_CONSTRAINT);
            constraintSet_list_set.connect(comments[i].getId(), ConstraintSet.LEFT,
                    textViews_comment[i].getId(), ConstraintSet.LEFT,0);
            constraintSet_list_set.connect(comments[i].getId(), ConstraintSet.TOP,
                    textViews_comment[i].getId(), ConstraintSet.TOP,0);
            comments[i].bringToFront();


            buttons_delete[i] = new Button(this);
            buttons_delete[i].setText("删除评论");

            int finalI = i;
            buttons_delete[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    HttpThread thread = new HttpThread(){
                        @Override
                        public void start(){
                            System.out.println(comment_key.get(finalI));
                            int res = doDeleteComment(Token, comment_key.get(finalI));
                            System.out.println(res);
                            Log.d("deleteComment", String.valueOf(res));
                            deleted[finalI] = true;
                            constraintLayout_list.removeView(textViews_comment[finalI]);
                            // textViews_comment[finalI].setVisibility(View.INVISIBLE);
                            constraintLayout_list.removeView(comments[finalI]);
                            //comments[finalI].setVisibility(View.INVISIBLE);
                            constraintLayout_list.removeView(buttons_delete[finalI]);
                            //buttons_delete[finalI].setVisibility(View.INVISIBLE);
                            if (finalI == textViews_comment.length - 1) return;
                            int b = -1;
                            for (int i = finalI - 1; i >= 0; i--) {
                                if (!deleted[i]) {
                                    constraintSet_list_set.connect(textViews_comment[finalI + 1].getId(), ConstraintSet.TOP,
                                            textViews_comment[i].getId(), ConstraintSet.BOTTOM, 15);
                                    b = 0;
                                    break;
                                }
                            }
                            if (b == -1)
                                constraintSet_list_set.connect(textViews_comment[finalI + 1].getId(), ConstraintSet.TOP,
                                        ConstraintSet.PARENT_ID, ConstraintSet.TOP, 15);
                            constraintSet_list_set.applyTo(constraintLayout_list);
                            constraintLayout_list.invalidate();
                        }
                    };
                    thread.start();
                    //MyCommentActivity.this.finish();
                }
            });
            buttons_delete[i].setTextColor(Color.WHITE);
            buttons_delete[i].setId(IDUtils.generateViewId());
            buttons_delete[i].setBackgroundResource(R.color.transparent);
            buttons_delete[i].setTextColor(R.color.black);
            constraintLayout_list.addView(buttons_delete[i]);
            constraintSet_list_set.constrainHeight(buttons_delete[i].getId(), ConstraintSet.WRAP_CONTENT);
            constraintSet_list_set.constrainWidth(buttons_delete[i].getId(), ConstraintSet.WRAP_CONTENT);
            constraintSet_list_set.connect(buttons_delete[i].getId(), ConstraintSet.RIGHT,
                    ConstraintSet.PARENT_ID, ConstraintSet.RIGHT,0);
            constraintSet_list_set.connect(buttons_delete[i].getId(), ConstraintSet.BOTTOM,
                    textViews_comment[i].getId(), ConstraintSet.BOTTOM,-10);

            constraintSet_list_set.connect(textViews_comment[i].getId(), ConstraintSet.BOTTOM,
                    comments[i].getId(), ConstraintSet.BOTTOM,-40);
            //constraintSet_list_set.connect( buttons_delete[i].getId(), ConstraintSet.BOTTOM,
            //        textViews_comment[i].getId(), ConstraintSet.BOTTOM,0);
        }
        constraintSet_list_set.applyTo(constraintLayout_list);
    }
}