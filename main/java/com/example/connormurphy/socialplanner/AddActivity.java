package com.example.connormurphy.socialplanner;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;

public class AddActivity extends AppCompatActivity {

    private ArrayList<String> thingsToDo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_activity);

        Intent intent = getIntent();
        thingsToDo = intent.getStringArrayListExtra(MainActivity.EXTRA_ARRAY_LIST);
    }

    public void finishAdding(View view) {
        EditText activityName = (EditText) findViewById(R.id.add_activity_name);
        String text = activityName.getText().toString();

        createReturnIntent(text);
        finish();
    }

    public void cancelAdding(View view) {
        createReturnIntent(null);
        finish();
    }

    /**
     * Creates the intent to return from this activity
     * @param extraToReturn the extra string to return from this activity
     */
    public void createReturnIntent(String extraToReturn)
    {
        if (extraToReturn != null && !extraToReturn.equals("")) {
            thingsToDo.add(extraToReturn);
        }

        Intent returnIntent = new Intent(this, MainActivity.class);
        returnIntent.putStringArrayListExtra(MainActivity.EXTRA_ARRAY_LIST, thingsToDo);
        setResult(Activity.RESULT_OK, returnIntent);
    }
}
