package com.example.appli20240809;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.nio.charset.StandardCharsets;

public class PanierActivity extends AppCompatActivity {

    // Liste contenant les titres des films dans le panier
    private final ArrayList<String> list = new ArrayList<>();

    private PanierAdapter adapter;  // Adaptateur personnalisé pour la liste
    private ListView panierListView;
    private TextView panierMessage;  // Message si panier vide

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panier);

        panierListView = findViewById(R.id.ListPanier);
        panierMessage = findViewById(R.id.panierItems);
        Button btnRetour = findViewById(R.id.btnRetour);
        Button btnValider = findViewById(R.id.btnValider);

        // Adaptateur personnalisé pour afficher le contenu du panier
        adapter = new PanierAdapter(this, list, panierMessage);
        panierListView.setAdapter(adapter);

        // Charger les films précédemment enregistrés dans le panier
        loadPanier();

        // Vérifie si un film a été passé via un Intent (depuis DetailsDvdActivity)
        String newArticle = getIntent().getStringExtra("titre");
        if (newArticle != null && !newArticle.isEmpty()) {
            add(newArticle);
        }

        // Bouton retour à la liste des films
        btnRetour.setOnClickListener(v -> {
            Intent intent = new Intent(PanierActivity.this, AfficherListeDvdsActivity.class);
            startActivity(intent);
        });

        // Bouton pour valider le panier (envoi à l’API)
        btnValider.setOnClickListener(v -> new ValiderPanierTask().execute());

        checkPanierVide(); // Affiche ou masque le message selon le contenu
    }

    // Ajoute un film au panier (évite les doublons)
    public void add(String article) {
        if (!list.contains(article)) {
            list.add(article);
            savePanier(); // Sauvegarde le panier
            adapter.notifyDataSetChanged();
        }
        checkPanierVide();
    }

    // Affiche un message si le panier est vide
    private void checkPanierVide() {
        if (list.isEmpty()) {
            panierMessage.setVisibility(View.VISIBLE);
        } else {
            panierMessage.setVisibility(View.GONE);
        }
    }

    // Sauvegarde le panier dans SharedPreferences (persistant)
    private void savePanier() {
        SharedPreferences prefs = getSharedPreferences("PanierPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Set<String> panierSet = new HashSet<>(list);
        editor.putStringSet("panier", panierSet);
        editor.apply();
    }

    // Charge le panier à l’ouverture de l’activité
    private void loadPanier() {
        SharedPreferences prefs = getSharedPreferences("PanierPrefs", MODE_PRIVATE);
        Set<String> panierSet = prefs.getStringSet("panier", new HashSet<>());

        list.clear();
        list.addAll(panierSet);
        adapter.notifyDataSetChanged();
        checkPanierVide();
    }

    // Tâche asynchrone qui envoie chaque film à l'API pour validation
    private class ValiderPanierTask extends AsyncTask<Void, Void, Boolean> {

        private static final String API_URL = "http://10.0.2.2:8080/toad/rental/add";
        private String errorMessage = "";

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                // Dates actuelles
                String rentalDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                String lastUpdate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                String returnDate = "";

                for (String film : list) {
                    int inventoryId = getInventoryIdForFilm(film); // ID du film dans le stock
                    if (inventoryId == -1) {
                        errorMessage = "Erreur : inventory_id non trouvé pour " + film;
                        return false;
                    }

                    int customerId = getCustomerId();  // ID de l'utilisateur
                    if (customerId == -1) {
                        errorMessage = "Erreur : customer_id non trouvé";
                        return false;
                    }

                    int staffId = 1;  // Staff ID par défaut

                    // Encodage des données à envoyer
                    String encodedRentalDate = URLEncoder.encode(rentalDate, "UTF-8");
                    String encodedLastUpdate = URLEncoder.encode(lastUpdate, "UTF-8");
                    String encodedReturnDate = URLEncoder.encode(returnDate, "UTF-8");

                    // Construction des paramètres POST
                    String params = "rental_date=" + encodedRentalDate +
                            "&inventory_id=" + inventoryId +
                            "&customer_id=" + customerId +
                            "&return_date=" + encodedReturnDate +
                            "&staff_id=" + staffId +
                            "&last_update=" + encodedLastUpdate;

                    // Envoi de la requête POST à l'API
                    URL url = new URL(API_URL);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                    OutputStream os = connection.getOutputStream();
                    os.write(params.getBytes(StandardCharsets.UTF_8));
                    os.flush();
                    os.close();

                    int responseCode = connection.getResponseCode();

                    // Vérifie si la réponse est OK
                    if (responseCode != 200 && responseCode != 201) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();
                        errorMessage = "Erreur API: " + response.toString();
                        return false;
                    }
                }
                return true;

            } catch (Exception e) {
                errorMessage = e.getMessage();
                Log.e("API_ERROR", "Erreur : " + errorMessage);
                return false;
            }
        }

        // Récupération du customer_id depuis les préférences
        private int getCustomerId() {
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            return prefs.getInt("customer_id", -1);
        }

        // Recherche de l’inventory_id correspondant à un titre de film
        private int getInventoryIdForFilm(String filmTitle) {
            try {
                String inventoryApiUrl = DonneesPartagees.getURLConnexion() + "/toad/inventory/getStockByStore";
                URL inventoryUrl = new URL(inventoryApiUrl);
                HttpURLConnection inventoryConnection = (HttpURLConnection) inventoryUrl.openConnection();
                inventoryConnection.setRequestMethod("GET");

                BufferedReader inventoryReader = new BufferedReader(new InputStreamReader(inventoryConnection.getInputStream()));
                StringBuilder inventoryResponse = new StringBuilder();
                String line;
                while ((line = inventoryReader.readLine()) != null) {
                    inventoryResponse.append(line);
                }
                inventoryReader.close();

                JSONArray inventoryArray = new JSONArray(inventoryResponse.toString());

                for (int i = 0; i < inventoryArray.length(); i++) {
                    JSONObject inventoryJson = inventoryArray.getJSONObject(i);
                    String title = inventoryJson.getString("title");

                    if (title.equalsIgnoreCase(filmTitle)) {
                        return inventoryJson.getInt("filmId"); // Assure-toi que c’est bien le bon champ (filmId ou inventoryId ?)
                    }
                }

                Log.e("API_ERROR", "Inventory ID non trouvé pour " + filmTitle);
                return -1;

            } catch (Exception e) {
                Log.e("API_ERROR", "Erreur : " + e.getMessage());
                return -1;
            }
        }

        // Appelé une fois la tâche terminée
        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(PanierActivity.this, "Panier validé avec succès !", Toast.LENGTH_LONG).show();
                clearPanier();
            } else {
                Toast.makeText(PanierActivity.this, "Erreur validation: " + errorMessage, Toast.LENGTH_LONG).show();
                Log.e("API_ERROR", errorMessage);
            }
        }
    }

    // Vide le panier après validation
    private void clearPanier() {
        list.clear();
        savePanier();
        adapter.notifyDataSetChanged();
        checkPanierVide();
    }
}
