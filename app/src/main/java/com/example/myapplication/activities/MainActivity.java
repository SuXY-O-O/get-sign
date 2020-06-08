package com.example.myapplication.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.client.LinkServer;

public class MainActivity extends AppCompatActivity {
    @SuppressLint("HandlerLeak")
    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case -1:
                    toFailPage();
                    break;
                case 1:
                    toStartPage();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = (Button) findViewById(R.id.button);
        button.setClickable(true);
        MainHandler.setHandler(mHandler);
    }

    public void connectToServer(View view) {
        Button button = (Button) findViewById(R.id.button);
        TextView text = (TextView) findViewById(R.id.textView3);
        button.setText("...");
        button.setClickable(false);
        String connecting = "CONNECTING";
        text.setText(connecting);
        LinkServer ls = new LinkServer();
        ls.start();
        //Intent fail = new Intent(this, MainPage.class);
        //startActivity(fail);
        //this.finish();
    }

    private void toFailPage() {
        Intent fail = new Intent(this, FailToConnect.class);
        startActivity(fail);
        this.finish();
    }

    private void toStartPage() {
        Intent start = new Intent(this, StartPage.class);
        startActivity(start);
        this.finish();
    }
}
