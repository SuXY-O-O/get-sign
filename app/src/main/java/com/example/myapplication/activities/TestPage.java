package com.example.myapplication.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.trays.PicToShow;

public class TestPage extends AppCompatActivity {
    private boolean noneStop = false;
    private PicToShow pts = new PicToShow();
    ImageView imageView = null;
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
                case 2:
                    Bitmap bitmap = pts.getBitmap();
                    if (bitmap != null && noneStop) {
                        imageView.setImageBitmap(bitmap);
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_page);
        MainHandler.setHandler(mHandler);
    }

    public void startShow(View view) {
        imageView = findViewById(R.id.imageView);
        Button button = findViewById(R.id.button6);
        button.setClickable(false);
        noneStop = true;
    }

    public void stopShow(View view) {
        noneStop = false;
        Intent start = new Intent(this, StartPage.class);
        startActivity(start);
        this.finish();
    }

    private void toFailPage() {
        Intent fail = new Intent(this, FailToConnect.class);
        startActivity(fail);
        this.finish();
    }
}
