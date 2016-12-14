package com.nnayram.ccmanager.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.nnayram.ccmanager.core.ResultWrapper;

/**
 * Created by Rufo on 12/4/2016.
 */
public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     *
     * @param resultWrapper
     * @param <T>
     * @return true if empty error messages
     */
    public <T> boolean validateResultWrapper(ResultWrapper<T> resultWrapper) {
        if (resultWrapper.getErrorMessages().isEmpty()) {
            return true;
        }

        StringBuilder errorMessage = new StringBuilder();
        for (String error : resultWrapper.getErrorMessages()) {
            errorMessage.append(error);
            errorMessage.append("\n");
        }
        Toast.makeText(getBaseContext(), errorMessage.toString(), Toast.LENGTH_SHORT).show();
        return false;
    }
}
