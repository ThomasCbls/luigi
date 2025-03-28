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
    private final ArrayList<String> list = new ArrayList<>();
    private PanierAdapter adapter;
    private ListView panierListView;
    private TextView panierMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panier);

        panierListView = findViewById(R.id.ListPanier);
        panierMessage = findViewById(R.id.panierItems);
        Button btnRetour = findViewById(R.id.btnRetour);
        Button btnValider = findViewById(R.id.btnValider);

        // Utiliser PanierAdapter au lieu de ArrayAdapter
        adapter = new PanierAdapter(this, list, panierMessage);
        panierListView.setAdapter(adapter);

        // Charger les articles du panier
        loadPanier();

        // Ajouter un nouvel article si reçu depuis DetailsDvdActivity
        String newArticle = getIntent().getStringExtra("titre");
        if (newArticle != null && !newArticle.isEmpty()) {
            add(newArticle);
        }

        // Bouton Retour
        btnRetour.setOnClickListener(v -> {
            Intent intent = new Intent(PanierActivity.this, AfficherListeDvdsActivity.class);
            startActivity(intent);
        });

        // Bouton Valider le panier
        btnValider.setOnClickListener(v -> new ValiderPanierTask().execute());

        checkPanierVide();
    }

    // Ajouter un film au panier
    public void add(String article) {
        if (!list.contains(article)) {
            list.add(article);
            savePanier();
            adapter.notifyDataSetChanged();
        }
        checkPanierVide();
    }

    // Vérifie si le panier est vide
    private void checkPanierVide() {
        if (list.isEmpty()) {
            panierMessage.setVisibility(View.VISIBLE);
        } else {
            panierMessage.setVisibility(View.GONE);
        }
    }

    // Sauvegarde du panier dans SharedPreferences
    private void savePanier() {
        SharedPreferences prefs = getSharedPreferences("PanierPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Set<String> panierSet = new HashSet<>(list);
        editor.putStringSet("panier", panierSet);
        editor.apply();
    }

    // Chargement du panier depuis SharedPreferences
    private void loadPanier() {
        SharedPreferences prefs = getSharedPreferences("PanierPrefs", MODE_PRIVATE);
        Set<String> panierSet = prefs.getStringSet("panier", new HashSet<>());

        list.clear();
        list.addAll(panierSet);
        adapter.notifyDataSetChanged();
        checkPanierVide();
    }

    // Validation du panier (envoi des films à l'API)
    private class ValiderPanierTask extends AsyncTask<Void, Void, Boolean> {
        private static final String API_URL = "http://10.0.2.2:8080/toad/rental/add";
        private String errorMessage = "";

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                // Vérification et assignation des valeurs par défaut si null
                String rentalDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                String lastUpdate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                String returnDate = "";

                for (String film : list) {
                    int inventoryId = getInventoryIdForFilm(film);
                    if (inventoryId == -1) {
                        errorMessage = "Erreur : inventory_id non trouvé pour " + film;
                        return false;
                    }
                    int customerId = 1;
                    int staffId = 1;

                    // Vérifie que les chaînes ne sont pas nulles avant de les encoder
                    String encodedRentalDate = rentalDate != null ? URLEncoder.encode(rentalDate, "UTF-8") : "";
                    String encodedLastUpdate = lastUpdate != null ? URLEncoder.encode(lastUpdate, "UTF-8") : "";
                    String encodedReturnDate = returnDate != null ? URLEncoder.encode(returnDate, "UTF-8") : "";

                    String params = "rental_date=" + encodedRentalDate +
                            "&inventory_id=" + inventoryId +
                            "&customer_id=" + customerId +
                            "&return_date=" + encodedReturnDate +
                            "&staff_id=" + staffId +
                            "&last_update=" + encodedLastUpdate;

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

        private int getInventoryIdForFilm(String filmTitle) {
            try {
                // Étape 1 : Rechercher dans l'inventaire par titre
                String inventoryApiUrl = "http://10.0.2.2:8080/toad/inventory/getStockByStore";
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

                // Parcours des résultats pour trouver le film correspondant
                for (int i = 0; i < inventoryArray.length(); i++) {
                    JSONObject inventoryJson = inventoryArray.getJSONObject(i);
                    String title = inventoryJson.getString("title");

                    if (title.equalsIgnoreCase(filmTitle)) {
                        int inventoryId = inventoryJson.getInt("filmId"); // Assure-toi que l'ID correct est retourné
                        Log.d("API_DEBUG", "Inventory ID trouvé pour " + filmTitle + " : " + inventoryId);
                        return inventoryId;
                    }
                }

                Log.e("API_ERROR", "Inventory ID non trouvé pour " + filmTitle);
                return -1;
            } catch (Exception e) {
                Log.e("API_ERROR", "Erreur : " + e.getMessage());
                return -1;
            }
        }




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

    // Vider le panier après validation
    private void clearPanier() {
        list.clear();
        savePanier();
        adapter.notifyDataSetChanged();
        checkPanierVide();
    }
}
