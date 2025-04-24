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

// Adaptateur personnalisé pour afficher une liste de DVDs dans un RecyclerView
public class DvdAdapter extends RecyclerView.Adapter<DvdAdapter.ViewHolder> {

    private Context context; // Contexte utilisé pour l'inflation des layouts et lancement d'activités
    private ArrayList<JSONObject> dvdList; // Liste des films à afficher (sous forme de JSONObjects)

    // Constructeur : convertit le JSONArray en ArrayList<JSONObject> pour manipulation facile
    public DvdAdapter(Context context, JSONArray jsonArray) {
        this.context = context;
        this.dvdList = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                dvdList.add(jsonArray.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace(); // En cas d'erreur de parsing JSON
            }
        }
    }

    // Crée une nouvelle vue pour chaque élément (appelée quand il faut un nouvel item à afficher)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // On gonfle le layout XML d’un item de la liste
        View view = LayoutInflater.from(context).inflate(R.layout.line_list, parent, false);
        return new ViewHolder(view);
    }

    // Remplit les données dans un élément de la liste à une position donnée
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            // Récupère les infos du film actuel
            JSONObject film = dvdList.get(position);
            int filmId = film.getInt("filmId");
            String title = film.getString("title");
            String releaseYear = film.getString("releaseYear");
            String languageId = film.getString("languageId");
            int length = film.getInt("length");

            // Remplit les vues avec les données du film
            holder.texteTitle.setText(title);
            holder.texteReleaseYear.setText(releaseYear);
            holder.filmLanguage.setText(languageId);
            holder.filmLength.setText(length + " min");

            // Gère le clic sur la carte : ouvre l’activité des détails du film
            holder.cardView.setOnClickListener(v -> {
                Intent intent = new Intent(context, DetailsDvdActivity.class);
                intent.putExtra("filmId", filmId); // Envoie l’ID du film à l’activité suivante
                context.startActivity(intent);
            });

        } catch (JSONException e) {
            e.printStackTrace(); // Gestion des erreurs JSON
        }
    }

    // Retourne le nombre total d’éléments dans la liste
    @Override
    public int getItemCount() {
        return dvdList.size();
    }

    // Classe interne qui représente chaque "ligne" ou élément de la liste
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView texteTitle, texteReleaseYear, filmLanguage, filmLength;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Récupère les vues à partir du layout line_list.xml
            texteTitle = itemView.findViewById(R.id.texteTitleId);
            texteReleaseYear = itemView.findViewById(R.id.texteReleaseYearId);
            filmLanguage = itemView.findViewById(R.id.texteLanguageId);
            filmLength = itemView.findViewById(R.id.texteDureeId);
            cardView = (CardView) itemView; // La racine est une CardView ici
        }
    }
}
