package com.example.appli20240809;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class PanierAdapter extends BaseAdapter {
    private final Context context;
    private final ArrayList<String> list;
    private final SharedPreferences prefs;
    private final TextView panierItems;

    public PanierAdapter(Context context, ArrayList<String> list, TextView panierItems) {
        this.context = context;
        this.list = list;
        this.prefs = context.getSharedPreferences("PanierPrefs", Context.MODE_PRIVATE);
        this.panierItems = panierItems;
        updateEmptyMessage();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_panier, parent, false);
        }

        TextView itemText = convertView.findViewById(R.id.itemText);
        Button btnRemove = convertView.findViewById(R.id.btnRemove);

        String article = list.get(position);
        itemText.setText(article);

        btnRemove.setOnClickListener(v -> removeItem(position));

        return convertView;
    }

    private void removeItem(int position) {
        if (position >= 0 && position < list.size()) {
            list.remove(position);
            savePanier();
            notifyDataSetChanged();
            updateEmptyMessage();
        }
    }

    private void savePanier() {
        SharedPreferences.Editor editor = prefs.edit();
        Set<String> panierSet = new HashSet<>(list);
        editor.putStringSet("panier", panierSet);
        editor.apply();
    }

    private void updateEmptyMessage() {
        if (list.isEmpty()) {
            panierItems.setVisibility(View.VISIBLE);
        } else {
            panierItems.setVisibility(View.GONE);
        }
    }
}
