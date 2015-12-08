package edu.temple.Stock_Information_App;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class HandleXML {

    private String link;
    private String title;
    private String urlString = null;
    private XmlPullParserFactory xmlFactoryObject;
    public volatile boolean parsingComplete = true;

    List<News> newsList = new ArrayList<>();
    private News item;

    public HandleXML(String url) {
        this.urlString = url;
    }

    public List<News> getNewsList() {
        return newsList;
    }

    public String getLink() {
        return link;
    }

    public String getTitle() {
        return title;
    }


    public void parseXMLAndStoreIt(XmlPullParser myParser) {
        int event;
        String text = null;
        try {
            event = myParser.getEventType();

            while (event != XmlPullParser.END_DOCUMENT) {
                String name = myParser.getName();

                switch (event) {
                    case XmlPullParser.START_TAG:
                        if (name.equalsIgnoreCase("item")) {
                            // create a new instance of News
                            item = new News();
                        }
                        break;

                    case XmlPullParser.TEXT:
                        text = myParser.getText();
                        break;

                    case XmlPullParser.END_TAG:
                        if (name.equals("item")) {
                            if (item != null) {
                                newsList.add(item);
                            }
                        } else if (name.equals("title")) {
                            if (item != null) {
                                item.setTitle(text);
                            }
                        } else if (name.equals("link")) {
                            text = text.replace("%3A", ":");
                            if (text.contains("*")) {
                                text = text.substring((text.lastIndexOf('*')+1));
                            }
                            if (item != null) {
                                item.setLink(text);
                            }
                        } else {
                        }
                        break;
                }

                event = myParser.next();
            }
            parsingComplete = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void fetchXML() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    URL url = new URL(urlString);

                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(
                                    url.openStream()));

                    String response = "", tmpResponse;

                    tmpResponse = reader.readLine();
                    while (tmpResponse != null) {
                        response = response + tmpResponse;
                        tmpResponse = reader.readLine();
                    }

                    xmlFactoryObject = XmlPullParserFactory.newInstance();
                    XmlPullParser myparser = xmlFactoryObject.newPullParser();

                    myparser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                    myparser.setInput(new StringReader(response));

                    parseXMLAndStoreIt(myparser);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
}
