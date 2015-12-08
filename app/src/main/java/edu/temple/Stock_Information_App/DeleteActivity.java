package edu.temple.Stock_Information_App;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

public class DeleteActivity extends Activity {

    SQLiteDatabase db;
    StockDBHelper mDbHelper;
    ListView stockList;
    ArrayList<String> stocksDelete;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete);

        mDbHelper = new StockDBHelper(this);

        ArrayList<String> stocks = new ArrayList<String>();
        //stocksDelete = new ArrayList<String>();
        stocks = getStocks();
        stockList = ((ListView) findViewById(R.id.stockList));
        stockList.setAdapter(new DeleteAdapter(this, stocks.size(), stocks));

        DeleteAdapter adapter = (DeleteAdapter)stockList.getAdapter();
        stocksDelete = adapter.getStocksToDelete();

        findViewById(R.id.deleteButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int i = 0; i < stocksDelete.size(); i++){
                    Log.d("Going to Delete", stocksDelete.get(i).toString());
                    deleteData(stocksDelete.get(i).toString());
                    Intent launchActivityIntent = new Intent(DeleteActivity.this, MainActivity.class);
                    startActivity(launchActivityIntent);
                }

            }
        });
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

    private void deleteData(String symbol){

        // Gets the data repository in write mode
        db = mDbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        String selection = StockDBContract.StockEntry.COLUMN_NAME_SYMBOL + " = '" + symbol + "'";


        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.delete(
                StockDBContract.StockEntry.TABLE_NAME,
                selection,
                null);

        if (newRowId > 0) {
            Log.d("Stock data Deleted ", newRowId + " - " + symbol);
            //SimpleCursorAdapter adapter = (SimpleCursorAdapter) stockList.getAdapter();
            //Cursor cursor = db.query(StockDBContract.StockEntry.TABLE_NAME, new String[]{"_id", StockDBContract.StockEntry.COLUMN_NAME_SYMBOL, StockDBContract.StockEntry.COLUMN_NAME_PRICE}, null, null, null, null, null);

            //adapter.changeCursor(cursor);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
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

}
