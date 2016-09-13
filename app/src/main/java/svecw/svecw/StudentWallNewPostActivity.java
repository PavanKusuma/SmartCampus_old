package svecw.svecw;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import internaldb.SmartCampusDB;
import model.Wall;
import utils.Constants;
import utils.Routes;
import utils.Snippets;

/**
 * Created by Pavan on 4/16/15.
 */
public class StudentWallNewPostActivity extends AppCompatActivity {

    // views from layout
    EditText studentWallNewPostDescription;
    ImageView studentWallSelectImage;
    TextView cancelPost, sendPost;
    ProgressBar progressBar;

    // progress indicator
    ProgressDialog progressDialog;

    // toobar for action bar
    Toolbar toolbar;

    private Uri fileUri; // file url to store image when camera is used
    public String picturePath; // url for the image when gallery is used to pick image
    public static String imageLocation, imageLocation_thumb; // variable for storing image location

    public static Bitmap bitmap;
    //public static String base64String; // encoded image string that will be uploaded

    //SmartDB smartDB = new SmartDB(this);
    byte[] b = Constants.null_indicator.getBytes();
    InputStream is;

    ActionBar actionBar;

    // internal db
    SmartCampusDB smartCampusDB = new SmartCampusDB(this);

    LayoutInflater layoutInflater;

    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    Random random = new Random();

    // is this alumniPost?
    Boolean alumniPost = false;

    // post id when activity is resumed
    String description, url;
    int mediaCount = 0;

    int serverResponseCode, status;
    JSONObject jsonResponse;

