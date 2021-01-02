package com.example.electiver.ui.schedule;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.content.Intent;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.electiver.CommentPopupWindow;
import com.example.electiver.Course;
import com.example.electiver.DeadlineActivity;
import com.example.electiver.HttpThread;
import com.example.electiver.R;
import com.example.electiver.TimeSelectActivity;

import java.util.Vector;


public class ScheduleFragment extends Fragment {

    String token;

    float density;

    SharedPreferences getCourse;

    Context mContext;
    View view;
    FrameLayout layout;

    UserSchedule user_schedule;
    int num_course;
    int num_class;

    boolean []course_nos;
    Button []btn_classes;
    Button []btn_delete_texts;

    volatile boolean flag;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mContext = getContext();

        SharedPreferences getToken = mContext.getSharedPreferences("loginInfo", mContext.MODE_PRIVATE);
        token = getToken.getString("Token","null");

        density = mContext.getResources().getDisplayMetrics().density;

        view = inflater.inflate(R.layout.fragment_schedule, container, false);
        layout = view.findViewById(R.id.schedule_table);

        getCourse = mContext.getSharedPreferences("courseInfo", mContext.MODE_PRIVATE);
        System.out.println(getCourse.getString("courseNum", "0"));


        this.user_schedule = new UserSchedule(getCourse);
        this.num_course = user_schedule.num;
        this.num_class = user_schedule.btn_num;
        System.out.println(num_course);
        course_nos = new boolean[20];
        this.btn_classes = new Button[42];
        this.btn_delete_texts = new Button[42];

        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 12; j++)
                createFreeButton(i + 1, j + 1);
        }

        int btn_cnt = 0;
        for (int i = 0; i < num_course; i++) {
            UserSchedule.OneCourse course = user_schedule.courses.elementAt(i);
            course_nos[course.no] = true;
            int []btn_nos = new int[course.num_time];
            for (int j = 0; j < course.num_time; j++)
                btn_nos[j] = j + btn_cnt;
            for (int j = 0; j < course.num_time; j++) {
                System.out.println(j);
                UserSchedule.CourseTime time = course.time[j];
                btn_classes[btn_cnt] = createButton(course.id, btn_cnt, course.no, course.name,
                        time.day, time.class_start, time.class_end, btn_nos);
                // btn_delete_texts[btn_cnt] = createDeleteButton(course.id, course.no, time.day, time.class_start, btn_nos);
                btn_cnt++;
            }
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getCourse = mContext.getSharedPreferences("courseInfo", Context.MODE_PRIVATE);
        this.user_schedule = new UserSchedule(getCourse);
        if (num_course == user_schedule.num) return;
        for (int no = 0; no < 20; no++) {
            if (!course_nos[no] && user_schedule.course_nos[no]) {

                this.num_course = user_schedule.num;
                this.num_class = user_schedule.btn_num;
                Button []btn_classes_old = this.btn_classes;
                this.btn_classes = new Button[42];
                System.arraycopy(btn_classes_old, 0, this.btn_classes, 0, btn_classes_old.length);

                UserSchedule.OneCourse course = user_schedule.getCourseByNo(no);
                course_nos[no] = true;
                int []btn_nos = new int[course.num_time];
                for (int j = 0, k = 0; j < 42 && k < course.num_time; j++) {
                    if (btn_classes[j] == null) {
                        btn_nos[k] = j;
                        k++;
                    }
                }

                for (int j = 0; j < course.num_time; j++) {
                    UserSchedule.CourseTime time = course.time[j];
                    btn_classes[btn_nos[j]] = createButton(course.id, btn_nos[j], course.no, course.name,
                            time.day, time.class_start, time.class_end, btn_nos);
                    // btn_delete_texts[btn_nos[j]] = createDeleteButton(course.id, course.no, time.day, time.class_start, btn_nos);
                }
                break;
            }
        }

    }

    // 没课的button; day是星期几，从1开始; class_no代表这一天的第几节课，也是从1开始
    private Button createFreeButton(int day, int class_no) {
        Button btn = new Button(mContext);
        int x = (int)((30 + (day - 1) * 75) * density + 0.5f) + day * 2 + 2;
        int y = (int)((30 + (class_no - 1) * 50) * density + 0.5f) + class_no * 4 + 2;
        int width = (int)(75 * density + 0.5f);
        int height = (int)(50 * density + 0.5f);

        // btn.setId(id);
        btn.setGravity(Gravity.TOP);
        btn.setBackgroundResource(R.drawable.schedule_course_btn_grey);
        btn.setTextAppearance(mContext, R.style.schedule_btn_text);
        FrameLayout.LayoutParams btn_params = new FrameLayout.LayoutParams(width, height);
        btn_params.leftMargin = x;
        btn_params.topMargin = y;
        btn.setLayoutParams(btn_params);
        layout.addView(btn);

        // click：go to elective assistant fragment
        int [][]convert_time = new int[][] {{1, 2}, {1, 2}, {3, 4}, {3, 4}, {5, 6}, {5, 6}, {7, 8}, {7, 8},
                                            {7, 9}, {10, 11}, {10, 11}, {10, 12}};
        btn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                Intent intent = new Intent(mContext, TimeSelectActivity.class);
                intent.putExtra("day", day);
                intent.putExtra("starttime", convert_time[class_no - 1][0]);
                intent.putExtra("endtime", convert_time[class_no - 1][1]);
                startActivity(intent);
            }
        });

        return btn;
    }

    // 有课的button
    private Button createButton(String course_id, int id, int course_no, String name,
                                int day, int class_start, int class_end, int []btn_nos) {
        Button btn = new Button(mContext);
        int x = (int)((30 + (day - 1) * 75) * density + 0.5f) + day * 2 + 2;
        int y = (int)((30 + (class_start - 1) * 50) * density + 0.5f) + class_start * 4 + 2;
        int width = (int)(75 * density + 0.5f);
        int height = (int)(((class_end - class_start + 1) * 50) * density + 0.5f) + (class_end - class_start) * 3;

        // btn.setId(id);
        btn.setText(name);
        btn.setGravity(Gravity.TOP);
        btn.setBackgroundResource(R.drawable.schedule_course_btn_green);
        btn.setTextAppearance(mContext, R.style.schedule_btn_text);
        FrameLayout.LayoutParams btn_params = new FrameLayout.LayoutParams(width, height);
        btn_params.leftMargin = x;
        btn_params.topMargin = y;
        btn.setLayoutParams(btn_params);
        layout.addView(btn);

        // click：go to deadline activity
        btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, DeadlineActivity.class);
                intent.putExtra("course_id", course_id);
                intent.putExtra("course_name", name);
                startActivity(intent);
            }
        });

        btn.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View view) {
                ShowDeleteWindow(btn, course_id, course_no, btn_nos, x, y);
                return true;
            }
        });

        return btn;
    }

    public void deleteCourse(String course_id, int course_no, int []btn_nos) {

        Thread t = new HttpThread() {
            @Override
            public void run() {
                flag = false;
                String res = doDeleteCourse(token, course_id);
                if (res.equals("delete success"))
                    flag = true;
            }
        };
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (!flag) {
            Toast.makeText(mContext, "删除课程失败", Toast.LENGTH_LONG).show();
            System.out.println("删除课程失败");
            return;
        }
        Toast.makeText(mContext, "课程已删除", Toast.LENGTH_LONG).show();
        System.out.println("课程已删除");

        SharedPreferences.Editor editor = getCourse.edit();
        editor.putString("course" + course_no, "null");
        int newCourseNum = num_course - 1;
        editor.putString("courseNum", String.valueOf(newCourseNum));
        editor.apply();

        SharedPreferences getTime = mContext.getSharedPreferences("timeAvail",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor_time = getTime.edit();
        Course course = new Course();
        course.SetAllAttr(user_schedule.getCourseByNo(course_no).courseInfo);
        if(!course.GetTimetag1().equals("null")){
            editor_time.putString(course.GetTimetag1(), "false");
        }
        if(!course.GetTimetag2().equals("null")){
            editor_time.putString(course.GetTimetag2(), "false");
        }
        editor_time.apply();

        course_nos[course_no] = false;
        num_course--;
        num_class -= btn_nos.length;

        for (int btn_no : btn_nos) {
            layout.removeView(btn_classes[btn_no]);
            // layout.removeView(btn_delete_texts[btn_no]);
        }

    }

    private void ShowDeleteWindow(Button btn, String course_id, int course_no, int []btn_nos, int x, int y) {
        SchedulePopupWindow deletePopupWindow = new SchedulePopupWindow(getActivity(), this, course_id, course_no, btn_nos);
        deletePopupWindow.showAtLocation(btn, Gravity.TOP | Gravity.LEFT, x + (int) (6 * density), y + (int) (38 * density));
    }
}


