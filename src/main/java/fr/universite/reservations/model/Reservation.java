package fr.universite.reservations.model;

import java.time.LocalDateTime;
import java.util.Objects;

public final class Reservation {
    private final String identifiant;
    private final Salle salle;
    private final Enseignant enseignant;
    private final LocalDateTime debut;
    private final LocalDateTime fin;
    private StatutReservation statut;

    public Reservation(String identifiant, Salle salle, Enseignant enseignant,
                       LocalDateTime debut, LocalDateTime fin) {
        if (identifiant == null || identifiant.isBlank()) {
            throw new IllegalArgumentException("L'identifiant de la réservation est obligatoire.");
        }
        this.identifiant = identifiant.trim();
        this.salle = Objects.requireNonNull(salle, "La salle est obligatoire.");
        this.enseignant = Objects.requireNonNull(enseignant, "L'enseignant est obligatoire.");
        this.debut = Objects.requireNonNull(debut, "La date et l'heure de début sont obligatoires.");
        this.fin = Objects.requireNonNull(fin, "La date et l'heure de fin sont obligatoires.");
        if (!fin.isAfter(debut)) throw new IllegalArgumentException("La fin doit être postérieure au début.");
        this.statut = StatutReservation.CONFIRMEE;
    }

    public String getIdentifiant() { return identifiant; }
    public Salle getSalle() { return salle; }
    public Enseignant getEnseignant() { return enseignant; }
    public LocalDateTime getDebut() { return debut; }
    public LocalDateTime getFin() { return fin; }
    public StatutReservation getStatut() { return statut; }
    public void annuler() { statut = StatutReservation.ANNULEE; }

    public boolean chevauche(LocalDateTime autreDebut, LocalDateTime autreFin) {
        return debut.isBefore(autreFin) && autreDebut.isBefore(fin);
    }
}
