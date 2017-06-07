package app.com.example.kajsa.talkto.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import java.util.HashSet;

import app.com.example.kajsa.talkto.db.TransDBcontract;
import app.com.example.kajsa.talkto.db.TransDBhelper;

/*
 *  Test used when initially setting up the DB.
 *  Used the Sunshine teaching project by google as a basis for how to set this up.
 */
public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getName();

    void deleteDB() {
        mContext.deleteDatabase(TransDBhelper.DATABASE_NAME);
    }

    public void setUp() {
        deleteDB();
    }

    public void testCreateDb() throws Throwable {
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(TransDBcontract.PhrasesDefs.TABLE_NAME);

        mContext.deleteDatabase(TransDBhelper.DATABASE_NAME);
        SQLiteDatabase db = new TransDBhelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );

        assertTrue("Error: Table missing",
                tableNameHashSet.isEmpty());

        c = db.rawQuery("PRAGMA table_info(" + TransDBcontract.PhrasesDefs.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        final HashSet<String> columnHashSet = new HashSet<String>();
        columnHashSet.add(TransDBcontract.PhrasesDefs._ID);
        columnHashSet.add(TransDBcontract.PhrasesDefs.PHRASE_COL);
        columnHashSet.add(TransDBcontract.PhrasesDefs.LANG_COL);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            columnHashSet.remove(columnName);
        } while(c.moveToNext());

        assertTrue("Error: Missing columns",
                columnHashSet.isEmpty());

        db.close();
    }

    public void testPhraseTable() {
        insertPhrase();
    }

    public long insertPhrase() {

        TransDBhelper dbHelper = new TransDBhelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues phraseValues = new ContentValues();

        phraseValues.put(TransDBcontract.PhrasesDefs.PHRASE_COL, "Test Phrase");
        phraseValues.put(TransDBcontract.PhrasesDefs.LANG_COL, "English");

        // Insert ContentValues into database and get a row ID back

        long locationRowId;
        locationRowId = db.insert(TransDBcontract.PhrasesDefs.TABLE_NAME, null, phraseValues);
        assertTrue(locationRowId != -1);

        // Query the database and receive a Cursor back
        Cursor cursor = db.query(
                TransDBcontract.PhrasesDefs.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );

        Log.v(LOG_TAG, DatabaseUtils.dumpCursorToString(cursor));

        assertTrue("Error: No Records return from location query", cursor.moveToFirst());

        assertFalse("Error: More than one record return from location entry",
                cursor.moveToNext());

        cursor.close();
        db.close();
        return locationRowId;
    }
}
