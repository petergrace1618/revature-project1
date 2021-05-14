package org.shivacorp.test;

import java.util.Calendar;
import java.util.Date;

public class Sandbox {
    public static void main(String[] args) {
        Date d = new Calendar();
    }
}

class Dummy implements Runnable {
    @Override
    public void run() {
        String c = this.getClass().getName();
        c = c.substring(c.lastIndexOf('.')+1);
        System.out.println(c);
    }
}