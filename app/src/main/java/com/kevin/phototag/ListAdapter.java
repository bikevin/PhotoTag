package com.kevin.phototag;

import android.content.ClipData;
import android.content.ClipData.Item;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class ListAdapter extends ArrayAdapter<String> {

    ArrayList<String> items;
    public ListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public ListAdapter(Context context, int resource, ArrayList<String> items) {
        super(context, resource, items);
        this.items = items;
    }

    @Override
    public String getItem(int position)
    {
        return items.get(position);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.word_list, null);
        }

        String p = getItem(position);

        if (p != null) {
            TextView tt1 = (TextView) v.findViewById(R.id.word);

            if (tt1 != null) {
                tt1.setText(p);
            }

        }

        return v;
    }

}
