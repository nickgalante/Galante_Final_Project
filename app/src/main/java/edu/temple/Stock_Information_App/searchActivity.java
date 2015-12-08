package edu.temple.Stock_Information_App;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

import edu.temple.Stock_Information_App.R;

public class searchActivity extends Activity {

    SQLiteDatabase db;
    StockDBHelper mDbHelper;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("ACTION_BROADCAST_QUOTE")){
                try {
                    Stock displayStock = new Stock(
                            new JSONObject(intent.getStringExtra("stock_data")).getJSONObject("list")
                                    .getJSONArray("resources")
                                    .getJSONObject(0)
                                    .getJSONObject("resource")
                                    .getJSONObject("fields"));
                    updateViews(displayStock);
                    Toast.makeText(searchActivity.this, "views updated", Toast.LENGTH_SHORT).show();
                } catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mDbHelper = new StockDBHelper(this);

        final AutoCompleteTextView acTextView = (AutoCompleteTextView) findViewById(R.id.autoComplete);
        acTextView.setAdapter(new SuggestionAdapter(this, acTextView.getText().toString()));

        // Register to receive intents
        IntentFilter filter = new IntentFilter();
        filter.addAction("ACTION_BROADCAST_QUOTE");
        registerReceiver(receiver, filter);

        findViewById(R.id.quoteButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String stock = acTextView.getText().toString();
                String[] parts = stock.split(" - ");
                final String stockSymbol = parts[0];
                Log.d("Stock Symbol", stockSymbol);

                //final String stockSymbol = stock
                Thread t = new Thread() {
                    @Override
                    public void run() {

                        URL stockQuoteUrl;

                        try {

                            stockQuoteUrl = new URL("http://finance.yahoo.com/webservice/v1/symbols/" + stockSymbol + "/quote?format=json");

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
                            Log.d("Stock response", stockObject.toString());
                            Message msg = Message.obtain();
                            msg.obj = stockObject;
                            stockResponseHandler.sendMessage(msg);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                t.start();

                /*Intent serviceIntent = new Intent(searchActivity.this, QuoteService.class);
                serviceIntent.putExtra("stock_symbol"
                        , ((EditText) findViewById(R.id.autoComplete)).getText().toString());
                startService(serviceIntent);*/

            }
        });
    }

    Handler stockResponseHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {

            JSONObject responseObject = (JSONObject) msg.obj;

            try {
                Stock stock = new Stock(responseObject.getJSONObject("list")
                        .getJSONArray("resources")
                        .getJSONObject(0)
                        .getJSONObject("resource")
                        .getJSONObject("fields"));

                stock.setPrice(responseObject.getJSONObject("list")
                        .getJSONArray("resources")
                        .getJSONObject(0)
                        .getJSONObject("resource")
                        .getJSONObject("fields")
                        .getDouble("price"));

                Toast.makeText(searchActivity.this, stock.getName().toString() + " $" + String.valueOf(stock.getPrice()) + " " + stock.getSymbol().toString(), Toast.LENGTH_SHORT).show();
                Log.d("Price", String.valueOf(stock.getPrice()));

                saveData(stock.getSymbol(), stock.getName(), stock.getPrice());
            } catch (Exception e) {
                e.printStackTrace();
            }


            return false;
        }
    });

    private void saveData(String symbol, String company, double price){

        // Gets the data repository in write mode
        db = mDbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(StockDBContract.StockEntry.COLUMN_NAME_SYMBOL, symbol);
        values.put(StockDBContract.StockEntry.COLUMN_NAME_COMPANY, company);
        values.put(StockDBContract.StockEntry.COLUMN_NAME_PRICE, price);

        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(
                StockDBContract.StockEntry.TABLE_NAME,
                null,
                values);

        if (newRowId > 0) {
            Log.d("Stock data saved ", newRowId + " - " + company);
            //populateListView();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateViews(Stock currentStock) {
        Toast.makeText(searchActivity.this, "updating views", Toast.LENGTH_SHORT).show();
        ((TextView)findViewById(R.id.companyName)).setText(currentStock.getName());
        ((TextView)findViewById(R.id.stockPrice)).setText(String.valueOf("$" + currentStock.getExchange()));
    }
}
