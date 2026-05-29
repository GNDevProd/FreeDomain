package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Main {
    private static final String ENDPOINT = "https://api.gname.com";
    private static final String APPID = "your_appid";
    private static final String APPKEY = "your_appkey";
    private static final String TIMEZONE = "Asia/Shanghai";

    public static void main(String[] args) {
        // Examples of Add domain template
        try {
            Map<String, String> params = new TreeMap<>();
            params.put("appid", APPID);
            params.put("gntime", String.valueOf(getTimestamp(TIMEZONE)));
            params.put("name", "test");
            params.put("xing", "Smith");
            params.put("ming", "James");
            params.put("email", "James@gname.com");
            params.put("guojia", "SG");
            params.put("province", "Singapore");
            params.put("city", "Central Region");
            params.put("address", "Tanjong Pagar");
            params.put("gjqh", "65");
            params.put("phone", "82563693");
            params.put("youbian", "123456");
            params.put("lang", "us");
            params.put("gntoken", generateSign(params, APPKEY));

            String response = postToApi("/api/template/add", params, 10);
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(response, JsonObject.class);
            int code = jsonObject.get("code").getAsInt();

            if (code == 1) {
                System.out.println("Request successful");
                System.out.println("ID:" + jsonObject.get("data").getAsString());
            } else {
                String msg = jsonObject.get("msg").getAsString();
                throw new RuntimeException("Request failed,err_code:" + code + ",err_msg:" + msg);
            }
        } catch (Exception e) {
            System.err.println("Request failed " + e.getMessage());
        }
    }

    /**
     * Request the interface using POST method
     *
     * @param uri
     * @param params
     * @param timeout s
     * @return
     * @throws IOException
     */
    private static String postToApi(String uri, Map<String, String> params, int timeout) throws IOException {
        String url = ENDPOINT + uri;
        StringBuilder requestBody = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (requestBody.length() > 0) {
                requestBody.append("&");
            }
            requestBody.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        URL apiUrl = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection();
        conn.setConnectTimeout(timeout * 1000);
        conn.setReadTimeout(timeout * 1000);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoOutput(true);
        try (OutputStream outputStream = conn.getOutputStream()) {
            outputStream.write(requestBody.toString().getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new RuntimeException("HTTP status code is " + responseCode);
        }

        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    /**
     * Generate Signature
     *
     * @param params
     * @param appkey
     * @return
     * @throws NoSuchAlgorithmException
     */
    private static String generateSign(Map<String, String> params, String appkey) throws NoSuchAlgorithmException {
        // Sort the parameters by key
        List<Map.Entry<String, String>> sortedParams = new ArrayList<>(params.entrySet());
        sortedParams.sort(Map.Entry.comparingByKey());

        // Construct the parameter string for signing
        StringBuilder signParamBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedParams) {
            if (signParamBuilder.length() > 0) {
                signParamBuilder.append('&');
            }
            try {
                signParamBuilder.append(entry.getKey())
                        .append('=')
                        .append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        // Append the appkey and generate the signature
        String signParam = signParamBuilder.toString();
        String signStr = signParam + appkey;

        // Calculate MD5 hash and convert to uppercase
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] md5Bytes = md.digest(signStr.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte md5Byte : md5Bytes) {
                String hex = Integer.toHexString(0xFF & md5Byte);
                if (hex.length() == 1) {
                    sb.append('0');
                }
                sb.append(hex);
            }
            return sb.toString().toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Get the UTC timestamp of the specified time zone
     *
     * @param timezoneStr
     * @return
     */
    private static long getTimestamp(String timezoneStr) {
        try {
            TimeZone timeZone = TimeZone.getTimeZone(timezoneStr);
            Calendar cal = Calendar.getInstance(timeZone);
            return cal.getTimeInMillis() / 1000;
        } catch (Exception e) {
            throw new RuntimeException("Error in obtaining timestamp", e);
        }
    }
}
