package edu.temple.Stock_Information_App;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class Details_fragment extends Fragment {

    TextView stockTitle;
    ImageView stockChart;

    TextView currentPrice;
    TextView percentChange;
    TextView openingPrice;
    TextView volume;

    public static Details_fragment newInstance() {
        return new Details_fragment();
    }

    public Details_fragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        Bundle bundle = this.getArguments();
        final View v;
        if (bundle != null) {
            if (bundle.containsKey("symbol")) {
                v = inflater.inflate(R.layout.fragment_details, container, false);
            } else {
                v = inflater.inflate(R.layout.fragment_news, container, false);
            }
        }
        else{
            v = inflater.inflate(R.layout.fragment_news, container, false);
            Log.d("Fragment", "news NO ARGUMENTS");
        }



        stockTitle = (TextView) v.findViewById(R.id.companyName);
        stockChart = (ImageView) v.findViewById(R.id.stockChart);
        currentPrice = (TextView) v.findViewById((R.id.currentPrice));
        percentChange = (TextView) v.findViewById((R.id.percentChange));
        openingPrice = (TextView) v.findViewById((R.id.openingPrice));
        volume = (TextView) v.findViewById((R.id.volume));


        if ((bundle != null) && (getArguments().containsKey("symbol"))) {
            final String stockSymbol = getArguments().getString("symbol");
            Picasso.with(getActivity()).load("https://chart.yahoo.com/z?t=1d&s=" + stockSymbol).resize(900, 600).into(stockChart);

            v.findViewById(R.id.oneDay).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Picasso.with(getActivity()).load("https://chart.yahoo.com/z?t=1d&s=" + stockSymbol).resize(900, 600).into(stockChart);
                }
            });

            v.findViewById(R.id.fiveDay).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Picasso.with(getActivity()).load("https://chart.yahoo.com/z?t=5d&s=" + stockSymbol).resize(900, 600).into(stockChart);
                }
            });

            v.findViewById(R.id.oneMonth).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Picasso.with(getActivity()).load("https://chart.yahoo.com/z?t=1m&s=" + stockSymbol).resize(900, 600).into(stockChart);
                }
            });

            v.findViewById(R.id.sixMonth).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Picasso.with(getActivity()).load("https://chart.yahoo.com/z?t=6m&s=" + stockSymbol).resize(900, 600).into(stockChart);
                }
            });

            v.findViewById(R.id.oneYear).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Picasso.with(getActivity()).load("https://chart.yahoo.com/z?t=1y&s=" + stockSymbol).resize(900, 600).into(stockChart);
                }
            });

            String newsURL = "http://finance.yahoo.com/rss/headline?s=" + stockSymbol;
            final HandleXML obj;
            obj = new HandleXML(newsURL);
            obj.fetchXML();
            while (obj.parsingComplete) ;
            /*for (int i = 0; i < obj.getNewsList().size(); i++) {
                Log.d("Tag", obj.getNewsList().get(i).getTitle());
            }*/
            LinearLayout linearLayout = (LinearLayout) v.findViewById(R.id.newsHolder);

            final TextView tv[] = new TextView[obj.getNewsList().size()];

            View.OnClickListener listener = new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    final int id = v.getId();
                    Log.d("clicked", Integer.toString(id));
                    //obj.getNewsList().get(id).getLink();
                    Log.d("clicked", obj.getNewsList().get(id).getLink());
                    Uri uriUrl = Uri.parse(obj.getNewsList().get(id).getLink());
                    Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                    startActivity(launchBrowser);


                }
            };

            for (int i = 0; i < obj.getNewsList().size(); i++) {
                tv[i] = new TextView((getActivity()));
                tv[i].setText(obj.getNewsList().get(i).getTitle());
                tv[i].setId(i);
                tv[i].setPadding(0, 40, 0, 0);
                linearLayout.addView(tv[i]);
                tv[i].setOnClickListener(listener);

            }


            Thread t = new Thread() {
                @Override
                public void run() {

                    URL stockQuoteUrl;
                    String stockSymbol = getArguments().getString("symbol");


                    try {

                        stockQuoteUrl = new URL("http://finance.yahoo.com/webservice/v1/symbols/" + stockSymbol + "/quote?format=json&view=basic");

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
        else if((getArguments() != null) && getArguments().containsKey("stocks")){
            String newsURL = "http://finance.yahoo.com/rss/headline?s=";
            ArrayList<String> stocks = getArguments().getStringArrayList("stocks");
            for(int i = 0; i < stocks.size(); i++){
                newsURL = newsURL + stocks.get(i).toString() + "+";

            }
            Log.d("news URL", newsURL);
            final HandleXML obj;
            obj = new HandleXML(newsURL);
            obj.fetchXML();
            while (obj.parsingComplete) ;
            /*for (int i = 0; i < obj.getNewsList().size(); i++) {
                Log.d("Tag", obj.getNewsList().get(i).getTitle());
            }*/
            LinearLayout linearLayout = (LinearLayout) v.findViewById(R.id.newsHolder);

            final TextView tv[] = new TextView[obj.getNewsList().size()];

            View.OnClickListener listener = new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    final int id = v.getId();
                    Log.d("clicked", Integer.toString(id));
                    //obj.getNewsList().get(id).getLink();
                    Log.d("clicked", obj.getNewsList().get(id).getLink());
                    Uri uriUrl = Uri.parse(obj.getNewsList().get(id).getLink());
                    Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                    startActivity(launchBrowser);


                }
            };

            for (int i = 0; i < obj.getNewsList().size(); i++) {
                tv[i] = new TextView((getActivity()));
                tv[i].setText(obj.getNewsList().get(i).getTitle());
                tv[i].setId(i);
                tv[i].setPadding(0, 40, 0, 0);
                linearLayout.addView(tv[i]);
                tv[i].setOnClickListener(listener);

            }
        }
        return v;

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


                double price = responseObject.getJSONObject("list")
                        .getJSONArray("resources")
                        .getJSONObject(0)
                        .getJSONObject("resource")
                        .getJSONObject("fields")
                        .getDouble("price");

                double change = responseObject.getJSONObject("list")
                        .getJSONArray("resources")
                        .getJSONObject(0)
                        .getJSONObject("resource")
                        .getJSONObject("fields")
                        .getDouble("change");

                double openPrice = price - change;

                stock.setPrice(price);

                currentPrice.setText(String.valueOf(stock.getPrice()));
                percentChange.setText(responseObject.getJSONObject("list")
                        .getJSONArray("resources")
                        .getJSONObject(0)
                        .getJSONObject("resource")
                        .getJSONObject("fields")
                        .getString("chg_percent"));

                openingPrice.setText(Double.toString(openPrice).substring(0, 5));
                volume.setText(responseObject.getJSONObject("list")
                        .getJSONArray("resources")
                        .getJSONObject(0)
                        .getJSONObject("resource")
                        .getJSONObject("fields")
                        .getString("volume"));


                stockTitle.setText(stock.getName().toString());

                //Toast.makeText(getActivity(), stock.getName().toString() + " $" + String.valueOf(stock.getPrice()) + " " + stock.getSymbol().toString(), Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                e.printStackTrace();
            }

            return false;
        }
    });


}

