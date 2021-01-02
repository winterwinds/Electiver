package com.example.electiver.ui.account;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.electiver.DeadlineActivity;
import com.example.electiver.ForgetPswActivity;
import com.example.electiver.HttpThread;
import com.example.electiver.LoginActivity;
import com.example.electiver.MyCommentActivity;
import com.example.electiver.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

public class AccountFragment extends Fragment {
    //------本页面需要修改get_class_list()
    //------使用ctrl+f寻找以便修改
    View view;
    String Token;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_account, container, false);


        //保险起见，每次createView时从SP中重新读取数据
        getValueRefreshed();

        SharedPreferences getToken = getActivity().getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
        Token = getToken.getString("Token","null");

        connect_other_activity();
        try {
            get_class_list();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (class_info == null) return view;
        set_class_list();

        test_only();//正常使用时需要注释掉这一行
        return view;
    }


    /*
     * 从服务器获取需要的量
     */
    Vector<JSONObject> CLASS_JSON;
    String[] class_info;
    private void get_class_list() throws JSONException {
        //
        final String[] result = new String[1];
        result[0] = "";
        HttpThread thread = new HttpThread(){
            @Override
            public void run(){
                result[0] = doQueryMyCourse(Token);
                Log.d("queryCourse", result[0]);
            }
        };
        thread.start();
        try {
            Thread.sleep( 500 );
        } catch (Exception e){
            System.exit( 0 ); //退出程序
        }

        if(result[0].equals("")) {
            class_info = new String[0];
            return ;
        }
        JSONObject json = new JSONObject(result[0]);
        Iterator iterator = json.keys();
        CLASS_JSON = new Vector<JSONObject>();
        while(iterator.hasNext()){
            String key = (String) iterator.next();
            CLASS_JSON.add(json.getJSONObject(key));
        }
        class_info = new String[CLASS_JSON.size()];
        for (int i = 0; i < CLASS_JSON.size(); i ++) {
            class_info[i] = CLASS_JSON.get(i).getString("name");
        }
        //网络有点bug拿不到cid，之后是假数据生成
        /*
         * Test Only
        class_info = new String[3];
        class_info[0] = "软件工程";
        class_info[1] = "太极拳";
        class_info[2] = "计算机网络";*/
        //Test End
    }
    /*
     * 动态生成课程列表
     */
    private void set_class_list() {
        LinearLayout list = view.findViewById(R.id.classList);

        Button class_name;
        for (int i = 0; i < class_info.length; i ++) {
            class_name = new Button(getContext());
            class_name.setText(class_info[i]);
            class_name.setTextColor(0xFFFFFFFF);
            list.addView(class_name);
        }
    }

    /*
     * 与其他活动相连接
     */
    private void connect_other_activity() {
        //查看评论界面的入口
        Button to_my_comment = (Button)view.findViewById(R.id.toMyComment);
        to_my_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), MyCommentActivity.class);
                startActivity(intent);
            }
        });

        //修改密码界面的入口
        Button edit_psw = (Button)view.findViewById(R.id.selfManage);
        edit_psw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ForgetPswActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){

        super.onActivityCreated(savedInstanceState);

        //退出登陆需要先清空SP文件再跳转到登录界面，重新登录完成后refresh数据
        Button btn_logout = (Button) getActivity().findViewById(R.id.account_logout);

        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "退出登录", Toast.LENGTH_SHORT).show();
                reLogin();
                getValueRefreshed();
            }
        });
    }

    public void reLogin(){
        //清空loginInfo
        SharedPreferences getUserInfo = getActivity().getSharedPreferences("loginInfo",getActivity().MODE_PRIVATE);
        SharedPreferences.Editor editor=getUserInfo.edit();
        editor.clear();
        editor.commit();
        //清空courseInfo
        SharedPreferences getCourseInfo = getActivity().getSharedPreferences("courseInfo",getActivity().MODE_PRIVATE);
        editor=getCourseInfo.edit();
        editor.clear();
        editor.commit();
        //清空timeAvail
        SharedPreferences occupyTime=getActivity().getSharedPreferences("timeAvail",getActivity().MODE_PRIVATE);
        editor = occupyTime.edit();
        String possibledays[] ={"mon","tue","wed","thu","fri","sat","sun"};
        String possibletime[] ={"1-2","3-4","5-6","7-8","10-11"};
        for(int i=0;i<possibledays.length;i++){
            for(int j=0;j<possibletime.length;j++){
                String timetag = possibledays[i]+possibletime[j];
                editor.putString(timetag,"false");
            }
        }
        editor.commit();
        //跳转到登录界面
        Intent intent = new Intent();
        intent.setClass(getActivity(), LoginActivity.class);
        startActivityForResult(intent, 1);

    }

    public void getValueRefreshed(){

        TextView username = (TextView)view.findViewById(R.id.account_username);
        TextView grade = (TextView)view.findViewById(R.id.account_grade);
        TextView department = (TextView)view.findViewById(R.id.account_department);
        TextView major = (TextView)view.findViewById(R.id.account_major);

        SharedPreferences saveinfo = getActivity().getSharedPreferences("loginInfo", getActivity().MODE_PRIVATE);

        username.setText(saveinfo.getString("UserName", "none"));
        grade.setText(saveinfo.getString("Grade","none"));
        department.setText(saveinfo.getString("Department","none"));
        major.setText(saveinfo.getString("Major", "none"));
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data!=null){
            SharedPreferences saveinfo = getActivity().getSharedPreferences("loginInfo", getActivity().MODE_PRIVATE);
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

    /*
     * Test Only 一般情况下界面的进入控件会隐藏
     */
    //ddl的入口
    private void test_only(){
        Button to_my_comment = (Button)view.findViewById(R.id.enter);
        to_my_comment.setVisibility(View.VISIBLE);
        to_my_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), DeadlineActivity.class);
                intent.putExtra("course_id", "04832191");
                intent.putExtra("course_name", "软件工程");
                startActivity(intent);
            }
        });
    }
    //Test End
}

