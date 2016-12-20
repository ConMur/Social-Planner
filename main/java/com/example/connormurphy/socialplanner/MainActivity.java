package com.example.connormurphy.socialplanner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private final int MORNING_END = 12;
    private final int AFTERNOON_END = 18;
    private final int EVENING_END = 24;

    // Rolled a die to get a truly random number for this
    private final int ADD_REQUEST_CODE = 4;

    public final static String EXTRA_ARRAY_LIST = "com.example.connormurphy.socialplanner.ARRAY_LIST";
    private final String THINGS_TO_DO_FILENAME = "ThingsToDo.txt";

    private ArrayList<String> thingsToDo;

    private CheckBox checkBox;
    private boolean checkboxChecked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set the greeting based on the time of day
        TextView greetings = (TextView) findViewById(R.id.greeting_text);
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (currentHour <= MORNING_END) {
            greetings.setText(R.string.greeting_morning);
        } else if (currentHour <= AFTERNOON_END) {
            greetings.setText(R.string.greeting_afternoon);
        } else if (currentHour <= EVENING_END) {
            greetings.setText(R.string.greeting_night);
        }

        //Populate the listview with things to do from file if it is empty
        Intent intent = getIntent();
        thingsToDo = intent.getStringArrayListExtra(MainActivity.EXTRA_ARRAY_LIST);

        if (thingsToDo == null) {
            thingsToDo = new ArrayList<>();
            File thingsToDoFile = new File(getApplicationContext().getFilesDir(), THINGS_TO_DO_FILENAME);

            try (BufferedReader buf = new BufferedReader(new FileReader(thingsToDoFile))) {
                String nextLine = null;
                while ((nextLine = buf.readLine()) != null) {
                    thingsToDo.add(nextLine);
                }
            } catch (FileNotFoundException fnf) {
                //do nothing
            } catch (IOException ioe) {
                Toast toast = Toast.makeText(getApplicationContext(), ioe.getMessage(), Toast.LENGTH_SHORT);
                toast.show();
            }
        }

       updateListView();

        // Determine if the ListView should be enabled or not
        checkBox = (CheckBox) findViewById(R.id.checkBox);
        changeListViewEnabledState(checkboxChecked);
    }

    /**
     * Called when the "Add" button is pressed to add an activity to the list of activities
     *
     * @param view the view that was clicked
     */
    public void addActivity(View view) {

        //TODO: look into startActivityForResult
        //https://stackoverflow.com/questions/10407159/how-to-manage-startactivityforresult-on-android
        Intent intent = new Intent(this, AddActivity.class);
        intent.putStringArrayListExtra(EXTRA_ARRAY_LIST, thingsToDo);
        startActivityForResult(intent, ADD_REQUEST_CODE);
    }

    /**
     * Updates the ListView with the items in the thingsToDo array
     */
    public void updateListView()
    {
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, thingsToDo);
        ListView listView = (ListView) findViewById(R.id.things_to_do);
        listView.setAdapter(adapter);
    }

    public void onCheckboxClicked(View view) {
        boolean checked = ((CheckBox) view).isChecked();

        changeListViewEnabledState(checked);
    }

    /**
     * If enabled is true, makes the ListView normal and if enabled is false, makes the
     * ListView greyed out and disabled
     *
     * @param enabled whether or not to enable the ListView
     */
    private void changeListViewEnabledState(boolean enabled) {
        ListView listView = (ListView) findViewById(R.id.things_to_do);

        if (enabled) {
            listView.setEnabled(true);
            listView.setAlpha(1.0f);
        } else {
            listView.setEnabled(false);
            listView.setAlpha(0.33f);
        }
    }

    /**
     * Saves all the activities in the ListView to local storage
     *
     * @param thingsToDo the list of activities to save
     */
    private void saveActivitiesToFile(ArrayList<String> thingsToDo) {
        File thingsToDoFile = new File(getApplicationContext().getFilesDir(), THINGS_TO_DO_FILENAME);

        try {
            FileOutputStream outputStream = new FileOutputStream(thingsToDoFile, false);
            for (int i = 0; i < thingsToDo.size(); ++i) {
                outputStream.write((thingsToDo.get(i) + "\n").getBytes());
            }
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK){
                thingsToDo = data.getStringArrayListExtra(MainActivity.EXTRA_ARRAY_LIST);
                updateListView();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //Write contents of the list view to a file
        saveActivitiesToFile(thingsToDo);
    }
}
