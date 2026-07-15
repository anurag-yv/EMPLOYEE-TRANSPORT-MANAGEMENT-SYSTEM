package com.example;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStreamReader;
import java.io.BufferedReader;

public class TestError {
    public static void main(String[] args) {
        try {
            URL url = new URL("http://localhost:1001/api/booking");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            // I need to send a valid request.
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
