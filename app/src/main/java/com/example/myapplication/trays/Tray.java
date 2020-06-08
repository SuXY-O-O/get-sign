package com.example.myapplication.trays;

public class Tray {
    private static int state = 0;
    private static int cancel = 0;
    private static int action = 0;

    public synchronized int setState(int i) {
        state = i;
        return cancel;
    }

    public synchronized int getState() {
        if (state == 0) {
            try {
                wait(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
                cancel = 1;
                return 0;
            }
        }
        if (state == 0) {
            cancel = 1;
            return 0;
        }
        try {
            return state;
        } finally {
            notifyAll();
        }
    }

    public synchronized void setAction(int i) {
        action = i;
        notifyAll();
    }

    public synchronized int getAction() {
        while (action == 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return -1;
            }
        }
        try {
            return action;
        } finally {
            notifyAll();
        }
    }
}
