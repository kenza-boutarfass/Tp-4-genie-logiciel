package fr.universite.reservations.service;

import fr.universite.reservations.model.Enseignant;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class GestionEnseignants {
    private final Map<String, Enseignant> enseignants = new LinkedHashMap<>();

    public void ajouterEnseignant(Enseignant enseignant) {
        if (enseignant == null) throw new IllegalArgumentException("L'enseignant est obligatoire.");
        if (enseignants.containsKey(enseignant.getIdentifiant())) {
            throw new IllegalArgumentException("Cet identifiant d'enseignant existe déjà.");
        }
        enseignants.put(enseignant.getIdentifiant(), enseignant);
    }

    public List<Enseignant> afficherTousLesEnseignants() {
        return enseignants.values().stream().sorted(Comparator.comparing(Enseignant::getNom)).toList();
    }

    public Enseignant obtenirEnseignant(String identifiant) {
        Enseignant enseignant = enseignants.get(identifiant);
        if (enseignant == null) throw new IllegalArgumentException("Enseignant introuvable.");
        return enseignant;
    }

    public boolean existe(String identifiant) { return enseignants.containsKey(identifiant); }
}
