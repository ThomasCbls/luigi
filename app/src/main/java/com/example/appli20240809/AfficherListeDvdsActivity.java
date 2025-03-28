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
import android.widget.AdapterView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AfficherListeDvdsActivity extends MainActivity {

    private SimpleCursorAdapter adapter;
    private MatrixCursor dvdCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_afficherlistedvds);

        String[] columns = new String[]{"_id", "title", "releaseYear", "languageId", "length"};
        dvdCursor = new MatrixCursor(columns);

        String[] from = new String[]{"title", "releaseYear", "languageId", "length"};
        int[] to = new int[]{R.id.texteTitleId, R.id.texteReleaseYearId, R.id.texteLanguageId, R.id.texteDureeId};
        adapter = new SimpleCursorAdapter(this, R.layout.line_list, dvdCursor, from, to, 0);

        ListView listviewDvds = findViewById(R.id.listeDvd);
        listviewDvds.setAdapter(adapter);
        listviewDvds.setTextFilterEnabled(true);

            listviewDvds.setOnItemClickListener((parent, view, position, id) -> {
                dvdCursor.moveToPosition(position);

                // Récupérer l'ID du film
                int filmId = dvdCursor.getInt(dvdCursor.getColumnIndexOrThrow("_id"));

                Intent intent = new Intent(AfficherListeDvdsActivity.this, DetailsDvdActivity.class);
                intent.putExtra("filmId", filmId);
                startActivity(intent);
            });

        String apiUrl = "http://10.0.2.2:8080/toad/film/all";
        new AppelerServiceRestGETAfficherListeDvdsTask().execute(apiUrl);

        // Récupération du bouton Voir Panier
        Button btnVoirPanier = findViewById(R.id.btnPanier);

        btnVoirPanier.setOnClickListener(v -> {
            Intent intent = new Intent(AfficherListeDvdsActivity.this, PanierActivity.class);
            startActivity(intent);
        });
    }

    private class AppelerServiceRestGETAfficherListeDvdsTask extends AsyncTask<String, Void, JSONArray> {

        @Override
        protected JSONArray doInBackground(String... urls) {
            String urlString = urls[0];
            StringBuilder result = new StringBuilder();

            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                Log.d("API_CALL", "Connexion à l'API établie avec succès");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();

                Log.d("API_CALL", "Réponse reçue : " + result);

                return new JSONArray(result.toString());

            } catch (Exception e) {
                Log.e("API_CALL", "Erreur de connexion ou de lecture : ", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONArray films) {
            if (films == null) {
                Log.e("API_CALL", "Erreur : la liste des films récupérée est nulle");
                return;
            }

            try {
                // Réinitialiser le cursor en fermant l'ancien
                if (dvdCursor != null && !dvdCursor.isClosed()) {
                    dvdCursor.close();
                }
                dvdCursor = new MatrixCursor(new String[]{"_id", "title", "releaseYear", "languageId", "length"});

                // Ajouter chaque film au cursor
                for (int i = 0; i < films.length(); i++) {
                    JSONObject film = films.getJSONObject(i);
                    int filmId = film.getInt("filmId");
                    String title = film.getString("title");
                    String releaseYear = film.getString("releaseYear");
                    String languageId = film.getString("languageId");
                    String rating = film.getString("rating");
                    int length = film.getInt("length");

                    dvdCursor.addRow(new Object[]{filmId, title, releaseYear, languageId, length});
                }

                // Mettre à jour l'adaptateur
                adapter.changeCursor(dvdCursor);
                Log.d("API_CALL", "Liste mise à jour avec succès");

            } catch (JSONException e) {
                Log.e("API_CALL", "Erreur de parsing du JSON : ", e);
            }
        }
    }
}
