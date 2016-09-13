package svecw.svecw;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import adapters.PlacementsAdapter;
import model.Placements;
import utils.Constants;

/**
 * Created by Pavan on 7/3/15.
 */
public class PlacementResultActivity extends AppCompatActivity {

    ListView placementsListView;

    String year;

    ProgressBar placementsProgressBar;

    PlacementsAdapter placementsAdapter;

    List<Placements> placementsList;

    LayoutInflater layoutInflater;

    List<String> companyList = new ArrayList<String>();
    List<Integer> countList = new ArrayList<Integer>();

    TextView placementYear;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.placement_results_activity);

        // get the toolbar for the activity
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);

        // change the title according to the activity
        TextView title = (TextView) toolbar.findViewById(R.id.appName);
        title.setText(getResources().getString(R.string.placements));

        // set the toolbar to the actionBar
        setSupportActionBar(toolbar);

        // get the action bar
        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setWindowTitle(""); // hide the main title

        // object for LayoutInflater
        layoutInflater = LayoutInflater.from(this);

        placementsListView = (ListView) findViewById(R.id.placementsListView);
        placementsProgressBar = (ProgressBar) findViewById(R.id.progressBarPlacements);
        placementYear = (TextView) findViewById(R.id.placementYear);

        placementsAdapter = new PlacementsAdapter(PlacementResultActivity.this);
        placementsListView.setAdapter(placementsAdapter);

        placementsList = new ArrayList<Placements>();

        // get the placement year
        year = getIntent().getStringExtra(Constants.year);
        placementYear.setText(year);
        //new GetPlacementsList().execute(year);











        InputStream is_07;
        InputStream is_count_07;

        BufferedReader reader_07 = null;
        BufferedReader reader_count_07 = null;


        if(year == "list") {

            // get the raw files
            is_07 = getResources().openRawResource(R.raw.recruiterslist);
            //is_count_07 = getResources().openRawResource(R.raw.count7);

            // reader
            reader_07 = new BufferedReader(new InputStreamReader(is_07));
            //reader_count_07 = new BufferedReader(new InputStreamReader(is_count_07));
        }
        else if(Integer.valueOf(year) == 2007) {

            // get the raw files
            is_07 = getResources().openRawResource(R.raw.company7);
            is_count_07 = getResources().openRawResource(R.raw.count7);

            // reader
            reader_07 = new BufferedReader(new InputStreamReader(is_07));
            reader_count_07 = new BufferedReader(new InputStreamReader(is_count_07));
        }
        else if(Integer.valueOf(year) == 2008) {

            // get the raw files
            is_07 = getResources().openRawResource(R.raw.company8);
            is_count_07 = getResources().openRawResource(R.raw.count8);

            // reader
            reader_07 = new BufferedReader(new InputStreamReader(is_07));
            reader_count_07 = new BufferedReader(new InputStreamReader(is_count_07));
        }
        else if(Integer.valueOf(year) == 2009) {

            // get the raw files
            is_07 = getResources().openRawResource(R.raw.company9);
            is_count_07 = getResources().openRawResource(R.raw.count9);

            // reader
            reader_07 = new BufferedReader(new InputStreamReader(is_07));
            reader_count_07 = new BufferedReader(new InputStreamReader(is_count_07));
        }
        else if(Integer.valueOf(year) == 2010) {

            // get the raw files
            is_07 = getResources().openRawResource(R.raw.company10);
            is_count_07 = getResources().openRawResource(R.raw.count10);

            // reader
            reader_07 = new BufferedReader(new InputStreamReader(is_07));
            reader_count_07 = new BufferedReader(new InputStreamReader(is_count_07));
        }
        else if(Integer.valueOf(year) == 2011) {

            // get the raw files
            is_07 = getResources().openRawResource(R.raw.company11);
            is_count_07 = getResources().openRawResource(R.raw.count11);

            // reader
            reader_07 = new BufferedReader(new InputStreamReader(is_07));
            reader_count_07 = new BufferedReader(new InputStreamReader(is_count_07));
        }
        else if(Integer.valueOf(year) == 2012) {

            // get the raw files
            is_07 = getResources().openRawResource(R.raw.company12);
            is_count_07 = getResources().openRawResource(R.raw.count12);

            // reader
            reader_07 = new BufferedReader(new InputStreamReader(is_07));
            reader_count_07 = new BufferedReader(new InputStreamReader(is_count_07));
        }
        else if(Integer.valueOf(year) == 2013) {

            // get the raw files
            is_07 = getResources().openRawResource(R.raw.company13);
            is_count_07 = getResources().openRawResource(R.raw.count13);

            // reader
            reader_07 = new BufferedReader(new InputStreamReader(is_07));
            reader_count_07 = new BufferedReader(new InputStreamReader(is_count_07));
        }
        else if(Integer.valueOf(year) == 2014) {

            // get the raw files
            is_07 = getResources().openRawResource(R.raw.company14);
            is_count_07 = getResources().openRawResource(R.raw.count14);

            // reader
            reader_07 = new BufferedReader(new InputStreamReader(is_07));
            reader_count_07 = new BufferedReader(new InputStreamReader(is_count_07));
        }
        else if(Integer.valueOf(year) == 2015) {

            // get the raw files
            is_07 = getResources().openRawResource(R.raw.company15);
            is_count_07 = getResources().openRawResource(R.raw.count15);

            // reader
            reader_07 = new BufferedReader(new InputStreamReader(is_07));
            reader_count_07 = new BufferedReader(new InputStreamReader(is_count_07));
        }
        else if(Integer.valueOf(year) == 2016) {

            // get the raw files
            is_07 = getResources().openRawResource(R.raw.company16);
            is_count_07 = getResources().openRawResource(R.raw.count16);

            // reader
            reader_07 = new BufferedReader(new InputStreamReader(is_07));
            reader_count_07 = new BufferedReader(new InputStreamReader(is_count_07));
        }

        String line1, line2, line3, line4, line5, line6, line7, line8, line9, line10;
        String[] rowData1, rowData2, rowData3, rowData4, rowData5, rowData6, rowData7, rowData8, rowData9, rowData10 = null;

        try {
            while ((line1 = reader_07.readLine()) != null) {

                line2 = reader_count_07.readLine();

                rowData1 = line1.split("\n");
                rowData2 = line2.split("\n");



                companyList.add(rowData1[0]);
                countList.add(Integer.valueOf(rowData2[0].toString()));

            }
        } catch (Exception ex) {
            Log.v(Constants.appName, ex.getMessage());
        }

        for(int i=0; i<companyList.size(); i++){

            Placements placements = new Placements();
            placements.setCompany(companyList.get(i));
            placements.setCount(countList.get(i));
            placements.setYear(year);

            placementsList.add(placements);
        }

