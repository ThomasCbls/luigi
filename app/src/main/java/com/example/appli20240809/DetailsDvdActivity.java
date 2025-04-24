package com.example.appli20240809;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.content.Intent;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DetailsDvdActivity extends AppCompatActivity {

    // Déclaration des éléments de l'interface
    private TextView titleTextView, releaseYearTextView, languageIdTextView, lengthTextView, detailsRatingTextView;
    private Button addToCartButton, btnReturn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_dvd);

        // Liaison des composants graphiques avec leur ID
        titleTextView = findViewById(R.id.detailsTitle);
        releaseYearTextView = findViewById(R.id.detailsReleaseYear);
        languageIdTextView = findViewById(R.id.detailsLanguageId);
        lengthTextView = findViewById(R.id.detailsLength);
        detailsRatingTextView = findViewById(R.id.detailsRating);
        addToCartButton = findViewById(R.id.addToCartButton);
        btnReturn = findViewById(R.id.btnReturn);

        // Récupère l'ID du film envoyé depuis l'activité précédente
        int filmId = getIntent().getIntExtra("filmId", -1);
        if (filmId != -1) {
            // Lance une tâche asynchrone pour récupérer les détails du film
            new FetchFilmDetailsTask().execute(String.valueOf(filmId));
        }

        // Action du bouton retour pour revenir à la liste des DVDs
        btnReturn.setOnClickListener(v -> {
            Intent intent = new Intent(DetailsDvdActivity.this, AfficherListeDvdsActivity.class);
            startActivity(intent);
        });
    }

    // Tâche asynchrone pour récupérer les détails du film via une API REST
    private class FetchFilmDetailsTask extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... params) {
            int filmId = params.length > 0 ? Integer.parseInt(params[0]) : -1;
            if (filmId == -1) {
                Log.e("API_CALL", "ID du film invalide !");
                return null;
            }

            String urlString = DonneesPartagees.getURLConnexion() + "/toad/film/getById?id=" + filmId;
            StringBuilder result = new StringBuilder();

            try {
                // Connexion HTTP GET
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Lecture de la réponse
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    reader.close();
                    return new JSONObject(result.toString());
                } else {
                    Log.e("API_CALL", "Erreur API, code : " + responseCode);
                    return null;
                }
            } catch (Exception e) {
                Log.e("API_CALL", "Erreur lors de la récupération des détails du film : ", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject film) {
            if (film == null) {
                Log.e("API_CALL", "Erreur : les détails du film sont null");
                return;
            }

            try {
                // Affichage des données dans les vues
                titleTextView.setText(film.getString("title"));
                releaseYearTextView.setText("Année : " + film.getString("releaseYear"));
                languageIdTextView.setText("Langue : " + film.getString("languageId"));
                lengthTextView.setText("Durée : " + film.getInt("length") + " minutes");
                detailsRatingTextView.setText("Note : " + film.getString("rating"));

                // Données nécessaires pour ajouter au panier
                final String title = film.getString("title");
                final String filmId = film.getString("filmId");

                // Action du bouton "Ajouter au panier"
                addToCartButton.setOnClickListener(view -> {
                    new CheckFilmAvailabilityTask(filmId, title).execute();
                });

            } catch (Exception e) {
                Log.e("API_CALL", "Erreur lors du parsing des détails : ", e);
            }
        }
    }

    // Tâche pour vérifier si le film est disponible à la location (via l'inventaire)
    private class CheckFilmAvailabilityTask extends AsyncTask<Void, Void, Integer> {
        private String filmId;
        private String title;

        public CheckFilmAvailabilityTask(String filmId, String title) {
            this.filmId = filmId;
            this.title = title;
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            String urlString = DonneesPartagees.getURLConnexion() + "/toad/inventory/available/getById?id=" + filmId;
            try {
                // Connexion API pour vérifier la disponibilité du film
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    reader.close();

                    // La réponse est soit "null" (film indisponible) soit un ID d'inventaire
                    String jsonString = result.toString();
                    if (jsonString.equals("null")) {
                        return null;
                    } else {
                        return Integer.parseInt(jsonString);
                    }
                } else {
                    Log.e("API_CALL", "Erreur API, code : " + responseCode);
                    return null;
                }
            } catch (Exception e) {
                Log.e("API_ERROR", "Erreur lors de l'appel API : ", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Integer inventoryId) {
            if (inventoryId != null && inventoryId > 0) {
                // Si le film est dispo, on l'ajoute au panier et on passe à l'activité Panier
                Intent intent = new Intent(DetailsDvdActivity.this, PanierActivity.class);
                intent.putExtra("titre", title);
                intent.putExtra("filmId", filmId);
                intent.putExtra("inventoryId", inventoryId);
                startActivity(intent);
            } else {
                // Si le film est indisponible, on affiche un message à l'utilisateur
                Toast.makeText(DetailsDvdActivity.this, "Ce film n'est pas disponible en ce moment.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
