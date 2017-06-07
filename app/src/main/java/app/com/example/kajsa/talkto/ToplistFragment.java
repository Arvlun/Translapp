package app.com.example.kajsa.talkto;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import app.com.example.kajsa.talkto.db.TransDBcontract;

//TODO: lägg till skickt mellan dbprovider och fragmentet?
/**
 * Fragment containing the saved phrases view.
 */
public class ToplistFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PHRASELIST_LOADERID = 0;
    private PhraseAdapter phraseAdapter;
    Spinner langSpinner;

    public ToplistFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    //Lets the user change textmode in this fragment aswell - will carry over to translator view.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.toggle_txtmode):
                Properties.getInstance().textmode = !item.isChecked();
                item.setChecked(true);
                return true;
            default:
                break;
        }
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Uri phraseUri = TransDBcontract.PhrasesDefs.CONTENT_URI;
        //ändra så att det inte är massa null som skickas in ???
        //Cursor cursor = getActivity().getContentResolver().query(phraseUri, null, null, null, null);

        phraseAdapter = new PhraseAdapter(getActivity(), null, 0);

        View rView = inflater.inflate(R.layout.fragment_toplist, container, false);
        langSpinner = (Spinner) rView.findViewById(R.id.lang_spinner);
        Button navTrans = (Button) rView.findViewById(R.id.transbutton);
        Button savePhrase = (Button) rView.findViewById(R.id.savePhraseButton);
        final EditText phraseToSave = (EditText) rView.findViewById(R.id.textPhrase);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getActivity(), R.array.lang_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        langSpinner.setAdapter(adapter);

        // Change view to the translator fragment
        navTrans.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.v("Button", "Trans Button");
                final FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.container, new TranslatorFragment());
                ft.commit();
            }
        });

        //Saves phrase using values of spinner och EditText
        savePhrase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.v("TEST spinner", phraseToSave.getText().toString() + " " + String.valueOf(langSpinner.getSelectedItem()));
                addPhrase(phraseToSave.getText().toString(), String.valueOf(langSpinner.getSelectedItem()));
            }
        });

        ListView listView = (ListView) rView.findViewById(R.id.listview_phrases);
        listView.setAdapter(phraseAdapter);

        return rView;
    }

//    @Override
//    public void onStart() {
//        super.onStart();
//    }

    /**
     * Adds a phrase to the database.
     *
     * @param phrase String containing the phrase to save.
     * @param lang The language of the phrase.
     * @return The id of the phrase added.
     */
    private long addPhrase(String phrase, String lang) {

        long phraseId;

        ContentValues phraseValues = new ContentValues();

        phraseValues.put(TransDBcontract.PhrasesDefs.PHRASE_COL, phrase);
        phraseValues.put(TransDBcontract.PhrasesDefs.LANG_COL, lang);
        Context lContext;
        if (this.isAdded()) {
            lContext = getActivity();
        } else {
            Log.v("CONTEXT ERR", "fragment not added");
            return -1;
        }

        Uri insertedUri = lContext.getContentResolver().insert(
                TransDBcontract.PhrasesDefs.CONTENT_URI,
                phraseValues
        );

        phraseId = ContentUris.parseId(insertedUri);
        return phraseId;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(PHRASELIST_LOADERID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Uri phraseUri = TransDBcontract.PhrasesDefs.CONTENT_URI;
        CursorLoader cursorLoader = new CursorLoader(getActivity(), phraseUri, null, null, null, null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        phraseAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        phraseAdapter.swapCursor(null);
    }
}