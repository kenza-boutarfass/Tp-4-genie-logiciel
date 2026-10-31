package fr.universite.reservations.model;

import java.util.regex.Pattern;

public final class Enseignant {
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private final String identifiant;
    private final String nom;
    private final String email;

    public Enseignant(String identifiant, String nom, String email) {
        this.identifiant = required(identifiant, "L'identifiant de l'enseignant est obligatoire.");
        this.nom = required(nom, "Le nom de l'enseignant est obligatoire.");
        if (email == null || !EMAIL.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("Veuillez saisir une adresse e-mail valide.");
        }
        this.email = email.trim();
    }

    private static String required(String value, String message) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(message);
        return value.trim();
    }

    public String getIdentifiant() { return identifiant; }
    public String getNom() { return nom; }
    public String getEmail() { return email; }

    @Override
    public String toString() { return nom + " (" + identifiant + ")"; }
}
