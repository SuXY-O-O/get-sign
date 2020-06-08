package com.example.myapplication.trays;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.myapplication.activities.MainHandler;

public class PicToShow {
    private static Bitmap bitmapDecoding;
    private static boolean isNew;
    private MainHandler handler = new MainHandler();

    public synchronized void setNew(byte[] bytes, int size) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, size);
        isNew = (bitmap != null);
        if (isNew) {
            bitmapDecoding = bitmap;
            System.out.println("New Picture");
        }
        handler.sentMessage(2);
        notifyAll();
    }

    public synchronized Bitmap getBitmap() {
        try {
            if (!isNew) {
                return null;
            }
            isNew = false;
            return bitmapDecoding;
        } finally {
            notifyAll();
        }
    }
}
