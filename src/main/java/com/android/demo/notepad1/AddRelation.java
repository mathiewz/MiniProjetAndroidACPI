package com.android.demo.notepad1;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;


public class AddRelation extends Activity {

    private PersonsDbAdapter mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new PersonsDbAdapter(this);
        mDbHelper.open();
        setContentView(R.layout.activity_add_relation);

        fillSpinnerRelation();
        fillSpinnerPerson();

        TextView textView = (TextView) findViewById(R.id.textView);
        Intent intent = getIntent();
        final int idPerson = Integer.valueOf(intent.getStringExtra("idPerson"));
        textView.setText(mDbHelper.fetchPerson(idPerson));

        Button bouton = (Button) findViewById(R.id.button);
        bouton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Spinner person = (Spinner) findViewById( R.id.spinnerPerson );
                Spinner relation = (Spinner) findViewById(R.id.spinnerRelation);
                int idRelation = relation.getSelectedItemPosition();
                mDbHelper.addRelation(idPerson, (int)mDbHelper.getIdFromName(((TextView)person.getSelectedView()).getText().toString()), idRelation);
                switch (idRelation){
                    case 0:
                        mDbHelper.addRelation((int)mDbHelper.getIdFromName(((TextView)person.getSelectedView()).getText().toString()),idPerson,  1);
                        break;
                    case 1:
                        mDbHelper.addRelation((int)mDbHelper.getIdFromName(((TextView)person.getSelectedView()).getText().toString()),idPerson,  0);
                        break;
                    case 2:
                        mDbHelper.addRelation((int)mDbHelper.getIdFromName(((TextView)person.getSelectedView()).getText().toString()),idPerson,  2);
                        break;
                }
                finish();
            }
        });
    }

    private void fillSpinnerRelation() {
        Spinner spinner = (Spinner) findViewById(R.id.spinnerRelation);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.famille, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
    }

    private void fillSpinnerPerson() {
        Cursor c = mDbHelper.fetchAllPersons();
        startManagingCursor(c);

        // create an array to specify which fields we want to display
        String[] from = new String[]{PersonsDbAdapter.KEY_PERSON_NAME};
        // create an array of the display item we want to bind our data to
        int[] to = new int[]{android.R.id.text1};
        // create simple cursor adapter
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, c, from, to );
        adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
        // get reference to our spinner
        Spinner s = (Spinner) findViewById( R.id.spinnerPerson );
        s.setAdapter(adapter);
    }
}
