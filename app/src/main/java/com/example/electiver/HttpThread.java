package com.example.electiver;

import android.os.StrictMode;
import android.util.Log;
import android.util.Pair;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

    /*------------------------------
    @Author 谭淑敏
    30版本的Android不允许在主线程中进行耗时的Http操作，需要通过线程实现。
    本类中提供若干Http请求函数，使用时需要在主线程新建匿名类并重写run函数，然后调用Start方法启动线程。
    示例：
    new HttpThread(){
        @Override
        public void run(){
            do-something
        }
    }.start();

    子线程中可以通过Handler向主线程传递信息，为了防止内存泄漏，重写了一个静态的myHandler类
    定义在LoginActivity.java的末尾，需要使用时请复制到自己的java文件中。
    new HttpThread(){
        @Override
        public void run(){

            do-something

            Message msg = Message.obtain();
            if(...){ msg.what = AnInteger; msg.obj = AnyThingYouWant; }
            else if(...){ ... }
            else { ... }

            myHandler.sendmessage(msg);
        }
    }.start();
    然后在myHandler类中重写handleMessage函数，对不同的msg.what进行处理
     -------------------------------*/

public class HttpThread extends Thread{
    URL url;
    String username;
    String password;
    String grade;
    String department;
    String major;

    /*------------------------------
       仅注册和登陆需要用到的注册函数
     -------------------------------*/

    public HttpThread(String url, String username, String password, String grade, String depart, String major){
        try{
            this.url = new URL(url);
        }catch(MalformedURLException e){
            e.printStackTrace();
        }
        this.username = username;
        this.password = password;
        this.grade = grade;
        this.department = depart;
        this.major = major;

    }

    /*------------------------------
        空构造函数，其余的请求都用这个就可以
     -------------------------------*/

    public HttpThread(){

    }

    /*------------------------------
        注册
        需要用户名、密码、年级、专业、院系作为参数（写在构造函数里）
        连接失败会在Logcat中打印提示。Tag: doPost
        成功返回字符串。
     -------------------------------*/

