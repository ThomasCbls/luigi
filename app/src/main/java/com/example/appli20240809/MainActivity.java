package com.example.appli20240809;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.database.MatrixCursor;
import androidx.appcompat.app.AppCompatActivity;
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*setContentView(R.layout.activity_main);
        // Définition des colonnes pour le MatrixCursor
        String[] columns = new String[]{"_id", "titre", "année", "langue", "durée"};
        MatrixCursor dvdsCursor = new MatrixCursor(columns);
        // Ajout de quelques lignes de données de DVD fictives
        dvdsCursor.addRow(new Object[]{1, "Inception", "2010", "Anglais", "148"});
        dvdsCursor.addRow(new Object[]{2, "Interstellar", "2014", "Anglais", "169"});
        dvdsCursor.addRow(new Object[]{3, "The Matrix", "1999", "Anglais", "136"});
        dvdsCursor.addRow(new Object[]{4, "Avatar", "2009", "Anglais", "162"});
        dvdsCursor.addRow(new Object[]{5, "Titanic", "1997", "Anglais", "195"});
        // Définition des données à lier aux vues
        String[] from = new String[]{"titre", "année", "langue", "durée"};
        int[] to = new int[]{R.id.texteTitleId, R.id.texteReleaseYearId, R.id.texteLanguageId, R.id.texteDureeId};
        // Création de l'adaptateur SimpleCursorAdapter
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.line_list, dvdsCursor, from, to, 0);
        // Remplissage de la ListView
        ListView listviewDvds = findViewById(R.id.listeDvd);
        listviewDvds.setAdapter(adapter);
        listviewDvds.setTextFilterEnabled(true);
        // Passer des données à l'activité suivante (AfficherListeDvdsActivity)
        Intent intent = new Intent(MainActivity.this, AfficherListeDvdsActivity.class);
        startActivity(intent);*/
    }
}