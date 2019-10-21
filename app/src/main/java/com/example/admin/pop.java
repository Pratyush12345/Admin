package com.example.admin;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;

public class pop extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.popupwindow);

        DisplayMetrics ds=new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(ds);

        int width=ds.widthPixels;
        int height=ds.heightPixels;

        getWindow().setLayout((int)(width*0.8),(int)(height*0.6));

    }
}
