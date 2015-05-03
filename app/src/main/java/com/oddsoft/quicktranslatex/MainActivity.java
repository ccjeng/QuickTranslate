package com.oddsoft.quicktranslatex;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import com.google.android.gms.ads.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

public class MainActivity extends Activity
        implements View.OnClickListener {

    private static final String TAG = "QuickTranslate";
    public static final String PREF = "TRANS";
    public static final String PREF_FROM = "PREF_From";
    public static final String PREF_TO = "PREF_To";

    private Spinner fromSpinner;
    private Spinner toSpinner;
    private EditText origText;
    private TextView transText;
    private TextView fromText;
    private Button changeButton;
    private Button clearButton;
    private Button searchButton;
    private Button shareButton;
    private boolean fuzzyPreference;
    private String searchPreference;

    private TextWatcher textWatcher;
    private AdapterView.OnItemSelectedListener itemListener;
    private String[] langShortNames;

    private Handler guiThread;
    private ExecutorService transThread;
    private Runnable updateTask;
    private Future<?> transPending;
    private AdView adView;

    final Handler updateHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initThreading();
        findViews();
        setAdapters();
        setListeners();
        restorePrefs();
        adView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (adView != null)
            adView.pause();
        // Save user preferences.
        SharedPreferences settings = getSharedPreferences(PREF, 3);
        settings.edit()
                .putInt(PREF_FROM, fromSpinner.getSelectedItemPosition())
                .putInt(PREF_TO, toSpinner.getSelectedItemPosition())
                .commit();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (adView != null)
            adView.resume();
    }
    @Override
    protected void onDestroy() {
        // Terminate extra threads here
        transThread.shutdownNow();
        if (adView != null)
            adView.destroy();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        getPrefs();
    }

    private void adView() {
        adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();

        //AdRequest adRequest = new AdRequest.Builder()
        //        .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)       // 仿真器
        //        .addTestDevice("7710C21FF2537758BF3F80963477D68E") // 我的 Galaxy Nexus 測試手機
        //        .build();
        adView.loadAd(adRequest);
    }

    /** Get a handle to all user interface elements */
    private void findViews() {
        fromSpinner = (Spinner) findViewById(R.id.from_language);
        toSpinner = (Spinner) findViewById(R.id.to_language);
        origText = (EditText) findViewById(R.id.original_text);
        transText = (TextView) findViewById(R.id.translated_text);
        fromText = (TextView) findViewById(R.id.from_text);
        changeButton = (Button) findViewById(R.id.change_button);
        clearButton = (Button) findViewById(R.id.clear_button);
        searchButton = (Button) findViewById(R.id.search_button);
        shareButton = (Button) findViewById(R.id.share_button);
        langShortNames = getResources().getStringArray(R.array.languages_values);

        // Set up click listeners for all the buttons
        changeButton.setOnClickListener(this);
        clearButton.setOnClickListener(this);
        searchButton.setOnClickListener(this);
        shareButton.setOnClickListener(this);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.change_button:
                switchLanguage();
                break;
            case R.id.clear_button:
                origText.setText("");
                transText.setText("");
                break;
            case R.id.search_button:
                search();
                break;
            case R.id.share_button:
                share();
                break;
        }
    }
    // Restore preferences
    private void restorePrefs() {
        SharedPreferences settings = getSharedPreferences(PREF, 0);
        Integer pref_from = settings.getInt(PREF_FROM, 0);
        Integer pref_to = settings.getInt(PREF_TO, 6);
        if(! "".equals(pref_from)) {
            fromSpinner.setSelection(pref_from);
            toSpinner.setSelection(pref_to);
        }
    }
    /** Define data source for the spinners */
    private void setAdapters() {
        // Spinner list comes from a resource,
        // Spinner user interface uses standard layouts
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.languages,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        fromSpinner.setAdapter(adapter);
        toSpinner.setAdapter(adapter);
    }

    /** Define switch language button*/
    private void switchLanguage() {
        //switch language button
        int tmpTo = toSpinner.getSelectedItemPosition();
        int tmpFrom = fromSpinner.getSelectedItemPosition();
        fromSpinner.setSelection(tmpTo);
        toSpinner.setSelection(tmpFrom);
        queueUpdate(1000 /* milliseconds */);

    }

    /** Setup user interface event handlers */
    private void setListeners() {
        // Define event listeners
        textWatcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            /* Do nothing */
            }
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                queueUpdate(1000 /* milliseconds */);
            }
            public void afterTextChanged(Editable s) {
            /* Do nothing */
            }
        };
        itemListener = new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View v,
                                       int position, long id) {
                queueUpdate(200 /* milliseconds */);
            }
            public void onNothingSelected(AdapterView<?> parent) {
            /* Do nothing */
            }
        };

        // Set listeners on graphical user interface widgets
        origText.addTextChangedListener(textWatcher);
        fromSpinner.setOnItemSelectedListener(itemListener);
        toSpinner.setOnItemSelectedListener(itemListener);
    }

    /**
     * Initialize multi-threading. There are two threads: 1) The main
     * graphical user interface thread already started by Android,
     * and 2) The translate thread, which we start using an executor.
     */
    private void initThreading() {
        guiThread = new Handler();
        transThread = Executors.newSingleThreadExecutor();

        // This task does a translation and updates the screen
        updateTask = new Runnable() {
            public void run() {
                // Get text to translate
                String original = origText.getText().toString().trim();

                // Cancel previous translation if there was one
                if (transPending != null)
                    transPending.cancel(true);

                // Take care of the easy case
                if (original.length() == 0) {
                    transText.setText(R.string.empty);
                } else {
                    // Let user know we're doing something
                    transText.setText(R.string.translating);

                    // Begin translation now but don't wait for it
                    try {
                        TranslateTask translateTask;
                        if (fuzzyPreference) {
                            translateTask = new TranslateTask(
                                    MainActivity.this, // reference to activity
                                    original, // original text
                                    "", // from language (blank)
                                    getLang(toSpinner) // to language
                            );
                        }
                        else {
                            translateTask = new TranslateTask(
                                    MainActivity.this, // reference to activity
                                    original, // original text
                                    getLang(fromSpinner), // from language
                                    getLang(toSpinner) // to language
                            );
                        }

                        transPending = transThread.submit(translateTask);
                    } catch (RejectedExecutionException e) {
                        // Unable to start new task
                        transText.setText(R.string.translation_error);
                    }
                }
            }
        };
    }

    /** Extract the language code from the current spinner item */
    private String getLang(Spinner spinner) {
        String result = langShortNames[spinner.getSelectedItemPosition()];
        Log.d(TAG, " getLang " + result);
        return result;
    }

    /** Request an update to start after a short delay */
    private void queueUpdate(long delayMillis) {
        // Cancel previous update if it hasn't started yet
        guiThread.removeCallbacks(updateTask);
        // Start an update if nothing happens after a few milliseconds
        guiThread.postDelayed(updateTask, delayMillis);
    }

    /** Modify text on the screen (called from another thread) */
    public void setTranslated(String text) {
        guiSetText(transText, text);
    }

    /** All changes to the GUI must be done in the GUI thread */
    private void guiSetText(final TextView view, final String text) {
        guiThread.post(new Runnable() {
            public void run() {
                view.setText(text);
                Log.d(TAG, " guiSetText " + text);
            }
        });
    }

    // Get preferences
    private void getPrefs() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        //fuzzyPreference = prefs.getBoolean("fuzzy", false);
        fuzzyPreference = false;
        searchPreference = prefs.getString("search", "Google");

        if (prefs.getBoolean("fuzzy", false)) {
            fromSpinner.setVisibility(View.GONE);
            fromText.setVisibility(View.GONE);
            changeButton.setVisibility(View.GONE);
        }
        else {
            //fromSpinner.setVisibility(View.VISIBLE);
            //fromText.setVisibility(View.VISIBLE);
            //changeButton.setVisibility(View.VISIBLE);
        }
    }

    private void share() {
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "");
        intent.putExtra(Intent.EXTRA_TEXT, transText.getText());
        startActivity(Intent.createChooser(intent, "Select an action for sharing"));
    }

    private void search() {
        Log.d(TAG, "searchPreference=" + searchPreference.toString());
        Uri uri;

        if (searchPreference.toString().equals("Google")) {
            Intent search = new Intent(Intent.ACTION_WEB_SEARCH);
            search.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            search.putExtra(SearchManager.QUERY, transText.getText());
            final Bundle appData = getIntent().getBundleExtra(SearchManager.APP_DATA);

            if (appData != null) {
                search.putExtra(SearchManager.APP_DATA, appData);
            }
            startActivity(search);
        }

        if (searchPreference.toString().equals("Bing")) {
            uri = Uri.parse("http://m.bing.com/search/search.aspx?A=webresults&Q="
                    + transText.getText().toString().replace(" ", "+"));
            Log.d(TAG, "bing uri=" + uri.toString());
            if (!transText.getText().equals("")) {
                startActivity( new Intent( Intent.ACTION_VIEW, uri ) );
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, Prefs.class));
                break;
            case R.id.exit_settings:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

}
