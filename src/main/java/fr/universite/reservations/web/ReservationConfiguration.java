package fr.universite.reservations.web;

import fr.universite.reservations.model.Enseignant;
import fr.universite.reservations.model.Salle;
import fr.universite.reservations.service.GestionEnseignants;
import fr.universite.reservations.service.GestionReservations;
import fr.universite.reservations.service.GestionSalles;
import fr.universite.reservations.service.ReservationApplicationService;
import fr.universite.reservations.service.SmtpEmailService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ReservationConfiguration {
    @Bean
    GestionSalles gestionSalles() {
        GestionSalles salles = new GestionSalles();
        salles.ajouterSalle(new Salle("S-101", "Amphithéâtre 101", 120,
                List.of("Projecteur", "Tableau", "Microphone")));
        salles.ajouterSalle(new Salle("S-204", "Salle de cours 204", 36,
                List.of("Projecteur", "Tableau blanc")));
        salles.ajouterSalle(new Salle("S-305", "Salle informatique 305", 28,
                List.of("Ordinateurs", "Projecteur")));
        return salles;
    }

    @Bean
    GestionEnseignants gestionEnseignants() {
        GestionEnseignants enseignants = new GestionEnseignants();
        enseignants.ajouterEnseignant(new Enseignant("ENS-001", "Camille Durand", "camille.durand@universite.fr"));
        enseignants.ajouterEnseignant(new Enseignant("ENS-002", "Alexandre Martin", "alexandre.martin@universite.fr"));
        return enseignants;
    }

    @Bean
    GestionReservations gestionReservations(GestionSalles salles, GestionEnseignants enseignants) {
        return new GestionReservations(salles, enseignants);
    }

    @Bean
    ReservationApplicationService reservationApplicationService(GestionReservations reservations) {
        return new ReservationApplicationService(reservations, new SmtpEmailService());
    }
}
