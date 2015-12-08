package edu.temple.Stock_Information_App;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class JSONParse {

    List<Stock>returnList;


    public JSONParse(){

    }

    public List<Stock> getParseJsonWCF(String sName) {

        List<Stock> ListData = new ArrayList<Stock>();

        try {
            String temp=sName.replace(" ", "%20");
            URL js = new URL("http://dev.markitondemand.com/MODApis/Api/v2/Lookup/json?input=" + temp);

            URLConnection jc = js.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(jc.getInputStream()));
            String line = reader.readLine();
            Log.d("JSON:", line);
            JSONArray jsonArray = new JSONArray(line);
            for(int i = 0; i < jsonArray.length(); i++){
                JSONObject r = jsonArray.getJSONObject(i);
                Stock stock = new Stock(r.getString("Symbol"), r.getString("Name"));
                stock.setExchange(r.getString("Exchange"));
                ListData.add(stock);
                //ListData.add(new Stock(r.getString("Symbol"), r.getString("Name"), r.getString("Exchange")));

            }

            return ListData;

        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("Returning", "return list");
        return returnList;
    }

}
