package fr.universite.reservations.service;

import fr.universite.reservations.model.Salle;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

public final class GestionSalles {
    private final Map<String, Salle> salles = new LinkedHashMap<>();
    private Predicate<String> suppressionAutorisee = identifiant -> true;

    public void protegerSuppressions(Predicate<String> suppressionAutorisee) {
        if (suppressionAutorisee == null) throw new IllegalArgumentException("La règle de suppression est obligatoire.");
        this.suppressionAutorisee = suppressionAutorisee;
    }

    public void ajouterSalle(Salle salle) {
        if (salle == null) throw new IllegalArgumentException("La salle est obligatoire.");
        if (salles.containsKey(salle.getIdentifiant())) {
            throw new IllegalArgumentException("Cet identifiant de salle existe déjà.");
        }
        salles.put(salle.getIdentifiant(), salle);
    }

    public void modifierSalle(String identifiant, Salle nouvelleSalle) {
        if (identifiant == null || !salles.containsKey(identifiant)) {
            throw new IllegalArgumentException("La salle à modifier est introuvable.");
        }
        if (nouvelleSalle == null || !identifiant.equals(nouvelleSalle.getIdentifiant())) {
            throw new IllegalArgumentException("L'identifiant d'une salle ne peut pas être modifié.");
        }
        salles.put(identifiant, nouvelleSalle);
    }

    public Salle supprimerSalle(String identifiant) {
        if (!salles.containsKey(identifiant)) throw new IllegalArgumentException("Salle introuvable.");
        if (!suppressionAutorisee.test(identifiant)) {
            throw new IllegalArgumentException("Cette salle est référencée par l'historique et ne peut pas être supprimée.");
        }
        Salle removed = salles.remove(identifiant);
        return removed;
    }

    public List<Salle> afficherToutesLesSalles() {
        return salles.values().stream().sorted(Comparator.comparing(Salle::getNom)).toList();
    }

    public List<Salle> rechercherParCapacite(int capaciteMinimale) {
        if (capaciteMinimale < 1) throw new IllegalArgumentException("La capacité minimale doit être positive.");
        return afficherToutesLesSalles().stream().filter(s -> s.getCapacite() >= capaciteMinimale).toList();
    }

    public List<Salle> rechercherParEquipement(String equipement) {
        if (equipement == null || equipement.isBlank()) {
            throw new IllegalArgumentException("Veuillez préciser un équipement.");
        }
        String query = equipement.trim().toLowerCase(Locale.ROOT);
        return afficherToutesLesSalles().stream()
                .filter(s -> s.getEquipements().stream().anyMatch(e -> e.toLowerCase(Locale.ROOT).contains(query)))
                .toList();
    }

    public Salle obtenirSalle(String identifiant) {
        Salle salle = salles.get(identifiant);
        if (salle == null) throw new IllegalArgumentException("Salle introuvable.");
        return salle;
    }

    public boolean existe(String identifiant) { return salles.containsKey(identifiant); }
}
