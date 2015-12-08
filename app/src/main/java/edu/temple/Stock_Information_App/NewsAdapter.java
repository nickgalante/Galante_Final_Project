package edu.temple.Stock_Information_App;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class NewsAdapter extends BaseAdapter {
    Context context;
    private int count;
    private int positionIncrement = 0;
    int row = 0;

    public NewsAdapter(Context context, int size){
        this.context = context;
        this.count = size;
    }

    @Override
    public int getCount() {
        return this.count;
    }

    @Override
    public Object getItem(int position) {
        return 0;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        TextView square = new TextView(context);

        Resources res = context.getResources();
        //String[] gridLabels = res.getStringArray(R.array.checker_numbers);

        //square.setText(String.valueOf(String.valueOf(gridLabels[position])));

        square.setGravity(Gravity.CENTER);
        square.setHeight(70);


        if ((position+row) % 2 == 0) {
            square.setBackgroundColor(Color.BLACK);
            square.setTextColor(Color.WHITE);

        }
        else {
            square.setBackgroundColor(Color.WHITE);
            square.setTextColor(Color.BLACK);
        }

        if (positionIncrement == Math.sqrt(count)) {
            positionIncrement = 0;
            row++;
        }

        positionIncrement++;

        return square;
    }
}
