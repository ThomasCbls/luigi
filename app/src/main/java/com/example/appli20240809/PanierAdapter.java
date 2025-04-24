package com.example.appli20240809;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

// Adaptateur personnalisé pour afficher et gérer une liste d'articles dans un panier
public class PanierAdapter extends BaseAdapter {

    private final Context context; // Contexte de l'application
    private final ArrayList<String> list; // Liste des articles dans le panier
    private final SharedPreferences prefs; // Pour stocker et récupérer le panier
    private final TextView panierItems; // Message affiché si le panier est vide

    // Constructeur
    public PanierAdapter(Context context, ArrayList<String> list, TextView panierItems) {
        this.context = context;
        this.list = list;
        this.panierItems = panierItems;
        this.prefs = context.getSharedPreferences("PanierPrefs", Context.MODE_PRIVATE); // Initialisation des préférences
        updateEmptyMessage(); // Affiche ou masque le message vide
    }

    // Retourne le nombre d'éléments dans le panier
    @Override
    public int getCount() {
        return list.size();
    }

    // Retourne l'élément à la position donnée
    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    // Retourne l'identifiant de l'élément (ici, on utilise simplement la position)
    @Override
    public long getItemId(int position) {
        return position;
    }

    // Retourne la vue pour un élément donné du panier
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Réutilisation de la vue si possible (pour optimiser les performances)
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_panier, parent, false);
        }

        // Récupération des éléments visuels
        TextView itemText = convertView.findViewById(R.id.itemText); // Titre de l’article
        Button btnRemove = convertView.findViewById(R.id.btnRemove); // Bouton de suppression

        // Récupère l'article correspondant à la position actuelle
        String article = list.get(position);
        itemText.setText(article); // Affiche le titre

        // Gestion du clic sur le bouton de suppression
        btnRemove.setOnClickListener(v -> removeItem(position));

        return convertView;
    }

    // Méthode pour retirer un article du panier
    private void removeItem(int position) {
        if (position >= 0 && position < list.size()) {
            list.remove(position); // Supprime l'article de la liste
            savePanier();          // Met à jour les données enregistrées
            notifyDataSetChanged(); // Notifie l'adaptateur que la liste a changé
            updateEmptyMessage();   // Affiche ou cache le message si le panier devient vide
        }
    }

    // Sauvegarde le panier dans SharedPreferences
    private void savePanier() {
        SharedPreferences.Editor editor = prefs.edit();
        Set<String> panierSet = new HashSet<>(list); // Conversion de la liste en Set
        editor.putStringSet("panier", panierSet);    // Sauvegarde des données
        editor.apply();                              // Applique les modifications
    }

    // Met à jour le message "panier vide" en fonction du contenu du panier
    private void updateEmptyMessage() {
        if (list.isEmpty()) {
            panierItems.setVisibility(View.VISIBLE); // Affiche le message
        } else {
            panierItems.setVisibility(View.GONE);    // Cache le message
        }
    }
}
