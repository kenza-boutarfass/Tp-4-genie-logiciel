package fr.universite.reservations.service;

import fr.universite.reservations.model.Reservation;

public interface EmailService {
    EmailResult notifierConfirmation(Reservation reservation);
}
