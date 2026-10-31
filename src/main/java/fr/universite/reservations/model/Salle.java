package fr.universite.reservations.model;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class Salle {
    private final String identifiant;
    private final String nom;
    private final int capacite;
    private final List<String> equipements;

    public Salle(String identifiant, String nom, int capacite, List<String> equipements) {
        this.identifiant = required(identifiant, "L'identifiant de la salle est obligatoire.");
        this.nom = required(nom, "Le nom de la salle est obligatoire.");
        if (capacite < 1) {
            throw new IllegalArgumentException("La capacité doit être supérieure à zéro.");
        }
        this.capacite = capacite;
        this.equipements = equipements == null ? List.of() : equipements.stream()
                .filter(Objects::nonNull).map(String::trim).filter(s -> !s.isEmpty()).distinct().toList();
    }

    private static String required(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    public String getIdentifiant() { return identifiant; }
    public String getNom() { return nom; }
    public int getCapacite() { return capacite; }
    public List<String> getEquipements() { return equipements; }

    @Override
    public String toString() { return nom + " (" + identifiant + ")"; }

    public boolean possedeEquipement(String equipement) {
        return equipement != null && equipements.stream()
                .anyMatch(item -> item.toLowerCase(Locale.ROOT).contains(equipement.trim().toLowerCase(Locale.ROOT)));
    }
}
