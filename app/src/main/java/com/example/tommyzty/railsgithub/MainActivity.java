package com.example.tommyzty.railsgithub;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    public static final int MY_PERMISSIONS_REQUEST_INTERNET = 999;
    private final IssueAdapter adapter = new IssueAdapter(this, new ArrayList<Issue>());
    private ArrayList<Issue> issues = new ArrayList<>();
    private ListView listView;
    private View popupView;
    private PopupWindow popupWindow;
    private int temp_pos;
    private Issue current;
    private String curr_comment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.listview);
        TextView columnHeader0 = (TextView) findViewById(R.id.column_header0);
        TextView columnHeader1 = (TextView) findViewById(R.id.column_header1);
        TextView columnHeader2 = (TextView) findViewById(R.id.column_header2);
        String number = "Number";
        String title = "Title";
        String body = "Issue (First 140 Chars)";
        columnHeader0.setText(number);
        columnHeader1.setText(title);
        columnHeader2.setText(body);
        String API_URL = "https://api.github.com/repos/rails/rails/issues";

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) {
            ConnectivityManager connMgr = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                try {
                    new NetworkConnection().execute(API_URL);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Network Connection Error!", Toast.LENGTH_LONG).show();
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET},
                    MY_PERMISSIONS_REQUEST_INTERNET);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_INTERNET: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(getApplicationContext(), "Network Access Needed for this APP!", Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.INTERNET},
                            MY_PERMISSIONS_REQUEST_INTERNET);
                }
            }
        }
    }

    public void dismissPopup(View view) {
        popupWindow.dismiss();
    }

    public void handleResult(ArrayList<JSONObject> result) {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        try {
            int i = 0;
            while (i < result.size()) {
                JSONObject jObject = result.get(i);
                int number = jObject.getInt("number");
                String title = jObject.getString("title");
                String body = jObject.getString("body");
                String date_string = jObject.getString("date");
                Date date = null;
                try {
                    date = formatter.parse(date_string);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                String comments_url = jObject.getString("comments_url");
                Issue issue = new Issue(number, title, body, comments_url, date);
                issues.add(issue);
                i++;
            }
            i = 0;
            adapter.clear();
            while (i < issues.size()) {
                adapter.insert(issues.get(i));
                i++;
            }
            adapter.sort();
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(onListClick);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void commentResult(ArrayList<JSONObject> result) {
        int j = 0;
        String comment = "";
        while (j < result.size()) {
            JSONObject comment_obj = result.get(j);
            String user = "";
            String comment_body = "";
            try {
                user = comment_obj.getString("user");
                comment_body = comment_obj.getString("body");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            comment = comment + "Username: " + user + "\nComment: " + comment_body + "\n\n";
        }
        curr_comment = comment;
    }

    void showPopup() {
        LayoutInflater layoutInflater = (LayoutInflater) getBaseContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        popupView = layoutInflater.inflate(R.layout.popup,
                (ViewGroup) findViewById(R.id.popup));
        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
        TextView comment = (TextView) popupView.findViewById(R.id.comment_text);
        String text = current.getComments();
        comment.setText(text);
    }

    private final AdapterView.OnItemClickListener onListClick = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            temp_pos = position;
            current = adapter.getItem(position);
            showPopup();
        }
    };

    /*
    * A Class for HTTP GET method
    * Retrieve JSON files from an API
    */
    private class NetworkConnection extends AsyncTask<String, Void, ArrayList<JSONObject>> {
        private String curr_url;

        @Override
        protected ArrayList<JSONObject> doInBackground(String... urls) {
            System.out.println("Sending Http GET request");
            try {
                curr_url = urls[0];
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return null;
            }
        }

        // displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(ArrayList<JSONObject> result) {
            if (curr_url == "https://api.github.com/repos/rails/rails/issues") {
                handleResult(result);
            } else {
                commentResult(result);
            }
        }

        private ArrayList<JSONObject> downloadUrl(String myUrl) throws IOException {
            InputStream is = null;
            try {
                URL url = new URL(myUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                // milliseconds
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoInput(true);
                conn.connect();
                int response = conn.getResponseCode();
                System.out.println("The response is: " + response);
                is = conn.getInputStream();
                return readJsonStream(is, myUrl);
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }

        ArrayList<JSONObject> readJsonStream(InputStream in, String url) throws IOException {
            JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
            ArrayList<JSONObject> jArray = new ArrayList<>();
            try {
                reader.beginArray();
                while (reader.hasNext()) {
                    jArray.add(parseJSON(reader, url));
                }
                reader.endArray();
            } finally {
                reader.close();
            }
            return jArray;
        }

        JSONObject parseJSON(JsonReader reader, String url) throws IOException {
            JSONObject jObject = new JSONObject();
            reader.beginObject();
            if (url == "https://api.github.com/repos/rails/rails/issues") {
                while (reader.hasNext()) {
                    String name = reader.nextName();
                    if (name.equals("number")) {
                        try {
                            jObject.put("number", reader.nextInt());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if (name.equals("body")) {
                        try {
                            jObject.put("body", reader.nextString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if (name.equals("title")) {
                        try {
                            jObject.put("title", reader.nextString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if (name.equals("updated_at")) {
                        try {
                            jObject.put("date", reader.nextString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if (name.equals("comments_url")) {
                        try {
                            jObject.put("comments_url", reader.nextString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        reader.skipValue();
                    }
                }
            } else {
                while (reader.hasNext()) {
                    String name = reader.nextName();
                    if (name.equals("login")) {
                        try {
                            jObject.put("user", reader.nextString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if (name.equals("body")) {
                        try {
                            jObject.put("body", reader.nextString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        reader.skipValue();
                    }
                }
            }
            reader.endObject();
            return jObject;
        }
    }
}

