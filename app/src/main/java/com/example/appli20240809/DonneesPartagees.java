package com.example.appli20240809;

// Classe utilitaire pour stocker des données partagées entre les différentes activités
public class DonneesPartagees {

    // Variable statique qui contient l'URL de connexion à l'API
    private static String URLConnexion;

    public void DonneesPartagees() {
        // Rien ici, constructeur vide
    }

    // Méthode statique pour définir l'URL de connexion
    public static void setURLConnexion(String URL) {
        URLConnexion = URL;
    }

    // Méthode statique pour récupérer l'URL de connexion
    public static String getURLConnexion() {
        return URLConnexion;
    }
}
