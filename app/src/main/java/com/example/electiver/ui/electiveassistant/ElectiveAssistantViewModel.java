package com.example.electiver.ui.electiveassistant;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ElectiveAssistantViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public ElectiveAssistantViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is elective assistant fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}