// 获取数据
class UserSchedule {
    public class CourseTime {
        public int day;
        public int class_start;
        public int class_end;
        public CourseTime(CourseTime t){
            this.day = t.day;
            this.class_start = t.class_start;
            this.class_end = t.class_end;
        };
        public CourseTime(int day, int class_start, int class_end) {
            this.day = day;
            this.class_start = class_start;
            this.class_end = class_end;
        }
    }
    public class OneCourse {
        int no;
        String id;
        String name;
        int num_time;
        CourseTime []time;
        String courseInfo;

        public OneCourse(int no, String id, String name, int num_time, CourseTime []time, String info) {
            this.no = no;
            this.id = new String(id);
            this.name = new String(name);
            this.courseInfo = new String(info);
            this.num_time = num_time;
            this.time = new CourseTime[num_time];
            for (int i = 0; i < num_time; i++) {
                this.time[i] = new CourseTime(time[i]);
            }
        }
    }

    public int num;
    public int btn_num;
    Vector<OneCourse> courses;
    boolean []course_nos;

    public UserSchedule(SharedPreferences getCourse) {
        this.courses = new Vector<>();
        this.course_nos = new boolean[20];
        this.num = 0;
        this.btn_num = 0;

        for (int l = 0; l < 20; l++) {
            Course my_course = new Course();
            String courseInfo = getCourse.getString("course" + String.valueOf(l), "null");
            if (courseInfo.equals("null")) continue;
            my_course.SetAllAttr(courseInfo);
            int time_cnt = 0;
            CourseTime []time = new CourseTime[7];
            for (int i = 0; i < 7; i++) {
                if (my_course.hasDayClasses(i)) {
                    System.out.println(my_course.daysClassTime(i));
                    String []start_end = my_course.daysClassTime(i).split("--|-");
                    for (int len = 0; len < start_end[1].length(); len++) {
                        if (!Character.isDigit(start_end[1].charAt(len))) {
                            start_end[1] = start_end[1].substring(0, len);
                        }
                    }
                    time[time_cnt] = new CourseTime(i + 1, Integer.parseInt(start_end[0]), Integer.parseInt(start_end[1]));
                    time_cnt++;
                }
            }
            courses.add(new OneCourse(l, my_course.GetCid(), my_course.GetName(), time_cnt, time, courseInfo));
            course_nos[l] = true;
            this.btn_num += time_cnt;
            this.num++;
        }
    }

    public OneCourse getCourseByNo(int no) {
        for (int i = 0; i < num; i++) {
            OneCourse c = courses.get(i);
            if (c.no == no)
                return c;
        }
        return null;
    }
}
