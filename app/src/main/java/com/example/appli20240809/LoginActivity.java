package com.example.appli20240809;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;
import java.io.IOException;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextPassword;
    private Button buttonLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editTextUsername.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                if (!username.isEmpty() && !password.isEmpty()) {
                    loginUser(username, password);
                } else {
                    Toast.makeText(LoginActivity.this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private int getUserIdFromEmail(String email) {
        String url = "http://localhost:8080/toad/customer/getByEmail?email=" + email;
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder().url(url).get().build();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                JSONObject jsonResponse = new JSONObject(responseBody);
                return jsonResponse.getInt("id"); // Supposons que l'API renvoie { "id": 123 }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("LoginError", "Erreur lors de la récupération de l'ID utilisateur");
        }
        return -1; // Retourne -1 si l'ID n'a pas été trouvé
    }

    private void loginUser(final String email, final String password) {
        int userId = getUserIdFromEmail(email); // Fonction pour récupérer l'ID utilisateur à partir de l'email
        if (userId == -1) {
            Toast.makeText(LoginActivity.this, "Utilisateur non trouvé", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://localhost:8080/toad/customer/getById/" + userId; // L'URL de l'API
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder().url(url).get().build(); // Requête GET

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LoginActivity.this, "Erreur de connexion", Toast.LENGTH_SHORT).show();
                    }
                });
                Log.e("LoginError", e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this, "Utilisateur non trouvé", Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }

                String responseBody = response.body().string();
                try {
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    String storedEmail = jsonResponse.getString("email");
                    String storedPassword = jsonResponse.getString("password");

                    // Vérifier si les identifiants correspondent
                    if (storedEmail.equals(email) && storedPassword.equals(password)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LoginActivity.this, "Connexion réussie", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(LoginActivity.this, AfficherListeDvdsActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LoginActivity.this, "Email ou mot de passe incorrect", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("LoginError", "Erreur lors de l'analyse de la réponse JSON");
                }
            }
        });
    }
}
