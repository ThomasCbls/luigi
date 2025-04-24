package com.example.appli20240809;

import android.content.Intent;
import android.database.MatrixCursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AfficherListeDvdsActivity extends MainActivity {

    private SimpleCursorAdapter adapter;  // Adapter pour afficher les données dans une ListView
    private MatrixCursor dvdCursor;       // Cursor temporaire en mémoire pour stocker les données des films

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_afficherlistedvds);  // Lien avec le fichier XML de layout

        // Colonnes du cursor (doivent correspondre aux noms utilisés plus tard)
        String[] columns = new String[]{"_id", "title", "releaseYear", "languageId", "length"};
        dvdCursor = new MatrixCursor(columns);  // Initialisation d’un cursor vide

        // Association entre les colonnes du cursor et les vues dans le layout d'une ligne (line_list.xml)
        String[] from = new String[]{"title", "releaseYear", "languageId", "length"};
        int[] to = new int[]{R.id.texteTitleId, R.id.texteReleaseYearId, R.id.texteLanguageId, R.id.texteDureeId};
        adapter = new SimpleCursorAdapter(this, R.layout.line_list, dvdCursor, from, to, 0);

        // Récupération de la ListView depuis le layout
        ListView listviewDvds = findViewById(R.id.listeDvd);
        listviewDvds.setAdapter(adapter);            // Association de l'adapter à la ListView
        listviewDvds.setTextFilterEnabled(true);     // Activation du filtre (recherche)

        // Gestion du clic sur un élément de la liste
        listviewDvds.setOnItemClickListener((parent, view, position, id) -> {
            dvdCursor.moveToPosition(position);  // Déplacement du cursor à la position de l’élément cliqué

            int filmId = dvdCursor.getInt(dvdCursor.getColumnIndexOrThrow("_id"));  // Récupération de l’ID

            // Ouverture de l'activité de détails, en passant l'ID du film
            Intent intent = new Intent(AfficherListeDvdsActivity.this, DetailsDvdActivity.class);
            intent.putExtra("filmId", filmId);
            startActivity(intent);
        });

        // Appel à l'API REST pour récupérer la liste des films
        String apiUrl = "http://10.0.2.2:8080/toad/film/all";
        new AppelerServiceRestGETAfficherListeDvdsTask().execute(apiUrl);

        // Gestion du bouton "Voir Panier"
        Button btnVoirPanier = findViewById(R.id.btnPanier);
        btnVoirPanier.setOnClickListener(v -> {
            Intent intent = new Intent(AfficherListeDvdsActivity.this, PanierActivity.class);
            startActivity(intent);
        });
    }

    // Classe interne pour exécuter l’appel à l’API en tâche de fond
    private class AppelerServiceRestGETAfficherListeDvdsTask extends AsyncTask<String, Void, JSONArray> {

        // Code exécuté en arrière-plan (hors du thread UI)
        @Override
        protected JSONArray doInBackground(String... urls) {
            String urlString = urls[0];
            StringBuilder result = new StringBuilder();

            try {
                // Ouverture de la connexion HTTP
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                Log.d("API_CALL", "Connexion à l'API établie avec succès");

                // Lecture de la réponse
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();

                Log.d("API_CALL", "Réponse reçue : " + result);

                // Conversion de la réponse JSON en tableau
                return new JSONArray(result.toString());

            } catch (Exception e) {
                Log.e("API_CALL", "Erreur de connexion ou de lecture : ", e);
                return null;
            }
        }

        // Code exécuté après le téléchargement, sur le thread principal
        @Override
        protected void onPostExecute(JSONArray films) {
            if (films == null) {
                Log.e("API_CALL", "Erreur : la liste des films récupérée est nulle");
                return;
            }

            try {
                // Fermeture de l'ancien cursor s’il est encore ouvert
                if (dvdCursor != null && !dvdCursor.isClosed()) {
                    dvdCursor.close();
                }

                // Création d’un nouveau cursor
                dvdCursor = new MatrixCursor(new String[]{"_id", "title", "releaseYear", "languageId", "length"});

                // Remplissage du cursor avec les données JSON
                for (int i = 0; i < films.length(); i++) {
                    JSONObject film = films.getJSONObject(i);

                    int filmId = film.getInt("filmId");
                    String title = film.getString("title");
                    String releaseYear = film.getString("releaseYear");
                    String languageId = film.getString("languageId");
                    String rating = film.getString("rating");  // Champ non utilisé ici
                    int length = film.getInt("length");

                    dvdCursor.addRow(new Object[]{filmId, title, releaseYear, languageId, length});
                }

                // Mise à jour de l’adapter avec le nouveau cursor
                adapter.changeCursor(dvdCursor);
                Log.d("API_CALL", "Liste mise à jour avec succès");

            } catch (JSONException e) {
                Log.e("API_CALL", "Erreur de parsing du JSON : ", e);
            }
        }
    }
}