    int height, width;
    Intent backIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_wall_newpost_activity);

        backIntent = getIntent();
        // get the post type
        alumniPost = backIntent.getBooleanExtra(Constants.alumniPost, false);

        // get the toolbar for the activity
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);

        // change the title according to the activity
        TextView title = (TextView) toolbar.findViewById(R.id.appName);

        // set the activity title based on post type
        if(alumniPost) {

            title.setText(getResources().getString(R.string.alumniWallPost));
        } else {

            title.setText(getResources().getString(R.string.studentWallPost));
        }

        // set the toolbar to the actionBar
        setSupportActionBar(toolbar);

        // get the action bar
        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setWindowTitle(""); // hide the main title

        // object for LayoutInflater
        layoutInflater = LayoutInflater.from(this);


        // get views from activity
        studentWallNewPostDescription = (EditText) findViewById(R.id.studentWallNewPostDescription);
        studentWallSelectImage = (ImageView) findViewById(R.id.studentWallSelectImage);
        cancelPost = (TextView) findViewById(R.id.cancelPost);
        sendPost = (TextView) findViewById(R.id.sendPost);
        progressBar = (ProgressBar) findViewById(R.id.postingProgress);

        studentWallSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // show the dialog to choose camera or gallery
                // custom dialog
                AlertDialog.Builder menuAlert = new AlertDialog.Builder(StudentWallNewPostActivity.this);
                final String[] menuList = { "Camera", "Gallery" };
                menuAlert.setTitle("Select from");
                menuAlert.setItems(menuList, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        switch (item) {
                            // camera selected
                            case 0:
                                // posting from camera
                                postingFromCamera();
                                break;

                            // gallery selected
                            case 1:

                                // posting from gallery
                                postingFromGallery();
                                break;
                        }
                    }


                });
                AlertDialog menuDrop = menuAlert.create();
                menuDrop.show();
            }
        });


        sendPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                // disable the post link
                sendPost.setText(R.string.verifying);
                sendPost.setEnabled(false);

                // get the description
                description = studentWallNewPostDescription.getText().toString();

                // check if the input is provided
                if(description.length()>0) {

                    try {
                        // we have 2 classes to execute based on mediaTable
                        // if mediaTable exists then 'CreateWallPostWithMedia' is executed
                        // else 'CreateWallPostWithoutMedia' is executed

                        // check if the mediaTable file is present
                        // if so enter the randomObjectId
                        if (Arrays.equals(b, Constants.null_indicator.getBytes())) {

                            mediaCount = 0;


                        } else {

                            mediaCount = 1;
                        }

                        // check whether the post is by Student or Alumni
                        if(alumniPost){

                            // show the status of posting
                            sendPost.setText(R.string.posting);

                            // enable post progress
                            progressBar.setVisibility(View.VISIBLE);
                            // set progress
                            progressBar.setProgress(10);

                            // execute the code
                            new CreateWallPost().execute(Routes.createWallPost, smartCampusDB.getUser().get(Constants.userObjectId).toString(), Snippets.getUniqueWallId(), Constants.ALUMNI, Snippets.escapeURIPathParam(description));

                        }
                        else {
                            // show the status of posting
                            sendPost.setText(R.string.posting);

                            // enable post progress
                            progressBar.setVisibility(View.VISIBLE);
                            // set progress
                            progressBar.setProgress(10);

                            // execute the code
                            new CreateWallPost().execute(Routes.createWallPost, smartCampusDB.getUser().get(Constants.userObjectId).toString(), Snippets.getUniqueWallId(), Constants.STUDENT, Snippets.escapeURIPathParam(description));

                        }

                    }
                    catch(Exception e){

                        // do nothing
                    }

                }
                else {

                    Toast.makeText(getApplicationContext(), "Enter post description", Toast.LENGTH_SHORT).show();
                    // enable the post link
                    sendPost.setText(R.string.post);
                    sendPost.setEnabled(true);
                }
            }
        });

        cancelPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setResult(0, backIntent);
                finish();
            }
        });

    }

    /**
     * this will deal with the result depending on
     * camera capture or gallery pick
     * it will get data from the result and sets the image to the imageview
     */

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        if (requestCode == Constants.CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // successfully captured the image
                // display it in image view
                previewCapturedImage();
            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled Image capture
                Toast.makeText(getApplicationContext(),
                        "You cancelled image capture", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to capture image
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                        .show();
            }
        }
        // if the result is ok
        if (resultCode == RESULT_OK && requestCode == Constants.IMG_PICK){

            //get the Uri for the captured image
            Uri selectedImage = data.getData();

            String[] filePathColumn = { MediaStore.MediaColumns.DATA };

            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            picturePath = cursor.getString(columnIndex);
            cursor.close();

            InputStream imageStream = null;
            try {
                imageStream = getContentResolver().openInputStream(selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            // bitmap options
            BitmapFactory.Options options = new BitmapFactory.Options();

            // get the bitmap from imageStream
            bitmap = BitmapFactory.decodeStream(imageStream, null, options);

            // call imageCompression method to compress the image
            // assign bitmap to ImageView
            BitmapDrawable bd = imageCompression(bitmap, options);
            studentWallSelectImage.setImageDrawable(bd);
            studentWallSelectImage.setAdjustViewBounds(true);

        }

    }

    /**
     * This method will take care of camera functionality in current post
     *
     */
    private void postingFromCamera() {

        // check if device supports camera
        // if so request permission to access it
        if (getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {

            // this device has a camera
            // check if this device has SDK less than or equal to Marshmallow
            if(Build.VERSION.SDK_INT >= 23){

                // check if this app is granted with camera access permission
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {

                    // The permission is NOT already granted.
                    // Check if the user has been asked about this permission already and denied
                    // it. If so, we want to give more explanation about why the permission is needed.
                    if (shouldShowRequestPermissionRationale(
                            Manifest.permission.CAMERA)) {
                        // Show our own UI to explain to the user why we need to read the contacts
                        // before actually requesting the permission and showing the default UI
                    }

                    // Fire off an async request to actually get the permission
                    // This will show the standard permission request dialog UI
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);


                }

                // as this app is granted with camera access permission previously
                else {

                    try{
                        // call the capture image method where we call the camera of phone
                        captureImage();

                    } catch (ActivityNotFoundException anfe) {
                        //display an error message
                        String errorMessage = "Whoops - your device doesn't support capturing images!";
                        Toast toast = Toast.makeText(getBaseContext(), errorMessage, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
            }

            // no need to tak permission as the device sdk is lower than Marshmallow
            else{

                try{
                    // call the capture image method where we call the camera of phone
                    captureImage();

                } catch (ActivityNotFoundException anfe) {
                    //display an error message
                    String errorMessage = "Whoops - your device doesn't support capturing images!";
                    Toast toast = Toast.makeText(getBaseContext(), errorMessage, Toast.LENGTH_SHORT);
                    toast.show();
                }
            }


        } else {
            // no camera on this device
            Toast.makeText(getApplicationContext(), "Sorry! Your device doesn't support camera", Toast.LENGTH_LONG).show();

        }
    }


    /**
     * this method will check for gallery permission
     * @return boolean
     */
    private void postingFromGallery() {

        // check if device supports camera
        if(Build.VERSION.SDK_INT >= 23){

            // check if this app is granted with gallery access permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                // The permission is NOT already granted.
                // Check if the user has been asked about this permission already and denied
                // it. If so, we want to give more explanation about why the permission is needed.
                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // Show our own UI to explain to the user why we need to read the contacts
                    // before actually requesting the permission and showing the default UI
                }

                // Fire off an async request to actually get the permission
                // This will show the standard permission request dialog UI
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);


            }
            else{

                // as the app is granted with camera access permission previously
                try{

                    Intent pickIntent = new Intent(Intent.ACTION_PICK);
                    pickIntent.setType("image/*");
                    //we will handle the returned data in onActivityResult
                    startActivityForResult(Intent.createChooser(pickIntent, "Select Picture"), Constants.IMG_PICK);

                } catch (ActivityNotFoundException anfe) {
                    //display an error message
                    String errorMessage = "Whoops - your device doesnot contain external storage!";
                    Toast toast = Toast.makeText(getBaseContext(), errorMessage, Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        }
        else{


            try{

                Intent pickIntent = new Intent(Intent.ACTION_PICK);
                pickIntent.setType("image/*");
                //we will handle the returned data in onActivityResult
                startActivityForResult(Intent.createChooser(pickIntent, "Select Picture"), Constants.IMG_PICK);

            } catch (ActivityNotFoundException anfe) {
                //display an error message
                String errorMessage = "Whoops - your device doesnot contain external storage!";
                Toast toast = Toast.makeText(getBaseContext(), errorMessage, Toast.LENGTH_SHORT);
                toast.show();
            }

        }


    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    // Callback with the request from calling requestPermissions(...)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        // Make sure it's our original CAMERA request
        if (requestCode == 1) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                try{
                    // call the capture image method where we call the camera of phone
                    captureImage();

                } catch (ActivityNotFoundException anfe) {
                    //display an error message
                    String errorMessage = "Whoops - your device doesn't support capturing images!";
                    Toast toast = Toast.makeText(getBaseContext(), errorMessage, Toast.LENGTH_SHORT);
                    toast.show();
                }


            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }


        else if (requestCode == 2) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                try{

                    Intent pickIntent = new Intent(Intent.ACTION_PICK);
                    pickIntent.setType("image/*");
                    //we will handle the returned data in onActivityResult
                    startActivityForResult(Intent.createChooser(pickIntent, "Select Picture"), Constants.IMG_PICK);

                } catch (ActivityNotFoundException anfe) {

                    //display an error message
                    String errorMessage = "Whoops - your device doesnot contain external storage!";
                    Toast toast = Toast.makeText(getBaseContext(), errorMessage, Toast.LENGTH_SHORT);
                    toast.show();
                }


            } else {
                Toast.makeText(this, "Read storage permission denied", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }




    /**
     * Display image from a path to ImageView
     */

    private void previewCapturedImage() {
        try {
            // bitmap factory
            BitmapFactory.Options options = new BitmapFactory.Options();

            // set the picturePath
            picturePath = fileUri.getPath();

            // decode file path to bitmap
            bitmap = BitmapFactory.decodeFile(fileUri.getPath(), options);

            // call imageCompression method to compress the image
            // assign bitmap to ImageView
            BitmapDrawable bd = imageCompression(bitmap, options);
            studentWallSelectImage.setImageDrawable(bd);
            studentWallSelectImage.setAdjustViewBounds(true);

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }


    /**
     * This method will return the bitmap drawable
     * it compresses the image with the given dimensions
     * @param bitmap bitmap image
     * @param options bitmap factory options
     * @return bitmap drawable to assign to ImageView
     */
    public BitmapDrawable imageCompression(Bitmap bitmap, BitmapFactory.Options options){

        /*// scale the respective image dimensions
        bitmap = scaleImage(options, 400, 400, bitmap);

        // we need bytes from the bitmap
        ByteArrayOutputStream BOAS = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, BOAS); //bm is the bitmap object
        b = BOAS.toByteArray();
*/
        //bitmap = scaleImage(options, 400, 400, bitmap);

        // we need bytes from the bitmap
        ByteArrayOutputStream BOAS = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, BOAS); //bm is the bitmap object
        b = BOAS.toByteArray();

        // check the image if it is greater than the buffer size
        // if so, scale the image to the buffer size
        if(b.length > (1024*1024)){

            bitmap = scaleImage(options, 1024 ,1024, bitmap);

            // we need bytes from the bitmap
            ByteArrayOutputStream BOAS1 = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, BOAS1); //bm is the bitmap object
            b = BOAS1.toByteArray();
        }


        // prepare the inputStream
        is = new ByteArrayInputStream(b);
        Bitmap yourSelectedImage = BitmapFactory.decodeStream(is);

        // assign bitmap to ImageView
        return new BitmapDrawable(getResources(), yourSelectedImage);
    }

    /**
     * this will save the file uri
     * when camera starts it will restart our activity which causes fileUri to be null
     * hence we need to save and retrieve fileUri
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on scren orientation
        // changes
        outState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");
        //System.out.println(fileUri.getPath());
    }

    /**
     * This method will call the camera of device and helps in capturing images
     */
    private void captureImage()
    {
        //use standard intent to capture an image
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        fileUri = getOutputMediaFileUri(Constants.MEDIA_TYPE_IMAGE);

        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        //we will handle the returned data in onActivityResult
        startActivityForResult(captureIntent, Constants.CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }


    /**
     * Creating file uri to store image/video
     */
    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * returning image / video
     */
    private static File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), Constants.appName);


        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(Constants.appName, "Oops! Failed to create, try again "
                        + Constants.appName + " directory");
                return null;
            }
        }

        // Create a mediaTable file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

        // here we generate the random number and add to the end of the filename string
        long random_number = 100000000 + (long)(Math.random()*900000000);

        File mediaFile;
        if (type == Constants.MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + "_" + random_number + ".jpg");

            // set the image location variable
            imageLocation = "IMG_" + timeStamp + "_" + random_number + ".jpg";

            // set the image thumbnail variable
            imageLocation_thumb = "IMG_" + timeStamp + "_" + random_number + "_thumb.jpg";
        }
        else {
            return null;
        }

        return mediaFile;
    }





    /**
     * this background task will post mediaTable to server
     * with the given name and the mediaTable count
     */
    private class PostMedia extends AsyncTask<String, Void, Void> {

        private String Content = "";
        private String Error = null;
        String data = "";
        FileInputStream fileInputStream;
        HttpURLConnection conn = null;
        DataOutputStream dos = null;


        @Override
        protected void onPreExecute() {

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

            StrictMode.setThreadPolicy(policy);

        }

        @Override
        protected Void doInBackground(String... strings) {

            //String upLoadServerUri = "http://orders.esprinkle.com/campus/public/index.php/upload";
            //String upLoadServerUri = "http://orders.esprinkle.com/campus/public/index.php/upload/name.jpg";
            String fileName = picturePath;

            Log.v(Constants.appName, strings[0]);
            Log.v(Constants.appName, "Image"+picturePath);

            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1024 * 1024;
            File sourceFile = new File(fileName);
            if (!sourceFile.isFile()) {

                serverResponseCode = -1;

                Log.v(Constants.appName, "Image not found");
            }
            else{

                Log.v(Constants.appName, "Image found");
            }
            try { // open a URL connection to the Servlet
                fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(strings[0]);
                conn = (HttpURLConnection) url.openConnection(); // Open a HTTP  connection to  the URL
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("file", fileName);
                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\""+ fileName + "\"" + lineEnd);
                dos.writeBytes(lineEnd);

                // get length of bytes
                bytesAvailable = b.length;

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = is.read(b, 0, b.length);
                Log.v(Constants.appName, "Size of bytes : " + b.length);
                dos.write(b, 0, b.length);
                while (bytesRead > 0) {
                    dos.write(b, 0, b.length);
                    bytesAvailable = is.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = is.read(buffer, 0, bufferSize);


                    Log.v(Constants.appName, "check 1");
                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : " + serverResponseMessage + ": " + serverResponseCode);
                if(serverResponseCode == 200){
                    runOnUiThread(new Runnable() {
                        public void run() {

                            Log.v(Constants.appName, "File Upload Completed.");

                            Toast.makeText(StudentWallNewPostActivity.this, "File Upload Complete.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {

                ex.printStackTrace();
                //Toast.makeText(AddEvent.this, "MalformedURLException", Toast.LENGTH_SHORT).show();
                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {

                e.printStackTrace();
                //Toast.makeText(AddEvent.this, "Exception : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Upload file Exception", "Exception : " + e.getMessage());
            }



            return null;


        }


        @Override
        protected void onPostExecute(Void aVoid) {

            if(Error==null){

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if(serverResponseCode == -1){

                            Log.v(Constants.appName, "check -1");
                            Toast.makeText(StudentWallNewPostActivity.this, R.string.errorMsg, Toast.LENGTH_SHORT).show();

                        }
                        if(serverResponseCode == 200){

                            Log.v(Constants.appName, "check 200");
                            Toast.makeText(StudentWallNewPostActivity.this, "File Upload Complete.", Toast.LENGTH_SHORT).show();

                        }
                        //finish();
                    }
                });

            }
            else {

                Log.v(Constants.appName, "check error"+Error);
                Toast.makeText(StudentWallNewPostActivity.this, R.string.errorMsg, Toast.LENGTH_SHORT).show();
                //finish();
            }

            try {
                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();
                Log.v(Constants.appName, "check 2");
            }
            catch (Exception e){

                // checking
            }
        }
    }


    /**
     * This background task will post wall post to server
     * if there are any mediaTable along with post
     * then Response for this request is the unique names of the mediaTable
     * accordingly the mediaTable is sent to server with the given names
     *
     * else
     * only post is created
     */
    private class CreateWallPost extends AsyncTask<String, Void, Void>{

        private String Content = "";
        private String Error = null;
        String data = "";

        @Override
        protected void onPreExecute() {

            /*progressDialog = new ProgressDialog(CollegeWallNewPostActivity.this);
            progressDialog.setMessage("Creating post ..");
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(true);
            progressDialog.show();
*/
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
                        + "&" + URLEncoder.encode(Constants.userObjectId, "UTF-8") + "=" + (urls[1])
                        + "&" + URLEncoder.encode(Constants.wallId, "UTF-8") + "=" + (urls[2])
                        + "&" + URLEncoder.encode(Constants.wallType, "UTF-8") + "=" + (urls[3])
                        + "&" + URLEncoder.encode(Constants.postDescription, "UTF-8") + "=" + (urls[4])
                        + "&" + URLEncoder.encode(Constants.mediaCount, "UTF-8") + "=" + mediaCount;

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

                Log.v(Constants.appName, "Chec");
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

            } catch (Exception ex) {

                ex.printStackTrace();
                Error = ex.getMessage();
                Log.e(Constants.appName, Error);


            } finally {

                try {

                    reader.close();

                } catch (Exception ex) {
                    Error = ex.getMessage();
                }
            }

            return null;
        }

        protected void onPostExecute(Void unused) {

            // clear the dialog
            //progressDialog.dismiss();

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

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            // check the status and proceed with the logic
                            switch (status){

                                // exception occurred
                                case -3:

                                    Toast.makeText(getApplicationContext(), R.string.errorMsg, Toast.LENGTH_SHORT).show();
                                    break;

                                // key mismatch
                                case -2:

                                    Toast.makeText(getApplicationContext(), R.string.errorMsg, Toast.LENGTH_SHORT).show();
                                    break;

                                // data founc
                                case 1:

                                    try {
                                        // set progress
                                        progressBar.setProgress(35);
                                        // check if mediaCount > 0
                                        // if so, get the unique image names
                                        if(mediaCount > 0){

                                            // get JSON Array of 'details'
                                            String imageNamesString = jsonResponse.getString(Constants.details);

                                            // names of images
                                            String[] imageNames;

                                            // get the string removing delimiters from the names
                                            String delimiter = ",";
                                            imageNames = imageNamesString.split(delimiter);

                                            // get the number of images
                                            // post each image with the name
                                            for(int i =0; i < imageNames.length ; i++){

                                                Wall wall = new Wall();
                                                // set progress
                                                progressBar.setProgress(50);

                                                // set the activity title based on post type
                                                if(alumniPost) {

                                                    Toast.makeText(getApplicationContext(), "Posted to alumni wall! wait a while for loading", Toast.LENGTH_SHORT).show();

                                                } else {

                                                    Toast.makeText(getApplicationContext(), "Posted to student wall! wait a while for loading", Toast.LENGTH_SHORT).show();
                                                }

                                                // post the image with the given image name
                                                new PostWallMedia().execute(Routes.postMedia, imageNames[i], wall);

                                                JSONObject jsonObject = jsonResponse.optJSONObject(Constants.post);

                                                // if post is present then fetch the data
                                                if(jsonObject != null) {


                                                    wall.setWallId(jsonObject.getString(Constants.wallId));
                                                    wall.setWallType(jsonObject.getString(Constants.wallType));
                                                    wall.setUserObjectId(jsonObject.getString(Constants.userObjectId));
                                                    wall.setPostDescription(jsonObject.getString(Constants.postDescription));
                                                    wall.setCreatedAt(jsonObject.getString(Constants.createdAt));
                                                    wall.setUpdatedAt(jsonObject.getString(Constants.updatedAt));
                                                    wall.setLikes(jsonObject.getInt(Constants.likes));
                                                    wall.setComments(jsonObject.getInt(Constants.comments));
                                                    wall.setMediaCount(jsonObject.getInt(Constants.mediaCount));
                                                    wall.setMedia(jsonObject.getString(Constants.media));
                                                    wall.setIsActive(jsonObject.getInt(Constants.isActive));
                                                    wall.setUserName(smartCampusDB.getUser().get(Constants.userName).toString());
                                                    wall.setUserImage(smartCampusDB.getUser().get(Constants.media).toString());
                                                }

                                            }
                                        }

                                        else {
                                            // get the jsonResponse of 'post'
                                            // get the JSON object inside Array
                                            JSONObject jsonObject = jsonResponse.optJSONObject(Constants.post);

                                            // if post is present then fetch the data
                                            if (jsonObject != null) {

                                            // input the current new post values to backIntent
                                            backIntent.putExtra(Constants.wallId, jsonObject.getString(Constants.wallId));
                                            backIntent.putExtra(Constants.wallType, jsonObject.getString(Constants.wallType));

                                            backIntent.putExtra(Constants.userObjectId, jsonObject.getString(Constants.userObjectId));
                                            backIntent.putExtra(Constants.postDescription, jsonObject.getString(Constants.postDescription));
                                            backIntent.putExtra(Constants.createdAt, jsonObject.getString(Constants.createdAt));
                                            backIntent.putExtra(Constants.updatedAt, jsonObject.getString(Constants.updatedAt));
                                            backIntent.putExtra(Constants.likes, jsonObject.getInt(Constants.likes));
                                            backIntent.putExtra(Constants.comments, jsonObject.getInt(Constants.comments));
                                            backIntent.putExtra(Constants.mediaCount, jsonObject.getInt(Constants.mediaCount));
                                            backIntent.putExtra(Constants.media, jsonObject.getString(Constants.media));
                                            backIntent.putExtra(Constants.isActive, jsonObject.getInt(Constants.isActive));
                                            backIntent.putExtra(Constants.userName, smartCampusDB.getUser().get(Constants.userName).toString());
                                            backIntent.putExtra(Constants.userImage, smartCampusDB.getUser().get(Constants.media).toString());
                                            backIntent.putExtra(Constants.collegeId, smartCampusDB.getUser().get(Constants.collegeId).toString());

                                            //backIntent.putExtra(Constants.wallId,)

                                            // set the activity title based on post type
                                            if(alumniPost) {

                                                Toast.makeText(getApplicationContext(), "Posted to alumni wall! wait a while for loading", Toast.LENGTH_SHORT).show();

                                            } else {

                                                Toast.makeText(getApplicationContext(), "Posted to student wall! wait a while for loading", Toast.LENGTH_SHORT).show();
                                            }

                                            // set the result for backIntent
                                            // send 1 as success message for post creation
                                            setResult(1, backIntent);
                                            finish();
                                            } else {

                                                // no post created
                                                Toast.makeText(getApplicationContext(), "Oops! something went wrong, try again later", Toast.LENGTH_SHORT).show();
                                                setResult(0, backIntent);
                                                finish();
                                            }

                                        }


                                }
                                catch(JSONException e){

                                    Log.e(Constants.appName, e.getMessage());
                                    Toast.makeText(getApplicationContext(), "Oops! something went wrong, try again later", Toast.LENGTH_SHORT).show();
                                    // send 0 as no post is created
                                    setResult(0, backIntent);
                                    finish();

                                }

                            break;


                            }
                        }
                    });





                } catch (JSONException e) {

                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Oops! something went wrong, try again later", Toast.LENGTH_SHORT).show();

                    setResult(0, backIntent);
                    finish();

                }

            }
        }
    }


    /**
     * this background task will post mediaTable to server
     * with the given name and the mediaTable count
     */
    private class PostWallMedia extends AsyncTask<Object, Void, Void> {

        private String Content = "";
        private String Error = null;
        String data = "";
        FileInputStream fileInputStream;
        HttpURLConnection conn = null;
        DataOutputStream dos = null;

        // return object
        Wall wall = new Wall();

        @Override
        protected void onPreExecute() {

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

            StrictMode.setThreadPolicy(policy);

        }

        @Override
        protected Void doInBackground(Object... strings) {

            // get the return object
            wall = (Wall) strings[2];

            //String upLoadServerUri = "http://orders.esprinkle.com/campus/public/index.php/upload";
            //String upLoadServerUri = "http://orders.esprinkle.com/campus/public/index.php/upload/name.jpg";
            String fileName = picturePath;

            //Log.v(Constants.appName, strings[0]);
            //Log.v(Constants.appName, "Image"+picturePath);

            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 4 * 1024 * 1024;
            File sourceFile = new File(fileName);
            if (!sourceFile.isFile()) {

                serverResponseCode = -1;

                //Log.v(Constants.appName, "Image not found");
            }
            else{

                //Log.v(Constants.appName, "Image found");
            }
            try {

                // Set Request parameter
                data += "?&" + URLEncoder.encode(Constants.name, "UTF-8") + "=" + (strings[1]);

                // open a URL connection to the Servlet
                fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(strings[0] + data);
                conn = (HttpURLConnection) url.openConnection(); // Open a HTTP connection to the URL
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("file", fileName);
                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\""+ fileName + "\"" + lineEnd);
                dos.writeBytes(lineEnd);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // set progress
                        progressBar.setProgress(60);
                    }
                });

                // get length of bytes
                bytesAvailable = b.length;

                bufferSize = Math.max(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = is.read(b, 0, b.length);
                //Log.v(Constants.appName, "Size of bytes : " + b.length);
                dos.write(b, 0, b.length);
                while (bytesRead > 0) {
                    dos.write(b, 0, b.length);
                    bytesAvailable = is.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = is.read(buffer, 0, bufferSize);


                    Log.v(Constants.appName, "check 1");
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        // set progress
                        progressBar.setProgress(70);
                    }
                });

                // send multipart form data necessary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        // set progress
                        progressBar.setProgress(80);
                    }
                });

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : " + serverResponseMessage + ": " + serverResponseCode);
                if(serverResponseCode == 200){
                    runOnUiThread(new Runnable() {
                        public void run() {

                            // set progress
                            progressBar.setProgress(90);
                            //Log.v(Constants.appName, "File Upload Completed.");

                            //Toast.makeText(CollegeWallNewPostActivity.this, "File Upload Complete.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {

                ex.printStackTrace();
                //Toast.makeText(AddEvent.this, "MalformedURLException", Toast.LENGTH_SHORT).show();
                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {

                e.printStackTrace();
                //Toast.makeText(AddEvent.this, "Exception : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Upload file Exception", "Exception : " + e.getMessage());
            }



            return null;


        }


        @Override
        protected void onPostExecute(Void aVoid) {

            if(Error==null){

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if(serverResponseCode == -1){

                            //Log.v(Constants.appName, "check -1");
                            Toast.makeText(StudentWallNewPostActivity.this, R.string.errorMsg, Toast.LENGTH_SHORT).show();

                        }
                        if(serverResponseCode == 200){

                            // set progress
                            progressBar.setProgress(100);

                            //Log.v(Constants.appName, "check 200");
                            //Toast.makeText(CollegeWallNewPostActivity.this, "File Upload Complete.", Toast.LENGTH_SHORT).show();

                            // input the current new post values to backIntent
                            backIntent.putExtra(Constants.wallId, wall.getWallId());
                            backIntent.putExtra(Constants.wallType, wall.getWallType());

                            backIntent.putExtra(Constants.userObjectId, wall.getUserObjectId());
                            backIntent.putExtra(Constants.postDescription, wall.getPostDescription());
                            backIntent.putExtra(Constants.createdAt, wall.getCreatedAt());
                            backIntent.putExtra(Constants.updatedAt, wall.getUpdatedAt());
                            backIntent.putExtra(Constants.likes, wall.getLikes());
                            backIntent.putExtra(Constants.comments, wall.getComments());
                            backIntent.putExtra(Constants.mediaCount, wall.getMediaCount());
                            backIntent.putExtra(Constants.media, wall.getMedia());
                            backIntent.putExtra(Constants.isActive, wall.getIsActive());
                            backIntent.putExtra(Constants.userName, smartCampusDB.getUser().get(Constants.userName).toString());
                            backIntent.putExtra(Constants.userImage, smartCampusDB.getUser().get(Constants.media).toString());

                            //backIntent.putExtra(Constants.wallId,)

                            //Toast.makeText(getApplicationContext(), "Posted to college wall! wait a while for loading", Toast.LENGTH_SHORT).show();

                            // set the result for backIntent
                            // send 1 as success message for post creation
                            setResult(1, backIntent);
                            finish();
                        }
                        //finish();
                    }
                });

            }
            else {

                Log.v(Constants.appName, "check error"+Error);
                Toast.makeText(StudentWallNewPostActivity.this, R.string.errorMsg, Toast.LENGTH_SHORT).show();
                //finish();
            }

            try {
                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();
            }
            catch (Exception e){

                // checking
            }
        }
    }


    /*// get calculated sample size
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }*/

    // Scale image to desired dimensions
    public Bitmap scaleImage(BitmapFactory.Options options, int reqHeight, int reqWidth, Bitmap bitmap) {

        // Raw height and width of image
        height = options.outHeight;
        width = options.outWidth;
        Log.v(Constants.appName, "Actual Height: "+ height);
        Log.v(Constants.appName, "Actual Width: "+ width);

        Bitmap scaledBitmap = bitmap.createScaledBitmap(bitmap, width-1, height-1, false);

        // verify whether image dimensions are meeting our desired dimension
        while (height > reqHeight || width > reqWidth) {

            // check if image is approximate to our desired dimension
            // if so do not compress
            if(Math.abs(height-reqHeight) > 100 || Math.abs(width-reqWidth) > 100) {

                height = height / 2;
                width = width / 2;
            }

            // scale the image to reduced dimensions
            scaledBitmap = bitmap.createScaledBitmap(bitmap, width, height, false);
        }

        Log.v(Constants.appName, "Compressed Height: "+ height);
        Log.v(Constants.appName, "Compressed Width: "+ width);

        return scaledBitmap;

    }

    // get calculated sample size
    public static int scaleWidth(BitmapFactory.Options options, int reqWidth) {
        // Raw height and width of image
        int actualWidth = options.outWidth;

        while (actualWidth > reqWidth) {

            actualWidth = actualWidth / 2;

        }

        return actualWidth;
    }

}
