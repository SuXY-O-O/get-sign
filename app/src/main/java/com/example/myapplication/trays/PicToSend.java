package com.example.myapplication.trays;

import com.example.myapplication.activities.MainHandler;

public class PicToSend {
    private static byte[] waiting;
    private static int size = 0;
    private boolean kill = false;
    private static MainHandler handler = new MainHandler();

    public synchronized void addIn(byte[] bytes, int length) {
        //System.out.println("a new in ptsend");
        waiting = bytes;
        size = length;
        notifyAll();
    }

    public void setKill(boolean kill) {
        this.kill = kill;
    }

    public synchronized byte[] getWaiting() {
        while (size <= 0) {
            System.out.println("getting pic");
            if (kill) {
                return null;
            }
            try {
                notifyAll();
                wait();
                System.out.println("I'm waked up");
            } catch (InterruptedException e) {
                e.printStackTrace();
                handler.sentMessage(-1);
            }
        }
        return waiting;
    }

    public synchronized int getSize() {
        try {
            return size;
        } finally {
            notifyAll();
        }
    }
}
