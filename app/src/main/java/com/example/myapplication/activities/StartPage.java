package com.example.myapplication.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

public class StartPage extends AppCompatActivity {
    @SuppressLint("HandlerLeak")
    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            System.out.println("handling");
            switch (msg.what) {
                case -1:
                    toFailPage();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_page);
        MainHandler.setHandler(mHandler);
    }

    public void test(View view) {
        Intent start = new Intent(this, TestPage.class);
        startActivity(start);
        this.finish();
    }

    public void toMain(View view) {
        Intent start = new Intent(this, MainPage.class);
        startActivity(start);
        this.finish();
    }

    private void toFailPage() {
        Intent fail = new Intent(this, FailToConnect.class);
        startActivity(fail);
        this.finish();
    }
}
