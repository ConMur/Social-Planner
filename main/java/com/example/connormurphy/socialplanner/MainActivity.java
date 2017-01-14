package com.example.connormurphy.socialplanner;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    private final int MORNING_END = 12;
    private final int AFTERNOON_END = 18;
    private final int EVENING_END = 24;

    // Rolled a die to get a truly random number for this
    private final int ADD_REQUEST_CODE = 4;
    private final int EDIT_REQUEST_CODE = 96;

    public final static String EXTRA_ARRAY_LIST = "com.example.connormurphy.socialplanner.ARRAY_LIST";
    private final String THINGS_TO_DO_FILENAME = "ThingsToDo.txt";

    private ArrayList<String> thingsToDo;

    private CheckBox checkBox;
    private boolean checkboxChecked;

    private boolean inEditMode;

    private Button editButton;
    private Button addButton;

    private ListView listView;
    private boolean listViewPrevState;

    private ArrayList<Integer> indicesToDelete;

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

        listView = (ListView) findViewById(R.id.things_to_do);
        updateListView(false);

        listView.setEnabled(true);
        listView.setItemChecked(0, true);

        // Determine if the ListView should be enabled or not
        checkBox = (CheckBox) findViewById(R.id.checkBox);
        changeListViewEnabledState(checkboxChecked);
        listViewPrevState = checkboxChecked;

        inEditMode = false;

        editButton = (Button) findViewById(R.id.edit_activities_button);
        addButton = (Button) findViewById(R.id.add_activities_button);

        indicesToDelete = new ArrayList<>();
    }

    /**
     * Called when the "Add" button is pressed to add an activity to the list of activities
     *
     * @param view the view that was clicked
     */
    public void onAddPressed(View view) {
        if (inEditMode) {
            deleteFromListView();
        } else {
            Intent intent = new Intent(this, AddActivity.class);
            intent.putStringArrayListExtra(EXTRA_ARRAY_LIST, thingsToDo);
            startActivityForResult(intent, ADD_REQUEST_CODE);
        }
    }

    /**
     * Called when the "Edit" button is pressed to edit the list of activities
     *
     * @param view the view that was clicked
     */
    public void onEditPressed(View view) {
        inEditMode = !inEditMode;

        if (inEditMode) {
            editButton.setText(R.string.done_button);

            //change add button to say delete
            addButton.setText(R.string.delete_button);

            listViewPrevState = listView.isEnabled();
            //Ensure the list view is enabled in order to select items
            changeListViewEnabledState(true);
        } else {
            editButton.setText(R.string.edit_activities);

            // Change add button back to add
            addButton.setText(R.string.add_activities);

            //Set the listview back to what it was before editing
            changeListViewEnabledState(listViewPrevState);
        }

        updateListView(inEditMode);
    }

    /**
     * Changes the list view to enabled if the "Free" check box is clicked and disables the
     * list view otherwise.
     *
     * @param view the view that was pressed
     */
    public void onCheckboxClicked(View view) {
        boolean checked = ((CheckBox) view).isChecked();

        changeListViewEnabledState(checked);
    }

    @Override
    public void onPause() {
        super.onPause();

        //Write contents of the list view to a file
        saveActivitiesToFile(thingsToDo);
    }

    /**
     * Updates the ListView with the items in the thingsToDo array
     *
     * @param showCheckBoxes if true, shows checkboxes next to each item in the ListView
     */
    public void updateListView(boolean showCheckBoxes) {
        ArrayAdapter<String> adapter;
        if (showCheckBoxes) {
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, thingsToDo);
            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        } else {
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, thingsToDo);
            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        }


        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // Only go to the next intent when the user is not in edit mode
                if (!inEditMode) {
                    String selectedFromList = (String) (listView.getItemAtPosition(i));

                    Intent intent = new Intent(getApplicationContext(), SelectItem.class);
                    intent.putExtra(SelectItem.ACTIVITY_NAME_EXTRA, selectedFromList);
                    startActivityForResult(intent, EDIT_REQUEST_CODE);
                }
                // Track which items to delete
                else {
                    //If the index is already in the list, remove it
                    if (indicesToDelete.contains(i)) {
                        // Do it this way so the value of the index is removed not the particular
                        //   index in the indiciesToRemove array
                        indicesToDelete.remove(Integer.valueOf(i));
                    } else {
                        indicesToDelete.add(i);
                    }
                }
            }
        });
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            thingsToDo = data.getStringArrayListExtra(MainActivity.EXTRA_ARRAY_LIST);
            updateListView(false);
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

    /**
     * Deletes all elements in the list view that are at the indices as defined by the
     * indicesToDelete arraylist
     */
    private void deleteFromListView() {
        // Sort the indices to ensure deletion works properly
        Collections.sort(indicesToDelete);

        int size = indicesToDelete.size();
        int thingsRemoved = 0;

        for (int i = 0; i < size; ++i)
        {
            thingsToDo.remove(indicesToDelete.get(i) - thingsRemoved);
            thingsRemoved++;
        }

        // Notify the listview that things have changed
        updateListView(true);
        // Reset the array list after deleting
        indicesToDelete.clear();
    }

}
