package edu.temple.Stock_Information_App;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import java.util.List;


public class Portfolio_fragment extends Fragment {

    SQLiteDatabase db;
    StockDBHelper mDbHelper;
    AutoCompleteTextView acTextView;
    boolean serviceRunning = false;

    ListView stockList;

    private OnFragmentInteractionListener mListener;

    public static Portfolio_fragment newInstance() {
        return new Portfolio_fragment();
    }

    public Portfolio_fragment() {
        // Required empty public constructor
    }

    // Respond to various notification messages
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("ACTION_BROADCAST_QUOTE")) {
                ArrayList<String> mList;
                mList = intent.getStringArrayListExtra("stock_data");
                for (int i = 0; i < mList.size(); i++) {
                    try {
                        Stock displayStock = new Stock(
                                new JSONObject(mList.get(i).toString()).getJSONObject("list")
                                        .getJSONArray("resources")
                                        .getJSONObject(0)
                                        .getJSONObject("resource")
                                        .getJSONObject("fields"));
                        displayStock.setPrice(new JSONObject(mList.get(i).toString()).getJSONObject("list")
                                .getJSONArray("resources")
                                .getJSONObject(0)
                                .getJSONObject("resource")
                                .getJSONObject("fields")
                                .getDouble("price"));

                        updateData(displayStock.getSymbol(), displayStock.getName(), displayStock.getPrice());

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mDbHelper = new StockDBHelper(getActivity());


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_portfolio, container, false);

        // Register to receive intents
        IntentFilter filter = new IntentFilter();
        filter.addAction("ACTION_BROADCAST_QUOTE");
        getActivity().registerReceiver(receiver, filter);

        //Populate the portfolio list
        db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.query(StockDBContract.StockEntry.TABLE_NAME, new String[]{"_id", StockDBContract.StockEntry.COLUMN_NAME_SYMBOL, StockDBContract.StockEntry.COLUMN_NAME_PRICE}, null, null, null, null, null);
        final SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(), R.layout.db_layout, cursor, new String[]{StockDBContract.StockEntry.COLUMN_NAME_SYMBOL, StockDBContract.StockEntry.COLUMN_NAME_PRICE}, new int[]{R.id.symbol, R.id.price}, 0);
        stockList = ((ListView) view.findViewById(R.id.stock_list));
        stockList.setAdapter(adapter);

        //Autocomplete view
        acTextView = (AutoCompleteTextView) view.findViewById(R.id.autoComplete);
        acTextView.setAdapter(new SuggestionAdapter(getActivity(), acTextView.getText().toString()));

        ArrayList<String> stocks = new ArrayList<>();
        for (int i = 0; i < stockList.getAdapter().getCount(); i++) {
            Cursor item = (Cursor) adapter.getItem(i);
            stocks.add(item.getString(1));
            Log.d("Stock in list", item.getString(1));
        }

        //Starting the Service
        Intent serviceIntent = new Intent(getActivity(), QuoteService.class);
        if (serviceRunning == false) {
            getActivity().startService(serviceIntent);
            serviceRunning = true;
            Log.d("staring", "service");
        }


        mListener.generateNews(stocks);

        stockList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                Cursor cursor = (Cursor) adapter.getItem(arg2);

                Log.v("TAG", "CLICKED row number: " + arg2 + " " + cursor.getString(1));
                mListener.generateStockDetails(cursor.getString(1));
            }

        });

        //View Quote Details
        view.findViewById(R.id.quoteButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String stock = acTextView.getText().toString();
                String[] parts = stock.split(" - ");
                final String stockSymbol = parts[0];
                Log.d("Stock Symbol", stockSymbol);

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

            }
        });


        return view;
    }

    Handler stockResponseHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {

            JSONObject responseObject = (JSONObject) msg.obj;

            try {
                final Stock stock = new Stock(responseObject.getJSONObject("list")
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

                new AlertDialog.Builder(getActivity())
                        .setTitle(stock.getName().toString())
                        .setMessage("$" + String.valueOf(stock.getPrice()) + "\n" + stock.getSymbol().toString())
                        .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                saveData(stock.getSymbol(), stock.getName(), stock.getPrice());
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //do nothing
                            }
                        })
                        .setIcon(R.drawable.ic_launcher)
                        .show();
                acTextView.setText("");


            } catch (Exception e) {
                e.printStackTrace();
            }


            return false;
        }
    });


    //Saving Stocks to the database
    private void saveData(String symbol, String company, double price) {

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
            populateListView();
            Fragment frg = null;
            frg = getFragmentManager().findFragmentById(R.id.fragment_portfolio);
            final FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.detach(frg);
            ft.attach(frg);
            ft.commit();
        }

    }

    //Updating the Database
    private void updateData(String symbol, String company, double price) {

        // Gets the data repository in write mode
        db = mDbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        String selection = StockDBContract.StockEntry.COLUMN_NAME_SYMBOL + " = '" + symbol + "'";
        values.put(StockDBContract.StockEntry.COLUMN_NAME_PRICE, price);

        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.update(
                StockDBContract.StockEntry.TABLE_NAME,
                values,
                selection,
                null);

        if (newRowId > 0) {
            Log.d("Stock data updated ", newRowId + " - " + company);
            SimpleCursorAdapter adapter = (SimpleCursorAdapter) stockList.getAdapter();
            Cursor cursor = db.query(StockDBContract.StockEntry.TABLE_NAME, new String[]{"_id", StockDBContract.StockEntry.COLUMN_NAME_SYMBOL, StockDBContract.StockEntry.COLUMN_NAME_PRICE}, null, null, null, null, null);
            adapter.changeCursor(cursor);

        }

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void generateStockDetails(String symbol);

        void generateNews(ArrayList<String> stocks);
    }

    private void populateListView() {
        db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.query(StockDBContract.StockEntry.TABLE_NAME, new String[]{"_id", StockDBContract.StockEntry.COLUMN_NAME_SYMBOL, StockDBContract.StockEntry.COLUMN_NAME_PRICE}, null, null, null, null, null);
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(), R.layout.db_layout, cursor, new String[]{StockDBContract.StockEntry.COLUMN_NAME_SYMBOL, StockDBContract.StockEntry.COLUMN_NAME_PRICE}, new int[]{R.id.symbol, R.id.price}, 0);
        stockList.setAdapter(adapter);
    }


}