// set the default list
        placementsAdapter.updateItems(placementsList);
        placementsAdapter.notifyDataSetChanged();

    }

    class GetPlacementsList extends AsyncTask<String, Float, String> {
        @Override
        protected void onProgressUpdate(Float... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPreExecute() {
            //super.onPreExecute();

            placementsProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {

            try {
                // create parse object for the "Placements"
                ParseQuery<ParseObject> placementQuery = ParseQuery.getQuery(Constants.Placements);

                placementQuery.whereEqualTo(Constants.year, params[0]);

                // get the list of shouts
                placementQuery.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(final List<ParseObject> parseObjects, ParseException e) {

                        if (e == null) {

                            if (parseObjects.size() > 0) {

                                // navigate through the list of objects
                                for (int i = 0; i < parseObjects.size(); i++) {

                                    try {

                                        // create object for collegeWallPost class
                                        final Placements placements = new Placements();

                                        // set the values of the wall post into object
                                        placements.setYear(parseObjects.get(i).getString(Constants.year));
                                        placements.setCount(parseObjects.get(i).getInt(Constants.count));
                                        placements.setCompany(parseObjects.get(i).getString(Constants.company));

                                        placementsList.add(placements);

                                    } catch (Exception ex) {

                                        Log.v(Constants.appName, ex.getMessage());
                                        placementsProgressBar.setVisibility(View.GONE);
                                    }
                                }

                                placementsProgressBar.setVisibility(View.GONE);
                                placementsAdapter.updateItems(placementsList);
                                placementsAdapter.notifyDataSetChanged();
                            }

                            // if there are no objects to display
                            else {

                                placementsProgressBar.setVisibility(View.GONE);
                                Toast.makeText(getApplicationContext(), "No more to display", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

            }

            // all parse data not found exceptions are caught
            catch(Exception e){
                finish();
            }

            return null;
        }


        @Override
        protected void onPostExecute(String s) {
            //super.onPostExecute(s);

            runOnUiThread(new Runnable() {
                public void run() {

                    /**
                     * Updating parsed JSON data into ListView
                     * */

                    // notify the adapter
                    placementsAdapter.updateItems(placementsList);
                    placementsAdapter.notifyDataSetChanged();

                    // save the data to shared preferences
                    //sharedPreferences.saveWallPosts(studentWallPostsList);

                }
            });
        }
    }
}
