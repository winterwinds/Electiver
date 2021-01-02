package com.example.electiver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Vector;


public class CommentActivity extends AppCompatActivity {

    String token;
    String cid;

    private ScrollView scroll;
    private ConstraintLayout comment_list_view;
    private ConstraintSet list_set;
    // private String[] class_comment;
    Vector<String> class_comment;
    // private TextView[] textViews_comment;


    CommentPopupWindow commentPopupWindow;
    TextView text_add_comment;

    private class getCommentThread extends HttpThread {
        @Override
        public void run() {
            String res_query_comment = doQueryComment(token, cid);
            // System.out.println(res_query_comment);
            try {
                JSONObject comments = new JSONObject(res_query_comment);
                // int n = comments.toString().split(";").length;
                // System.out.println(n);
                for (int i = 0; ; i++) {
                    try {
                        JSONArray arr = comments.getJSONArray(String.valueOf(i));
                        class_comment.add(arr.getString(1));
                    }
                    catch (JSONException e) {
                        break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        Intent intent = getIntent();
        // TextView text = (TextView)findViewById(R.id.text_ddl);
        // text.setText(String.valueOf(intent.getIntExtra("course_id", 0)));
        cid = intent.getStringExtra("course_id");
        String name = intent.getStringExtra("course_name");
        String teacher = intent.getStringExtra("course_teacher");
        TextView text = (TextView)findViewById(R.id.comment_info);
        text.setText(name + " (" + teacher + ")");

        SharedPreferences getToken = this.getSharedPreferences("loginInfo", this.MODE_PRIVATE);
        token = getToken.getString("Token","null");

        class_comment = new Vector<>();

        Thread t = new getCommentThread();
        t.start();

        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        scroll = (ScrollView)findViewById(R.id.scrollList);
        comment_list_view = (ConstraintLayout)scroll.findViewById(R.id.comment_list);
        list_set = new ConstraintSet();
        // System.out.println(class_comment);
        //if (class_comment != null) textViews_comment = new TextView[class_comment.size()];
        // showComments();
        dynamic_create_View();


        // 添加评论
        text_add_comment = (TextView)this.findViewById(R.id.add_comment_text);
        text_add_comment.setBackgroundResource(R.drawable.comment_text_border);
        text_add_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditWindow();
            }
        });

        Button btn_add_comment = (Button)this.findViewById(R.id.add_comment_button);
        Context c = this;
        btn_add_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = text_add_comment.getText().toString().trim();

                // 将content发到服务器
                new HttpThread() {
                    @Override
                    public void run(){
                        int res = doSubmitComment(token, cid, content);
                        if (res != 200) {
                            Looper.prepare();
                            Toast.makeText(c, "评论失败", Toast.LENGTH_LONG).show();
                            System.out.println("add comment: " + res);
                        }
                        else {
                            Looper.prepare();
                            Toast.makeText(c, "评论成功，退出重进即可查看", Toast.LENGTH_LONG).show();
                            text_add_comment.setText("点此输入评论");
                        }
                        Looper.loop();
                    }
                }.start();
            }
        });

    }

    public void showEditWindow() {
        commentPopupWindow = new CommentPopupWindow(this, text_add_comment.getText().toString().trim());
        commentPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                String content = commentPopupWindow.comment_text.getText().toString().trim();
                text_add_comment.setText(content);
            }
        });
        commentPopupWindow.showAtLocation(findViewById(R.id.comment_main), Gravity.CENTER, 0, 0);
    }

    private void dynamic_create_View() {
        System.out.println(class_comment);
        if (class_comment.size() == 0) {
            TextView no_comment = new TextView(this);
            no_comment.setId(IDUtils.generateViewId());
            no_comment.setText("目前还没有评论哦");
            no_comment.setTextSize(20);
            no_comment.setTextColor(getResources().getColor(R.color.snow3));
            no_comment.setGravity(Gravity.CENTER);

            comment_list_view.addView(no_comment);
            list_set.constrainHeight(no_comment.getId(), 1200);
            list_set.constrainWidth(no_comment.getId(), ConstraintSet.MATCH_CONSTRAINT);
            list_set.connect(no_comment.getId(), ConstraintSet.LEFT,
                    ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 0);
            list_set.connect(no_comment.getId(), ConstraintSet.RIGHT,
                    ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 0);
            list_set.connect(no_comment.getId(), ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0);
            list_set.connect(no_comment.getId(), ConstraintSet.BOTTOM,
                    ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0);
        }
        else {
            TextView last = null;
            for (int i = 0; i < class_comment.size(); i++) {
                TextView textView_comment = new TextView(this);
                textView_comment.setId(IDUtils.generateViewId());
                textView_comment.setText(class_comment.get(i));
                textView_comment.setMinLines(4);
                textView_comment.setBackgroundResource(R.drawable.comment_text_filled);
                textView_comment.setTextSize(18);
                textView_comment.setPadding(28, 15, 28, 15);

                comment_list_view.addView(textView_comment);
                list_set.constrainHeight(textView_comment.getId(), ConstraintSet.WRAP_CONTENT);
                list_set.constrainWidth(textView_comment.getId(), ConstraintSet.MATCH_CONSTRAINT);
                list_set.connect(textView_comment.getId(), ConstraintSet.LEFT,
                        ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 15);
                list_set.connect(textView_comment.getId(), ConstraintSet.RIGHT,
                         ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 15);
                if (last == null)
                    list_set.connect(textView_comment.getId(), ConstraintSet.TOP,
                            ConstraintSet.PARENT_ID, ConstraintSet.TOP, 20);
                else list_set.connect(textView_comment.getId(), ConstraintSet.TOP,
                        last.getId(), ConstraintSet.BOTTOM, 20);
                last = textView_comment;
            }
        }
        list_set.applyTo(comment_list_view);
    }
}
