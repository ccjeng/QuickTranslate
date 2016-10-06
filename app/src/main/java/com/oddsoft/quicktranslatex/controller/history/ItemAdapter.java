package com.oddsoft.quicktranslatex.controller.history;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.oddsoft.quicktranslatex.R;
import com.oddsoft.quicktranslatex.views.HistoryActivity;

import java.util.List;

/**
 * Created by andycheng on 2016/2/27.
 */
public class ItemAdapter extends ArrayAdapter<Item> {

    private static final String TAG = "ItemAdapter";
    private int resource;
    private List<Item> items;
    private HistoryActivity context;

    HistoryDAO historyDAO;

    public ItemAdapter(HistoryActivity context, int resource, List<Item> items) {
        super(context, resource, items);
        this.context = context;
        this.resource = resource;
        this.items = items;

        historyDAO = new HistoryDAO(context);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LinearLayout itemView;
        final Item item = getItem(position);


        if (convertView == null) {
            // 建立項目畫面元件
            itemView = new LinearLayout(getContext());
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater li = (LayoutInflater)
                    getContext().getSystemService(inflater);
            li.inflate(resource, itemView, true);
        }
        else {
            itemView = (LinearLayout) convertView;
        }

        TextView dateTimeView = (TextView) itemView.findViewById(R.id.datetime);
        TextView fromIdView = (TextView) itemView.findViewById(R.id.from_id);
        TextView fromTextView = (TextView) itemView.findViewById(R.id.from_text);
        TextView toIdView = (TextView) itemView.findViewById(R.id.to_id);
        TextView toTextView = (TextView) itemView.findViewById(R.id.to_text);
        ImageButton deleteButton = (ImageButton) itemView.findViewById(R.id.delete);

        dateTimeView.setText(item.getLocaleDatetime());
        fromIdView.setText(item.getFromId());
        fromTextView.setText(item.getFromText());
        toIdView.setText(item.getToId());
        toTextView.setText(item.getToText());

        deleteButton.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_delete_black_24px));

        //onClick for image button inside list view
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //delete
                historyDAO.delete(item.getId());

                //refresh
                context.refreshData();

                Toast.makeText(context, R.string.result_deleted, Toast.LENGTH_LONG).show();

            }
        });

        return itemView;
    }

    public void set(int index, Item item) {
        if (index >= 0 && index < items.size()) {
            items.set(index, item);
            notifyDataSetChanged();
        }
    }

    public Item get(int index) {
        return items.get(index);
    }
}
