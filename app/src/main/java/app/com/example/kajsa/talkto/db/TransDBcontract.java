package app.com.example.kajsa.talkto.db;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Contains the database and URI information (tablenames, column names etc)
 */
public class TransDBcontract {

    public static final String CONTENT_AUTHORITY = "app.com.example.kajsa.talkto";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    //PHRASElist uri
    public static final String PATH_PHRASE = "phrase";

    /**
     * Innerclass containing strings used to set up the DB and uriBuilding.
     */
    public static final class PhrasesDefs implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PHRASE).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PHRASE;

        public static final String TABLE_NAME = "translations";

        public static final String PHRASE_COL = "phrases";
        public static final String LANG_COL = "lang";

        public static Uri buildUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }

}
