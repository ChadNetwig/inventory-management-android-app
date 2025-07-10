package com.cs360.netwiginventoryapp;

import static android.view.Gravity.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.List;

/*******************************************************************
 Author      : Chad Netwig
 App Name    : Chad's Inventory App
 Version     : 1.0
 Date        : March 31, 2022
 Updated     : April 14, 2022
             :
 Description : Simple inventory app
             : Controller logic for Inventory Layout
             : Comments have been added throughout to explain logic
********************************************************************/

public class InventoryActivity extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener {

    MyRecyclerViewAdapter adapter;          // adapter object from custom MyRecyclerViewAdapter class
    private InventoryDatabase mInventoryDb; // var for SQLite database

    // vars hold inventory item name and qty
    private String item_name_str;
    private String qty_str;
    private List<String> inventory;

    // vars hold selected position in the RecyclerView
    private int selectedPosition = 0;
    private String selectedValue;

    // *** CONSTANTS ***
    // arbitrary constant for SMS permission code
    private static final int SMS_PERMISSION_CODE = 100;
    // arbitrary constant for sending SMS notification if an inventory item reaches this quantity
    private static final int LOW_INVENTORY_ALERT = 2;
    // SMS destination address constant (set to phone number of Android Emulator)
    private static final String SMS_DESTINATION = "6505551212";

    // added Menu obj for edit/delete icons enabled/disabled
    private Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        // Instantiates Singleton instance of SQLite database
        mInventoryDb = InventoryDatabase.getInstance(getApplicationContext());

        // Reads Inventory Table and populates the data String Array used for RecyclerView
        String[] data = loadInventoryTable();

