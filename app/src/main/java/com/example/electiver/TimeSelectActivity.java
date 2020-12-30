package com.example.electiver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.daimajia.swipe.util.Attributes;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class TimeSelectActivity extends AppCompatActivity {

    ScrollView scroll;
    private ListView mListView;
    private ListViewAdapter mAdapter;
    String Token;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.time_select);
        scroll = (ScrollView)findViewById(R.id.time_scrollList);

        SharedPreferences sp = getSharedPreferences("loginInfo",MODE_PRIVATE);
        Token = sp.getString("Token","null");

        Intent intent = getIntent();
        int day = intent.getIntExtra("day",1);
        int starttime = intent.getIntExtra("starttime",3);
        int endtime = intent.getIntExtra("endtime", 4);

        AskForData(day, starttime,endtime);

        Button btn_return = (Button)findViewById(R.id.time_return);
        btn_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Button btn_show_result = (Button) findViewById(R.id.time_showresult);
        btn_show_result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getData();
            }
        });

    }

    public void freshCourseList(List<Course> courseData){
        mListView = (ListView) findViewById(R.id.time_swipe_listview);
        mAdapter = new ListViewAdapter(this,courseData);
        mAdapter.setMode(Attributes.Mode.Single);
        mListView.setAdapter(mAdapter);
        setListViewHeight(mListView);
    }
    public void getData(){
        Log.d("askforData","try open files");
        String getResult;
        try{
            FileInputStream readFile = openFileInput("tmpCourseData1.txt");
            byte[] resultByte = new byte[readFile.available()];
            readFile.read(resultByte);
            getResult = new String(resultByte);

            JSONObject jsonObject = new JSONObject(getResult);
            List<Course> myCourseData=new ArrayList<Course>();
            int num = jsonObject.length();
            int count=0;
            int i=0;
            while(true){
                String info = jsonObject.getString(String.valueOf(i));
                Course course = new Course();
                course.SetAllAttr(info);
                if(course.ifOktoAddCourse(this)){
                    myCourseData.add(course);
                    count++;
                }
                if(count==30) break;
                i++;
                if(i==num) break;
            }
            if(count==0){
                Toast.makeText(this, "所有课程均与您的课表冲突",
                        Toast.LENGTH_SHORT).show();
            }
            freshCourseList(myCourseData);
            deleteFile("tmpCourseData1.txt");
        }catch (IOException | JSONException e ){
            e.printStackTrace();
        }
    }

    public void AskForData(int day, int starttime, int endtime){
        new HttpThread(){
            @Override
            public void run(){
                String queryResults = doQueryOnSchedule(Token, day, starttime, endtime);
                String filename = "tmpCourseData1.txt";
                Log.d("askforData",queryResults);
                byte[] buffer = queryResults.getBytes();
                int len = buffer.length;
                FileOutputStream outputStream = null;
                try{
                    outputStream = openFileOutput(filename, MODE_PRIVATE);
                    try{
                        outputStream.write(buffer,0,len);
                        Log.d("fileOutput","write something");
                    }catch(IOException e){
                        e.printStackTrace();
                    }finally{
                        outputStream.close();
                    }
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }.start();


    }

    public void setListViewHeight(ListView listView){
        ListAdapter listAdapter = listView.getAdapter();
        if(listAdapter==null){
            return ;
        }
        int totalHeight = 0;
        int len=listAdapter.getCount();
        for(int i=0;i<len;i++){
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0,0);
            totalHeight+=listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight+(listView.getDividerHeight()*(listAdapter.getCount()-1));
        listView.setLayoutParams(params);
    }
}
