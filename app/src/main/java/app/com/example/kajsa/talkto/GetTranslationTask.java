package app.com.example.kajsa.talkto;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *AsyncTask that translates a String using API from https://tech.yandex.com/translate/
 */
class GetTranslationTask extends AsyncTask<String, Void, String> {

    private static String LOG_TAG = GetTranslationTask.class.getName();
    public AsyncResponse delegate = null;

    public GetTranslationTask(AsyncResponse delegate) {
        this.delegate = delegate;
    }

    /**
     * Returns the translated String from the JSON returned by the API.
     *
     * @param json String containing the JSON-data from the API.
     * @return The translated string.
     */
    private String getTransText(String json) throws JSONException {
        final String T_TEXT = "text";
        try {
            JSONObject forecastJson = new JSONObject(json);
            JSONArray textObject = forecastJson.getJSONArray(T_TEXT);
            String translation = textObject.getString(0);
            if (translation != null) {
                Log.v("Transplation: ", translation);
                return translation;
            } else {
                return "snett";
            }
        } catch (JSONException e) {
            Log.e("JSONERROR", e.getMessage(), e);
            e.printStackTrace();
            return "Error JSON";
        }
    }

    /**
     * Used to convert spinner language values to API-syntax.
     *
     * @param lang Langague value passed to the Task(from the spinnervalues in the UI).
     * @return Correct language syntax for API.
     */
    private String getLangAPISyntax(String lang) {
        String ret;
        switch (lang) {
            case "Swedish":
                ret = "sv";
                break;
            case "English":
                ret = "en";
                break;
            case "French":
                ret = "fr";
                break;
            case "Spanish":
                ret = "es";
                break;
            case "German":
                ret = "de";
                break;
            case "Italian":
                ret = "it";
                break;
            default:
                ret = "";
        }
        return ret;
    }

    /**
     * Performs to API-calls to get a String translated
     *
     * @param params String parameters passed in an array. (Should be in the order of String to translate, from lang, to lang).
     * @return The translated String.
     */
    @Override
    protected String doInBackground(String... params) {
        String res = "..";
        //String res = "testing";
        if (params.length == 0) {
            return "";
        }
        //Första parameter som skickas in (strängen som skall översättas)
        String trans = params[0];

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String forecastJsonStr;
        // Värden för APIets parametrar
        String format = "plain";
        // Gör om parameter 2 och 3 till apiets format
        String lang = getLangAPISyntax(params[1]) + "-" + getLangAPISyntax(params[2]);
        // API-nyckel TODO: borde nog förvaras på bättre sätt
        String apikey = "trnsl.1.1.20161120T095352Z.d04c8868138af811.50da8dc61d350802f9cf417f1718d51451f6023c";
        Log.v(LOG_TAG, trans + " " + lang);
        try {
            //APIets parametrar
            final String BASE_URL = "https://translate.yandex.net/api/v1.5/tr.json/translate?";
            final String TEXT_PARAM = "text";
            final String LANG_PARAM = "lang";
            final String FORMAT_PARAM = "format";
            final String KEY_PARAM = "key";

            //Skapar URL för API
            Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter(KEY_PARAM, apikey)
                    .appendQueryParameter(TEXT_PARAM, trans)
                    .appendQueryParameter(LANG_PARAM, lang)
                    .appendQueryParameter(FORMAT_PARAM, format)
                    .build();

            URL url = new URL(builtUri.toString());
            //Log.v(LOG_TAG, "URL: " + url);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            //prettyprint
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }

            forecastJsonStr = buffer.toString();
            Log.v(LOG_TAG, "TXTJSON:" + forecastJsonStr);
            res = getTransText(forecastJsonStr);
            // VIEWTEXT
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            return null;
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        return res;
    }

    /**
     *Runs after doInBackground - sets the delegate variable.
     */
    protected void onPostExecute(String res) {
        delegate.processFinish(res);
    }

    /**
     *Interface to make the translated string accessible from classes implementing
     * this interface and overrides the function processFinish
     */
    public interface AsyncResponse {
        void processFinish(String output);
    }
}