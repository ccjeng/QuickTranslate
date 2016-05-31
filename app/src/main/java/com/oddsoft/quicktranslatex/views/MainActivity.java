package com.oddsoft.quicktranslatex.views;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.oddsoft.quicktranslatex.QuickTranslateX;
import com.oddsoft.quicktranslatex.R;
import com.oddsoft.quicktranslatex.controller.Secret;
import com.oddsoft.quicktranslatex.controller.TranslateService;
import com.oddsoft.quicktranslatex.controller.history.HistoryDAO;
import com.oddsoft.quicktranslatex.controller.history.Item;
import com.oddsoft.quicktranslatex.utils.Analytics;
import com.oddsoft.quicktranslatex.utils.Constant;
import com.oddsoft.quicktranslatex.utils.Utils;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "QuickTranslate";
    public static final String PREF = "TRANS";
    public static final String PREF_FROM = "PREF_From";
    public static final String PREF_TO = "PREF_To";

    @Bind(com.oddsoft.quicktranslatex.R.id.from_language) Spinner fromSpinner;
    @Bind(R.id.to_language) Spinner toSpinner;
    @Bind(R.id.original_text) EditText origText;
    @Bind(R.id.translated_text) TextView transText;
    @Bind(R.id.from_text) TextView fromText;
    @Bind(R.id.navigation) NavigationView navigation;
    @Bind(R.id.drawerlayout) DrawerLayout drawerLayout;
    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.save_result) ImageButton saveResult;

    //private boolean fuzzyPreference;
    private String searchPreference;

    private TextWatcher textWatcher;
    private AdapterView.OnItemSelectedListener itemListener;
    private String[] langShortNames;
    private String[] langLongNames;

    private Handler guiThread;
    private ExecutorService transThread;
    private Runnable updateTask;
    private Future<?> transPending;
    private AdView adView;

    private Analytics ga;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    private static final int DIALOG_WELCOME = 1;
    private static final int DIALOG_UPDATE = 2;

    private HistoryDAO historyDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //Firebase.setAndroidContext(this);

        initThreading();
        findViews();
        initActionBar();
        initDrawer();
        setAdapters();
        setListeners();
        restorePrefs();

        saveResult.setVisibility(View.GONE);
        saveResult.setBackground(new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_save)
                .color(Color.GRAY));

        saveResult.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    saveResult();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // 建立資料庫物件
        historyDAO = new HistoryDAO(this);

        if (Utils.isNetworkConnected(this)) {
            Secret.execFirebase();
            incomingContent();
            adView();
        } else {
            transText.setText(R.string.network_error);
            Toast.makeText(this, R.string.network_error, Toast.LENGTH_LONG).show();
        }

        ga = new Analytics();
        ga.trackerPage(this);

        if (Utils.newVersionInstalled(this)) {
            this.showDialog(DIALOG_UPDATE);
        }
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
                .apply();
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
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
        getPrefs();
    }

    private void adView() {

        adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest;
        if (QuickTranslateX.APPDEBUG) {
            adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)       // 仿真器
                    .addTestDevice(Constant.ADMOB_TEST_DEVICE) // 我的 Galaxy Nexus 測試手機
                    .build();
        } else {
            adRequest = new AdRequest.Builder().build();
        }
        adView.loadAd(adRequest);
    }

    /**
     * Get a handle to all user interface elements
     */
    private void findViews() {

        langShortNames = getResources().getStringArray(R.array.languages_values);
        langLongNames = getResources().getStringArray(R.array.languages);

        // Font
        transText.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf"));
        origText.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf"));

    }

    // Restore preferences
    private void restorePrefs() {
        SharedPreferences settings = getSharedPreferences(PREF, 0);
        Integer pref_from = settings.getInt(PREF_FROM, 0);
        Integer pref_to = settings.getInt(PREF_TO, 6);
        if (!"".equals(pref_from)) {
            fromSpinner.setSelection(pref_from);
            toSpinner.setSelection(pref_to);
        }
    }

    /**
     * Define data source for the spinners
     */
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

    /**
     * Define switch language button
     */
    private void switchLanguage() {
        //switch language button
        int tmpTo = toSpinner.getSelectedItemPosition();
        int tmpFrom = fromSpinner.getSelectedItemPosition();
        fromSpinner.setSelection(tmpTo);
        toSpinner.setSelection(tmpFrom);
        queueUpdate(1000 /* milliseconds */);
    }

    /**
     * Setup user interface event handlers
     */
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
                //if (transPending != null)
                //    transPending.cancel(true);

                // Take care of the easy case
                if (original.length() == 0) {
                    transText.setText(R.string.empty);
                } else {
                    // Let user know we're doing something
                    transText.setText(R.string.translating);

                    // Begin translation now but don't wait for it
                    try {

                        ga.trackEvent(MainActivity.this, "Button", "Language", getLang(fromSpinner) + " - " + getLang(toSpinner), 0);

                        TranslateService translateTask;
                        translateTask = new TranslateService(
                                MainActivity.this,
                                original,
                                getLang(fromSpinner),
                                getLang(toSpinner)
                        );

                        //transPending = transThread.submit(translateTask);
                    } catch (RejectedExecutionException e) {
                        // Unable to start new task
                        transText.setText(R.string.translation_error);
                    }
                }
            }
        };
    }

    /**
     * Extract the language code from the current spinner item
     */
    private String getLang(Spinner spinner) {
        String result = langShortNames[spinner.getSelectedItemPosition()];
        if (QuickTranslateX.APPDEBUG) {
            Log.d(TAG, " getLang " + result);
        }
        return result;
    }

    private String getLangName(Spinner spinner) {
        String result = langLongNames[spinner.getSelectedItemPosition()];
        if (QuickTranslateX.APPDEBUG) {
            Log.d(TAG, " getLang " + result);
        }
        return result;
    }
    /**
     * Request an update to start after a short delay
     */
    private void queueUpdate(long delayMillis) {
        // Cancel previous update if it hasn't started yet
        guiThread.removeCallbacks(updateTask);
        // Start an update if nothing happens after a few milliseconds
        guiThread.postDelayed(updateTask, delayMillis);
    }

    /**
     * Modify text on the screen (called from another thread)
     */
    public void setTranslated(String text) {
        guiSetText(transText, text);
    }

    /**
     * All changes to the GUI must be done in the GUI thread
     */
    private void guiSetText(final TextView view, final String text) {
        guiThread.post(new Runnable() {
            public void run() {
                view.setText(text);

                if (text.equals("") ||
                        text.equals("..........") ||
                        origText.getText().toString().trim().equals("")) {
                    saveResult.setVisibility(View.GONE);
                } else {
                    saveResult.setVisibility(View.VISIBLE);
                }

                if (QuickTranslateX.APPDEBUG) {
                    Log.d(TAG, " guiSetText " + text);
                }
            }
        });
    }

    // Get preferences
    private void getPrefs() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        //fuzzyPreference = prefs.getBoolean("fuzzy", false);
        //fuzzyPreference = false;
        searchPreference = prefs.getString("search", "Google");

        if (prefs.getBoolean("fuzzy", false)) {
            fromSpinner.setVisibility(View.GONE);
            fromText.setVisibility(View.GONE);
            //changeButton.setVisibility(View.GONE);
        } else {
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
        Log.d(TAG, "searchPreference=" + searchPreference);
        Uri uri;

        if (searchPreference.equals("Google")) {
            /*
            Intent search = new Intent(Intent.ACTION_WEB_SEARCH);
            search.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            search.putExtra(SearchManager.QUERY, transText.getText());
            final Bundle appData = getIntent().getBundleExtra(SearchManager.APP_DATA);

            if (appData != null) {
                search.putExtra(SearchManager.APP_DATA, appData);
            }
            startActivity(search);
            */
            uri = Uri.parse("http://www.google.com/search?q="
                    + transText.getText().toString().replace(" ", "+"));
            Log.d(TAG, "Google uri=" + uri.toString());
            if (!transText.getText().equals("")) {
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            }
        }

        if (searchPreference.equals("Bing")) {
            uri = Uri.parse("http://m.bing.com/search/search.aspx?A=webresults&Q="
                    + transText.getText().toString().replace(" ", "+"));
            Log.d(TAG, "bing uri=" + uri.toString());
            if (!transText.getText().equals("")) {
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            }
        }
    }

    private void initActionBar() {
        setSupportActionBar(toolbar);
    }

    private void initDrawer() {
        navigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                //Checking if the item is in checked state or not, if not make it in checked state
                if(menuItem.isChecked())
                    menuItem.setChecked(false);
                else
                    menuItem.setChecked(true);

                //Closing drawer on item click
                drawerLayout.closeDrawers();

                switch (menuItem.getItemId()) {
                    case R.id.navHistory:
                        startActivity(new Intent(MainActivity.this, HistoryActivity.class));
                        break;
                    case R.id.navSetting:
                        startActivity(new Intent(MainActivity.this, Prefs.class));
                        break;
                    case R.id.navAbout:
                        new LibsBuilder()
                                .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                                .withAboutIconShown(true)
                                .withAboutVersionShown(true)
                                .withAboutAppName(getString(R.string.app_name))
                                .withActivityTitle(getString(R.string.about_title))
                                .withAboutDescription(getString(R.string.license))
                                .start(MainActivity.this);
                        break;

                }
                return false;
            }
        });

        //change navigation drawer item icons
        navigation.getMenu().findItem(R.id.navHistory).setIcon(new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_history)
                .color(Color.GRAY)
                .sizeDp(24));

        navigation.getMenu().findItem(R.id.navSetting).setIcon(new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_settings)
                .color(Color.GRAY)
                .sizeDp(24));

        navigation.getMenu().findItem(R.id.navAbout).setIcon(new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_info)
                .color(Color.GRAY)
                .sizeDp(24));

        actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar
                ,R.string.app_name, R.string.app_name){

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank

                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem menuItem1 = menu.findItem(R.id.clear_button);
        menuItem1.setIcon(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_delete).actionBar().color(Color.WHITE));

        MenuItem menuItem2 = menu.findItem(R.id.change_button);
        menuItem2.setIcon(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_loop).actionBar().color(Color.WHITE));

        MenuItem menuItem3 = menu.findItem(R.id.search_button);
        menuItem3.setIcon(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_search).actionBar().color(Color.WHITE));

        MenuItem menuItem4 = menu.findItem(R.id.share_button);
        menuItem4.setIcon(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_share).actionBar().color(Color.WHITE));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.clear_button:
                ga.trackEvent(this, "Click", "Button", "Clean", 0);
                saveResult.setVisibility(View.GONE);
                origText.setText("");
                transText.setText("");
                break;
            case R.id.change_button:
                ga.trackEvent(this, "Click", "Button", "Change", 0);
                switchLanguage();
                break;
            case R.id.search_button:
                ga.trackEvent(this, "Click", "Button", "Search", 0);
                search();
                break;
            case R.id.share_button:
                ga.trackEvent(this, "Click", "Button", "Share", 0);
                share();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    protected final Dialog onCreateDialog(final int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setIcon(new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_info)
                .color(Color.GRAY)
                .sizeDp(24));

        builder.setCancelable(true);
        builder.setPositiveButton(android.R.string.ok, null);

        //final Context context = this;

        switch (id) {
            // case DIALOG_WELCOME:
            //     builder.setTitle(getResources().getString(R.string.welcome_title));
            //     builder.setMessage(getResources().getString(R.string.welcome_message));
            //     break;
            case DIALOG_UPDATE:
                builder.setTitle(getString(R.string.changelog_title));
                final String[] changes = getResources().getStringArray(R.array.updates);
                final StringBuilder buf = new StringBuilder();
                for (int i = 0; i < changes.length; i++) {
                    buf.append("\n\n");
                    buf.append(changes[i]);
                }
                builder.setMessage(buf.toString().trim());
                break;
        }
        return builder.create();
    }

    //Handle the Incoming Content
    private void incomingContent(){

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                // Handle text being sent
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (sharedText != null) {
                    // Update UI to reflect text being shared
                    origText.setText(sharedText);

                }
            }
        }

    }

    private void saveResult() {

        String text = transText.getText().toString().trim();
        String orgText = origText.getText().toString().trim();

        if (!text.equals("") &&
                !text.equals("..........") &&
                !orgText.equals("")) {

            ga.trackEvent(this, "Click", "Button", "Save", 0);

            Item item = new Item();
            item.setDatetime(new Date().getTime());
            item.setFromId(getLangName(fromSpinner));
            item.setToId(getLangName(toSpinner));
            item.setFromText(origText.getText().toString().trim());
            item.setToText(transText.getText().toString().trim());
            historyDAO.insert(item);

            Toast.makeText(MainActivity.this, getString(R.string.result_saved), Toast.LENGTH_LONG).show();
            Log.d(TAG, "Save Result!");
        }
    }
}
