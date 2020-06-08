package com.example.myapplication.client;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static java.lang.Thread.sleep;

class Head {

    public static void sentHead(BufferedOutputStream bos, String type, int length) throws IOException {
        byte[] header = new byte[12];
        String formatType = type;
        while (formatType.length() < 6) {
            formatType = "!" + formatType;
        }
        String head = "HEAD";
        formatType = head + ":" + formatType + ":";
        header = formatType.getBytes(StandardCharsets.UTF_8);
        bos.write(header, 0, 12);
        String len = String.valueOf(length);
        while (len.length() < 12) {
            len = "0" + len;
        }
        bos.write(len.getBytes(StandardCharsets.UTF_8));
        bos.flush();
    }

    public static String readHead(InputStream dis) throws IOException, InterruptedException {
        while (dis.available() < 24) {
            if (dis.available() == -1) {
                return "STOP";
            }
            sleep(10);
        }
        byte[] header = new byte[12];
        int need = 12;
        int length;
        String read = "";
        while (need > 0) {
            length = dis.read(header, 0, need);
            need -= length;
            read += new String(header);
        }
        System.out.println(read);
        String[] s = read.split(":");
        if (!s[0].startsWith("HEAD")) {
            return "WRONG";
        } else {
            String[] s2 = s[1].split("!");
            return s2[s2.length - 1];
        }
    }
}
