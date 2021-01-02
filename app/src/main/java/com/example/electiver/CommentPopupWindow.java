package com.example.electiver;

import android.app.Activity;
import android.content.Context;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

public class CommentPopupWindow extends PopupWindow {

    private Context mContext;
    private View view;
    // private Button btn_save_pop;
    public EditText comment_text;


    public CommentPopupWindow(Activity mContext, String text_content) {

        this.mContext = mContext;

        this.view = LayoutInflater.from(mContext).inflate(R.layout.window_comment, null);

        comment_text = (EditText) view.findViewById(R.id.comment_text);
        comment_text.setText(text_content);

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
        this.setWidth((int) (d.getWidth() * 0.8));

        // 设置弹出窗体可点击
        this.setFocusable(true);
    }

}
