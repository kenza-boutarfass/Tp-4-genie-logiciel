package fr.universite.reservations.service;

import fr.universite.reservations.model.Enseignant;
import fr.universite.reservations.model.Reservation;
import fr.universite.reservations.model.Salle;
import fr.universite.reservations.model.StatutReservation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class GestionReservationsTest {
    private GestionSalles salleService;
    private GestionEnseignants enseignantService;
    private GestionReservations reservationService;
    private Salle salle;
    private Enseignant enseignant;
    private final LocalDate date = LocalDate.of(2026, 11, 12);

    @BeforeEach
    void setUp() {
        salleService = new GestionSalles();
        enseignantService = new GestionEnseignants();
        reservationService = new GestionReservations(salleService, enseignantService);
        salle = new Salle("S-40", "Salle 40", 40, List.of("Projecteur", "Tableau"));
        salleService.ajouterSalle(salle);
        enseignant = new Enseignant("ENS-1", "Samira Benali", "samira@universite.fr");
        enseignantService.ajouterEnseignant(enseignant);
    }

    @Test
    void test01AjouteSalleEtLaRetrouveDansLaListe() {
        assertEquals(List.of(salle), salleService.afficherToutesLesSalles());
    }

    @Test
    void test02RechercheCapaciteMinimale() {
        salleService.ajouterSalle(new Salle("S-20", "Salle 20", 20, List.of()));
        assertEquals(List.of(salle), salleService.rechercherParCapacite(30));
    }

    @Test
    void test03CreeUneReservationValide() {
        Reservation reservation = reservationService.creerReservation("S-40", "ENS-1",
                at(10, 0), at(12, 0));
        assertEquals(StatutReservation.CONFIRMEE, reservation.getStatut());
        assertEquals(1, reservationService.afficherToutesLesReservations().size());
    }

    @Test
    void test04RefuseLesPeriodesQuiSeChevauchent() {
        reservationService.creerReservation("S-40", "ENS-1", at(10, 0), at(12, 0));
        assertThrows(IllegalArgumentException.class,
                () -> reservationService.creerReservation("S-40", "ENS-1", at(11, 0), at(13, 0)));
        assertEquals(1, reservationService.afficherToutesLesReservations().size());
    }

    @Test
    void test05AccepteLesPeriodesConsecutives() {
        reservationService.creerReservation("S-40", "ENS-1", at(10, 0), at(12, 0));
        Reservation next = reservationService.creerReservation("S-40", "ENS-1", at(12, 0), at(13, 0));
        assertNotNull(next);
        assertEquals(2, reservationService.afficherToutesLesReservations().size());
    }

    @Test
    void test06AnnulationLibereLaPeriodeEtConserveLHistorique() {
        Reservation original = reservationService.creerReservation("S-40", "ENS-1", at(10, 0), at(12, 0));
        reservationService.annulerReservation(original.getIdentifiant());
        assertTrue(reservationService.estDisponible("S-40", at(10, 30), at(11, 30)));
        assertEquals(StatutReservation.ANNULEE, original.getStatut());
        assertEquals(1, reservationService.afficherReservationsEnseignant("ENS-1").size());
    }

    @Test
    void test07RefuseUneFinAvantLeDebut() {
        assertThrows(IllegalArgumentException.class,
                () -> reservationService.creerReservation("S-40", "ENS-1", at(12, 0), at(10, 0)));
        assertTrue(reservationService.afficherToutesLesReservations().isEmpty());
    }

    @Test
    void rejetteIdentifiantsDupliquesEtDonneesInvalides() {
        assertThrows(IllegalArgumentException.class,
                () -> salleService.ajouterSalle(new Salle("S-40", "Autre salle", 20, List.of())));
        assertThrows(IllegalArgumentException.class,
                () -> new Salle("S-INVALID", "Salle invalide", 0, List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> new Enseignant("ENS-X", "Test", "adresse-invalide"));
        assertThrows(IllegalArgumentException.class,
                () -> enseignantService.ajouterEnseignant(enseignant));
    }

    @Test
    void rejetteReferencesInexistantesEtIdentifiantsDeSalleModifies() {
        assertThrows(IllegalArgumentException.class,
                () -> reservationService.creerReservation("inconnu", "ENS-1", at(10, 0), at(11, 0)));
        assertThrows(IllegalArgumentException.class,
                () -> reservationService.creerReservation("S-40", "inconnu", at(10, 0), at(11, 0)));
        assertThrows(IllegalArgumentException.class,
                () -> salleService.modifierSalle("S-40", new Salle("S-41", "Autre salle", 10, List.of())));
        assertTrue(reservationService.afficherToutesLesReservations().isEmpty());
    }

    @Test
    void salleAvecHistoriqueNePeutPasEtreSupprimee() {
        reservationService.creerReservation("S-40", "ENS-1", at(10, 0), at(11, 0));
        assertThrows(IllegalArgumentException.class, () -> salleService.supprimerSalle("S-40"));
        assertTrue(salleService.existe("S-40"));
    }

    @Test
    void lesConflitsNeConcernentQueLaMemeSalle() {
        salleService.ajouterSalle(new Salle("S-41", "Salle 41", 25, List.of()));
        reservationService.creerReservation("S-40", "ENS-1", at(10, 0), at(12, 0));
        Reservation otherRoom = reservationService.creerReservation("S-41", "ENS-1", at(11, 0), at(13, 0));
        assertEquals("S-41", otherRoom.getSalle().getIdentifiant());
    }

    @Test
    void verificationDeDisponibiliteDetecteUnChevauchementPartiel() {
        reservationService.creerReservation("S-40", "ENS-1", at(10, 0), at(12, 0));
        assertFalse(reservationService.estDisponible("S-40", at(11, 45), at(12, 30)));
        assertTrue(reservationService.estDisponible("S-40", at(12, 0), at(13, 0)));
    }

    @Test
    void enseignantNePeutPasAnnulerLaReservationDUnAutre() {
        enseignantService.ajouterEnseignant(new Enseignant("ENS-2", "Noor Haddad", "noor@universite.fr"));
        Reservation reservation = reservationService.creerReservation("S-40", "ENS-1", at(10, 0), at(11, 0));
        assertThrows(IllegalArgumentException.class,
                () -> reservationService.annulerReservationEnseignant(reservation.getIdentifiant(), "ENS-2"));
        assertEquals(StatutReservation.CONFIRMEE, reservation.getStatut());
    }

    @Test
    void reservationRefuseeNeDeclencheAucuneNotification() {
        reservationService.creerReservation("S-40", "ENS-1", at(10, 0), at(12, 0));
        AtomicInteger notifications = new AtomicInteger();
        ReservationApplicationService application = new ReservationApplicationService(reservationService, reservation -> {
            notifications.incrementAndGet();
            return new EmailResult(true, "envoyé");
        });
        assertThrows(IllegalArgumentException.class,
                () -> application.creerEtNotifier("S-40", "ENS-1", at(11, 0), at(13, 0)));
        assertEquals(0, notifications.get());
        assertEquals(1, reservationService.afficherToutesLesReservations().size());
    }

    private LocalDateTime at(int hour, int minute) {
        return LocalDateTime.of(date, LocalTime.of(hour, minute));
    }
}
