package svecw.svecw;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

import adapters.LearnSubModuleAdapter;
import model.Learn;
import model.StudentWall;
import utils.Constants;

/**
 * Created by Pavan on 6/25/15.
 */
public class LearnSubModuleActivity extends AppCompatActivity {

    // gegt views from activity
    ExpandableListView learnListView;
    ProgressBar progressBar;

    LearnSubModuleAdapter learnSubModuleAdapter;

    List<String> listDataHeader, listDataItem;

    String selectedTopic;

    LayoutInflater layoutInflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.learn_submodule_activity);

        // get the selected topic of the learn module
        selectedTopic = getIntent().getStringExtra(Constants.topic);

        // get the toolbar for the activity
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);

        // change the title according to the activity
        TextView title = (TextView) toolbar.findViewById(R.id.appName);
        title.setText(selectedTopic);

        // set the toolbar to the actionBar
        setSupportActionBar(toolbar);

        // get the action bar
        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setWindowTitle(""); // hide the main title

        // object for LayoutInflater
        layoutInflater = LayoutInflater.from(this);

        listDataHeader = new ArrayList<String>();
        listDataItem = new ArrayList<String>();

        // get views from activity
        learnListView = (ExpandableListView) findViewById(R.id.learnListView);
        progressBar = (ProgressBar) findViewById(R.id.progressBarLearn);
        learnSubModuleAdapter = new LearnSubModuleAdapter(this, listDataHeader, listDataItem);
        learnListView.setAdapter(learnSubModuleAdapter);


        // get the list of the topic
        new GetLearnModuleList().execute(selectedTopic);

    }

    class GetLearnModuleList extends AsyncTask<String, Float, String> {
        @Override
        protected void onProgressUpdate(Float... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPreExecute() {
            //super.onPreExecute();

            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {

            try {
                // create parse object for the "CollegeWall"
                ParseQuery<ParseObject> learnModuleQuery = ParseQuery.getQuery(Constants.Learn);

                // order by createdAt
                learnModuleQuery.whereEqualTo(Constants.topic, params[0]);

                // get the list of shouts
                learnModuleQuery.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(final List<ParseObject> parseObjects, ParseException e) {

                        if (e == null) {

                            if (parseObjects.size() > 0) {

                                // navigate through the list of objects
                                for (int i = 0; i < parseObjects.size(); i++) {

                                    try {

                                        // create object for collegeWallPost class
                                        final Learn studentWall = new Learn();

                                        final ParseObject parseObject = parseObjects.get(i);

                                        // set the values of the wall post into object
                                        studentWall.setTopic(parseObjects.get(i).getString(Constants.topic));
                                        studentWall.setQuestion(parseObjects.get(i).getString(Constants.question));
                                        studentWall.setAnswer(parseObjects.get(i).getString(Constants.answer));

                                        listDataHeader.add(parseObjects.get(i).getString(Constants.question));
                                        listDataItem.add(parseObjects.get(i).getString(Constants.answer));

                                        learnSubModuleAdapter.notifyDataSetChanged();

                                        progressBar.setVisibility(View.GONE);


                                    } catch (Exception ex) {

                                        Log.v(Constants.appName, ex.getMessage());
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }

                            }

                            // if there are no objects to display
                            else {

                                progressBar.setVisibility(View.GONE);
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
                    learnSubModuleAdapter.notifyDataSetChanged();

                    // save the data to shared preferences
                    //sharedPreferences.saveWallPosts(studentWallPostsList);

                }
            });
        }
    }

}
