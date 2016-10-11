package com.oddsoft.quicktranslatex.views;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.oddsoft.quicktranslatex.R;
import com.oddsoft.quicktranslatex.controller.history.HistoryDAO;
import com.oddsoft.quicktranslatex.controller.history.Item;
import com.oddsoft.quicktranslatex.controller.history.ItemAdapter;
import com.oddsoft.quicktranslatex.utils.Analytics;
import com.oddsoft.quicktranslatex.utils.Constant;
import com.oddsoft.quicktranslatex.views.base.BaseActivity;
import com.oddsoft.quicktranslatex.views.base.QuickTranslateX;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HistoryActivity extends BaseActivity {

    private static final String TAG = "HistoryActivity";
    private Analytics ga;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.listView)
    ListView listView;

    private HistoryDAO historyDAO;
    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        adView();

        historyDAO = new HistoryDAO(this);

        if (historyDAO.getCount() == 0) {
            Toast.makeText(this, R.string.result_nodata, Toast.LENGTH_LONG ).show();
        } else {
            refreshData();
        }

    }



    @Override
    protected void onPause() {
        super.onPause();
        if (adView != null)
            adView.pause();;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adView != null)
            adView.resume();
    }

    @Override
    protected void onDestroy() {
        if (adView != null)
            adView.destroy();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    public void refreshData() {
        List<Item> items = historyDAO.getAll();
        ItemAdapter itemAdapter = new ItemAdapter(this, R.layout.history_item, items);
        listView.setAdapter(itemAdapter);
    }


    private void adView() {

        LinearLayout adBannerLayout = (LinearLayout) findViewById(R.id.adLayout);

        adView = new AdView(this);
        adView.setAdUnitId(Constant.ADMOB_QT_HISTORY);
        adView.setAdSize(AdSize.SMART_BANNER);
        adBannerLayout.addView(adView);

        AdRequest adRequest;
        if (QuickTranslateX.APPDEBUG) {
            adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .addTestDevice(Constant.ADMOB_TEST_DEVICE)
                    .build();
        } else {
            adRequest = new AdRequest.Builder().build();
        }
        adView.loadAd(adRequest);
    }
}
