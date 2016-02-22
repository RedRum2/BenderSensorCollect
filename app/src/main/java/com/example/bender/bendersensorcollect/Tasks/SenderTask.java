package com.example.bender.bendersensorcollect.Tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;

import com.example.bender.bendersensorcollect.database.DbAdapter;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Objects;


public class SenderTask extends AsyncTask <Void,Void,Void> {
    private DbAdapter db;
    private Context context;
    private Cursor cursor;
    private static int position;
    public static final String MY_PREFS_NAME = "MyPrefsFile";


    public SenderTask(DbAdapter dbAdapter, Context c) {
        super();
        db=dbAdapter;
        context = c;
    }

    @Override
    protected Void doInBackground(Void... params) {

        //Retrieve position from SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        position = prefs.getInt("position", 0);

        String msgResp = sendPostMsg();
        if (Objects.equals(msgResp, "1")) {
            checkdbdimension();
            position = cursor.getPosition();
//            cursor.close();


            SharedPreferences.Editor editor = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE).edit();
            editor.putInt("position", position);
            editor.apply();

        }

        return null;
    }

    private String sendPostMsg() {
        StringBuilder sb = null;
        try {
            URL paginaurl = new URL("http://www.tottoryan.altervista.org/SensorServerPost.php");
            HttpURLConnection client = (HttpURLConnection) paginaurl.openConnection();

            //CREARW LA STRINGA DI INVIO
            String datipost = fillData();

            client.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(client.getOutputStream());
            wr.write(datipost);
            wr.flush();
            InputStream risposta= new BufferedInputStream(client.getInputStream());
            sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(risposta));
            String nextline;
            while ((nextline = reader.readLine()) != null)
                sb.append(nextline);

        } catch (IOException e) {
            e.printStackTrace();
        }

        assert sb != null;
        return sb.toString();
    }

    private String fillData() {

        db.open();
        cursor = db.fetchAllSensorData();

        String data = "";
        int counter = 0;

        if (cursor.moveToPosition(position)) {
            int _idIndex = cursor.getColumnIndex("_id");
            int dateIndex = cursor.getColumnIndex("date");
            int timeIndex = cursor.getColumnIndex("time");
            int deviceIndex = cursor.getColumnIndex("device");
            int dataIndex = cursor.getColumnIndex("data");
            do {
                counter++;
                String i = String.valueOf(counter);
                try {
                    data += "i" + i + "='" + URLEncoder.encode(cursor.getString(_idIndex),"UTF-8") + "'&";
                    data += "d" + i + "='" + URLEncoder.encode(cursor.getString(dateIndex),"UTF-8") + "'&";
                    data += "t" + i + "='" + URLEncoder.encode(cursor.getString(timeIndex),"UTF-8") + "'&";
                    data += "de" + i + "='" + URLEncoder.encode(cursor.getString(deviceIndex),"UTF-8") + "'&";
                    data += "da" + i + "='" + URLEncoder.encode(cursor.getString(dataIndex),"UTF-8") + "'&";
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            } while (cursor.moveToNext());
        }

        data += "n=" + counter;
//        db.close();
        return data;
    }


    private void checkdbdimension()
    {
        if(cursor.getPosition() > 1000)
        {
            db.open();
            db.delete();
//            db.close();
            cursor.moveToFirst();
        }

    }
}