        // set up the RecyclerView
        RecyclerView recyclerView = findViewById(R.id.inventory_recycler);
        int numberOfColumns = 2;
        recyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        adapter = new MyRecyclerViewAdapter(this, data);

        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        // sets default selected value at position 0 in RecyclerView if there is at least 1 item
        if (data.length > 0) {
            selectedValue = adapter.getItem(selectedPosition);
        }

    } // end onCreate()


    // inflates app action bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.appbar_menu, menu);
        mMenu = menu;
        Log.i("LOADINVENTORY", "mData length : " + adapter.mData.length);
        if (adapter.mData.length > 0) {
            // enables pencil and trashcan icons on app menu if there is inventory
            mMenu.findItem(R.id.action_delete).setEnabled(true);
            mMenu.findItem(R.id.action_edit).setEnabled(true);
        }
        return true;
    }


    // Implements reaction to tapping action bar items
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                // edit action bar icon
                Log.i("MENU", "Edit Icon Clicked!");
                // opens dialog box to edit qty value
                editAction();
                return true;

            case R.id.action_delete:
                // delete action bar icon
                Log.i("MENU", "Delete Icon Clicked!");
                // deletes the item selected in the RecyclerView
                deleteAction();
                return true;

            case R.id.action_new_item:
                // add new item menu option
                Log.i("MENU", "Add New Inventory Item Clicked!");
                // open dialog box to add a new inventory item
                showInventoryDialogBox();
                return true;

            case R.id.action_notify:
                // notifications menu option
                Log.i("MENU", "Notifications Menu Item Clicked!");
                // Checks for SMS permissions and prompts user to allow/deny the permission
                checkPermission(Manifest.permission.SEND_SMS, SMS_PERMISSION_CODE);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    // Implements onItemClick for abstract interface MyRecyclerViewAdapter.ItemClickListener
    @Override
    public void onItemClick(View view, int position) {
        // Information to Logcat for debugging
        Log.i("CLICK", "You clicked number " + adapter.getItem(position) + ", which is at cell position " + position);
        Log.i("CLICK", "Previous clicked number  is " + adapter.previousPosition);


        // re-bind current position and previous position so that selected/deselected colors change
        adapter.notifyItemChanged(position);
        adapter.notifyItemChanged(adapter.previousPosition);

        // sets selected position and field value
        selectedPosition = position;
        selectedValue = adapter.getItem(position);

    } // end itemOnClick()


    // FAB Button for adding inventory
    public void addButtonClick(View view) {
        Log.i("TAG", "You clicked FAB");
        // creates AlertDialog to pass two EdtiText values into inventory variables
        showInventoryDialogBox();
    }


    // helper function to parse string for integer and return int
    public int validateInteger(String str) {
        int qty_int = 0;
        String qty_str;

        char[] chars = str.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (char c : chars) {
            if (Character.isDigit(c)) {
                sb.append(c);
            }
        }
        if (sb.length() > 0){
            qty_str = sb.toString();
            qty_int = Integer.parseInt(qty_str);
        }
        return qty_int;
    }

    // helper function to convert ArrayList of Strings into String Array
    public String[] loadInventoryTable(){

        // Reads in inventory table to an ArrayList of Strings
        inventory = mInventoryDb.readInventory();
        Log.i("LOADINVENTORY", "Inventory table loaded!");

        // Converts ArrayList to String Array
        String[] inventory_arr = new String[inventory.size()];
        for (int i = 0; i < inventory.size(); i++) {
            inventory_arr[i] = inventory.get(i);
        }
        return inventory_arr;
    }


    // Responds to tapping pencil icon (edit) in App Bar by opening a dialog box with EditText
    // and passes edited quantity value to updateInventoryItemQuantity
    public void editAction() {
        // If odd position (quantity column) in RecyclerView is selected displays error, else performs edit
        if (selectedPosition % 2 == 0) {
            Toast.makeText(InventoryActivity.this, "Cannot edit the inventory name!", Toast.LENGTH_SHORT).show();
        } else {

            AlertDialog.Builder alertdialog = new AlertDialog.Builder( InventoryActivity.this );
            alertdialog.setTitle("Enter New Quantity");

            final EditText qty = new EditText(InventoryActivity.this);

            qty.setHint("Quantity");  // EditText hint
            qty.setGravity(CENTER);
            qty.setBackgroundResource(R.drawable.inventory_addbox);

            //set up in a linear layout
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            // setup lin layout parameters
            layoutParams.setMargins( 20,20,20,20); //set margin

            LinearLayout lp = new LinearLayout( getApplicationContext() );
            lp.setOrientation(LinearLayout.VERTICAL);

            lp.addView(qty, layoutParams );

            alertdialog.setView(lp);
            alertdialog.setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    int qty_int;
                    qty_str=qty.getText().toString().trim();

                    // convert qty string to integer
                    qty_int = validateInteger(qty_str);

                    if (qty_int > 0) {
                        int id = -1;
                        // gets the inventory item name associated with the currently-selected quantity
                        String item_name_str = adapter.getItem(selectedPosition - 1);

                        Log.i("SQL", "item name of selected qty: " + item_name_str);

                        // get primary key for the selected selected quantity
                        id = mInventoryDb.inventoryPrimaryKeyLookup(item_name_str);
                        Log.i("SQL", "primary key val: " + Integer.toString(id));

                        boolean updateSuccess;
                        updateSuccess = mInventoryDb.updateInventoryItemQuantity(id, qty_int);

                        if (updateSuccess) {
                            // populate the inventory String Array with new inventory item
                            adapter.mData = loadInventoryTable();
                            // Update the RecyclerView with refreshed data
                            adapter.notifyDataSetChanged();

                            // checks to see if user had previously granted SMS permission before sending text message
                            if (checkPermissionAlreadyGranted(Manifest.permission.SEND_SMS)){
                                // checks inventory level and sends SMS text message based on LOW_INVENTORY_ALERT constant
                                if (qty_int <= LOW_INVENTORY_ALERT){
                                    notificationsHandler(SMS_DESTINATION, "LOW INVENTORY ALERT: Inventory item " +
                                            item_name_str + " has a quantity of " + qty_int);
                                }
                            }

                            Toast.makeText(InventoryActivity.this, "Quantity successfully updated to " + qty_int + "!", Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(InventoryActivity.this, "Problem updating quantity!", Toast.LENGTH_SHORT).show();
                            dialogInterface.dismiss();
                        }

                    } else {
                        Toast.makeText(InventoryActivity.this, "Quantity must be greater than 0!", Toast.LENGTH_SHORT).show();
                        dialogInterface.dismiss();
                    }
                    dialogInterface.dismiss();
                }
            } );

            alertdialog.setNegativeButton( R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            } );

            AlertDialog alert=alertdialog.create();
            alert.setCanceledOnTouchOutside( false );
            alert.show();
        }
    } // end editAction()


    // Responds to tapping trashcan icon (delete) in App Bar by deleting the currently-selected item in RecyclerView
    public void deleteAction() {
        // If odd position (quantity column) in RecyclerView is selected displays error, else performs deletion
        if (selectedPosition % 2 == 1) {
            Toast.makeText(InventoryActivity.this, "Cannot delete a quantity field!", Toast.LENGTH_SHORT).show();
        } else {
            int id = -1;
            String item_str = selectedValue;

            // get primary key for the selected inventory item
            id = mInventoryDb.inventoryPrimaryKeyLookup(item_str);
            Log.i("SQL", "primary key val: " + Integer.toString(id));

            // delete selected object
            boolean deleteSuccess;
            deleteSuccess = mInventoryDb.deleteInventoryItem(id);

            Log.i("SQL", "deleteSuccess val: " + deleteSuccess);

            if (deleteSuccess) {
                // populate the inventory String Array with new inventory item
                adapter.mData = loadInventoryTable();

                Log.i("LOADINVENTORY", "RecyclerView Count:  " + adapter.mData.length);
                if (adapter.mData.length == 0) {
                    // disables pencil and trashcan icons on app bar if all items are deleted
                    mMenu.findItem(R.id.action_delete).setEnabled(false);
                    mMenu.findItem(R.id.action_edit).setEnabled(false);
                } else {
                    // after item deleted, sets the RecyclerView position to first item and gets the item's value
                    selectedPosition = 0;
                    selectedValue = adapter.getItem(selectedPosition);
                }

                // update the RecyclerView with refreshed data from the inventory table
                adapter.notifyDataSetChanged();
                Toast.makeText(InventoryActivity.this, "Item \"" + item_str + "\" successfully deleted!", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(InventoryActivity.this, "Problem deleting! Select item to delete.", Toast.LENGTH_SHORT).show();
            }
        }
    } // end deleteAction()


    // Loads AlertDialog box with two EditText fields for adding an inventory item
    public void showInventoryDialogBox() {

        AlertDialog.Builder alertdialog = new AlertDialog.Builder( InventoryActivity.this );
        alertdialog.setTitle("Enter New Inventory Item");
        final EditText item_name = new EditText(InventoryActivity.this);
        final EditText qty = new EditText(InventoryActivity.this);

        item_name.setHint("Item Name");  //editbox1 hint
        item_name.setGravity(CENTER); //editbox in center
        item_name.setBackgroundResource(R.drawable.inventory_addbox); //editbox style design

        qty.setHint("Quantity");  //editbox2 hint
        qty.setGravity(CENTER);
        qty.setBackgroundResource(R.drawable.inventory_addbox);

        //set up in a linear layout
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        // setup lin layout parameters
        layoutParams.setMargins( 20,20,20,20); //set margin

        LinearLayout lp = new LinearLayout( getApplicationContext() );
        lp.setOrientation(LinearLayout.VERTICAL);

        lp.addView(item_name, layoutParams);
        lp.addView(qty, layoutParams );

        alertdialog.setView(lp);
        alertdialog.setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                int qty_int;
                boolean found_item = false;
                item_name_str = item_name.getText().toString().trim();
                qty_str=qty.getText().toString().trim();

                // validate user input
                if (item_name_str.isEmpty() || qty_str.isEmpty()){
                    Toast.makeText(InventoryActivity.this, "Cannot add new inventory item with an empty field!", Toast.LENGTH_SHORT).show();
                    dialogInterface.dismiss();
                } else {
                    // convert qty string to integer
                    qty_int = validateInteger(qty_str);
                    if (qty_int > 0) {
                        // input validation passed - now check if inventory item exists
                        found_item = mInventoryDb.inventoryItemLookup(item_name_str);

                        if (found_item) {
                            Toast.makeText(InventoryActivity.this, "Inventory item \"" + item_name_str + "\" already exists!", Toast.LENGTH_SHORT).show();
                            dialogInterface.dismiss();
                        } else {
                            // Adds inventory item because it is unique
                            mInventoryDb.addInventoryItem(item_name_str, qty_int);
                            // populate the inventory String Array with new inventory item
                            adapter.mData = loadInventoryTable();

                            // force update of RecyclerView for newly added inventory item
                            adapter.notifyDataSetChanged();

                            // set app bar pencil and trashcan to enabled if not already
                            if (!mMenu.findItem(R.id.action_delete).isEnabled()) {
                                mMenu.findItem(R.id.action_delete).setEnabled(true);
                                mMenu.findItem(R.id.action_edit).setEnabled(true);
                            }
                            Toast.makeText(InventoryActivity.this, "New Inventory Item Added!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(InventoryActivity.this, "Quantity must be greater than 0!", Toast.LENGTH_SHORT).show();
                        dialogInterface.dismiss();
                    }
                }

                dialogInterface.dismiss();
            }
        } );

        alertdialog.setNegativeButton( R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        } );

        AlertDialog alert=alertdialog.create();
        alert.setCanceledOnTouchOutside( false );
        alert.show();
    } // end showInventoryDialogBox()


    // function to check for SMS permissions and prompt user when user selects "Notifications" in App Bar
    public void checkPermission(String permission, int requestCode)
    {
        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(InventoryActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(InventoryActivity.this, new String[] { permission }, requestCode);
        }
        else {
            Toast.makeText(InventoryActivity.this, "Notifications are Enabled", Toast.LENGTH_SHORT).show();
        }
    }


    // called automatically when user accepts/declines a permission
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,
                permissions,
                grantResults);

        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(InventoryActivity.this, "SMS Permission Granted", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(InventoryActivity.this, "SMS Permission Denied", Toast.LENGTH_SHORT) .show();
            }
        }
    }


    // function to check if the user already granted SMS permission
    public boolean checkPermissionAlreadyGranted(String permission) {
        if (ContextCompat.checkSelfPermission(InventoryActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
            Log.i("SMS", "SMS not granted!");
            return false;
        } else {
            Log.i("SMS", "SMS already granted!");
            return true;
        }
    }


    // sends a text message to the device using the android.telephony.SmsManager
    public void notificationsHandler(String destination, String message) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage
                (destination, null, message,
                        null, null);
    }

} // end InventoryActivity()