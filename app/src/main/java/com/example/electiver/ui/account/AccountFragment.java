package com.example.electiver.ui.account;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.electiver.LoginActivity;
import com.example.electiver.MainActivity;
import com.example.electiver.R;

public class AccountFragment extends Fragment {
    View view;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_account, container, false);

        getValueRefreshed();
        return view;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        Button btn_logout = (Button) getActivity().findViewById(R.id.account_logout);
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "退出登录", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent();
                intent.setClass(getActivity(), LoginActivity.class);
                startActivity(intent);

                getValueRefreshed();
            }
        });
    }

    public void getValueRefreshed(){
        TextView username = (TextView)view.findViewById(R.id.account_username);
        TextView grade = (TextView)view.findViewById(R.id.account_grade);
        TextView department = (TextView)view.findViewById(R.id.account_department);
        TextView major = (TextView)view.findViewById(R.id.account_major);
        SharedPreferences saveinfo = getActivity().getSharedPreferences("loginInfo", getActivity().MODE_PRIVATE);
        username.setText(saveinfo.getString("UserName", "none"));
        grade.setText(saveinfo.getString("Grade","none"));
        department.setText(saveinfo.getString("Department", "none"));
        major.setText(saveinfo.getString("Major", "none"));
    }
}