package com.example.appli20240809;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

public class DvdAdapter extends RecyclerView.Adapter<DvdAdapter.ViewHolder> {

    private Context context;
    private ArrayList<JSONObject> dvdList;

    public DvdAdapter(Context context, JSONArray jsonArray) {
        this.context = context;
        this.dvdList = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                dvdList.add(jsonArray.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.line_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            JSONObject film = dvdList.get(position);
            int filmId = film.getInt("filmId");
            String title = film.getString("title");
            String releaseYear = film.getString("releaseYear");
            String languageId = film.getString("languageId");
            int length = film.getInt("length");

            holder.texteTitle.setText(title);
            holder.texteReleaseYear.setText(releaseYear);
            holder.filmLanguage.setText(languageId);
            holder.filmLength.setText(length + " min");

            // Ajouter un effet de clic
            holder.cardView.setOnClickListener(v -> {
                Intent intent = new Intent(context, DetailsDvdActivity.class);
                intent.putExtra("filmId", filmId);
                context.startActivity(intent);
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return dvdList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView texteTitle, texteReleaseYear, filmLanguage, filmLength;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            texteTitle = itemView.findViewById(R.id.texteTitleId);
            texteReleaseYear = itemView.findViewById(R.id.texteReleaseYearId);
            filmLanguage = itemView.findViewById(R.id.texteLanguageId);
            filmLength = itemView.findViewById(R.id.texteDureeId);
            cardView = (CardView) itemView;
        }
    }
}
