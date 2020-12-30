package com.example.electiver;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class Course {
    private String cid;
    private String name;
    private String category;
    private String credit;
    private String teacher;
    private boolean[] days;
    private String[] time;
    private String depart;
    private String timetag1;
    private String timetag2;

    public Course(){
        days=new boolean[7];
        time=new String[7];
        timetag1="null";
        timetag2="null";
    }
    public Course(String c1, String c2, String c3, String c4, String c5, boolean[] c6, String[] c7, String c8){
        days=new boolean[7];
        time=new String[7];
        cid=c1;
        name=c2;
        category=c3;
        credit=c4;
        teacher=c5;
        timetag1="null";
        timetag2="null";
        String[] sevendays={"mon","tue","wed","thu","fri","sat","sun"};
        for(int i=0;i<7;i++) days[i]=c6[i];
        for(int i=0;i<7;i++){
            if(days[i]==true) {
                time[i]=c7[i];
                if(timetag1.equals("null")){
                    timetag1=sevendays[i];
                    timetag1+=modifyTime(time[i]);
                }else{
                    timetag2=sevendays[i];
                    timetag2+=modifyTime(time[i]);
                }
            }
            else time[i]="";
        }
        depart=c8;
    }
    public String modifyTime(String originalTime){
        String modified;
        switch(originalTime){
            case "1-2":
            case "1-2单周":
            case "1-2双周":
                modified="1-2";
                break;
            case "3-4":
            case "3-4单周":
            case "3-4双周":
                modified="3-4";
                break;
            case "5-6":
            case "5-6单周":
            case "5-6双周":
                modified="5-6";
                break;
            case "7-8":
            case "7-8单周":
            case "7-8双周":
            case "7-9":
            case "7-9单周":
            case "7-9双周":
                modified="7-8";
                break;
            case "10-11":
            case "10-11单周":
            case "10-11双周":
            case "10-12":
            case "10-12单周":
            case "10-12双周":
                modified="10-11";
                break;
            default:
                modified="1-2";
                break;
        }
        return modified;
    }
    public void SetCid(String c){
        cid = c;
    }
    public void SetName(String n){
        name = n;
    }
    public void SetCategory(String c){
        category = c;
    }
    public void SetCredit(String c){
        credit = c;
    }
    public void SetTeacher(String t){
        teacher = t;
    }
    public void SetDepartment(String d){
        depart = d;
    }
    public Boolean SetSchedule(Boolean[] d, String[] t){
        if(d.length!=7 || t.length!=7){
            return false;
        }
        for(int i=0;i<7;i++){
            days[i]=d[i];
            if(d[i]){
                time[i]=t[i];
            }else{
                time[i]="";
            }
        }
        return true;
    }
    public String Course2JSONString(){
        JSONObject json = new JSONObject();
        try{
            json.put("name", name);
            json.put("cid", cid);
            json.put("category", category);
            json.put("credit", credit);
            json.put("teacher", teacher);
            json.put("depart",depart);
            String[] sevendays={"mon","tue","wed","thu","fri","sat","sun"};
            String[] possibletimes={"1-2","3-4","5-6","7-8","7-9","10-11","10-12"};
            for(int i=0;i<7;i++){
                if(days[i]){
                    json.put(sevendays[i],time[i]);
                }else{
                    json.put(sevendays[i],"");
                }
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
        return json.toString();
    }
    public Boolean SetAllAttr(String allAttr){
        try{
            JSONObject obj = new JSONObject(allAttr);
            String getAttr;
            getAttr = obj.getString("name");
            SetName(getAttr);
            getAttr = obj.getString("cid");
            SetCid(getAttr);
            getAttr = obj.getString("category");
            SetCategory(getAttr);
            getAttr = obj.getString("credit");
            SetCredit(getAttr);
            getAttr = obj.getString("teacher");
            SetTeacher(getAttr);

            getAttr = obj.getString("mon");
            if(!getAttr.equals("")){
                days[0]=true;
                time[0]=getAttr;
                if(timetag1.equals("null")){
                    timetag1="mon"+modifyTime(getAttr);
                }else{
                    timetag2="mon"+modifyTime(getAttr);
                }
            }
            getAttr = obj.getString("tue");
            if(!getAttr.equals("")){
                days[1]=true;
                time[1]=getAttr;
                if(timetag1.equals("null")){
                    timetag1="tue"+modifyTime(getAttr);
                }else{
                    timetag2="tue"+modifyTime(getAttr);
                }
            }
            getAttr = obj.getString("wed");
            if(!getAttr.equals("")){
                days[2]=true;
                time[2]=getAttr;
                if(timetag1.equals("null")){
                    timetag1="wed"+modifyTime(getAttr);
                }else{
                    timetag2="wed"+modifyTime(getAttr);
                }
            }
            getAttr = obj.getString("thu");
            if(!getAttr.equals("")){
                days[3]=true;
                time[3]=getAttr;
                if(timetag1.equals("null")){
                    timetag1="thu"+modifyTime(getAttr);
                }else{
                    timetag2="thu"+modifyTime(getAttr);
                }
            }
            getAttr = obj.getString("fri");
            if(!getAttr.equals("")){
                days[4]=true;
                time[4]=getAttr;
                if(timetag1.equals("null")){
                    timetag1="fri"+modifyTime(getAttr);
                }else{
                    timetag2="fri"+modifyTime(getAttr);
                }
            }
            getAttr = obj.getString("sat");
            if(!getAttr.equals("")){
                days[5]=true;
                time[5]=getAttr;
                if(timetag1.equals("null")){
                    timetag1="sat"+modifyTime(getAttr);
                }else{
                    timetag2="sat"+modifyTime(getAttr);
                }
            }
            getAttr = obj.getString("sun");
            if(!getAttr.equals("")){
                days[6]=true;
                time[6]=getAttr;
                if(timetag1.equals("null")){
                    timetag1="sun"+modifyTime(getAttr);
                }else{
                    timetag2="sun"+modifyTime(getAttr);
                }
            }

        }catch(JSONException e){
            e.printStackTrace();
        }


        return true;
    }

    public String GetCid(){
        return cid;
    }
    public String GetName(){
        return name;
    }
    public String GetCategory(){
        return category;
    }
    public String GetCredit(){
        return credit;
    }
    public String GetTeacher(){
        return teacher;
    }
    public String GetDepartment(){
        return depart;
    }
    public boolean hasDayClasses(int i){return days[i];}
    public String GetTimetag1(){return timetag1;}
    public String GetTimetag2(){return timetag2;}

    public String GetTime(){
        String result="";
        String[] sevendays={"周一","周二","周三","周四","周五","周六","周日"};
        String[] possibletimes={"1-2","3-4","5-6","7-8","7-9","10-11","10-12"};
        for(int i=0;i<7;i++){
            if(days[i]){
                result+=sevendays[i];
                result+=": ";
                result+=time[i];
                result+="  ";
            }
        }
        return result;
    }

    public String daysClassTime(int i){
        return time[i];
    }
    public Boolean checktime(int day, String t){
        if(days[day] && !time[day].equals(t)){
            return true;
        }
        return false;
    }
    public boolean ifOktoAddCourse(Context mContext){
        SharedPreferences occupyTime=mContext.getSharedPreferences("timeAvail",mContext.MODE_PRIVATE);
        String check=occupyTime.getString(timetag1,"null");

        if(check.equals("true")){
            return false;
        }
        check=occupyTime.getString(timetag2,"null");
        if(check.equals("true")){
            return false;
        }
        return true;
    }
}
