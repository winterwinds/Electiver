package com.example.electiver;

public class Course {
    private String cid;
    private String name;
    private String category;
    private int credit;
    private String teacher;
    private Boolean[] days;
    private int[] time;
    private String depart;

    public Course(){
        days=null;
        time=null;
    }
    public Course(String c1, String c2, String c3, int c4, String c5, Boolean[] c6, int[] c7, String c8){
        cid=c1;
        name=c2;
        category=c3;
        credit=c4;
        teacher=c5;
        days=new Boolean[7];
        for(int i=0;i<7;i++) days[i]=c6[i];
        time=new int[7];
        for(int i=0;i<7;i++){
            if(days[i]==true) time[i]=c7[i];
            else time[i]=-1;
        }
        depart=c8;
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
    public void SetCredit(int c){
        credit = c;
    }
    public void SetTeacher(String t){
        teacher = t;
    }
    public void SetDepartment(String d){
        depart = d;
    }
    public Boolean SetSchedule(Boolean[] d, int[] t){
        if(d.length!=7 || t.length!=7){
            return false;
        }
        if(days==null)days = new Boolean[7];
        if(time==null)time = new int[7];
        for(int i=0;i<7;i++){
            days[i]=d[i];
            if(d[i]==true){
                time[i]=t[i];
            }else{
                time[i]=-1;
            }
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
    public int GetCredit(){
        return credit;
    }
    public String GetTeacher(){
        return teacher;
    }
    public String GetDepartment(){
        return depart;
    }
    public int montime(){
        return time[0];
    }
    public int tuestime(){
        return time[1];
    }
    public int wedtime(){
        return time[2];
    }
    public int thurtime(){
        return time[3];
    }
    public int fritime(){
        return time[4];
    }
    public int sattime(){
        return time[5];
    }
    public int suntime(){
        return time[6];
    }
    public Boolean checktime(int day, int t){
        if(days[day]==true && time[day]!=t){
            return true;
        }
        return false;
    }
}
