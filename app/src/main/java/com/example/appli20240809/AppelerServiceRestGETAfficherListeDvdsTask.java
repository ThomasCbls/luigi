/*package com.example.appli20240809;

import android.os.AsyncTask;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
public class AppelerServiceRestGETAfficherListeDvdsTask extends AsyncTask<URL,Integer,String> {
    //URL urlAAppeler = new URL(« <l’url à appelr : « http ::/…»>

    private String appelerServiceRestHttp(URL urlAAppeler ) {
        HttpURLConnection urlConnection = null;
        int responseCode = -1;
        String sResultatAppel = "";
        try {
            //Exemple pour un appel GET
            urlConnection = (HttpURLConnection) urlAAppeler.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setRequestProperty("User-Agent", System.getProperty("http.agent"));
            responseCode = urlConnection.getResponseCode();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            //lecture du résulat de l'appel et alimentation de la chaine de caractères à renvoyer vers l'appelant
            int codeCaractere = -1;
            while ((codeCaractere = in.read()) != -1) {
                sResultatAppel = sResultatAppel + (char) codeCaractere;
            }
            in.close();
        } catch (IOException ioe) {
            Log.d("mydebug", ">>>Pour appelerServiceRestHttp - IOException ioe =" +
                    ioe.toString());
        } catch (Exception e) {
            Log.d("mydebug",">>>Pour appelerServiceRestHttp - Exception="+e.toString());
        } finally {
            urlConnection.disconnect();
        }
        return sResultatAppel;
    }
}*/
