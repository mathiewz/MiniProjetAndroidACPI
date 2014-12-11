/*
 * Copyright (C) 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.android.demo.notepad1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.HashMap;

/**
 * Simple notes database access helper class. Defines the basic CRUD operations
 * for the notepad example, and gives the ability to list all notes as well as
 * retrieve or modify a specific note.
 * 
 * This has been improved from the first version of this tutorial through the
 * addition of better error handling and also using returning a Cursor instead
 * of using a collection of inner classes (which is less scalable and not
 * recommended).
 */
public class PersonsDbAdapter {

    private static final String DATABASE_NAME = "data";

    private static final String DATABASE_TABLE_PERSON = "persons";
    public static final String KEY_PERSON_ROWID = "_id";
    public static final String KEY_PERSON_NAME = "name";

    private static final String DATABASE_TABLE_RELATION = "relations";
    public static final String KEY_RELATION_ID1 = "id1";
    public static final String KEY_RELATION_ID2 = "id2";
    public static final String KEY_RELATION_TYPE = "TYPE";

    private static final String TAG = "PersonsDbAdapter";

    private SQLiteDatabase mDb;

    private static final int DATABASE_VERSION = 9;

    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE_TABLE_PERSON = "create table "+ DATABASE_TABLE_PERSON +" ("+KEY_PERSON_ROWID+" integer primary key autoincrement, "+KEY_PERSON_NAME+" text not null);";
    private static final String DATABASE_CREATE_TABLE_RELATION = "create table "+ DATABASE_TABLE_RELATION +" ("+KEY_RELATION_ID1+" integer, "+KEY_RELATION_ID2+" integer, "+KEY_RELATION_TYPE+" integer);";

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DATABASE_CREATE_TABLE_PERSON);
            db.execSQL(DATABASE_CREATE_TABLE_RELATION);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS "+ DATABASE_TABLE_PERSON);
            db.execSQL("DROP TABLE IF EXISTS "+ DATABASE_TABLE_RELATION);
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public PersonsDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public PersonsDbAdapter open() throws SQLException {
        DatabaseHelper mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    /**
     * Create a new note using the title and body provided. If the note is
     * successfully created return the new rowId for that note, otherwise return
     * a -1 to indicate failure.
     * 
     * @param name the title of the note
     * @return rowId or -1 if failed
     */
    public long createNote(String name) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_PERSON_NAME, name);

        return mDb.insert(DATABASE_TABLE_PERSON, null, initialValues);
    }

    /**
     * Delete the note with the given rowId
     * 
     * @param rowId id of note to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteNote(long rowId) {

        return mDb.delete(DATABASE_TABLE_PERSON, KEY_PERSON_ROWID + "=" + rowId, null) > 0;
    }

    public boolean deleteAll(){
        return mDb.delete(DATABASE_TABLE_PERSON, null, null) > 0;
    }

    /**
     * Return a Cursor over the list of all notes in the database
     * 
     * @return Cursor over all notes
     */
    public Cursor fetchAllPersons() {

        return mDb.query(DATABASE_TABLE_PERSON, new String[] {KEY_PERSON_ROWID, KEY_PERSON_NAME
                }, null, null, null, null, null);
    }

    public String fetchPerson(long rowId) throws SQLException {
        String ret = null;
        Cursor mCursor =

                mDb.query(true, DATABASE_TABLE_PERSON, new String[] {KEY_PERSON_ROWID,
                                KEY_PERSON_NAME}, KEY_PERSON_ROWID + "=" + rowId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
            ret = mCursor.getString(mCursor.getColumnIndex(KEY_PERSON_NAME));
        }
        return ret;

    }

    public long getIdFromName(String name) throws SQLException {
        long ret = -1;
        Cursor mCursor =

                mDb.query(true, DATABASE_TABLE_PERSON, new String[] {KEY_PERSON_ROWID,
                                KEY_PERSON_NAME}, KEY_PERSON_NAME + "='" + name+"'", null,
                        null, null, null, null);
        if (mCursor != null) if (mCursor.moveToFirst())
            ret = mCursor.getLong(mCursor.getColumnIndex(KEY_PERSON_ROWID));
        return ret;

    }

    public long addRelation(int id1, int id2, int type) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_RELATION_ID1, id1);
        initialValues.put(KEY_RELATION_ID2, id2);
        initialValues.put(KEY_RELATION_TYPE, type);

        return mDb.insert(DATABASE_TABLE_RELATION, null, initialValues);
    }

    public HashMap<Integer, Integer> getRelations(int id){
        HashMap<Integer, Integer> map = new HashMap<>();
        Cursor cursor = mDb.query(DATABASE_TABLE_RELATION,new String[] {KEY_RELATION_ID2, KEY_RELATION_TYPE},KEY_RELATION_ID1 + "=" + id, null, null, null, null);
        if (cursor.moveToFirst()) do {
            int id2 = cursor.getInt(cursor.getColumnIndex(KEY_RELATION_ID2));
            int type = cursor.getInt(cursor.getColumnIndex(KEY_RELATION_TYPE));
            map.put(id2, type);
        } while (cursor.moveToNext());
        return map;
    }
}
