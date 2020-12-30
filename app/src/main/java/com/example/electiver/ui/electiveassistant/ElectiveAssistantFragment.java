package com.example.electiver.ui.electiveassistant;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.daimajia.swipe.util.Attributes;
import com.example.electiver.Course;
import com.example.electiver.HttpThread;
import com.example.electiver.IDUtils;
import com.example.electiver.R;
import com.example.electiver.ListViewAdapter;
import com.example.electiver.RegisterActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

public class ElectiveAssistantFragment extends Fragment {
    View view;
    ScrollView scroll;
    ConstraintLayout course_list_view;
    ConstraintSet list_set;
    int screenWidth;
    float density;
    private ListView mListView;
    private ListViewAdapter mAdapter;
    private List<Course> mDatas;

    private String token;
    private String searchOnCategory="任选";
    private String searchOnDepartment="不选择任何学院";
    private String searchOnName="";
    private String recommendType="通选课";
    int recommend_num = 10;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_elective_assistant, container, false);

        scroll = (ScrollView)view.findViewById(R.id.assistant_scrollList);


        density = getContext().getResources().getDisplayMetrics().density;
        screenWidth = getContext().getResources().getDisplayMetrics().widthPixels;

        Button start_search = (Button)view.findViewById(R.id.assis_search);
        Button end_search = (Button)view. findViewById(R.id.assis_search_end);

        Spinner spin_category = (Spinner)view.findViewById(R.id.spin_category);
        Spinner spin_department = (Spinner)view.findViewById(R.id.spin_coursedepart);
        Spinner spin_recommend = (Spinner)view.findViewById(R.id.spin_rec_category);

        spin_category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                searchOnCategory = (String)spin_category.getItemAtPosition(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spin_department.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                searchOnDepartment = (String)spin_department.getItemAtPosition(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spin_recommend.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                recommendType = (String)spin_recommend.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        start_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int searchwhich=0;
                getEditValue(view);

                SharedPreferences saveinfo = getActivity().getSharedPreferences("loginInfo", getActivity().MODE_PRIVATE);
                token=saveinfo.getString("Token", "none");
                String ifRecommend = saveinfo.getString("alreadyTaken","0");

                if(!recommendType.equals("不限制")){
                    if(ifRecommend.equals("0")){
                        Toast.makeText(getContext(),"请至少添加一门已上过的课程",
                                Toast.LENGTH_LONG).show();
                        searchwhich=-1;
                    }else{

                        Toast.makeText(getContext(),"请点击显示查看推荐结果",
                                Toast.LENGTH_LONG).show();
                        searchwhich=0;
                    }
                }
                else if(searchOnCategory.equals("任选") && searchOnDepartment.equals("不选择任何学院")
                && searchOnName.equals("")){
                    Toast.makeText(getContext(), "请至少选择一项筛选条件",
                            Toast.LENGTH_SHORT).show();
                    getData();
                    freshCourseList(view,mDatas);
                    searchwhich=-1;
                }else{
                    Toast.makeText(getContext(), "开始搜索",
                            Toast.LENGTH_SHORT).show();
                    if(token.equals("none")){
                        Toast.makeText(getContext(), "Token失效，请重新登录",
                                Toast.LENGTH_SHORT).show();
                        searchwhich=-1;
                    }
                    else{
                        searchwhich=1;
                    }
                }
                ArrayList<Pair<String, String>> paras= new ArrayList<Pair<String, String>>();
                Pair<String, String> gettoken=new Pair<>("token",token);
                paras.add(gettoken);

                if(!searchOnCategory.equals("任选")){
                    Pair<String, String> getpara=new Pair<>("category",searchOnCategory);
                    paras.add(getpara);
                }
                if(!searchOnDepartment.equals("不选择任何学院")){
                    Pair<String, String> getpara=new Pair<>("depart",searchOnDepartment);
                    paras.add(getpara);
                }
                if(!searchOnName.equals("")){
                    Pair<String, String> getpara=new Pair<>("name",searchOnName);
                    paras.add(getpara);
                }

                int finalSearchwhich = searchwhich;
                new HttpThread(){
                    @Override
                    public void run(){
                        String filename = "tmpCourseData.txt";
                        FileOutputStream outputStream = null;
                        if(finalSearchwhich ==1){
                            String queryResults = doCourseQuery(paras);
                            Log.d("ShowResult","1:"+queryResults);
                            byte[] buffer = queryResults.getBytes();
                            int len = buffer.length;
                            try{
                                outputStream = getContext().openFileOutput(filename,Context.MODE_PRIVATE);
                                try{
                                    outputStream.write(buffer,0,len);
                                }catch(IOException e){
                                    e.printStackTrace();
                                }finally{
                                    outputStream.close();
                                }
                            }catch(IOException e){
                                e.printStackTrace();
                            }
                        }
                        else if(finalSearchwhich==0){
                            String queryResults = doRecommend(token, recommendType,recommend_num);
                            Log.d("ShowResult","2:"+queryResults);
                            byte[] buffer = queryResults.getBytes();
                            int len = buffer.length;
                            try{
                                outputStream = getContext().openFileOutput(filename,Context.MODE_PRIVATE);
                                try{
                                    outputStream.write(buffer,0,len);
                                }catch(IOException e){
                                    e.printStackTrace();
                                }finally{
                                    outputStream.close();
                                }
                            }catch(IOException e){
                                e.printStackTrace();
                            }
                        }
                    }
                }.start();
            }
        });

        end_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences saveResult= getActivity().getSharedPreferences("courseInfo",getActivity().MODE_PRIVATE);
                String getResult;
                try{
                    FileInputStream readFile = getContext().openFileInput("tmpCourseData.txt");
                    byte[] resultByte = new byte[readFile.available()];
                    readFile.read(resultByte);
                    getResult = new String(resultByte);
                    Log.d("fileOutput",getResult);

                    if(getResult.equals("null")){
                        Toast.makeText(getContext(), "正在搜索中，请稍后重试",
                                Toast.LENGTH_SHORT).show();
                    }else if(getResult.equals("{}")){
                        Toast.makeText(getContext(), "没有符合条件的课程",
                                Toast.LENGTH_SHORT).show();
                    }else if(getResult.equals("")){
                        Toast.makeText(getContext(),"no Result",
                                Toast.LENGTH_SHORT).show();
                    }else{

                        try{
                            JSONObject jsonObject = new JSONObject(getResult);
                            List<Course> myCourseData=new ArrayList<Course>();
                            int num = jsonObject.length();
                            int count=0;
                            int i=0;
                            while(true){
                                String info = jsonObject.getString(String.valueOf(i));
                                Course course = new Course();
                                course.SetAllAttr(info);
                                if(course.ifOktoAddCourse(getContext())){
                                    myCourseData.add(course);
                                    count++;
                                }
                                if(count==30) break;
                                i++;
                                if(i==num) break;
                            }
                            if(count==0){
                                Toast.makeText(getContext(), "所有课程均与您的课表冲突",
                                        Toast.LENGTH_SHORT).show();
                            }
                            Log.d("checkSearch",String.valueOf(count));
                            freshCourseList(view, myCourseData);
                            getContext().deleteFile("tmpCourseData.txt");
                        } catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                }catch(IOException e){
                    e.printStackTrace();
                }



            }
        });

        getData();
        freshCourseList(view,mDatas);


        return view;
    }

    public void freshCourseList(View view, List<Course> courseData){
        mListView = (ListView) view.findViewById(R.id.swipe_listview);
        mAdapter = new ListViewAdapter(getContext(),courseData);
        mAdapter.setMode(Attributes.Mode.Single);
        mListView.setAdapter(mAdapter);
        setListViewHeight(mListView);
    }

    public void getEditValue(View view){
        EditText et_course_name = (EditText)view.findViewById(R.id.search_on_name);
        EditText et_recom_num = (EditText)view.findViewById(R.id.recommend_num);

        searchOnName = et_course_name.getText().toString().trim();
        if(!TextUtils.isEmpty(et_recom_num.getText())){
            recommend_num = Integer.parseInt(et_recom_num.getText().toString().trim());
        }else{
            recommend_num = 10;
        }
        Log.d("assist",String.valueOf(recommend_num));


    }

    public void getData(){
        mDatas = new ArrayList<Course>();

        try{
            InputStream is = getContext().getClassLoader().getResourceAsStream("assets/CourseSet.txt");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String text = new String(buffer,"utf-8");
            try{
                JSONObject json=new JSONObject(text);
                int len=json.length();
                for(int i=0;i<len;i++){
                    String infostr = json.getString(String.valueOf(i));
                    Course tmpc = new Course();
                    tmpc.SetAllAttr(infostr);
                    if(tmpc.ifOktoAddCourse(getContext())){
                        mDatas.add(tmpc);
                    }
                }
            }catch(JSONException e){
                e.printStackTrace();
            }

        }catch(IOException e){
            e.printStackTrace();
        }

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