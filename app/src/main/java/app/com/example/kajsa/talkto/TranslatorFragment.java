package app.com.example.kajsa.talkto;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.speech.tts.TextToSpeech;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.support.v4.view.MenuItemCompat;
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
import android.widget.ImageButton;
import android.support.v7.widget.ShareActionProvider;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

//TODO: Se hur man använder den inte decreperade versionen av speak
/**
 * Fragment containing the translation view used to translate phrases
 */
public class TranslatorFragment extends Fragment implements GetTranslationTask.AsyncResponse {

    private static String LOG_TAG = TranslatorFragment.class.getName();

    private TextView txtSpeechInput;
    private TextView txtSpeechOutput;
    private EditText editSpeechInput;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    public TextToSpeech t1;
    private ShareActionProvider menuShareActionProvider;
    private Spinner fromSpinner;
    private Spinner toSpinner;
    private ViewFlipper flipper;

    String phraseString;
    String langString;

    public TranslatorFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Gets arguments passed if the fragments is created from the fragment listing saved phrases
        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey("phrase") && args.containsKey("lang")) {
                phraseString = args.getString("phrase");
                langString = args.getString("lang");
            }
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.sharemenu, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);
        menuShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    //Handles the toggles of the setting checkbox
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.toggle_txtmode):
                Properties.getInstance().textmode = !item.isChecked();
                item.setChecked(true);

                //Transfer the text from textview to edittextview and viceversa when textmode setting changes
                if (Properties.getInstance().textmode) {
                    editSpeechInput.setText(txtSpeechInput.getText().toString());
                } else {
                    txtSpeechInput.setText(editSpeechInput.getText().toString());
                }
                setMode();
                return true;
            default:
                break;
        }
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View transView = inflater.inflate(R.layout.fragment_translator, container, false);

        flipper = (ViewFlipper) transView.findViewById(R.id.modeFlipper);

        setMode();

        txtSpeechInput = (TextView) transView.findViewById(R.id.txtSpeechInput);
        editSpeechInput = (EditText) transView.findViewById(R.id.editSpeechInput);
        txtSpeechOutput = (TextView) transView.findViewById(R.id.txtSpeechOutput);
        ImageButton btnListen = (ImageButton) transView.findViewById(R.id.btnListen);
        ImageButton btnTalk = (ImageButton) transView.findViewById(R.id.btnTalk);
        ImageButton btnTrans = (ImageButton) transView.findViewById(R.id.btnTrans);
        Button navTop = (Button) transView.findViewById(R.id.topbutton);
        Button clearBotton = (Button) transView.findViewById(R.id.clearbutton);
        fromSpinner = (Spinner) transView.findViewById(R.id.from_spinner);
        toSpinner = (Spinner) transView.findViewById(R.id.to_spinner);

        ArrayAdapter<CharSequence> fromAdapter = ArrayAdapter.createFromResource(this.getActivity(), R.array.lang_array, android.R.layout.simple_spinner_item);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpinner.setAdapter(fromAdapter);
        toSpinner.setAdapter(fromAdapter);

        if (phraseString != null && langString != null ) {
            if (Properties.getInstance().textmode) {
                editSpeechInput.setText(phraseString);
            } else {
                txtSpeechInput.setText(phraseString);
            }
            int spinnerPos = fromAdapter.getPosition(langString);
            fromSpinner.setSelection(spinnerPos);
        }

        //Changes view to phraselist fragment when pressed
        navTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.v("Button", "Top Button");
                final FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.container, new ToplistFragment());
                ft.commit();
            }
        });

        //Clears textfields when pressed
        clearBotton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editSpeechInput.setText("");
                txtSpeechInput.setText("");
                txtSpeechOutput.setText("");
            }
        });

        //Speaks the translated text when pressed
        btnTalk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speakTranslatedText(txtSpeechOutput.getText().toString());
            }
        });

        //Translates the text when pressed
        btnTrans.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Properties.getInstance().textmode) {
                    translatePhrase(editSpeechInput.getText().toString());
                } else {
                    translatePhrase(txtSpeechInput.getText().toString());
                }
            }
        });

        //When pressing the textview it automatically changes to textmode
        txtSpeechInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editSpeechInput.setText(txtSpeechInput.getText().toString());
                flipper.setDisplayedChild(1);
                Properties.getInstance().textmode = true;
            }
        });

        //Starts voicerecognizer intent when pressed
        btnListen.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });

        return transView;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPause() {
        if (t1 != null) {
            t1.shutdown();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        t1=new TextToSpeech(getActivity(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.ENGLISH);
                }
            }
        });
        super.onResume();
    }


    /**
     *  Sets the textmode depending on textmode variable.
     */
    private void setMode() {
        if (Properties.getInstance().textmode) {
            flipper.setDisplayedChild(1);
        } else {
            flipper.setDisplayedChild(0);
        }
    }

    //TODO: Kanske bättre format på share - fixa om möjligt så att om täxtfälten är tomma så finns inget att sharea.
    /**
     * Creates a share intent using the to och from Strings displayed in the UI
     *
     * @return A share intent with sprecifications set in function.
     */
    private Intent shareForcastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.setType("text/plain");
        if (Properties.getInstance().textmode) {
            shareIntent.putExtra(Intent.EXTRA_TEXT, editSpeechInput.getText().toString() + " - " + txtSpeechOutput.getText().toString());
        } else {
            shareIntent.putExtra(Intent.EXTRA_TEXT, txtSpeechInput.getText().toString() + " - " + txtSpeechOutput.getText().toString());
        }
        return shareIntent;
    }

    /**
     * This functions converts the Spinner values to the String values required
     * for the Recognizer intent and the txtToSpeech Locale setting.
     *
     * @param lang The string to be convert (ex. Swedish).
     * @return The langague setting in the correct string format.
     */
    private String getLocaleLang(String lang) {
        String ret;
        switch (lang) {
            case "Swedish":
                ret = "sv-SE";
                break;
            case "English":
                ret = "en-US";
                break;
            case "French":
                ret = "fr-FR";
                break;
            case "Spanish":
                ret = "es-ES";
                break;
            case "German":
                ret = "de-DE";
                break;
            case "Italian":
                ret = "it-IT";
                break;
            default:
                ret = "def";
        }
        return ret;
    }

    /**
     * Reads a String variable using textToSpeech
     * Sets the language to use depening on the toSpinner value.
     *
     * @param phrase String to be read.
     */
    private void speakTranslatedText(String phrase) {
        if (!phrase.equals("")) {
            t1.setLanguage(new Locale(getLocaleLang(String.valueOf(toSpinner.getSelectedItem()))));
            t1.speak(phrase, TextToSpeech.QUEUE_FLUSH, null);
        } else {
            Toast.makeText(getActivity(),
                    getString(R.string.translate_text_first),
                    Toast.LENGTH_SHORT).show();
            Log.v(LOG_TAG, "No string to say");
        }
    }

    /**
     * Listens to speech input. Sets the text to be translated to whatever could be transcribed.
     */
    public void translatePhrase(String phrase) {
        if (!phrase.equals("")) {
            GetTranslationTask translationTasker = new GetTranslationTask(TranslatorFragment.this);
            translationTasker.execute(phrase, String.valueOf(fromSpinner.getSelectedItem()), String.valueOf(toSpinner.getSelectedItem()));
        } else {
            Log.v(LOG_TAG, "No string to translate");
        }
    }

    /**
     * Asks for speech input. Checks spinner value to set the listening language.
     */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        //Gets the value from the "from" langague selection spinner
        String language = getLocaleLang(String.valueOf(fromSpinner.getSelectedItem()));
        if (language.equals("def")) {
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        } else {
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);
        }
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.text_to_translate));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getActivity(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Listens to speech input. Sets the text to be translated to whatever could be transcribed.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == Activity.RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (Properties.getInstance().textmode) {
                        editSpeechInput.setText(result.get(0));
                    } else {
                        txtSpeechInput.setText(result.get(0));
                    }

                    txtSpeechOutput.setText("");

                }
                break;
            }

        }
    }


    /**
     * When the asynctask to get the translation is done it will call this function to set the text element and the share intent
     */
    @Override
    public void processFinish(String res) {
        txtSpeechOutput.setText(res);
        if ((!txtSpeechInput.getText().toString().equals("") || !editSpeechInput.getText().toString().equals("")) && (!txtSpeechOutput.getText().toString().equals(""))) {
            menuShareActionProvider.setShareIntent(shareForcastIntent());
        }
    }
}
