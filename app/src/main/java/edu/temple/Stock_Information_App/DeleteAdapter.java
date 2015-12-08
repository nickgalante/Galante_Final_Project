package edu.temple.Stock_Information_App;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class DeleteAdapter extends BaseAdapter {

    Context c;
    int count = 6;
    ArrayList<String> stocks;
    ArrayList<String> returnStocks = new ArrayList<>();


    public DeleteAdapter(Context c, int count, ArrayList<String> stocks){
        this(c);
        this.count = count;
        this.stocks = stocks;
    }


    public DeleteAdapter(Context c){
        this.c = c;
    }


    //  Return the total amount of items available. This might the size of an array.
    @Override
    public int getCount() {
        return count;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    public ArrayList<String> getStocksToDelete(){
        return returnStocks;
    }


    //  Build and return the view for a single item that will be displayed in the adapterview
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        RadioButton radioButton = new RadioButton(c);
        TextView textView = new TextView(c);

        textView.setText(stocks.get(i).toString());

        //  Give this item an ID or value so it can be retrived later

        textView.setId(i);
        final String symbol = stocks.get(i).toString();
        radioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("TAG", "selected: " + symbol);
                returnStocks.add(symbol);
            }
        });

        LinearLayout ll = new LinearLayout(c);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.addView(radioButton);
        ll.addView(textView);

        return ll;
    }
}
