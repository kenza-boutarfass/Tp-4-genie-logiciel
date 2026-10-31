package fr.universite.reservations.service;

import fr.universite.reservations.model.Enseignant;
import fr.universite.reservations.model.Reservation;
import fr.universite.reservations.model.Salle;
import fr.universite.reservations.model.StatutReservation;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class GestionReservations {
    private final GestionSalles gestionSalles;
    private final GestionEnseignants gestionEnseignants;
    private final Map<String, Reservation> reservations = new LinkedHashMap<>();

    public GestionReservations(GestionSalles gestionSalles, GestionEnseignants gestionEnseignants) {
        this.gestionSalles = gestionSalles;
        this.gestionEnseignants = gestionEnseignants;
        this.gestionSalles.protegerSuppressions(salleId -> reservations.values().stream()
                .noneMatch(reservation -> reservation.getSalle().getIdentifiant().equals(salleId)));
    }

    public Reservation creerReservation(String salleId, String enseignantId,
                                        LocalDateTime debut, LocalDateTime fin) {
        Salle salle = gestionSalles.obtenirSalle(salleId);
        Enseignant enseignant = gestionEnseignants.obtenirEnseignant(enseignantId);
        if (debut == null || fin == null || !fin.isAfter(debut)) {
            throw new IllegalArgumentException("La date et l'heure de fin doivent être postérieures au début.");
        }
        if (!estDisponible(salleId, debut, fin)) {
            throw new IllegalArgumentException("Cette salle est déjà réservée sur la période choisie.");
        }
        Reservation reservation = new Reservation(UUID.randomUUID().toString(), salle, enseignant, debut, fin);
        reservations.put(reservation.getIdentifiant(), reservation);
        return reservation;
    }

    public List<Reservation> afficherToutesLesReservations() {
        return reservations.values().stream().sorted(Comparator.comparing(Reservation::getDebut)).toList();
    }

    public List<Reservation> afficherReservationsEnseignant(String enseignantId) {
        gestionEnseignants.obtenirEnseignant(enseignantId);
        return afficherToutesLesReservations().stream()
                .filter(r -> r.getEnseignant().getIdentifiant().equals(enseignantId)).toList();
    }

    public List<Reservation> afficherReservationsSalle(String salleId) {
        gestionSalles.obtenirSalle(salleId);
        return afficherToutesLesReservations().stream()
                .filter(r -> r.getSalle().getIdentifiant().equals(salleId)).toList();
    }

    public boolean estDisponible(String salleId, LocalDateTime debut, LocalDateTime fin) {
        Salle salle = gestionSalles.obtenirSalle(salleId);
        if (debut == null || fin == null || !fin.isAfter(debut)) {
            throw new IllegalArgumentException("La fin doit être postérieure au début.");
        }
        return reservations.values().stream().noneMatch(r ->
                r.getSalle().getIdentifiant().equals(salle.getIdentifiant())
                        && r.getStatut() == StatutReservation.CONFIRMEE && r.chevauche(debut, fin));
    }

    public List<Salle> rechercherSallesDisponibles(LocalDateTime debut, LocalDateTime fin) {
        return gestionSalles.afficherToutesLesSalles().stream()
                .filter(salle -> estDisponible(salle.getIdentifiant(), debut, fin)).toList();
    }

    public void annulerReservation(String reservationId) {
        Reservation reservation = obtenirReservation(reservationId);
        reservation.annuler();
    }

    public void annulerReservationEnseignant(String reservationId, String enseignantId) {
        Reservation reservation = obtenirReservation(reservationId);
        if (!reservation.getEnseignant().getIdentifiant().equals(enseignantId)) {
            throw new IllegalArgumentException("Vous ne pouvez gérer que vos propres réservations.");
        }
        reservation.annuler();
    }

    public Reservation obtenirReservation(String reservationId) {
        Reservation reservation = reservations.get(reservationId);
        if (reservation == null) throw new IllegalArgumentException("Réservation introuvable.");
        return reservation;
    }
}
