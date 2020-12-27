package com.example.electiver;

import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;

public class ListViewAdapter extends BaseSwipeAdapter {

    private Context mContext;
    private List<Course> mDatas;
    //private TextView mDelete;
    //private SwipeLayout swipeLayout;
    private int pos ;

    public ListViewAdapter(Context context, List<Course> mDatas) {
        this.mContext = context;
        this.mDatas = mDatas;
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void fillValues(int position, View convertView) {
        //填充课程信息
        TextView tv_name = (TextView) convertView.findViewById(R.id.swipe_courseName);
        //tv.setText((position + 1) + ".");
        tv_name.setText(mDatas.get(position).GetName().trim());
        tv_name.setTextSize(14);

        TextView tv_teacher = (TextView) convertView.findViewById(R.id.swipe_teacher);
        tv_teacher.setText("teacher: "+mDatas.get(position).GetTeacher());
        tv_teacher.setTextSize(10);

        TextView tv_time = (TextView) convertView.findViewById(R.id.swipe_time);
        tv_time.setText("time: "+mDatas.get(position).GetTime());
        tv_time.setTextSize(10);

        //选课按钮
        final TextView choose = (TextView) convertView.findViewById(R.id.swipe_choose);
        choose.setTag(position);
        choose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //检查目前已保存的课程数量
                SharedPreferences chooseCourse=mContext.getSharedPreferences("courseInfo",mContext.MODE_PRIVATE);
                String courseNum = chooseCourse.getString("courseNum","0");
                int addcourseNum = Integer.valueOf(courseNum).intValue()+1;

                SharedPreferences.Editor editor = chooseCourse.edit();
                editor.putString("courseNum",String.valueOf(addcourseNum));

                String courseTag = "course";
                for(int i=0;i<addcourseNum;i++){
                    String whereToPlace = chooseCourse.getString("courseNum"+i,"null");
                    //findone!
                    if(!whereToPlace.equals("null")){
                        whereToPlace+=i;
                        break;
                    }
                }

                String toSave = mDatas.get(position).Course2JSONString();
                editor.putString(courseTag,toSave);
                editor.commit();

                SharedPreferences occupyTime=mContext.getSharedPreferences("timeAvail",mContext.MODE_PRIVATE);
                SharedPreferences.Editor editor1 = occupyTime.edit();

                String[] sevendays={"mon","tue","wed","thu","fri","sat","sun"};
                String timetag="";
                for(int i=0;i<7;i++){
                    if(mDatas.get(position).hasDayClasses(i)){
                        timetag=sevendays[i];
                        switch(mDatas.get(position).daysClassTime(i)){
                            case "1-2":
                            case "1-2单周":
                            case "1-2双周":
                                timetag+="1-2";
                                break;
                            case "3-4":
                            case "3-4单周":
                            case "3-4双周":
                                timetag+="3-4";
                                break;
                            case "5-6":
                            case "5-6单周":
                            case "5-6双周":
                                timetag+="5-6";
                                break;
                            case "7-8":
                            case "7-8单周":
                            case "7-8双周":
                            case "7-9":
                            case "7-9单周":
                            case "7-9双周":
                                timetag+="7-8";
                                break;
                            case "10-11":
                            case "10-11单周":
                            case "10-11双周":
                            case "10-12":
                            case "10-12单周":
                            case "10-12双周":
                                timetag+="10-11";
                                break;
                            default:
                                timetag+="1-2";
                                break;
                        }
                        editor1.putString(timetag,"true");
                        editor1.commit();
                        Log.d("ifOkto",timetag);
                    }
                }
                final SwipeLayout sl = (SwipeLayout) convertView.findViewById(getSwipeLayoutResourceId(position));
                int pos = (Integer) choose.getTag();
                Course obj = mDatas.get(pos);

                SharedPreferences userInfo=mContext.getSharedPreferences("loginInfo",mContext.MODE_PRIVATE);
                String getToken = userInfo.getString("Token","null");
                String getCid = obj.GetCid();
                new HttpThread(){
                    @Override
                    public void run(){
                        doInsertCourse(getToken,getCid);
                    }
                }.start();

                mDatas.remove(obj);
                int len = mDatas.size();

                Iterator<Course> it=mDatas.iterator();
                while(it.hasNext()){
                    Course tmpcourse = it.next();
                    if(!tmpcourse.ifOktoAddCourse(convertView.getContext())){
                        it.remove();
                    }
                }
                notifyDataSetChanged();
                sl.close();
            }
        });

        //已上过该课
        final SwipeLayout sl = (SwipeLayout) convertView.findViewById(getSwipeLayoutResourceId(position));
        final TextView delete  = (TextView) convertView.findViewById(R.id.delete);
        delete.setTag(position);
        delete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                new HttpThread(){
                    @Override
                    public void run(){

                    }
                }.start();

                int pos = (Integer) delete.getTag();
                Course obj = mDatas.get(pos);

                mDatas.remove(obj);
                notifyDataSetChanged();
                sl.close();
            }
        });
        final TextView courseInfo = (TextView) convertView.findViewById(R.id.swipe_checkInfo);

        //显示课程信息
        //onClick应该设置成打开一个课程详细介绍页面
        courseInfo.setTag(position);
        courseInfo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "CourseInfo",Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public View generateView(int position, ViewGroup arg1) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.swipeview,null);
        pos = position;
        final SwipeLayout swipeLayout = (SwipeLayout) v.findViewById(R.id.swipe);

        swipeLayout.addSwipeListener(new SimpleSwipeListener() {
            @Override
            public void onOpen(SwipeLayout layout) {//当隐藏的删除menu被打开的时候的回调函数
                YoYo.with(Techniques.Tada).duration(500).delay(100).playOn(layout.findViewById(R.id.trash));

            }
        });

        swipeLayout.setOnDoubleClickListener(new SwipeLayout.DoubleClickListener() {
            @Override
            public void onDoubleClick(SwipeLayout layout,
                                      boolean surface) {


            }
        });


        return v;
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }

}

