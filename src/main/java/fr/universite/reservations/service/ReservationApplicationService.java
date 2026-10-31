package fr.universite.reservations.service;

import fr.universite.reservations.model.Reservation;

import java.time.LocalDateTime;

public final class ReservationApplicationService {
    private final GestionReservations gestionReservations;
    private final EmailService emailService;

    public ReservationApplicationService(GestionReservations gestionReservations, EmailService emailService) {
        this.gestionReservations = gestionReservations;
        this.emailService = emailService;
    }

    public Confirmation creerEtNotifier(String salleId, String enseignantId,
                                         LocalDateTime debut, LocalDateTime fin) {
        Reservation reservation = gestionReservations.creerReservation(salleId, enseignantId, debut, fin);
        return new Confirmation(reservation, emailService.notifierConfirmation(reservation));
    }

    public record Confirmation(Reservation reservation, EmailResult resultatEmail) { }
}
