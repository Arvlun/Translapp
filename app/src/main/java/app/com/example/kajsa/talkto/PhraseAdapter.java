package app.com.example.kajsa.talkto;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import app.com.example.kajsa.talkto.db.TransDBcontract;

/**
 * Adapater to display the list of phrases saved from the database.
 */
public class PhraseAdapter extends CursorAdapter {

    private static String LOG_TAG = GetTranslationTask.class.getName();
    Context aContext;

    public PhraseAdapter(Context context, Cursor cur, int flags) {
        super(context, cur, flags);
        aContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup vGroup) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_phrase, vGroup, false);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView textView = (TextView) view.findViewById(R.id.list_item_phrase_textview);

        int phraseColID = cursor.getColumnIndex(TransDBcontract.PhrasesDefs.PHRASE_COL);
        int langColID = cursor.getColumnIndex(TransDBcontract.PhrasesDefs.LANG_COL);
        Button delButton = (Button) view.findViewById(R.id.list_item_removeButton);

        //Sets the values from the database to String variables to be used when clicked
        final String phrase = cursor.getString(phraseColID);
        final String lang = cursor.getString(langColID);
        textView.setText(phrase);
        int id = cursor.getInt(cursor.getColumnIndex("_id"));
        delButton.setTag(id);
        textView.setTag(id);

        //Deletes phrase from database when pressed
        delButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                int rowId = (Integer) v.getTag();
                long ret = deletePhrase(rowId, v.getContext());
                if (ret > 0) {
                    notifyDataSetChanged();
                }
            }
        });

        //TODO: Vet inte detta är ett vettigt sätt att göra detta på?
        //Creates a new TranslatorFragments and passes arguments containing the phrase and languague to be set in the ui.
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //int rowId = (Integer) v.getTag();
                Bundle bundle = new Bundle();
                bundle.putString("phrase", phrase);
                bundle.putString("lang", lang);
                TranslatorFragment transFrag = new TranslatorFragment();
                transFrag.setArguments(bundle);
                FragmentTransaction ft = ((Activity) aContext).getFragmentManager().beginTransaction();
                ft.replace(R.id.container, transFrag);
                ft.commit();
            }
        });
    }

    /**
     * Deletes the phrase with the ID passed.
     *
     * @param id int containing DB id of phrase.
     * @param context Context.
     * @return The id if the deleted phrase
     */
    public long deletePhrase(int id, Context context) {

        String[] whereArgs = new String[] {""+id};

        int deletedId = context.getContentResolver().delete(
                TransDBcontract.PhrasesDefs.CONTENT_URI,
                "_id=?",
                whereArgs
        );
        Log.v(LOG_TAG, "row deleted: " + deletedId);
        return deletedId;
    }
}
