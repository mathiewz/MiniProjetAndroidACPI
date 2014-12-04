/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.demo.notepad1;

import android.annotation.TargetApi;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;

import java.util.ArrayList;
import java.util.Collections;

public class ArbreV1 extends ListActivity {
    public static final int IMPORT_CONTACT = Menu.FIRST;
    private static final int DELETE_ID = Menu.FIRST + 1;
    public static final int IMPORT_ALL_CONTACT = Menu.FIRST+2;
    public static final int DELETE_ALL_CONTACT = Menu.FIRST+3;

    private int mNoteNumber = 1;
	private PersonsDbAdapter mDbHelper;

    static final int PICK_CONTACT_REQUEST = 1;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notepad_list);
        mDbHelper = new PersonsDbAdapter(this);
        mDbHelper.open();
        registerForContextMenu(getListView());
        fillData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	boolean result = super.onCreateOptionsMenu(menu);
        menu.add(0, IMPORT_CONTACT, IMPORT_CONTACT, R.string.import_contact);
        menu.add(0, IMPORT_ALL_CONTACT, IMPORT_ALL_CONTACT, R.string.import_all_contact);
        menu.add(0, DELETE_ALL_CONTACT, DELETE_ALL_CONTACT, R.string.delete_all_contact);
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
        case IMPORT_CONTACT:
            pickContact();
            return true;
        case IMPORT_ALL_CONTACT:
            AsyncTask<Void, Void, ArrayList<String>> myTask = new ImportAllContact().execute();
            return true;
        case DELETE_ALL_CONTACT:
            mDbHelper.deleteAll();
            fillData();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @TargetApi(Build.VERSION_CODES.ECLAIR)
    private void importAllContacts() {
        String[] projection = {ContactsContract.Contacts.DISPLAY_NAME};
        // Perform the query on the contact to get the NUMBER column
        // We don't need a selection or sort order (there's only one result for the given URI)
        // CAUTION: The query() method should be called from a separate thread to avoid blocking
        // your app's UI thread. (For simplicity of the sample, this code doesn't do that.)
        // Consider using CursorLoader to perform the query.
        Cursor cursor = managedQuery(ContactsContract.RawContacts.CONTENT_URI, projection, null, null, null);
        int column = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
        if(cursor.moveToFirst()) {
            while(!cursor.isAfterLast()) {     // still a valid entry left?
                String label = cursor.getString(column);
                createNote(label);
                cursor.moveToNext();           // move to the next entry
            }
        }
        // Retrieve the phone number from the LABEL column
    }

    private void createNote(String nom) {
        mDbHelper.createNote(nom);
        fillData();
    }
    
    private void fillData() {
        // Get all of the notes from the database and create the item list
        Cursor c = mDbHelper.fetchAllNotes();
        startManagingCursor(c);

        String[] from = new String[] { PersonsDbAdapter.KEY_NAME};
        int[] to = new int[] { R.id.text1 };
        
        // Now create an array adapter and set it to display using our row
        SimpleCursorAdapter notes =
            new SimpleCursorAdapter(this, R.layout.notes_row, c, from, to);
        setListAdapter(notes);
    }
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_ID, 0, R.string.menu_delete);
    }

    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case DELETE_ID:
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                mDbHelper.deleteNote(info.id);
                fillData();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private void pickContact() {
        Intent pickContactIntent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));
        pickContactIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE); // Show user only contacts w/ phone numbers
        startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == PICK_CONTACT_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // Get the URI that points to the selected contact
                Uri contactUri = data.getData();
                // We only need the LABEL column, because there will be only one row in the result
                String[] projection = {ContactsContract.Contacts.DISPLAY_NAME};
                // Perform the query on the contact to get the NUMBER column
                // We don't need a selection or sort order (there's only one result for the given URI)
                // CAUTION: The query() method should be called from a separate thread to avoid blocking
                // your app's UI thread. (For simplicity of the sample, this code doesn't do that.)
                // Consider using CursorLoader to perform the query.
                Cursor cursor = getContentResolver()
                        .query(contactUri, projection, null, null, null);
                cursor.moveToFirst();
                // Retrieve the phone number from the LABEL column
                int column = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                String label = cursor.getString(column);
                createNote(label);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    private class ImportAllContact extends AsyncTask<Void, Void, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(Void... params) {
            System.out.println("import !!!");
            ArrayList<String> ret = new ArrayList<String>();
            String[] projection = {ContactsContract.Contacts.DISPLAY_NAME};
            // Perform the query on the contact to get the NUMBER column
            // We don't need a selection or sort order (there's only one result for the given URI)
            // CAUTION: The query() method should be called from a separate thread to avoid blocking
            // your app's UI thread. (For simplicity of the sample, this code doesn't do that.)
            // Consider using CursorLoader to perform the query.
            Cursor cursor = getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, projection, null, null, null);
            int column = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {     // still a valid entry left?
                    String label = cursor.getString(column);
                    System.out.println(label);
                    ret.add(label);
                    cursor.moveToNext();           // move to the next entry

                }
            }
            Collections.sort(ret, String.CASE_INSENSITIVE_ORDER);
            return ret;
        }

        @Override
        protected void onPostExecute(ArrayList<String> params){
            for(String data : params) {
                createNote(data);
            }
            fillData();
        }
    }
}
