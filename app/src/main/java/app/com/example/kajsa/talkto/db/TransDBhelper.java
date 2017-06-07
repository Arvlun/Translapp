package app.com.example.kajsa.talkto.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import app.com.example.kajsa.talkto.db.TransDBcontract;

/**
 * Initial setup of the database
 */
public class TransDBhelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 3;

    public static final String DATABASE_NAME = "trans.db";

    public TransDBhelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_TOPLIST_TABLE = "CREATE TABLE " + TransDBcontract.PhrasesDefs.TABLE_NAME + " (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TransDBcontract.PhrasesDefs.PHRASE_COL + " TEXT NOT NULL, " +
                TransDBcontract.PhrasesDefs.LANG_COL + " TEXT NOT NULL" +
                " );";
        Log.v("DB LOG", "onCreate- sqlcreateSTR = " + SQL_CREATE_TOPLIST_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_TOPLIST_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // Borde ändra när funktionaliteten är klar så att det inte tar bort allt vid uppdatering
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TransDBcontract.PhrasesDefs.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
