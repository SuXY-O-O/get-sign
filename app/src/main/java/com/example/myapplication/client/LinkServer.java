package com.example.myapplication.client;

import com.example.myapplication.activities.MainHandler;
import com.example.myapplication.trays.Tray;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class LinkServer extends Thread {
    private Tray tray = new Tray();
    private MainHandler handler = new MainHandler();

    @Override
    public void run() {
        Socket socket = new Socket();
        System.out.println("linking");
        try {
            socket.connect(new InetSocketAddress("49.233.212.163", 8100), 1000);
            socket.setSoTimeout(1000);
        } catch (IOException e) {
            e.printStackTrace();
            handler.sentMessage(-1);
            return;
        }
        System.out.println("linked");
        try {
            InputStream dis = socket.getInputStream();
            String state = "0";
            byte[] b = new byte[1];
            int count = 0;
            while (!state.equals("1")) {
                count++;
                if (count > 2000) {
                    tray.setState(-1);
                    handler.sentMessage(-1);
                    dis.close();
                    return;
                }
                dis.read(b, 0, 1);
                state = new String(b);
                if (state.equals("1")) {
                    System.out.println("Connect successful");
                    handler.sentMessage(1);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            handler.sentMessage(-1);
            return;
        }
        Receiver receiver = new Receiver(socket);
        receiver.start();
        Sender sender = new Sender(socket);
        sender.start();
    }

}
