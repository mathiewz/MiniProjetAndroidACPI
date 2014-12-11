package com.android.demo.notepad1;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class ListRelations extends ListActivity {

    private final int MENU_ADD_RELATION = Menu.FIRST;
    private PersonsDbAdapter mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_relations);
        mDbHelper = new PersonsDbAdapter(this);
        mDbHelper.open();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Get all of the notes from the database and create the item list
        int idPerson = Integer.valueOf(getIntent().getStringExtra("idPerson"));
        HashMap<Integer, Integer> mapRelations = mDbHelper.getRelations(idPerson);
        ArrayList<String> lines = new ArrayList<>();
        for(Map.Entry<Integer, Integer> entry : mapRelations.entrySet()) {
            Integer cle = entry.getKey();
            Integer valeur = entry.getValue();
            String relation;
            switch (valeur){
                case 0:
                    relation="Parent de ";
                    break;
                case 1:
                    relation="Enfant de ";
                    break;
                case 2:
                    relation="Conjoint de ";
                    break;
                default:
                    relation = "undefined ";
                    break;
            }
            lines.add(relation + mDbHelper.fetchPerson(cle));
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                lines );
        setListAdapter(arrayAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_ADD_RELATION, MENU_ADD_RELATION, R.string.add_relation);
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ADD_RELATION:
                Intent intent = new Intent(this, AddRelation.class);
                intent.putExtra("idPerson",String.valueOf(Integer.valueOf(getIntent().getStringExtra("idPerson"))));
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
