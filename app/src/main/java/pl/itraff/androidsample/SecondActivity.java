package pl.itraff.androidsample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.Toast;
import pl.itraff.androidsample.ExpandableListAdapter;
import pl.itraff.androidsample.R;

public class SecondActivity extends Activity{

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;

    String companyName;
    String companySummary;
    ArrayList<String> companyStocks;
    ArrayList<String> companyNews;
    ArrayList<String> companyProducts;
    ArrayList<String> companyReviews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bundle b = getIntent().getExtras();
        if (b != null) {
            companyName = b.getString("COMPANY"); // company name
            companySummary = b.getString("SUMMARY"); // wiki
            companyStocks = b.getStringArrayList("STOCKS"); // stocks
            companyNews = b.getStringArrayList("ARTICLES"); // urls
            companyProducts = b.getStringArrayList("PRODUCTS"); // products
            Log.d("is null:", Boolean.toString(companyProducts == null));
            companyReviews = b.getStringArrayList("REVIEWS"); // reviews

            String temp = "1";
            try {
                temp = companyStocks.get(1);

                //LAST PRICE, CHANGE PERCENT, HIGH, LOW
                companyStocks.add(0, "Last Price:      $" + companyStocks.remove(0));
                companyStocks.add(1, "Change Percent:  " + companyStocks.remove(1).substring(0, Math.min(temp.length(),4)) + "%");
                companyStocks.add(2, "High Price:      $" + companyStocks.remove(2));
                companyStocks.add(3, "Low Price:       $" + companyStocks.remove(3));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        // get the listview
        expListView = (ExpandableListView) findViewById(R.id.lvExp);

        Log.d("exp null check:", Boolean.toString(expListView == null));

        // preparing list data
        prepareListData();

        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);

        // setting list adapter
        expListView.setAdapter(listAdapter);

        expListView.setOnChildClickListener(new OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                if (groupPosition == 3) {
                    String url = companyNews.get(childPosition);
                    Log.d("status", url);
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);
                }
                return false;
            }
        });

        expListView.expandGroup(0);
    }

    /*
     * Preparing the list data
     */
    private void prepareListData() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        // Adding child data
        listDataHeader.add(companyName);
        List<String> underName = new ArrayList<String>();
        underName.add(companySummary);

        listDataHeader.add("Products");
        List<String> underProducts = new ArrayList<String>();
        underProducts.addAll(companyProducts);

        listDataHeader.add("Reviews");
        List<String> underReviews = new ArrayList<String>();
        underReviews.addAll(companyReviews);

        listDataHeader.add("News");
        List<String> underNews = new ArrayList<String>();
        underNews.addAll(companyNews);

        listDataHeader.add("Stocks");
        List<String> underStocks = new ArrayList<String>();
        if (companyStocks != null)
            underStocks.addAll(companyStocks);
        // Adding child data


        listDataChild.put(listDataHeader.get(0), underName); // Header, Child data
        listDataChild.put(listDataHeader.get(1), underProducts);
        listDataChild.put(listDataHeader.get(2), underReviews);
        listDataChild.put(listDataHeader.get(3), underNews);
        listDataChild.put(listDataHeader.get(4), underStocks);
    }

}