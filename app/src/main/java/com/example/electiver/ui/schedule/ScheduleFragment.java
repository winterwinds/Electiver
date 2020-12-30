package com.example.electiver.ui.schedule;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.electiver.Course;
import com.example.electiver.DeadlineActivity;
import com.example.electiver.R;

import org.json.JSONObject;

import java.util.Vector;
import java.util.Map;

public class ScheduleFragment extends Fragment {

    float density;

    View view;
    FrameLayout layout;

    // ScheduleViewModel scheduleViewModel;
    UserSchedule user_schedule;
    int num_course;
    int num_class;

    // 以下的成员变量暂时没用
    Button []btn_classes;
    int []btn_course;  //每个时间段对应的课程序号
    boolean [][]time_selected;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        //this.scheduleViewModel =
        //        new ViewModelProvider(this).get(ScheduleViewModel.class);
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);

        density = getContext().getResources().getDisplayMetrics().density;

        layout = view.findViewById(R.id.schedule_table);

        SharedPreferences getCourse = getContext().getSharedPreferences("courseInfo", getContext().MODE_PRIVATE);
        System.out.println(getCourse.getString("courseNum", "0"));

        // System.out.println(key_value.size());
        this.user_schedule = new UserSchedule(getCourse);
        this.num_course = user_schedule.num;
        this.num_class = user_schedule.btn_num;
        this.btn_classes = new Button[num_class];
        this.btn_course = new int[num_class];
        this.time_selected = new boolean[7][12];


        int btn_cnt = 0;
        for (int i = 0; i < num_course; i++) {
            UserSchedule.OneCourse course = user_schedule.courses.elementAt(i);
            for (int j = 0; j < course.num_time; j++) {
                UserSchedule.CourseTime time = course.time[j];
                for (int k = time.class_start; k <= time.class_end; k++)
                    time_selected[time.day - 1][k - 1] = true;
                btn_classes[btn_cnt] = createButton(course.id, btn_cnt, course.name, course.room,
                        time.day, time.class_start, time.class_end);
                btn_course[btn_cnt] = i;
                btn_cnt++;
            }
        }

        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 12; j++) {
                if (time_selected[i][j]) continue;
                createFreeButton(i + 1, j + 1);
            }
        }
        return view;
    }

    // 没课的button；day是星期几，注意是从1开始。class_no代表这一天的第几节课，也是从1开始。
    private Button createFreeButton(int day, int class_no) {
        Button btn = new Button(getContext());
        int x = (int)((30 + (day - 1) * 75) * density + 0.5f) + day * 2 + 2;
        int y = (int)((30 + (class_no - 1) * 50) * density + 0.5f) + class_no * 4 + 2;
        int width = (int)(75 * density + 0.5f);
        int height = (int)(50 * density + 0.5f);

        // btn.setId(id);
        btn.setGravity(Gravity.TOP);
        btn.setBackgroundResource(R.drawable.schedule_course_btn_grey);
        btn.setTextAppearance(getContext(), R.style.schedule_btn_text);
        FrameLayout.LayoutParams btn_params = new FrameLayout.LayoutParams(width, height);
        btn_params.leftMargin = x;
        btn_params.topMargin = y;
        btn.setLayoutParams(btn_params);
        layout.addView(btn);

        // click：go to elective assistant fragment
        btn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                //
            }
        });

        return btn;
    }

    // 有课的button
    private Button createButton(String course_id, int id, String name, String room, int day, int class_start, int class_end) {
        Button btn = new Button(getContext());
        int x = (int)((30 + (day - 1) * 75) * density + 0.5f) + day * 2 + 2;
        int y = (int)((30 + (class_start - 1) * 50) * density + 0.5f) + class_start * 4 + 2;
        int width = (int)(75 * density + 0.5f);
        int height = (int)(((class_end - class_start + 1) * 50) * density + 0.5f) + (class_end - class_start) * 2;

        btn.setId(id);
        btn.setText(name);
        btn.setGravity(Gravity.TOP);
        btn.setBackgroundResource(R.drawable.schedule_course_btn_green);
        btn.setTextAppearance(getContext(), R.style.schedule_btn_text);
        FrameLayout.LayoutParams btn_params = new FrameLayout.LayoutParams(width, height);
        btn_params.leftMargin = x;
        btn_params.topMargin = y;
        btn.setLayoutParams(btn_params);
        layout.addView(btn);

        // click：go to deadline activity
        btn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), DeadlineActivity.class);
                // Intent intent = new Intent(getContext(), CommentActivity.class);
                intent.putExtra("course_id", course_id);
                startActivity(intent);
            }
        });

        return btn;
    }
}


// 造数据
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
        String id;
        String name;
        String room;
        String nearest_ddl;    // yymmddhhmm
        int num_time;
        CourseTime []time;

        public OneCourse(String id, String name, String room, String ddl, int num_time, CourseTime []time) {
            // System.out.println(id);
            this.id = new String(id);
            this.name = new String(name);
            if (room != null) this.room = new String(room);
            this.nearest_ddl = new String(ddl);
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

    public UserSchedule(SharedPreferences getCourse) {/*
        courses = new Vector<>();
        this.num = 3;
        this.btn_num = 0;
        courses.add(new Course(1,"汉字太极与养生课", "第一体育馆", "", 1,
                new CourseTime[]{new CourseTime(1, 7, 8)}));
        this.btn_num += 1;

        courses.add(new Course(2,"理论计算机科学基础", "二教404", "", 1,
                new CourseTime[]{new CourseTime(2, 7, 9)}));
        this.btn_num += 1;

        courses.add(new Course(3,"软件工程","理教201", "2012152359", 2,
                new CourseTime[]{new CourseTime(2, 3, 4),
                        new CourseTime(4, 5, 6)}));
        this.btn_num += 2;*/

        courses = new Vector<>();
        this.num = 0;
        this.btn_num = 0;

        for (int l = 0; l < 20; l++) {
            Course my_course = new Course();
            String getInfo = getCourse.getString("course" + String.valueOf(l), "null");
            if (getInfo.equals("null")) continue;
            my_course.SetAllAttr(getInfo);
            int time_cnt = 0;
            CourseTime []time = new CourseTime[7];
            for (int i = 0; i < 7; i++) {
                if (my_course.hasDayClasses(i)) {
                    String []start_end = my_course.daysClassTime(i).split("-");
                    time[time_cnt] = new CourseTime(i + 1, Integer.parseInt(start_end[0]), Integer.parseInt(start_end[1]));
                    time_cnt++;
                }
            }
            courses.add(new OneCourse(my_course.GetCid(), my_course.GetName(), my_course.GetDepartment(),
                    "", time_cnt, time));
            this.btn_num += time_cnt;
            this.num++;
        }
    }
}
