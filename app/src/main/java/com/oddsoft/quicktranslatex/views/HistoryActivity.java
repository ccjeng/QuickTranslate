package com.oddsoft.quicktranslatex.views;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.oddsoft.quicktranslatex.R;
import com.oddsoft.quicktranslatex.controller.history.HistoryDAO;
import com.oddsoft.quicktranslatex.controller.history.Item;
import com.oddsoft.quicktranslatex.controller.history.ItemAdapter;
import com.oddsoft.quicktranslatex.utils.Analytics;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class HistoryActivity extends AppCompatActivity {

    private static final String TAG = "HistoryActivity";
    private Analytics ga;

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    //@Bind(R.id.recyclerView)
    //RecyclerView recyclerView;
    @Bind(R.id.listView)
    ListView listView;

    private ItemAdapter itemAdapter;
    private HistoryDAO historyDAO;
    private List<Item> items;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationIcon(new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_arrow_back)
                .color(Color.WHITE)
                .actionBar());


        historyDAO = new HistoryDAO(this);

        Log.d(TAG, "count = " + historyDAO.getCount());


        if (historyDAO.getCount() == 0) {
            Toast.makeText(this, "", Toast.LENGTH_LONG ).show();
        } else {

            items = historyDAO.getAll();

            itemAdapter = new ItemAdapter(this, R.layout.history_item, items);
            listView.setAdapter(itemAdapter);
            listView.setOnItemClickListener(new ListView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position,
                                        long id) {

                    Item item = items.get(position);
                    Log.d(TAG, item.getFromText());

                    //confirm
                    //delete
                    //refresh
                }
            });
        }


    }


    @Override
    protected void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
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

}
