package com.apptastic.fininsyn.bitly;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class BitlyClient {
    private final String token;
    public String finalUrl = "";
    private String longUrl = "";

    public BitlyClient(String theLongUrl, String token)
    {
        this.token = token;
        this.finalUrl = theLongUrl;
        this.longUrl = theLongUrl;
    }

    public void shortenLink()
    {
        final String theURLString =  "https://api-ssl.bitly.com/v4/bitlinks";
        String basicAuth = "Bearer " + token;

        JSONObject theJsonObjResp = null;
        HttpURLConnection connection = null;
        try
        {
            JSONObject inputJSON = new JSONObject();
            inputJSON.put("long_url", this.longUrl);
            String inputPostString = inputJSON.toString();

            URL theURL = new URL(theURLString);
            connection = (HttpURLConnection) theURL.openConnection();
            connection.setRequestProperty("Authorization", basicAuth);
            connection.setRequestProperty("Content-Type","application/json");
            connection.setRequestProperty("Accept","application/json");
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(25000);
            connection.setFixedLengthStreamingMode(inputPostString.getBytes().length);
            connection.connect();

            //Send request
            DataOutputStream wr = new DataOutputStream (connection.getOutputStream ());
            wr.write(inputPostString.getBytes());
            wr.flush ();
            wr.close ();

            //Get Response
            int status = connection.getResponseCode();

            String jsonString = "";
            switch (status) {
                case 200:
                case 201:
                case 202:
                    //read successful response into string
                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line+"\n");
                    }
                    br.close();
                    jsonString = sb.toString();
                    break;
                case 400:
                case 401:
                case 402:
                case 403:
                case 404:
                case 500:
                    //handle each of the different failed responses properly via https://dev.bitly.com/v4_documentation.html#section
                    throw new Exception("status " + status);
            }

            try
            {
                //convert json string into json object
                JSONParser parser = new JSONParser();
                Object parsedObj = parser.parse(jsonString);
                theJsonObjResp =(JSONObject)parsedObj;
                String shortenedLinkString = scrubString ((String) theJsonObjResp.get("link"));
                //Utilities.log("shortened link = " + shortenedLinkString);
                if (shortenedLinkString.length() > 0)
                    this.finalUrl = shortenedLinkString;
            }
            catch (Exception e)
            {
                throw new Exception("Failed to parse json!");
            }

        }
        catch (Exception e)
        {
            //Utilities.logException(e, null);
        }
        finally
        {
            if (connection != null) {
                connection.disconnect();
            }
        }

    }

    private static String scrubString(String input)
    {
        if (input == null)
            return "";
        else
            return input;
    }

}

