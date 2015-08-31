package com.oddsoft.quicktranslatex;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.ads.*;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.oddsoft.quicktranslatex.app.Analytics;
import com.oddsoft.quicktranslatex.app.OAuth;
import com.oddsoft.quicktranslatex.app.QuickTranslateX;
import com.oddsoft.quicktranslatex.drawer.DrawerItem;
import com.oddsoft.quicktranslatex.drawer.DrawerItemAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends ActionBarActivity {

    private static final String TAG = "QuickTranslate";
    public static final String PREF = "TRANS";
    public static final String PREF_FROM = "PREF_From";
    public static final String PREF_TO = "PREF_To";

    @Bind(R.id.from_language) Spinner fromSpinner;
    @Bind(R.id.to_language) Spinner toSpinner;
    @Bind(R.id.original_text) EditText origText;
    @Bind(R.id.translated_text) TextView transText;
    @Bind(R.id.from_text) TextView fromText;

    private ActionBar actionbar;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

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

    @Bind(R.id.drw_layout) DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    @Bind(R.id.llv_left_drawer) LinearLayout mLlvDrawerContent;
    @Bind(R.id.lsv_drawer_menu) ListView mLsvDrawerMenu;

    // 記錄被選擇的選單指標用
    private int mCurrentMenuItemPosition = -1;

    private Analytics ga;
    private String mAuthToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initThreading();
        findViews();
        initActionBar();
        initDrawer();
        initDrawerList();
        setAdapters();
        setListeners();
        restorePrefs();
        initAuth();
        adView();

        ga = new Analytics();
        ga.trackerPage(this);


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
                    .addTestDevice("7710C21FF2537758BF3F80963477D68E") // 我的 Galaxy Nexus 測試手機
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


    private void initAuth() {
        //oAuth
        OAuth mOAuth = new OAuth();
        mAuthToken = mOAuth.getToken();

        QuickTranslateX.setAuthState(true);
        QuickTranslateX.setAuthToken(mAuthToken);

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
                                MainActivity.this, // reference to activity
                                original, // original text
                                getLang(fromSpinner), // from language
                                getLang(toSpinner) // to language
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
        fuzzyPreference = false;
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
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            }
        }
    }

    private void initActionBar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_menu)
                .color(Color.WHITE)
                .actionBarSize());

        actionbar = getSupportActionBar();
    }

    private void initDrawer() {
        // 設定 Drawer 的影子
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,    // 讓 Drawer Toggle 知道母體介面是誰
                R.drawable.ic_drawer, // Drawer 的 Icon
                R.string.app_name, // Drawer 被打開時的描述
                R.string.app_name // Drawer 被關閉時的描述
        ) {
            //被打開後要做的事情
            @Override
            public void onDrawerOpened(View drawerView) {
                // 將 Title 設定為自定義的文字
                actionbar.setTitle(R.string.app_name);
            }

            //被關上後要做的事情
            @Override
            public void onDrawerClosed(View drawerView) {
                // 將 Title 設定回 APP 的名稱
                actionbar.setTitle(R.string.app_name);
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

    }

    private void initDrawerList() {

        String[] drawer_menu = this.getResources().getStringArray(R.array.drawer_menu);

        DrawerItem[] drawerItem = new DrawerItem[2];

        drawerItem[0] = new DrawerItem(new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_build)
                .color(Color.GRAY)
                .sizeDp(24),
                drawer_menu[0]);
        drawerItem[1] = new DrawerItem(new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_info)
                .color(Color.GRAY)
                .sizeDp(24),
                drawer_menu[1]);

        DrawerItemAdapter adapter = new DrawerItemAdapter(this, R.layout.drawer_item, drawerItem);

        mLsvDrawerMenu.setAdapter(adapter);

        // 當清單選項的子物件被點擊時要做的動作
        mLsvDrawerMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                selectMenuItem(position);
            }
        });

    }

    private void selectMenuItem(int position) {
        mCurrentMenuItemPosition = position;

        switch (mCurrentMenuItemPosition) {
            case 0:
                startActivity(new Intent(this, Prefs.class));
                break;
            case 1:
                startActivity(new Intent(this, AboutActivity.class));
                break;
        }

        // 將選單的子物件設定為被選擇的狀態
        mLsvDrawerMenu.setItemChecked(position, true);

        // 關掉 Drawer
        mDrawerLayout.closeDrawer(mLlvDrawerContent);
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem menuItem1 = menu.findItem(R.id.clear_button);
        menuItem1.setIcon(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_delete).actionBarSize().color(Color.WHITE));

        MenuItem menuItem2 = menu.findItem(R.id.change_button);
        menuItem2.setIcon(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_loop).actionBarSize().color(Color.WHITE));

        MenuItem menuItem3 = menu.findItem(R.id.search_button);
        menuItem3.setIcon(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_search).actionBarSize().color(Color.WHITE));

        MenuItem menuItem4 = menu.findItem(R.id.share_button);
        menuItem4.setIcon(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_share).actionBarSize().color(Color.WHITE));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.clear_button:
                ga.trackEvent(this, "Click", "Button", "Clean", 0);
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

}
