package com.cs360.netwiginventoryapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/*******************************************************************
 Author      : Chad Netwig
 App Name    : Chad's Inventory App
 Version     : 1.0
 Date        : March 31, 2022
 Updated     : April 14, 2022
             :
 Description : Simple inventory app with following functionality:
             : 1. SQLite CRUD functionality
             : 2. User login screen with authentication
             : 3. Inventory gridview screen using RecyclerView
             : 4. Ability to add, delete, update, and read
             :    inventory items
             : 5. SMS text messaging alerts for low inventory
             :
             : Controller logic for MainActivity (login) Layout
             : Comments have been added throughout to explain logic
********************************************************************/

public class MainActivity extends AppCompatActivity {

    // var for SQLite database
    private InventoryDatabase mInventoryDb;
    // variables for login screen EditText
    private EditText userNameEdt;
    private EditText passwordEdt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Instantiates Singleton instance of SQLite database
        mInventoryDb = InventoryDatabase.getInstance(getApplicationContext());

        // initialize EditText vars on Login screen
        userNameEdt = findViewById(R.id.username);
        passwordEdt = findViewById(R.id.password);
    }


    // responds to clicking "Login" button by authenticating username and password against the SQLite 'users' table
    // if user does not exist, creates new user account in database
    public void onLoginClick(View view) {

        // get data from all edit text fields on login screen
        String userName = userNameEdt.getText().toString().trim();
        String password = passwordEdt.getText().toString();

        // validate username and password EditText entries
        if (userName.isEmpty() || password.isEmpty()) {
            if (userName.isEmpty()){
                Toast.makeText(MainActivity.this, "Please enter a username", Toast.LENGTH_SHORT).show();
            } else if (password.isEmpty()){
                Toast.makeText(MainActivity.this, "Please enter a password", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        else {
            // check to see if username exists in SQLite db
            if (mInventoryDb.userLookup(userName)){
                Log.i("USER", "User found!");
                // check password
                if (mInventoryDb.pwLookup(userName, password)){
                    Log.i("USER", "User passed authentication!");
                    // Launch Inventory layout after user passed authentication
                    launchInventoryActivity();
                    //Intent myIntent = new Intent(this, InventoryActivity.class);
                    //startActivity(myIntent);
                } else {
                    Toast.makeText(MainActivity.this, "Password Incorrect!", Toast.LENGTH_SHORT).show();
                    passwordEdt.setText("");
                }

            } else {
                Log.i("USER", "User not found!");
                // call dialog to prompt for new user creation
                newUserDialog(userName, password);
            }
        }
    }


    // shows dialog box to prompt user to create the new user account, if select 'yes' launches
    // the Inventory Activity layout
    public void newUserDialog(String uname, String  pw) {

        // Create the object of AlertDialog Builder class
        AlertDialog.Builder builder
                = new AlertDialog
                .Builder(MainActivity.this);

        // Set the message show for the Alert time
        builder.setMessage("Do you want to create a new account for " + uname + " ?");

        // Set Alert Title
        builder.setTitle("Username Does Not Exist");

        // Set Cancelable false
        builder.setCancelable(false);

        // Set the positive button with 'yes' name
        // Set OnClickListener method using DialogInterface interface
        builder
                .setPositiveButton(
                        "Yes",
                        new DialogInterface
                                .OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which)
                            {
                                 mInventoryDb.addUser(uname, pw);
                                Toast.makeText(MainActivity.this, "New Account Created!", Toast.LENGTH_SHORT).show();

                                // Launch Inventory layout after user account successfully created
                                launchInventoryActivity();
                            }
                        });

        // Set the Negative button with 'No' name
        // Set OnClickListener method using DialogInterface interface
        builder
                .setNegativeButton(
                        "No",
                        new DialogInterface
                                .OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which)
                            {
                                // If user clicked nod ialog box is canceled
                                Log.i("USER", "You Clicked No");
                                dialog.cancel();
                            }
                        });

        // Create the Alert dialog
        AlertDialog alertDialog = builder.create();

        // Show the Alert Dialog box
        alertDialog.show();
    }


    // helper function to launch the Inventory Activity with a new Intent object
    public void launchInventoryActivity() {
        Intent myIntent = new Intent(this, InventoryActivity.class);
        startActivity(myIntent);

    }

} // end MainActivity()