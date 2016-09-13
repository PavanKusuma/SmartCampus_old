package svecw.svecw;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import adapters.StudentWallAdapter;
import adapters.StudentsWallAdapter;
import adapters.WallAdapter;
import internaldb.SmartCampusDB;
import internaldb.SmartSessionManager;
import model.Wall;
import utils.ConnectionDetector;
import utils.Constants;
import utils.Routes;

/**
 * Created by Pavan on 4/16/15.
 */
public class StudentActivity extends AppCompatActivity {

    // adapter to populate list view
    StudentWallAdapter adapter;

    // list of college wall posts
    List<Wall> studentWallPostsList;

    // list view
    ListView listView;
    TextView noData;

    // toolbar
    Toolbar toolbar;

    //SmartDB smartDB = new SmartDB(this);
    byte[] b = Constants.null_indicator.getBytes();

    // instance for ConnectionDetector
    ConnectionDetector connectionDetector;

    // progress bar
    ProgressBar progressBar;

    // skip counter
    int skipCounter = 0;

    // layout inflater
    LayoutInflater layoutInflater;

    // listView footer button
    //Button btnMore;

    JSONObject jsonResponse;
    int status;

    // url for get wall posts
    String url = Routes.getWallPosts + Constants.key + "/" + Constants.STUDENT + "/";

    SmartCampusDB smartCampusDB = new SmartCampusDB(this);

    Activity activity;

    SmartSessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_wall_activity);

        // get the toolbar for the activity
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);

        // change the title according to the activity
        TextView title = (TextView) toolbar.findViewById(R.id.appName);
        title.setText(getResources().getString(R.string.student));

        // set the toolbar to the actionBar
        setSupportActionBar(toolbar);

        // get the action bar
        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setWindowTitle(""); // hide the main title

        // object for LayoutInflater
        layoutInflater = LayoutInflater.from(this);

        activity = this.activity;


        // get views from layout
        listView = (ListView) findViewById(R.id.studentWallListView);
        progressBar = (ProgressBar) findViewById(R.id.progressBarStudentWall);

        // create 'more' button for footerView

/*        // more button for listView
        btnMore = new Button(this);
        btnMore.setText("More...");
        btnMore.setBackgroundResource(R.drawable.card_background);
        btnMore.setVisibility(View.GONE);*/

        // adding footerView to listView
        //listView.addFooterView(btnMore);

        // set emptyTextView for listView
        noData = (TextView) findViewById(R.id.emptyElement);

        // adapter for the college wall posts
        adapter = new StudentWallAdapter(StudentActivity.this);
        //setListAdapter(adapter);
        listView.setAdapter(adapter);

        session = new SmartSessionManager(StudentActivity.this);

        // list for storing college wall posts
        studentWallPostsList = new ArrayList<Wall>();

        // object for ConnectionDetector
        connectionDetector = new ConnectionDetector(getApplicationContext());

        // check if internet is working
        // else show no network toast
        if(connectionDetector.isInternetWorking()) {

            // get the wall posts from database
            //new GetWallPosts().execute(Routes.getWallPosts, Constants.STUDENT, String.valueOf(skipCounter));
            callGetPosts();

        } else {

            progressBar.setVisibility(View.GONE);
            Toast.makeText(getApplicationContext(), "No internet", Toast.LENGTH_SHORT).show();
        }

