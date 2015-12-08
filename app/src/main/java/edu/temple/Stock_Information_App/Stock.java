package edu.temple.Stock_Information_App;

import org.json.JSONException;
import org.json.JSONObject;

public class Stock {
    private String name, symbol;
    private String exchange;
    private double price;

    public Stock(String symbol, String name, String exchange) {
        this.name = name;
        this.symbol = symbol;
        this.exchange = exchange;
    }

    public Stock(String symbol, String name, String exchange, double price) {
        this.name = name;
        this.symbol = symbol;
        this.price = price;
    }

    public Stock(String symbol, String name) {
        this.name = name;
        this.symbol = symbol;
    }

    public Stock(JSONObject stockObject) throws JSONException{
        this(stockObject.getString("symbol"), stockObject.getString("name"));
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }




    @Override
    public boolean equals(Object object){
        return (object instanceof Stock) &&
            this.symbol.equalsIgnoreCase(((Stock)object).symbol);
    }

    public JSONObject getStockAsJSON(){
        JSONObject stockObject = new JSONObject();
        try {
            stockObject.put("name", name);
            stockObject.put("symbol", symbol);
            stockObject.put("exchange", exchange);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return stockObject;
    }
}
