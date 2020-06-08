package com.example.myapplication.client;

import com.example.myapplication.activities.MainHandler;
import com.example.myapplication.trays.PicToShow;
import com.example.myapplication.trays.Tray;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Random;

public class Receiver extends Thread {
    private Socket socket;
    private MainHandler handler = new MainHandler();

    public Receiver(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        PicToShow picToShow = new PicToShow();
        while (true) {
            try {
                socket.setSoTimeout(21000);
                InputStream dis = socket.getInputStream();
                String type = Head.readHead(dis);
                byte[] b = new byte[12];
                int need = 12;
                int length;
                String str = "";
                handler.sentMessage(3);
                while (need > 0) {
                    length = dis.read(b, 0, need);
                    need -= length;
                    str += new String(b);
                }
                length = Integer.parseInt(str);
                int tmp;
                int get = 0;
                byte[] buf = new byte[10240];
                if (type.equals("STOP")) {
                    System.out.println("Stop receive");
                    handler.sentMessage(-1);
                    socket.close();
                    break;
                } else {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    while (get < length) {
                        tmp = dis.available();
                        //System.out.println(tmp);
                        if (tmp == -1) {
                            System.out.println("Receive canceled");
                            handler.sentMessage(-1);
                            break;
                        } else if (length - get > 10240) {
                            tmp = dis.read(buf, 0, 10240);
                            bos.write(buf, 0, tmp);
                            get += tmp;
                        } else {
                            tmp = dis.read(buf, 0, length - get);
                            bos.write(buf, 0, tmp);
                            get += tmp;
                        }
                        //System.out.println(length + " \t" + (length - get));
                    }
                    bos.flush();
                    bos.close();
                    picToShow.setNew(bos.toByteArray(), length);
                    System.out.println("Get file : " + "." + type);
                }
            } catch (IOException | InterruptedException e) {
                Tray tray = new Tray();
                tray.setState(-1);
                handler.sentMessage(-1);
                e.printStackTrace();
                break;
            }
        }
    }
}