    public String doPost()throws IOException{
        String result = "";
        BufferedReader reader = null;
        int getresult=0;
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().
                detectDiskWrites().detectNetwork().penaltyLog().build());
        try{
            String data = "username="+username+"&password="+password;
            data += "&major="+major+"&department="+department+"&grade="+grade;
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setUseCaches(false);
            con.getOutputStream().write(data.getBytes("UTF-8"));
            con.getInputStream();
            Log.d("signUp",String.valueOf(con.getResponseCode()));
            if(con.getResponseCode()==200) {
                reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                result = reader.readLine();
                if(reader != null){
                    try{
                        reader.close();
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
            } else{
                Log.d("doPost","responseCode error");
            }
            getresult=con.getResponseCode();
        }catch(MalformedURLException e){
            e.printStackTrace();
        }
        return result;
    }

    public String doLogin(String userName, String passWord)throws IOException{
        String result = "";
        BufferedReader reader = null;
        int getresult=0;
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().
                detectDiskWrites().detectNetwork().penaltyLog().build());
        try{
            String myurl = "http://47.92.240.179:5001/user/enroll";
            URL MyUrl = new URL(myurl);
            String data = "username="+userName+"&password="+passWord;
            HttpURLConnection con = (HttpURLConnection) MyUrl.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setUseCaches(false);
            con.getOutputStream().write(data.getBytes("UTF-8"));
            con.getInputStream();

            if(con.getResponseCode()==200) {
                reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                result = reader.readLine();
                if(reader != null){
                    try{
                        reader.close();
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
            } else{
                Log.d("doPost","responseCode error");
            }
            getresult=con.getResponseCode();
        }catch(MalformedURLException e){
            e.printStackTrace();
        }
        return result;
    }

    /*------------------------------
        查询课程
        参数为String值对的List，例如：{<"name", "计算概论">, <"category", "任选">}
        第一个值对需为token
        合法的key值包括：cid, name, category, credit, teacher, mon, tue, wed, thu, fri, sat, sun, depart
        其中日期key的value值形式为"%d--%d"，例如周三1-2节表示为<"wed", "1--2">
        连接失败在Logcat中打印提示。Tag：doCourseQuery
        成功返回字符串，字符串中包含若干Json，包含若干门课程的信息
     -------------------------------*/

    public String doCourseQuery(ArrayList<Pair<String, String>> para){
        StringBuilder tosend = new StringBuilder("token=" + para.get(0).second);
        for(int i=1;i<para.size();i++){
            tosend.append("&").append(para.get(i).first).append("=").append(para.get(i).second);
        }
        String result = "";
        BufferedReader reader = null;
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().
                detectDiskWrites().detectNetwork().penaltyLog().build());
        try{
            String myurl = "http://47.92.240.179:5001/querycourse";
            URL MyUrl = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) MyUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.getOutputStream().write(tosend.toString().getBytes("UTF-8"));
            conn.getInputStream();

            if(conn.getResponseCode()==200) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                result = reader.readLine();
                if(reader != null){
                    try{
                        reader.close();
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }else{
                Log.d("doCourseQuery", "Response error");
            }

        }catch(MalformedURLException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        return result;
    }

    /*------------------------------
        查询*某一个*时间段的课程，供Schedule使用
        需要传入一周中的哪一天（1-7 represent mon-sun），开始时间和结束时间（1到12）
        例如查询周三3-4的课，应调用doQueryOnSchedule(token, 3, 3, 4);
        连接失败在Logcat中提示，Tag：doQueryOnSchedule
        成功返回字符串
     -------------------------------*/

    public String doQueryOnSchedule(String token, int whichday, int starttime, int endtime){
        String tosend = "token="+token;
        String day="";
        switch(whichday){
            case 1:
                day="mon";
                break;
            case 2:
                day="tue";
                break;
            case 3:
                day="wed";
                break;
            case 4:
                day="thu";
                break;
            case 5:
                day="fri";
                break;
            case 6:
                day="sat";
                break;
            case 7:
                day="sun";
                break;
        }
        tosend+="&"+day+"=";
        tosend+=String.valueOf(starttime);
        tosend+="--";
        tosend+=String.valueOf(endtime);
        String result = "";
        BufferedReader reader = null;
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().
                detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());

        try{
            String myurl = "http://47.92.240.179:5001/querycourse";
            URL MyUrl = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) MyUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.getOutputStream().write(tosend.getBytes("UTF-8"));
            conn.getInputStream();

            if(conn.getResponseCode()==200) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                result = reader.readLine();
                if(reader != null){
                    try{
                        reader.close();
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }else{
                Log.d("doQueryOnSchedule", "responsecode error");
            }

        }catch(MalformedURLException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        return result;
    }

    /*------------------------------
        发布评论。
        参数包括token、课程cid、评论内容
        成功返回200，失败返回404或405
    -------------------------------*/

    public int doSubmitComment(String token, String cid, String comment){

        int getresult=0;
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().
                detectDiskWrites().detectNetwork().penaltyLog().build());
        try{
            String myurl = "http://47.92.240.179:5001/comment/submitcomm";
            String tosend ="token="+token+"&cid="+cid+"&comment="+comment;
            URL MyUrl = new URL(myurl);
            HttpURLConnection con = (HttpURLConnection) MyUrl.openConnection();

            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setUseCaches(false);
            con.getOutputStream().write(tosend.getBytes("UTF-8"));
            con.getInputStream();

            getresult=con.getResponseCode();
        }catch(MalformedURLException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
        return getresult;
    }

    /*------------------------------
        删除评论。
        参数包括token、评论comid
        成功返回200，失败返回404或405
    -------------------------------*/

    public int doDeleteComment(String token, String comid){
        int getresult=0;
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().
                detectDiskWrites().detectNetwork().penaltyLog().build());
        try{
            String myurl = "http://47.92.240.179:5001/comment/deletecomment";
            String tosend ="token="+token+"&comid="+comid;
            URL MyUrl = new URL(myurl);
            HttpURLConnection con = (HttpURLConnection) MyUrl.openConnection();

            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setUseCaches(false);
            con.getOutputStream().write(tosend.getBytes("UTF-8"));
            con.getInputStream();

            getresult=con.getResponseCode();
        }catch(MalformedURLException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
        return getresult;
    }

    /*------------------------------
        通过cid查询课程评论
        参数为token
        连接失败打印Logcat，Tag：doQueryComment
        成功返回包含若干条评论的Json形式字符串
     -------------------------------*/

    public String doQueryComment(String token){
        int getresult=0;
        String result = "";
        BufferedReader reader = null;
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().
                detectDiskWrites().detectNetwork().penaltyLog().build());
        try{
            String myurl = "http://47.92.240.179:5001/comment/querybycid";
            myurl+="?token="+token;
            URL MyUrl = new URL(myurl);
            HttpURLConnection con = (HttpURLConnection) MyUrl.openConnection();

            con.setRequestMethod("GET");
            con.connect();
            con.getInputStream();

            if(con.getResponseCode()==200){
                reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                result = reader.readLine();
                if(reader!=null){
                    try{
                        reader.close();
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }else{
                Log.d("doQueryComment", "responseCode error");
            }
        }catch(MalformedURLException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
        return result;
    }

    /*------------------------------
        查询我发表过的评论
        参数为token、课程cid
        连接失败打印Logcat，Tag：doQueryComment
        成功返回包含若干条评论的Json形式字符串
     -------------------------------*/

    public String doQueryMyComment(String token, String cid){
        int getresult=0;
        String result = "";
        BufferedReader reader = null;
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().
                detectDiskWrites().detectNetwork().penaltyLog().build());
        try{
            String myurl = "http://47.92.240.179:5001/comment/querybyuid";
            myurl+="?token="+token+"&cid="+cid;
            URL MyUrl = new URL(myurl);
            HttpURLConnection con = (HttpURLConnection) MyUrl.openConnection();

            con.setRequestMethod("GET");
            con.connect();
            con.getInputStream();

            if(con.getResponseCode()==200){
                reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                result = reader.readLine();
                if(reader!=null){
                    try{
                        reader.close();
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }else{
                Log.d("doQueryComment", "responsecode error");
            }
        }catch(MalformedURLException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
        return result;
    }

    /*------------------------------
        查询ddl
        参数为token、课程cid
        连接失败在logcat中提示，Tag：doQueryDDL
        成功返回包含ddlid、ddlcontent、ddltime、ddlstate的json形式字符串
     -------------------------------*/

    public String doQueryDDL(String token, String cid){
        int getresult=0;
        String result = "";
        BufferedReader reader = null;
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().
                detectDiskWrites().detectNetwork().penaltyLog().build());
        try{
            String myurl = "http://47.92.240.179:5001/ddl/queryddl";
            String tosend ="token="+token+"&cid="+cid;
            URL MyUrl = new URL(myurl);
            HttpURLConnection con = (HttpURLConnection) MyUrl.openConnection();

            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setUseCaches(false);
            con.getOutputStream().write(tosend.getBytes("UTF-8"));
            con.getInputStream();

            getresult=con.getResponseCode();
            if(con.getResponseCode()==200){
                reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                result = reader.readLine();
                if(reader!=null){
                    try{
                        reader.close();
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }else{
                Log.d("doQueryDDL", "responsecode error");
            }

        }catch(MalformedURLException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
        return result;
    }

    /*------------------------------
        插入ddl
        参数为token、课程cid、ddl内容、ddl时间、ddl状态
        成功返回200，失败返回404或405
     -------------------------------*/

    public int doInsertDDL(String token, String cid,
                           String ddlcontent, String ddltime, String ddlstate){
        int getresult=0;
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().
                detectDiskWrites().detectNetwork().penaltyLog().build());
        try{
            String myurl = "http://47.92.240.179:5001/ddl/insertddl";
            String tosend ="token="+token+"&cid="+cid+"&ddlcontent="+ddlcontent;
            tosend+="&ddltime="+ddltime+"&ddlstate="+ddlstate;
            URL MyUrl = new URL(myurl);
            HttpURLConnection con = (HttpURLConnection) MyUrl.openConnection();

            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setUseCaches(false);
            con.getOutputStream().write(tosend.getBytes("UTF-8"));
            con.getInputStream();

            getresult=con.getResponseCode();
        }catch(MalformedURLException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
        return getresult;
    }

    /*------------------------------
        改变ddl完成状态
        参数为token、ddlid、ddl状态
        成功返回200，失败返回404或405
     -------------------------------*/

    public int doUpdateDDLState(String token, String ddlid, String ddlstate){
        int getresult=0;
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().
                detectDiskWrites().detectNetwork().penaltyLog().build());
        try{
            String myurl = "http://47.92.240.179:5001/ddl/updatestate";
            String tosend ="token="+token+"&ddlid="+ddlid+"&ddlstate="+ddlstate;
            URL MyUrl = new URL(myurl);
            HttpURLConnection con = (HttpURLConnection) MyUrl.openConnection();

            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setUseCaches(false);
            con.getOutputStream().write(tosend.getBytes("UTF-8"));
            con.getInputStream();

            getresult=con.getResponseCode();
        }catch(MalformedURLException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
        return getresult;
    }

    /*------------------------------
        更改ddl信息
        参数为token、ddlid、ddl内容、ddl时间
        成功返回200，失败返回404或405
     -------------------------------*/

    public int doUpdateDDLInfo(String token, String ddlid,
                           String ddlcontent, String ddltime){
        int getresult=0;
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().
                detectDiskWrites().detectNetwork().penaltyLog().build());
        try{
            String myurl = "http://47.92.240.179:5001/ddl/updateddlinfo";
            String tosend ="token="+token+"&ddlid="+ddlid+"&ddlcontent="+ddlcontent;
            tosend+="&ddltime="+ddltime;
            URL MyUrl = new URL(myurl);
            HttpURLConnection con = (HttpURLConnection) MyUrl.openConnection();

            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setUseCaches(false);
            con.getOutputStream().write(tosend.getBytes("UTF-8"));
            con.getInputStream();

            getresult=con.getResponseCode();
        }catch(MalformedURLException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
        return getresult;
    }

    /*------------------------------
        删除ddl
        参数为token、ddlid
        成功返回200，失败返回404或405
     -------------------------------*/

    public int doDeleteDDL(String token, String ddlid){
        int getresult=0;
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().
                detectDiskWrites().detectNetwork().penaltyLog().build());
        try{
            String myurl = "http://47.92.240.179:5001/ddl/deleteddl";
            String tosend ="token="+token+"&ddlid="+ddlid;
            URL MyUrl = new URL(myurl);
            HttpURLConnection con = (HttpURLConnection) MyUrl.openConnection();

            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setUseCaches(false);
            con.getOutputStream().write(tosend.getBytes("UTF-8"));
            con.getInputStream();

            getresult=con.getResponseCode();
        }catch(MalformedURLException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
        return getresult;
    }

    /*------------------------------
        用户增加自己的课程
        参数为token、课程cid
        成功返回200，失败返回404或405
     -------------------------------*/

    public int doInsertCourse(String token, String cid){
        int getresult=0;
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().
                detectDiskWrites().detectNetwork().penaltyLog().build());
        try{
            String myurl = "http://47.92.240.179:5001/usrcou/insertusrcou";
            String tosend ="token="+token+"&cid="+cid;
            URL MyUrl = new URL(myurl);
            HttpURLConnection con = (HttpURLConnection) MyUrl.openConnection();

            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setUseCaches(false);
            con.getOutputStream().write(tosend.getBytes("UTF-8"));
            con.getInputStream();

            getresult=con.getResponseCode();
        }catch(MalformedURLException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
        return getresult;
    }

    /*------------------------------
        查询自己的课程
        参数为token
        连接失败在logcat中提示，Tag：doQueryCourse
        成功返回cid
     -------------------------------*/

    public String doQueryMyCourse(String token){
        int getresult=0;
        String result = "";
        BufferedReader reader = null;
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().
                detectDiskWrites().detectNetwork().penaltyLog().build());
        try{
            String myurl = "http://47.92.240.179:5001/usrcou/queryusrcou";
            String tosend ="token="+token;
            URL MyUrl = new URL(myurl);
            HttpURLConnection con = (HttpURLConnection) MyUrl.openConnection();

            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setUseCaches(false);
            con.getOutputStream().write(tosend.getBytes("UTF-8"));
            con.getInputStream();

            getresult=con.getResponseCode();
            if(con.getResponseCode()==200){
                reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                result = reader.readLine();
                if(reader!=null){
                    try{
                        reader.close();
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }else{
                Log.d("doQueryCourse", "responsecode error");
            }

        }catch(MalformedURLException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
        return result;
    }

    /*------------------------------
        删除自己的课程
        参数为token
        连接失败在logcat中提示，Tag：doDeleteCourse
        成功返回cid
     -------------------------------*/

    public String doDeleteCourse(String token, String cid){
        int getresult=0;
        String result = "";
        BufferedReader reader = null;
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().
                detectDiskWrites().detectNetwork().penaltyLog().build());
        try{
            String myurl = "http://47.92.240.179:5001/usrcou/deleteusrcou";
            String tosend ="token="+token+"&cid="+cid;
            URL MyUrl = new URL(myurl);
            HttpURLConnection con = (HttpURLConnection) MyUrl.openConnection();

            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setUseCaches(false);
            con.getOutputStream().write(tosend.getBytes("UTF-8"));
            con.getInputStream();

            getresult=con.getResponseCode();
            if(con.getResponseCode()==200){
                reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                result = reader.readLine();
                if(reader!=null){
                    try{
                        reader.close();
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }else{
                Log.d("doDeleteCourse", "responsecode error");
            }

        }catch(MalformedURLException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
        return result;
    }

    /*------------------------------
        修改密码
        参数为username, oldpsw, newpsw
        连接失败在logcat中提示，Tag：doAlterPassword
        成功返回字符串
     -------------------------------*/

    public String doAlterPassword(String token, String oldpsw, String newpsw){
        int getresult=0;
        String result = "";
        BufferedReader reader = null;
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().
                detectDiskWrites().detectNetwork().penaltyLog().build());
        try{
            String myurl = "http://47.92.240.179:5001/user/alter";
            String tosend ="token="+token+"&password="+oldpsw+"&new_password="+newpsw;
            URL MyUrl = new URL(myurl);
            HttpURLConnection con = (HttpURLConnection) MyUrl.openConnection();

            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setUseCaches(false);
            con.getOutputStream().write(tosend.getBytes("UTF-8"));
            con.getInputStream();

            getresult=con.getResponseCode();
            if(con.getResponseCode()==200){
                reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                result = reader.readLine();
                if(reader!=null){
                    try{
                        reader.close();
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }else{
                Log.d("doDeleteCourse", "responsecode error");
            }

        }catch(MalformedURLException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
        return result;
    }

    public void run() {
        try {
            String res = doPost();
            Log.d("aa","doPost");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
