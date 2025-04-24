package com.example.appli20240809;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

// Imports pour le Spinner (liste déroulante)
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;

public class LoginActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    // Déclaration des vues
    private EditText usernameField, passwordField, edittextURL;
    private Button loginButton;
    private Spinner spinnerURLs;
    private String[] listeURLs;
    private String urlSelectionnee = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialisation du Spinner avec les URLs prédéfinies (depuis res/values/strings.xml)
        listeURLs = getResources().getStringArray(R.array.listeURLs);
        spinnerURLs = findViewById(R.id.spinnerURLs);
        ArrayAdapter<CharSequence> adapterListeURLs = ArrayAdapter.createFromResource(
                this, R.array.listeURLs, android.R.layout.simple_spinner_item
        );
        adapterListeURLs.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerURLs.setAdapter(adapterListeURLs);
        spinnerURLs.setOnItemSelectedListener(this); // L’activité écoute les sélections

        // Initialisation des champs de saisie
        usernameField = findViewById(R.id.editTextUsername);
        passwordField = findViewById(R.id.editTextPassword);
        edittextURL = findViewById(R.id.URLText);
        loginButton = findViewById(R.id.buttonLogin);

        // Action du bouton de connexion
        loginButton.setOnClickListener(v -> {
            String email = usernameField.getText().toString();
            String password = passwordField.getText().toString();
            String urlManuelle = edittextURL.getText().toString().trim();

            // Choix final de l’URL : celle du champ manuel si remplie, sinon celle du Spinner
            String urlFinale = urlManuelle.isEmpty() ? urlSelectionnee : urlManuelle;
            DonneesPartagees.setURLConnexion(urlFinale);
            Toast.makeText(this, "Connexion à : " + urlFinale, Toast.LENGTH_SHORT).show();

            // Vérifie que les champs sont remplis
            if (!email.isEmpty() && !password.isEmpty()) {
                login(email, password); // Lance la connexion
            } else {
                Toast.makeText(this, "Veuillez remplir tous les champs.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Méthode de connexion (appelée dans un thread séparé)
    private void login(String email, String password) {
        new Thread(() -> {
            try {
                // Construction de l’URL de l’API pour récupérer les infos du client via l’email
                String apiUrl = DonneesPartagees.getURLConnexion() + "/toad/customer/getByEmail?email=" + email;
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Lecture de la réponse JSON
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // Conversion de la réponse en JSONObject
                    JSONObject userJson = new JSONObject(response.toString());
                    String passwordFromApi = userJson.getString("password");

                    // Vérifie si le mot de passe entré correspond
                    if (password.equals(passwordFromApi)) {
                        int customerId = userJson.getInt("customerId");

                        // Stocke le customerId dans les SharedPreferences pour réutilisation
                        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putInt("customer_id", customerId);
                        editor.apply();

                        // Redirige vers l’activité principale
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Connexion réussie !", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, AfficherListeDvdsActivity.class));
                            finish();
                        });
                    } else {
                        // Mot de passe incorrect
                        runOnUiThread(() -> Toast.makeText(this, "Mot de passe incorrect.", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    // Utilisateur introuvable (HTTP 404 ou autre erreur)
                    runOnUiThread(() -> Toast.makeText(this, "Utilisateur non trouvé.", Toast.LENGTH_SHORT).show());
                }

            } catch (Exception e) {
                // Erreur générale lors de l’appel API
                Log.e("LOGIN", "Erreur : ", e);
                runOnUiThread(() -> Toast.makeText(this, "Erreur de connexion au serveur.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    // Callback quand une URL est sélectionnée dans le Spinner
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        urlSelectionnee = listeURLs[position];
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        urlSelectionnee = ""; // Par défaut si rien n’est sélectionné
    }
}
