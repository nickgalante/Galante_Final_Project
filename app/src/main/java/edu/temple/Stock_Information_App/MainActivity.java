package edu.temple.Stock_Information_App;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements Portfolio_fragment.OnFragmentInteractionListener {

    boolean twoPanes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //  Determine if only one or two panes are visible
        twoPanes = (findViewById(R.id.fragment_details) != null);

        //  Load navigation fragment by default

        loadFragment(R.id.fragment_portfolio, new Portfolio_fragment(), false);

        /*
         *  Check if details pain is visible in current layout (e.g. large or landscape)
         *  and load fragment if true.
         */
        if (twoPanes) {

            loadFragment(R.id.fragment_details, new Details_fragment(), false);
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
        if (id == R.id.search) {
            Intent launchActivityIntent = new Intent(MainActivity.this, searchActivity.class);
            startActivity(launchActivityIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadFragment(int paneId, Fragment fragment, boolean placeOnBackstack) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction()
                .replace(paneId, fragment);
        if (placeOnBackstack)
            ft.addToBackStack(null);
        ft.commit();

        //  Ensure fragment is attachecd before attempting to call its public methods
        fm.executePendingTransactions();
    }

    @Override
    public void generateStockDetails(String symbol) {
        Details_fragment detailsFragment = new Details_fragment();

        Bundle bundle = new Bundle();
        bundle.putString("symbol", symbol);
        detailsFragment.setArguments(bundle);

        loadFragment(twoPanes ? R.id.fragment_details : R.id.fragment_portfolio, detailsFragment, !twoPanes);

        Log.d("TAG", "generate stock details fragment");
    }

    @Override
    public void generateNews(ArrayList<String> stocks) {
        Details_fragment detailsFragment = new Details_fragment();

        Bundle bundle = new Bundle();
        bundle.putStringArrayList("stocks", stocks);
        detailsFragment.setArguments(bundle);


        if (twoPanes) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(twoPanes ? R.id.fragment_details : R.id.fragment_portfolio, detailsFragment);
            ft.addToBackStack(null);
            ft.commit();
        }

        Log.d("TAG", "generate NEWS fragment");

    }

}
