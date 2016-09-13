package svecw.svecw;

import android.Manifest;
import android.app.Activity;
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
import android.graphics.Typeface;
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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
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

import internaldb.SmartCampusDB;
import utils.Constants;
import utils.Routes;
import utils.Snippets;

/**
 * Created by Pavan_Kusuma on 4/17/2015.
 */
public class NewMessageActivity extends AppCompatActivity {

    // views from layout
    TextView selectBranch, cancel, send, sendStudent, sendFaculty;
    EditText newMessage;
    Spinner messageBranch, messageYear, messageSemester;
    LinearLayout branchLayout;
    ImageView newMessageSelectImage;

    // progress dialog
    ProgressDialog progressDialog;

    // object of internal db
    SmartCampusDB smartCampusDB = new SmartCampusDB(this);

    // read request code of content provider
    private static final int READ_REQUEST_CODE = 42;

    // uri of selected file
    Uri uri = null;

    // layout inflater
    LayoutInflater layoutInflater;

    // to message
    String to = Constants.student;
    String selectedBranch="";
    int selectedYear=0;
    int selectedSemester=0;
    String message="";
    JSONObject jsonResponse;
    int status;
    int mediaCount;
    InputStream is;

    int serverResponseCode;

    private Uri fileUri; // file url to store image when camera is used
    public String picturePath; // url for the image when gallery is used to pick image
    public static String imageLocation, imageLocation_thumb; // variable for storing image location

    public static Bitmap bitmap;
    public static String base64String; // encoded image string that will be uploaded

    //SmartDB smartDB = new SmartDB(this);
    byte[] b = Constants.null_indicator.getBytes();
    int height, width;

