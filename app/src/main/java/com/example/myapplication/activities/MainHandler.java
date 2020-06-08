package com.example.myapplication.activities;

import android.os.Handler;
import android.os.Message;

public class MainHandler {
    private static Handler globalHandler = null;

    public static void setHandler(Handler handler) {
        System.out.println("Handler set");
        globalHandler = handler;
    }

    public void sentMessage(int state) {
        Message msg = new Message();
        msg.what = state;
        globalHandler.sendMessage(msg);
    }
}
