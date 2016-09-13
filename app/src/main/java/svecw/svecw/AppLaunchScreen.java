package svecw.svecw;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import internaldb.SmartCampusDB;

/**
 * Created by Pavan_Kusuma on 4/26/2015.
 */
public class AppLaunchScreen extends AppCompatActivity {

    // views from layout
    TextView launchText;
    Button guest, login;

    // instance of internal db
    SmartCampusDB smartCampusDB = new SmartCampusDB(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_launch_screen1);

        // get views from layout
        launchText = (TextView) findViewById(R.id.launchText);
        login = (Button) findViewById(R.id.login);
        guest = (Button) findViewById(R.id.guest);

        Typeface typefaceRegular = Typeface.createFromAsset(this.getResources().getAssets(), "fonts/Roboto-Regular.ttf");
        Typeface typefaceLight = Typeface.createFromAsset(this.getResources().getAssets(), "fonts/Roboto-Light.ttf");
        launchText.setTypeface(typefaceLight);
        login.setTypeface(typefaceRegular);
        guest.setTypeface(typefaceRegular);



/*

        // check if the user is already registered
        if(smartCampusDB.getUser().size() > 0){

            // check if the email of user is verified
            if(smartCampusDB.isUserVerified()){

                Log.v(Constants.appName, "EmailVerified : "+ smartCampusDB.isUserVerified());
                // Navigate the user to home screen
                Intent homeIntent = new Intent(getApplicationContext(), NewHomeActivity.class);
                startActivity(homeIntent);
                finish();
            }
            // show the user Pending Verification Activity
            else{

                Log.v(Constants.appName, "EmailVerified : "+ smartCampusDB.isUserVerified());

                Intent verificationIntent = new Intent(getApplicationContext(), PendingUserVerificationActivity.class);
                startActivity(verificationIntent);
                finish();
            }

        }

*/


        // navigate the user to home activity
        if(smartCampusDB.getUser().size() > 0){

            Intent homeIntent = new Intent(getApplicationContext(), WelcomeActivity.class);
            startActivity(homeIntent);
            finish();
        }

        // navigate to login activity
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // navigate to Login showing info
                //navigateToLogin();

                Intent loginIntent = new Intent(getApplicationContext(), GlobalLoginActivity.class);
                startActivity(loginIntent);
                finish();
            }
        });

        // navigate to guest activity
        guest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent guestIntent = new Intent(getApplicationContext(), GuestActivity.class);
                startActivity(guestIntent);
            }
        });




    }

    // navigate to Login showing the info in popup
    public void navigateToLogin(){

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                switch (which) {

                    case DialogInterface.BUTTON_POSITIVE:
                        // Yes button clicked

                        Intent loginIntent = new Intent(getApplicationContext(), GlobalLoginActivity.class);
                        startActivity(loginIntent);
                        finish();
                        //Toast.makeText(EditProfileActivity.this, "Yes Clicked", Toast.LENGTH_LONG).show();
                        break;

                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Only Student and Faculty can login to the app!")
                .setPositiveButton("Ok", dialogClickListener).show();


    }
}
