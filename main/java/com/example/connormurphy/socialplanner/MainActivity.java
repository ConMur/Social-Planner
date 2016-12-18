package com.example.connormurphy.socialplanner;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
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

    private final String THINGS_TO_DO_FILENAME = "ThingsToDo.txt";

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

        //Populate the listview with things to do from file
        File thingsToDoFile = new File(getApplicationContext().getFilesDir(), THINGS_TO_DO_FILENAME);
        ArrayList<String> thingsToDo = new ArrayList<>();

        try (BufferedReader buf = new BufferedReader(new FileReader(thingsToDoFile))) {
            String nextLine = null;
            while ((nextLine = buf.readLine()) != null) {
                thingsToDo.add(nextLine);
            }
        } catch (IOException ioe) {
            Toast toast = Toast.makeText(getApplicationContext(), ioe.getMessage(), Toast.LENGTH_SHORT);
            toast.show();
        }

        //TODO: remove
        thingsToDo.add("Test");
        thingsToDo.add("Test");
        thingsToDo.add("Test");
        thingsToDo.add("Test");
        thingsToDo.add("Test");
        thingsToDo.add("Test");
        thingsToDo.add("Test");
        thingsToDo.add("Test");
        thingsToDo.add("Test");
        thingsToDo.add("Test");
        thingsToDo.add("Test");



        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, thingsToDo);
        ListView listView = (ListView) findViewById(R.id.things_to_do);
        listView.setAdapter(adapter);
    }


}
