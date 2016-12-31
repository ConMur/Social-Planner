package com.example.connormurphy.socialplanner;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SelectItem extends AppCompatActivity {

    public static final String ACTIVITY_NAME_EXTRA = "com.example.connormurphy.socialplanner.ACTIVITY_NAME";

    private final String DEFAULT_CONTACT_NAME = "No contact selected";

    private static final int CONTACT_PICKER_RESULT = 1001;

    private TextView contactName;
    private String phoneNumber;

    private int day, month, year;
    private int hour, minute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_item);

        Intent intent = getIntent();

        // Display the selected activity
        TextView activityName = (TextView) findViewById(R.id.activity_name);
        activityName.setText(intent.getStringExtra(ACTIVITY_NAME_EXTRA));

        //Initialize the selected contact to a default name
        contactName = (TextView) findViewById(R.id.selected_contact_name);
        contactName.setText(DEFAULT_CONTACT_NAME);

        phoneNumber = null;
    }

    /**
     * Called when the "Choose Contact" button is clicked
     *
     * @param view the view
     */
    public void onChooseContactClick(View view) {
        Intent chooseContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(chooseContact, CONTACT_PICKER_RESULT);
    }

    /**
     * Called when the "Text a friend" button is clicked
     *
     * @param view the view
     */
    public void onTextFriend(View view) {
        if(phoneNumber == null)
        {
            Toast.makeText(getApplicationContext(), "Please choose a valid contact", Toast.LENGTH_LONG).show();
        }
        else
        {
            DatePicker dp = (DatePicker)findViewById(R.id.datePicker);
            int day = dp.getDayOfMonth();
            int month = dp.getMonth();
            int year = dp.getYear() - 1900; // Just have to subtract 1900 to get correct date


            Date d = new Date(year, month, day);
            String strDate = DateFormat.getDateInstance().format(d);

            TimePicker tp = (TimePicker)findViewById(R.id.timePicker);
            int hour = tp.getCurrentHour();
            int minute = tp.getCurrentMinute();
            String period = "am";

            //Check for am or pm
            if(hour > 12)
            {
                period = "pm";
                hour -= 12;
            }

            String textMessage =
                    "Want to " + ((TextView)findViewById(R.id.activity_name)).getText()
                    + " on " + strDate +  " at " + hour + ":" + minute + period + "?";

            //Start the text messaging app with the template text message
            Intent smsIntent = new Intent(Intent.ACTION_VIEW);
            smsIntent.setType("vnd.android-dir/mms-sms");
            smsIntent.putExtra("address", phoneNumber);
            smsIntent.putExtra("sms_body",textMessage);
            startActivity(smsIntent);

            finish();

            /*
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, "text message", null, null);*/
        }

    }

    public void onCancelClicked(View view) {
        Intent returnIntent = new Intent(this, MainActivity.class);
        returnIntent.putExtra(SelectItem.ACTIVITY_NAME_EXTRA, "");
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String id = null, name = null, phone = null, hasPhone;
        if (resultCode == RESULT_OK && requestCode == CONTACT_PICKER_RESULT) {
            //From: https://stackoverflow.com/questions/14069375/get-specific-contact-information-from-uri-returned-from-intent-action-pick
            Uri contactUri = data.getData();
            int idx;
            Cursor cursor = getContentResolver().query(contactUri, null, null, null, null);
            if (cursor.moveToFirst()) {
                idx = cursor.getColumnIndex(ContactsContract.Contacts._ID);
                id = cursor.getString(idx);

                idx = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                name = cursor.getString(idx);

                idx = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
                hasPhone = cursor.getString(idx);

            }

            cursor.close();

            // Build the Entity URI.
            Uri.Builder b = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, id).buildUpon();
            b.appendPath(ContactsContract.Contacts.Entity.CONTENT_DIRECTORY);
            contactUri = b.build();

            // Create the projection (SQL fields) and sort order.
            String[] projection = {
                    ContactsContract.Contacts.Entity.RAW_CONTACT_ID,
                    ContactsContract.Contacts.Entity.DATA1,
                    ContactsContract.Contacts.Entity.MIMETYPE};
            String sortOrder = ContactsContract.Contacts.Entity.RAW_CONTACT_ID + " ASC";
            cursor = getContentResolver().query(contactUri, projection, null, null, sortOrder);

            String mime;
            int mimeIdx = cursor.getColumnIndex(ContactsContract.Contacts.Entity.MIMETYPE);
            int dataIdx = cursor.getColumnIndex(ContactsContract.Contacts.Entity.DATA1);
            if (cursor.moveToFirst()) {
                do {
                    mime = cursor.getString(mimeIdx);
                    if (mime.equalsIgnoreCase(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)) {
                        phone = cursor.getString(dataIdx);
                    }
                } while (cursor.moveToNext());
            }
        }

        contactName.setText(name);
        phoneNumber = phone;
    }
}