    Intent backIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_message_activity);

        // get the toolbar for the activity
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);

        // change the title according to the activity
        TextView title = (TextView) toolbar.findViewById(R.id.appName);
        title.setText(getResources().getString(R.string.newMessage));

        // set the toolbar to the actionBar
        setSupportActionBar(toolbar);

        // get the action bar
        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setWindowTitle(""); // hide the main title

        // object for LayoutInflater
        layoutInflater = LayoutInflater.from(this);

        backIntent = getIntent();

        selectBranch = (TextView) findViewById(R.id.selectBranch);
        newMessage = (EditText) findViewById(R.id.writeMessage);
        cancel = (TextView) findViewById(R.id.cancelMessage);
        send = (TextView) findViewById(R.id.sendMessage);
        newMessageSelectImage = (ImageView) findViewById(R.id.newMessageSelectImage);
        messageBranch = (Spinner) findViewById(R.id.messageBranch);
        messageYear = (Spinner) findViewById(R.id.messageYear);
        messageSemester = (Spinner) findViewById(R.id.messageSemester);
        sendStudent = (TextView) findViewById(R.id.sendStudent);
        sendFaculty = (TextView) findViewById(R.id.sendFaculty);
        branchLayout = (LinearLayout) findViewById(R.id.branchLayout);

        Typeface typeface = Typeface.createFromAsset(getAssets(), Constants.FONTNAME);
        selectBranch.setTypeface(typeface);
        newMessage.setTypeface(typeface);
        send.setTypeface(typeface);
        cancel.setTypeface(typeface);

        // set the focusable to true programmatically
        newMessage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                view.setFocusable(true);
                view.setFocusableInTouchMode(true);
                return false;
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // close the activity
                setResult(0, backIntent);
                finish();

            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {

                    // check if branch, semester, year are selected
                    if (!messageBranch.getSelectedItem().toString().contentEquals("Select branch") || !messageSemester.getSelectedItem().toString().contentEquals("Select semester") || !messageYear.getSelectedItem().toString().contentEquals("Select year")) {

                        // check if message is written
                        if (newMessage.getText().toString().length() > 0) {

                            selectedBranch = messageBranch.getSelectedItem().toString();
                            message = newMessage.getText().toString();

                            if (to.contentEquals(Constants.student)) {

                                selectedSemester = Integer.valueOf(messageSemester.getSelectedItem().toString());
                                selectedYear = Integer.valueOf(messageYear.getSelectedItem().toString());

                            } else {

                                selectedSemester = 0;
                                selectedYear = 0;
                            }

                            // we have 2 classes to execute based on mediaTable
                            // if mediaTable exists then 'CreateWallPostWithMedia' is executed
                            // else 'CreateWallPostWithoutMedia' is executed

                            // check if the mediaTable file is present
                            // if so enter the randonObjectId
                            if (Arrays.equals(b, Constants.null_indicator.getBytes())) {

                                mediaCount = 0;


                            } else {

                                mediaCount = 1;
                            }

                            // new message url
                            // as this is broadcast message
                            // there will not be groupId and toUserObjectId
                            String messageURL = Routes.createMessage + Constants.key + "/" + Snippets.getUniqueMessageId() + "/" +
                                    Constants.Broadcast + "/" + Snippets.escapeURIPathParam(message) + "/" + smartCampusDB.getUser().get(Constants.userObjectId) + "/" +
                                    "-" + "/" + "-" + "/" + selectedYear + "/" + selectedBranch + "/" + selectedSemester + "/" + mediaCount;

                            // post new message
                            new PostNewMessage().execute(Routes.createMessage, Snippets.getUniqueMessageId(), Constants.Broadcast, Snippets.escapeURIPathParam(message), smartCampusDB.getUser().get(Constants.userObjectId).toString());
                        }
                    }
                    // if none of spinner is selected, show the error toast
                    else {

                        Toast.makeText(NewMessageActivity.this, "Select Branch & Semester & Year", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (Exception e){

                    // do nothing
                }
            }

        });

        sendStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // change the color of name search factor to blue
                // to show it active
                sendStudent.setTextColor(Color.WHITE);
                sendStudent.setBackgroundColor(getResources().getColor(R.color.lightBlue));

                sendFaculty.setTextColor(getResources().getColor(R.color.lightBlue));
                sendFaculty.setBackgroundResource(R.drawable.button_rounded_shape_blue); //BackgroundColor(Color.WHITE);

                // set visible the year and semester views
                branchLayout.setVisibility(View.VISIBLE);

                // message will be send to student
                to = Constants.student;
            }
        });

        sendFaculty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // change the color of name search factor to blue
                // to show it active
                sendFaculty.setTextColor(Color.WHITE);
                sendFaculty.setBackgroundColor(getResources().getColor(R.color.lightBlue));

                sendStudent.setTextColor(getResources().getColor(R.color.lightBlue));
                sendStudent.setBackgroundResource(R.drawable.button_rounded_shape_blue); //BackgroundColor(Color.WHITE);

                // set visible the year and semester views
                branchLayout.setVisibility(View.GONE);

                // message will be send to faculty
                to = Constants.faculty;

            }
        });

        newMessageSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // show the dialog to choose camera or gallery
                // custom dialog
                final AlertDialog.Builder menuAlert = new AlertDialog.Builder(NewMessageActivity.this);
                final String[] menuList = {"Camera", "Gallery"};
                menuAlert.setTitle("Select from");
                menuAlert.setItems(menuList, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        switch (item) {
                            // camera selected
                            case 0:
                                // post from camera
                                // clear bytes if any
                                b = Constants.null_indicator.getBytes();
                                postingFromCamera();
                                break;

                            // gallery selected
                            case 1:

                                // post from gallery
                                // clear bytes if any
                                b = Constants.null_indicator.getBytes();
                                postingFromGallery();
                                break;
                        }
                    }


                });
                AlertDialog menuDrop = menuAlert.create();
                menuDrop.show();
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
            newMessageSelectImage.setImageDrawable(bd);

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
                    if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
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

                    } catch (ActivityNotFoundException ante) {
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

                } catch (ActivityNotFoundException ante) {
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
     * Send message
     */
    class NewMessageBG extends AsyncTask<String, Float, String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(NewMessageActivity.this);
            progressDialog.setMessage("Creating post ..");
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            try{

                //byte[] data1 =  "This is awesome".getBytes();
                final ParseFile file = new ParseFile("image.txt", b);
                file.saveInBackground();

                ParseObject parseObject = new ParseObject(Constants.messagesTable);

                parseObject.put(Constants.userObjectId, smartCampusDB.getUser().get(Constants.objectId));
                parseObject.put(Constants.userName, smartCampusDB.getUser().get(Constants.userName));
                parseObject.put(Constants.branch, selectedBranch);

                if(to.contentEquals(Constants.student)) {
                    parseObject.put(Constants.year, Integer.valueOf(selectedYear));
                    parseObject.put(Constants.semester, Integer.valueOf(selectedSemester));
                }
                else {
                    parseObject.put(Constants.year, 0);
                    parseObject.put(Constants.semester, 0);
                }

                parseObject.put(Constants.message, message);
                parseObject.put(Constants.to, to);
                parseObject.put(Constants.isGroup, false);
                parseObject.put(Constants.groupId, Constants.null_indicator);
                parseObject.put(Constants.mediaFile, file);

                parseObject.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {

                        if(e==null){

                            finish();
                        }
                    }
                });


            }
            catch (Exception e){

                progressDialog.dismiss();
                Log.e(Constants.appName, e.getMessage());

                runOnUiThread(new Runnable() {
                    public void run() {

                        Toast.makeText(getApplicationContext(), "Please try again", Toast.LENGTH_SHORT).show();
                    }
                });

                // closing this screen
                finish();
            }

            return null;
        }


        @Override
        protected void onPostExecute(String s) {
            //super.onPostExecute(s);

            // dismiss the dialog once done
            progressDialog.dismiss();

            // success message toast
            Toast.makeText(getApplicationContext(), "Message sent", Toast.LENGTH_SHORT).show();

            // closing this screen
            finish();
        }
    }

    /**
     * this method will check for device camera
     * @return boolean
     */
    private boolean isDeviceSupportCamera() {
        if (getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    /**
     * Display image from a path to ImageView
     */

    private void previewCapturedImage() {
        try {
            // bimatp factory
            BitmapFactory.Options options = new BitmapFactory.Options();

            // set the picturePath
            picturePath = fileUri.getPath();

            // decode file path to bitmap
            bitmap = BitmapFactory.decodeFile(fileUri.getPath(), options);

            // call imageCompression method to compress the image
            // assign bitmap to ImageView
            BitmapDrawable bd = imageCompression(bitmap, options);
            newMessageSelectImage.setImageDrawable(bd);


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
        bitmap = scaleImage(options, 500 ,500, bitmap);

        // we need bytes from the bitmap
        ByteArrayOutputStream BOAS = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, BOAS); //bm is the bitmap object
        b = BOAS.toByteArray();

        // prepare the inputStream
        is = new ByteArrayInputStream(b);
        Bitmap yourSelectedImage = BitmapFactory.decodeStream(is);

        // assign bitmap to ImageView
        return new BitmapDrawable(getResources(), yourSelectedImage);*/

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
     * Post a new message to server
     */
    private class PostNewMessage extends AsyncTask<String, Void, Void>{

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
                        + "&" + URLEncoder.encode(Constants.messageId, "UTF-8") + "=" + (urls[1])
                        + "&" + URLEncoder.encode(Constants.messageType, "UTF-8") + "=" + (urls[2])
                        + "&" + URLEncoder.encode(Constants.message, "UTF-8") + "=" + (urls[3])
                        + "&" + URLEncoder.encode(Constants.fromUserObjectId, "UTF-8") + "=" + (urls[4])
                        + "&" + URLEncoder.encode(Constants.toUserObjectId, "UTF-8") + "=" + "-"
                        + "&" + URLEncoder.encode(Constants.groupId, "UTF-8") + "=" + "-"
                        + "&" + URLEncoder.encode(Constants.year, "UTF-8") + "=" + selectedYear
                        + "&" + URLEncoder.encode(Constants.branch, "UTF-8") + "=" + selectedBranch
                        + "&" + URLEncoder.encode(Constants.semester, "UTF-8") + "=" + selectedSemester
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

                                // data found
                                case 1:

                                    try {
                                        // check if mediaCount > 0
                                        // if so, get the unique image names
                                        // and request upload
                                        // else do nothing
                                        if (mediaCount > 0) {

                                            // get JSON Array of 'details'
                                            String imageNamesString = jsonResponse.getString(Constants.details);

                                            // names of images
                                            String[] imageNames;

                                            // get the string removing delimiters from the names
                                            String delimiter = ",";
                                            imageNames = imageNamesString.split(delimiter);

                                            // get the number of images
                                            // post each image with the name
                                            for (int i = 0; i < imageNames.length; i++) {

                                                // post the image with the given image name
                                                new PostWallMedia().execute(Routes.postMedia, imageNames[i]);

                                            }

                                        }

                                        // get the jsonResponse of 'post'
                                        // get the JSON object inside Array
                                        JSONObject jsonObject = jsonResponse.optJSONObject(Constants.message);

                                        // if post is present then fetch the data
                                        if(jsonObject != null) {

                                            // input the current new post values to backIntent
                                            backIntent.putExtra(Constants.messageId, jsonObject.getString(Constants.messageId));
                                            backIntent.putExtra(Constants.messageType, jsonObject.getString(Constants.messageType));

                                            backIntent.putExtra(Constants.message, jsonObject.getString(Constants.message));
                                            backIntent.putExtra(Constants.fromUserObjectId, jsonObject.getString(Constants.fromUserObjectId));
                                            backIntent.putExtra(Constants.toUserObjectId, jsonObject.getString(Constants.toUserObjectId));
                                            backIntent.putExtra(Constants.groupId, jsonObject.getString(Constants.groupId));
                                            backIntent.putExtra(Constants.year, jsonObject.getInt(Constants.year));
                                            backIntent.putExtra(Constants.branch, jsonObject.getString(Constants.branch));
                                            backIntent.putExtra(Constants.semester, jsonObject.getInt(Constants.semester));
                                            backIntent.putExtra(Constants.createdAt, jsonObject.getString(Constants.createdAt));
                                            backIntent.putExtra(Constants.updatedAt, jsonObject.getString(Constants.updatedAt));
                                            backIntent.putExtra(Constants.mediaCount, jsonObject.getInt(Constants.mediaCount));
                                            backIntent.putExtra(Constants.media, jsonObject.getString(Constants.media));
                                            backIntent.putExtra(Constants.groupId, jsonObject.getString(Constants.groupId));

                                            backIntent.putExtra(Constants.userName, smartCampusDB.getUser().get(Constants.userName).toString());

                                            Toast.makeText(getApplicationContext(), "Message sent! wait a while for loading", Toast.LENGTH_SHORT).show();
                                            setResult(1, backIntent);
                                            finish();
                                        }
                                        else {

                                            // no post created
                                            Toast.makeText(getApplicationContext(), "Oops! something went wrong, try again later", Toast.LENGTH_SHORT).show();
                                            setResult(0, backIntent);
                                            finish();
                                        }

                                    }
                                    catch (Exception e){

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
    private class PostWallMedia extends AsyncTask<String, Void, Void> {

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

            //Log.v(Constants.appName, strings[0]);
            //Log.v(Constants.appName, "Image"+picturePath);

            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
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
                            Toast.makeText(NewMessageActivity.this, R.string.errorMsg, Toast.LENGTH_SHORT).show();

                        }
                        if(serverResponseCode == 200){

                            //Log.v(Constants.appName, "check 200");
                            //Toast.makeText(CollegeWallNewPostActivity.this, "File Upload Complete.", Toast.LENGTH_SHORT).show();

                        }
                        //finish();
                    }
                });

            }
            else {

                Log.v(Constants.appName, "check error"+Error);
                Toast.makeText(NewMessageActivity.this, R.string.errorMsg, Toast.LENGTH_SHORT).show();
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

}
