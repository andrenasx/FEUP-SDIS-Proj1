package utils;

import java.util.concurrent.ThreadLocalRandom;

public class Utils {
    public static final int CRLF=0xDA;

    public static void sleepRandom(){
        try {
            int sleepFor = ThreadLocalRandom.current().nextInt(0, 401);
            Thread.sleep(sleepFor);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
