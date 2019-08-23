package com.classup;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.Random;

/**
 * Created by root on 9/8/15.
 * this is a singleton class that will contain useful methods/functions/routines
 */
public class MiscFunctions extends Activity {

    private static final String CHAR_LIST =
            "abcdefghijklmnopqrstuadxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    private static final int RANDOM_STRING_LENGTH = 10;
    //String server_ip = "http://10.0.2.2:8000";
    String server_ip = "https://www.classupclient.com";


    private static MiscFunctions ourInstance = new MiscFunctions();

    public static MiscFunctions getInstance() {
        return ourInstance;
    }

    private MiscFunctions() {
    }

    /*
    method to return the ip of main server.
    The ip is written in plain text in file server_url.txt in assets folder
    */

    public  String getServerIP(Context c)  {
        return server_ip;
    }

    public Boolean checkConnection(Context context)    {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    /**
     * This method generates random string
     * @return
     */
    public String generateRandomString()    {
        StringBuffer randStr = new StringBuffer();
        for(int i=0; i<RANDOM_STRING_LENGTH; i++){
            int number = getRandomNumber();
            char ch = CHAR_LIST.charAt(number);
            randStr.append(ch);
        }
        return randStr.toString();
    }

    /**
     * This method generates random numbers
     * @return int
     */
    private int getRandomNumber() {
        int randomInt = 0;
        Random randomGenerator = new Random();
        randomInt = randomGenerator.nextInt(CHAR_LIST.length());
        if (randomInt - 1 == -1) {
            return randomInt;
        } else {
            return randomInt - 1;
        }
    }

    public boolean isValidEmailAddress(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }
}