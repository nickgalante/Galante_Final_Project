package edu.temple.Stock_Information_App;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.SimpleCursorAdapter;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class QuoteService extends Service {

    SQLiteDatabase db;
    StockDBHelper mDbHelper;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId){

    Log.d("Started", "service");
        mDbHelper = new StockDBHelper(this);

        //Bundle extra = intent.getBundleExtra("stock_symbols");
        //ArrayList<String> stocks = (ArrayList<String>) extra.getSerializable("objects");
        //Log.d("stocks in service", stocks.get(1).toString());
        ArrayList<String> stocks = new ArrayList<String>();
        stocks = getStocks();
        getQuote(stocks);

        return START_STICKY;
    }



    @Override
    public void onCreate(){
        //startForeground();
    }

    public void getQuote(final ArrayList<String> stocks) {
        //timer.scheduleAtFixedRate(new mainTask(), 0, 5000);

        final ArrayList<String> JSONResponse = new ArrayList();
        Thread t = new Thread() {
            @Override
            public void run() {
                while(true) {

                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    URL stockQuoteUrl;

                    try {
                        for (int i = 0; i < stocks.size(); i++) {

                            stockQuoteUrl = new URL("http://finance.yahoo.com/webservice/v1/symbols/" + stocks.get(i).toString() + "/quote?format=json");

                            BufferedReader reader = new BufferedReader(
                                    new InputStreamReader(
                                            stockQuoteUrl.openStream()));

                            String response = "", tmpResponse;

                            tmpResponse = reader.readLine();
                            while (tmpResponse != null) {
                                response = response + tmpResponse;
                                tmpResponse = reader.readLine();
                            }

                            JSONObject stockObject = new JSONObject(response);
                            JSONResponse.add(stockObject.toString());
                            Log.d("SERVICE", "updating stock");
                            //Thread.sleep(1000);

                        }
                        Intent broadcastIntent = new Intent();
                        broadcastIntent.setAction("ACTION_BROADCAST_QUOTE");
                        broadcastIntent.putStringArrayListExtra("stock_data", JSONResponse);
                        sendBroadcast(broadcastIntent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        };
        t.start();
    }

    private ArrayList<String> getStocks() {
        db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.query(StockDBContract.StockEntry.TABLE_NAME, new String[]{"_id", StockDBContract.StockEntry.COLUMN_NAME_SYMBOL, StockDBContract.StockEntry.COLUMN_NAME_PRICE}, null, null, null, null, null);

        ArrayList<String> stocks = new ArrayList<>();
        while(cursor.moveToNext()) {
            stocks.add(cursor.getString(cursor.getColumnIndex(StockDBContract.StockEntry.COLUMN_NAME_SYMBOL))); //add the item
        }

        for(int i = 0; i < stocks.size(); i++){
            Log.d("Queried Database", stocks.get(i).toString());
        }
        return stocks;
    }

    private void startForeground(){
        Notification.Builder n;

        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.setAction("SOME_ACTION");
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, i, 0);
        n  = new Notification.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Your service is running")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher))
                .setContentIntent(pIntent)
                .setAutoCancel(false);

        startForeground(1234, n.build());
    }
}
