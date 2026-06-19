package com.example.quizzly.utils;

import android.util.Log;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class EmailJSService {

    private static final String TAG = "EmailJSService";

    public static void sendEmail(String toEmail, String otp) {
        new Thread(() -> {
            try {
                URL url = new URL("https://api.emailjs.com/api/v1.0/email/send");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("origin", "http://localhost");
                conn.setDoOutput(true);

                //EmailJS configuration
                String serviceId = "service_t8p05lu";
                String templateId = "template_ifuwudd";
                String publicKey = "ak8qvTyiFs5DZDFiq";

                String jsonInputString = "{"
                        + "\"service_id\": \"" + serviceId + "\","
                        + "\"template_id\": \"" + templateId + "\","
                        + "\"user_id\": \"" + publicKey + "\","
                        + "\"template_params\": {"
                        + "\"to_email\": \"" + toEmail + "\","
                        + "\"otp\": \"" + otp + "\""
                        + "}"
                        + "}";

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int code = conn.getResponseCode();
                if (code == 200) {
                    Log.d(TAG, "Email sent successfully!");
                } else {
                    Log.e(TAG, "Failed to send email. HTTP Code: " + code);
                }

                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Error sending email", e);
            }
        }).start();
    }
}
