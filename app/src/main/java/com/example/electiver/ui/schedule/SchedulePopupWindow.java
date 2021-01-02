package com.example.electiver.ui.schedule;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.electiver.Course;
import com.example.electiver.HttpThread;
import com.example.electiver.R;
import com.google.android.material.snackbar.BaseTransientBottomBar;

public class SchedulePopupWindow extends PopupWindow {

    private Context mContext;
    private View view;
    public Button btn_delete;

    @SuppressLint("InflateParams")
    public SchedulePopupWindow(Activity mContext, ScheduleFragment mFragment, String course_id, int course_no, int []btn_nos) {

        this.mContext = mContext;

        this.view = LayoutInflater.from(mContext).inflate(R.layout.window_delete_course, null);

        btn_delete = (Button) view.findViewById(R.id.button_delete);

        // 设置外部可点击
        this.setOutsideTouchable(true);

        // 设置视图
        this.setContentView(this.view);

        // 设置弹出窗体的宽和高
        Window dialogWindow = mContext.getWindow();

        WindowManager m = mContext.getWindowManager();
        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
        WindowManager.LayoutParams p = dialogWindow.getAttributes(); // 获取对话框当前的参数值

        this.setHeight(RelativeLayout.LayoutParams.WRAP_CONTENT);
        this.setWidth(RelativeLayout.LayoutParams.WRAP_CONTENT);

        // 设置弹出窗体可点击
        this.setFocusable(true);

        btn_delete.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                mFragment.deleteCourse(course_id, course_no, btn_nos);
                dismiss();
            }
        });
    }

}
