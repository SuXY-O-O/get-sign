package com.example.myapplication.client;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.example.myapplication.activities.MainHandler;
import com.example.myapplication.trays.PicToSend;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;


public class Sender extends Thread {
    private MainHandler mainHandler = new MainHandler();
    private BufferedOutputStream bos;
    private PicToSend pts = new PicToSend();
    private static Handler myHandler;

    public Sender(Socket socket) {
        try {
            bos = new BufferedOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            mainHandler.sentMessage(-1);
        }
    }

    public static Handler getMyHandler() {
        return myHandler;
    }

    @SuppressLint("HandlerLeak")
    @Override
    public void run() {
        Looper.prepare();
        myHandler = new Handler() {
            private int count = 0;

            @Override
            public void handleMessage(Message msg) {
                byte[] image;
                int size;
                if (msg.what == 5) {
                    count++;
                    count = count % 10;
                    if (count != 1) {
                        return;
                    }
                    System.out.println("getting a pic and maybe waiting");
                    image = pts.getWaiting();
                    size = pts.getSize();
                    if (image == null) {
                        try {
                            Head.sentHead(bos, "STOP", 0);
                            bos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return;
                    }
                    if (size > 0) {
                        System.out.println("get an image");
                        try {
                            Head.sentHead(bos, "jpg", size);
                            System.out.println("A head sent");
                            bos.write(image, 0, size);
                            bos.flush();
                            //mainHandler.sentMessage(3);
                        } catch (IOException e) {
                            e.printStackTrace();
                            mainHandler.sentMessage(-1);
                        }
                    } else {
                        System.out.println("Not a file");
                    }
                } else if (msg.what == 6) {
                    try {
                        Head.sentHead(bos, "STOP", 0);
                        bos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        Looper.loop();
    }
}