/*        btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // increment the skip counter
                skipCounter = skipCounter + 10;
                new GetWallPosts().execute(String.valueOf(skipCounter));
            }
        });*/


        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {

                int threshold = 1;
                int count = listView.getCount();

                if (scrollState == SCROLL_STATE_IDLE) {
                    if (listView.getLastVisiblePosition() >= count- threshold) {

                        // show loading
                        progressBar.setVisibility(View.VISIBLE);

                        // increment the skip counter
                        skipCounter = skipCounter + 4;
                        // Fetch additional posts skipping existing posts
                        //new GetWallPosts().execute(Routes.getWallPosts, Constants.STUDENT, String.valueOf(skipCounter));
                        callGetPosts();
                    }

                }
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {

            }
        });



    }

    // call GlobalInfo
    public void callGetPosts(){

        new GetWallPosts().execute(Routes.getWallPosts, Constants.STUDENT, String.valueOf(skipCounter));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.v(Constants.appName, "Checking"+1);
        // if the result is capturing Image
        if (requestCode == 100) {

            Log.v(Constants.appName, "Checking"+2);
            if (resultCode == RESULT_OK) {

                Log.v(Constants.appName, "Checking"+3);
                // fetch the comment count and position
                int commentCount = data.getIntExtra(Constants.commentCount, 0);
                int position = data.getIntExtra(Constants.position, 0);

                Log.v(Constants.appName, "Checking"+4);
                // check the comment count and notify the adapter
                if(commentCount > 0) {

                    Log.v(Constants.appName, "Checking"+5);
                    Log.v(Constants.appName, "Checking"+commentCount);
                    Log.v(Constants.appName, "Checking"+position);
                    // add the comment count to current comment count of given wall post
                    //studentWallPostsList.get(position).setComments(studentWallPostsList.get(position).getComments()+commentCount);
                    studentWallPostsList.get(position).setComments(commentCount);

                    adapter.notifyDataSetChanged();

                } else {

                    // if no new comment appeared, do nothing
                }
            }
        }
        // check if result is from new post activity
        else if(requestCode == 200){

            // check if the post is created
            if(resultCode == 1){

                // update the backIntent to display new post on wall
                Wall wall = new Wall();

                // fetch the details from intent and assign it to object
                wall.setWallId(data.getStringExtra(Constants.wallId));
                wall.setWallType(data.getStringExtra(Constants.wallType));
                wall.setUserObjectId(data.getStringExtra(Constants.userObjectId));
                wall.setCollegeId(data.getStringExtra(Constants.collegeId));
                wall.setPostDescription(data.getStringExtra(Constants.postDescription));
                wall.setCreatedAt(data.getStringExtra(Constants.createdAt));
                wall.setUpdatedAt(data.getStringExtra(Constants.updatedAt));
                wall.setLikes(data.getIntExtra(Constants.likes, 0));
                wall.setComments(data.getIntExtra(Constants.comments, 0));
                wall.setMediaCount(data.getIntExtra(Constants.mediaCount, 0));
                wall.setMedia(data.getStringExtra(Constants.media));
                wall.setIsActive(data.getIntExtra(Constants.isActive, 0));
                wall.setUserName(data.getStringExtra(Constants.userName));
                wall.setMedia(data.getStringExtra(Constants.media));
                wall.setUserImage(session.getProfilePhoto());

                // add the object to list at top and notify adapter
                studentWallPostsList.add(0, wall);
                adapter.notifyDataSetChanged();

            }
            // check if the post is not created
            else{

            }
        }
        else {
            adapter.notifyDataSetChanged();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if(smartCampusDB.getUser().get(Constants.role).toString().contains(Constants.student))
            getMenuInflater().inflate(R.menu.wall_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.createNewWallPost) {

            // navigate to selectNewPostActivity to show posting options based on privileges
            Intent newPostIntent = new Intent(getApplicationContext(), StudentWallNewPostActivity.class);
            newPostIntent.putExtra(Constants.alumniPost, false);
            startActivityForResult(newPostIntent, 200);

            return true;
        }


        return super.onOptionsItemSelected(item);
    }


/*
    class GetStudentWallPosts extends AsyncTask<String, Float, String>{
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
                ParseQuery<ParseObject> studentPostsQuery = ParseQuery.getQuery(Constants.studentWallTable);

                // filter non-alumni posts and order by createdAt
                studentPostsQuery.addDescendingOrder(Constants.createdAt);
                studentPostsQuery.whereEqualTo(Constants.alumniPost, false);

                // check if skipCounter is not '0'
                // this indicates, request is received from 'load more' button click
                // to fetch more posts from db
                if(Integer.valueOf(params[0]) != 0){

                    studentPostsQuery.setSkip(skipCounter);
                }

                // limit results to 10
                studentPostsQuery.setLimit(10);

                // get the list of shouts
                studentPostsQuery.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(final List<ParseObject> parseObjects, ParseException e) {

                        if (e == null) {

                            if (parseObjects.size() > 0) {

                                // navigate through the list of objects
                                for (int i = 0; i < parseObjects.size(); i++) {

                                    try {

                                        // create object for collegeWallPost class
                                        final StudentWall studentWall = new StudentWall();

                                        final ParseObject parseObject = parseObjects.get(i);

                                        // set the values of the wall post into object
                                        studentWall.setPostDescription(parseObjects.get(i).getString(Constants.postDescription));
                                        studentWall.setCreatedAt(parseObjects.get(i).getCreatedAt());
                                        studentWall.setUpdatedAt(parseObjects.get(i).getUpdatedAt());
                                        studentWall.setObjectId(parseObjects.get(i).getObjectId());
                                        studentWall.setLikes(parseObjects.get(i).getInt(Constants.likes));
                                        studentWall.setDislikes(parseObjects.get(i).getInt(Constants.dislikes));
                                        studentWall.setComments(parseObjects.get(i).getInt(Constants.comments));
                                        studentWall.setUserName(parseObjects.get(i).getString(Constants.userName));
                                        studentWall.setUserObjectId(parseObjects.get(i).getString(Constants.userObjectId));
                                        studentWall.setAlumniPost(parseObjects.get(i).getBoolean(Constants.alumniPost));
                                        studentWall.setStudentYear(parseObjects.get(i).getInt(Constants.studentYear));

                                        // check if the mediaId is not '0'
                                        // if so get the mediaFile from 'Media' table
                                        // else do nothing
                                        if(!parseObjects.get(i).getString(Constants.mediaId).contentEquals("0")) {

                                            ParseQuery<ParseObject> userDataObject = ParseQuery.getQuery(Constants.mediaTable);

                                            // check if the objectId exists as 'relativeId' in mediaTable table
                                            userDataObject.whereEqualTo(Constants.relativeId, parseObjects.get(i).getString(Constants.mediaId));

                                            userDataObject.getFirstInBackground(new GetCallback<ParseObject>() {
                                                @Override
                                                public void done(ParseObject parseObject, ParseException e) {

                                                    if(e==null) {
                                                        // get the mediaFile of the wall post
                                                        ParseFile mediaFile = (ParseFile) parseObject.get(Constants.mediaFile);

                                                        if (mediaFile != null)
                                                            mediaFile.getDataInBackground(new GetDataCallback() {
                                                                @Override
                                                                public void done(byte[] bytes, ParseException e) {

                                                                    // check if the image is available
                                                                    if (e == null) {

                                                                        // check if the mediaTable file bytes are null indicator bytes
                                                                        // if so assign the mediaTable file string to null indicator
                                                                        // else fetch the mediaTable file bytes and assign
                                                                        if (bytes == Constants.null_indicator.getBytes()) {

                                                                            // set the mediaTable file string to null indicator
                                                                            studentWall.setMediaFile(Constants.null_indicator.getBytes());
                                                                            //adapter.notifyDataSetChanged();
                                                                        } else {

                                                                            // set the mediaTable file string to image base 64 string
                                                                            //studentWall.setMediaFile(Base64.encodeToString(bytes, Base64.DEFAULT).toString());
                                                                            studentWall.setMediaFile(bytes);

                                                                        }

                                                                    }
                                                                }
                                                            });
                                                    }
                                                }
                                            });

                                        }
                                        else {

                                            // set the mediaTable file string to null indicator
                                            studentWall.setMediaFile(Constants.null_indicator.getBytes());
                                        }


                                        ParseQuery<ParseObject> userDataObject = ParseQuery.getQuery(Constants.mediaTable);

                                            // check if the objectId exists as 'relativeId' in mediaTable table
                                            userDataObject.whereEqualTo(Constants.relativeId, parseObjects.get(i).getString(Constants.userObjectId));

                                            userDataObject.getFirstInBackground(new GetCallback<ParseObject>() {
                                                @Override
                                                public void done(ParseObject parseObject, ParseException e) {

                                                    if (e == null) {
System.out.println("ok1");
                                                        // get the user image file
                                                        ParseFile userImageFile = (ParseFile) parseObject.get(Constants.mediaFile);

                                                                // check if the data is available
                                                                userImageFile.getDataInBackground(new GetDataCallback() {
                                                                    @Override
                                                                    public void done(byte[] bytes, ParseException e) {

                                                                        // check if the image is available
                                                                        if (e == null) {

                                                                            if (bytes == Constants.null_indicator.getBytes()) {

                                                                                // set the user image file string to null indicator
                                                                                studentWall.setUserImage(Constants.null_indicator.getBytes());
                                                                                studentWallPostsList.add(studentWall);

                                                                                btnMore.setVisibility(View.VISIBLE);
                                                                                adapter.notifyDataSetChanged();
                                                                            } else {

                                                                                // set the user image file string to image base 64 string
                                                                                //studentWallPost.setUserImage(Base64.encodeToString(bytes, Base64.DEFAULT).toString());
                                                                                studentWall.setUserImage(bytes);
                                                                                studentWallPostsList.add(studentWall);

                                                                                btnMore.setVisibility(View.VISIBLE);
                                                                                adapter.notifyDataSetChanged();
                                                                            }


                                                                        }
                                                                    }
                                                                });

                                                    }

                                                    // if there is no record found for the given user id
                                                    // show the default image
                                                    else {
                                                        // set the user image file string to null indicator
                                                        studentWall.setUserImage(Constants.null_indicator.getBytes());
                                                        studentWallPostsList.add(studentWall);

                                                        btnMore.setVisibility(View.VISIBLE);
                                                        adapter.notifyDataSetChanged();
                                                    }
                                                }
                                            });

                                        adapter.updateItems(studentWallPostsList);
                                        adapter.notifyDataSetChanged();

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
                                btnMore.setVisibility(View.GONE);
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

                    */
    /**
     * Updating parsed JSON data into ListView
     * *//*


                    adapter.updateItems(studentWallPostsList);
                    // notify the adapter
                    adapter.notifyDataSetChanged();

                    // save the data to shared preferences
                    //sharedPreferences.saveWallPosts(studentWallPostsList);

                }
            });
        }
    }
*/

    /*
     * This method will resume the activity after returning from other activity
     * hence, adapter to the listview is set here
     *
     * (non-Javadoc)
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
        super.onResume();

        adapter.updateItems(studentWallPostsList);
        adapter.notifyDataSetChanged();
    }

    /**
     * Verify whether user has previous active login session
     *
     * if so, restrict the user for login
     * else
     *      Check if the collegeId and secretCode are matching
     *      if so, get the user details of collegeId from db and create a active login session
     */
    private class GetWallPosts extends AsyncTask<String, Void, Void>{

        private String Content = "";
        private String Error = null;
        String data = "";

        @Override
        protected void onPreExecute() {

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

            StrictMode.setThreadPolicy(policy);
        }

        @Override
        protected Void doInBackground(String... urls) {

            /************ Make Post Call To Web Server ***********/
            BufferedReader reader = null;

            // Send data
            try {


                // Set Request parameter
                data += "?&" + URLEncoder.encode(Constants.KEY, "UTF-8") + "=" + Constants.key
                        + "&" + URLEncoder.encode(Constants.wallType, "UTF-8") + "=" + (urls[1])
                        + "&" + URLEncoder.encode(Constants.limit, "UTF-8") + "=" + (urls[2]);

                Log.v(Constants.appName, urls[0]+data);

                // Defined URL  where to send data
                URL url = new URL(urls[0]+data);

                // Send POST data request
                URLConnection conn = url.openConnection();
                conn.setDoOutput(true);
                //conn.setDoInput(true);
                //OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                //wr.write(data);
                //wr.flush();

                // setting timeout
                conn.setConnectTimeout(15000);

                // Get the server response
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = null;

                // Read Server Response
                while ((line = reader.readLine()) != null) {
                    // Append server response in string
                    sb.append(line + " ");
                }

                // Append Server Response To Content String
                Content = sb.toString();

                // close the reader
                //reader.close();

            } catch (SocketTimeoutException e){

                e.printStackTrace();
                Error = e.getMessage();
                callGetPosts();
            }

            catch (Exception ex) {

                ex.printStackTrace();
                Error = ex.getMessage();

            }

            finally {

                try {

                    reader.close();

                } catch (Exception ex) {
                    Error = ex.getMessage();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            // hide the progress
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "Network unreachable! Please try again", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            return null;
        }

        protected void onPostExecute(Void unused) {

            if (Error != null) {

                Log.i("Connection", Error);

            } else {

                //Log.i("Connection", Content);
                /****************** Start Parse Response JSON Data *************/


                try {

                    /****** Creates a new JSONObject with name/value mappings from the JSON string. ********/
                    jsonResponse = new JSONObject(Content);


                    /***** Returns the value mapped by name if it exists and is a JSONArray. ***/
                    status = jsonResponse.getInt(Constants.status);



                            // check the status and proceed with the logic
                            switch (status){

                                // exception occurred
                                case -3:

                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(getApplicationContext(), R.string.errorMsg, Toast.LENGTH_SHORT).show();
                                    break;

                                // key mismatch
                                case -2:

                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(getApplicationContext(), R.string.errorMsg, Toast.LENGTH_SHORT).show();
                                    break;

                                // Reached end, no more to display
                                case 0:

                                    progressBar.setVisibility(View.GONE);
                                    // no more data to display
                                    Toast.makeText(getApplicationContext(), R.string.noData, Toast.LENGTH_SHORT).show();
                                    noData.setVisibility(View.VISIBLE);
                                    break;

                                // data found
                                case 1:

                                    try {

                                        // get JSON Array of 'details'
                                        JSONArray jsonArray = jsonResponse.getJSONArray(Constants.details);

                                        for(int i=0; i<jsonArray.length(); i++){

                                            // get the JSON object inside Array
                                            JSONObject jsonObject = (JSONObject) jsonArray.get(i);

                                            // get the data and store in internal db
                                            Wall wall = new Wall();

                                            wall.setWallId(jsonObject.getString(Constants.wallId));
                                            wall.setWallType(jsonObject.getString(Constants.wallType)); // secretCode.getText().toString()
                                            wall.setUserObjectId(jsonObject.getString(Constants.userObjectId));
                                            wall.setPostDescription(jsonObject.getString(Constants.postDescription));
                                            wall.setCreatedAt(jsonObject.getString(Constants.createdAt));
                                            wall.setUpdatedAt(jsonObject.getString(Constants.updatedAt));
                                            wall.setLikes(jsonObject.getInt(Constants.likes));
                                            wall.setDislikes(jsonObject.getInt(Constants.dislikes));
                                            wall.setComments(jsonObject.getInt(Constants.comments));
                                            wall.setMediaCount(jsonObject.getInt(Constants.mediaCount));
                                            wall.setMedia(jsonObject.getString(Constants.media));
                                            wall.setIsActive(jsonObject.getInt(Constants.isActive));
                                            wall.setUserName(jsonObject.getString(Constants.userName));

                                            //Log.v(Constants.appName, "Comment count:" +jsonObject.getInt(Constants.comments));

                                            // get the user image
                                            JSONObject userObject = (JSONObject) jsonObject.get(Constants.user);
                                            int mediaCount = userObject.getInt(Constants.mediaCount);
                                            if(mediaCount > 0){
                                                Log.v(Constants.appName, "userImage : "+userObject.getString(Constants.media));
                                                wall.setUserImage(userObject.getString(Constants.media));
                                            }
                                            else{
                                                Log.v(Constants.appName, "userImage : "+userObject.getString(Constants.media));
                                                wall.setUserImage(Constants.null_indicator);
                                            }

                                            // get the collegeId of user
                                            wall.setCollegeId(userObject.getString(Constants.collegeId));

                                            // add the object to list
                                            studentWallPostsList.add(wall);
                                        }

                                        // update the items
                                        adapter.updateItems(studentWallPostsList);
                                        adapter.notifyDataSetChanged();
                                        progressBar.setVisibility(View.GONE);



                                       /* // detect the end of scrolling
                                        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                                            @Override
                                            public void onScrollStateChanged(AbsListView absListView, int scrollState) {

                                                int threshold = 1;
                                                int count = listView.getCount();

                                                if (scrollState == SCROLL_STATE_IDLE) {
                                                    if (listView.getLastVisiblePosition() >= count- threshold) {

                                                        // show loading
                                                        progressBar.setVisibility(View.VISIBLE);

                                                        // increment the skip counter
                                                        skipCounter = skipCounter + 4;
                                                        // Fetch additional posts skipping existing posts
                                                        //new GetWallPosts().execute(Routes.getWallPosts, Constants.STUDENT, String.valueOf(skipCounter));
                                                        callGetPosts();
                                                    }

                                                }
                                            }

                                            @Override
                                            public void onScroll(AbsListView absListView, int i, int i1, int i2) {

                                            }
                                        });*/
                                    }
                                    catch(Exception e){

                                        progressBar.setVisibility(View.GONE);
                                        Log.e(Constants.error, e.getMessage());
                                        Toast.makeText(getApplicationContext(), R.string.errorMsg, Toast.LENGTH_SHORT).show();

                                    }

                                    progressBar.setVisibility(View.GONE);

                                    break;

                            }




                } catch (JSONException e) {

                    progressBar.setVisibility(View.GONE);
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Oops! something went wrong, try again later", Toast.LENGTH_SHORT).show();

                }

            }
        }
    }
}
