package com.example.electiver.ui.electiveassistant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.electiver.R;

public class ElectiveAssistantFragment extends Fragment {
    View view;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        //electiveassistantViewModel =
         //       new ViewModelProvider(this).get(ElectiveAssistantViewModel.class);
        view = inflater.inflate(R.layout.fragment_account, container, false);
       /* final TextView textView = root.findViewById(R.id.text_account);
        electiveassistantViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });*/
        return view;
    }
}