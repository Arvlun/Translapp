package app.com.example.kajsa.talkto.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import app.com.example.kajsa.talkto.db.TransDBcontract;

/**
 * ContentProvider, providing access to db.
 */
@SuppressWarnings("ConstantConditions")
public class TransDBProvider extends ContentProvider {

    private static String LOG_TAG = TransDBProvider.class.getName();
    private static final UriMatcher matcher = buildUriMatcher();

    private TransDBhelper dbHelper;

    static final int PHRASE = 55;

    @Override
    public boolean onCreate() {
        dbHelper = new TransDBhelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        final int match = matcher.match(uri);

        switch (match) {
            case PHRASE:
                return TransDBcontract.PhrasesDefs.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri:" + uri);
        }
    }

    static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String auth = TransDBcontract.CONTENT_AUTHORITY;

        matcher.addURI(auth, TransDBcontract.PATH_PHRASE, PHRASE);

        return matcher;
    }

    /**
     * Saves a phrase to the DB.
     *
     * @param values Values passed - should contain the phrase and language
     * @return The id of the added phrase.
     */
    public long savePhrase(ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long rowId = db.insert(TransDBcontract.PhrasesDefs.TABLE_NAME, null, values);
        db.close();
        return rowId;
    }

    /**
     * Get the complete list of added phrases
     * @return Cursor containing the list of phrases in the database ordered by language.
     */
    public Cursor getSavedPhrases() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String[] projection = {
                "_id",
                "phrases",
                "lang"
        };

        String sortOrder = "lang DESC";

        //String selection = "phrase" + " =?";
        //String[] selectionArgs = {
        Cursor res = db.query(
                "translations",                           // The table to query
                projection,                               // The columns to return
                null,                                     // No where clauses
                null,                                     // No where clausevalues
                null,                                     // No grouping
                null,                                     // No filter by grouprows
                sortOrder                                 // The sort order
        );

        //Log.v(LOG_TAG, DatabaseUtils.dumpCursorToString(res));
        return res;
    }

    //Query the database to get saved phrases
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionsArgs, String sortOrder) {

        Cursor returnCursor;

        switch (matcher.match(uri)) {
            case PHRASE: {
                returnCursor = getSavedPhrases();
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        returnCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return returnCursor;
    }

    //Insert new phrase into database
    @Override
    public Uri insert(Uri uri, ContentValues values) {

        final int match = matcher.match(uri);

        Uri returnUri;

        switch (match) {
            case PHRASE: {
                long id = savePhrase(values);
                //Log.v("INSTERT" , "RED ID: " + id);
                if (id >= 0) {
                    returnUri = TransDBcontract.PhrasesDefs.buildUri(id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    //Delete phrase from database
    @Override
    public int delete(Uri uri, String selection, String[] selectionsArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int rowsDeleted;

        switch (matcher.match(uri)) {
            case PHRASE: {
                //Log.v("DELETE", "DELETE PHRASE");
                rowsDeleted = db.delete(TransDBcontract.PhrasesDefs.TABLE_NAME, selection, selectionsArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    //Update phrase in database
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionsArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int res;

        if (selection == null) selection = "1";

        switch (matcher.match(uri)) {
            case PHRASE: {
                //Log.v("UPDATE", "UPDATE PHRASE");
                res = db.update(TransDBcontract.PhrasesDefs.TABLE_NAME, values, selection, selectionsArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (res != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return res;
    }
}
