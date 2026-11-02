package fr.universite.reservations.web;

import fr.universite.reservations.model.Enseignant;
import fr.universite.reservations.model.Salle;
import fr.universite.reservations.service.EmailResult;
import fr.universite.reservations.service.GestionEnseignants;
import fr.universite.reservations.service.GestionReservations;
import fr.universite.reservations.service.GestionSalles;
import fr.universite.reservations.service.ReservationApplicationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

@Controller
public class ReservationController {
    private final GestionSalles salles;
    private final GestionEnseignants enseignants;
    private final GestionReservations reservations;
    private final ReservationApplicationService reservationService;

    public ReservationController(GestionSalles salles, GestionEnseignants enseignants,
                                 GestionReservations reservations,
                                 ReservationApplicationService reservationService) {
        this.salles = salles;
        this.enseignants = enseignants;
        this.reservations = reservations;
        this.reservationService = reservationService;
    }

    @GetMapping("/")
    public String accueil() {
        return "redirect:/salles";
    }

    @GetMapping("/salles")
    public String afficherSalles(@RequestParam(name = "capacite", required = false) String capacite,
                                 @RequestParam(name = "equipement", required = false) String equipement,
                                 Model model) {
        List<Salle> resultat = salles.afficherToutesLesSalles();
        try {
            if (capacite != null && !capacite.isBlank()) {
                resultat = salles.rechercherParCapacite(Integer.parseInt(capacite));
            } else if (equipement != null && !equipement.isBlank()) {
                resultat = salles.rechercherParEquipement(equipement);
            }
        } catch (IllegalArgumentException exception) {
            model.addAttribute("erreur", "La capacité minimale doit être un nombre positif.");
        }
        model.addAttribute("salles", resultat);
        model.addAttribute("capacite", capacite);
        model.addAttribute("equipement", equipement);
        return "salles";
    }

    @PostMapping("/salles/ajouter")
    public String ajouterSalle(@RequestParam(name = "identifiant") String identifiant,
                               @RequestParam(name = "nom") String nom,
                               @RequestParam(name = "capacite") String capacite,
                               @RequestParam(name = "equipements", required = false) String equipements,
                               RedirectAttributes redirect) {
        return action(redirect, "Salle ajoutée.", () -> salles.ajouterSalle(
                new Salle(identifiant, nom, Integer.parseInt(capacite), parseEquipements(equipements))), "/salles");
    }

    @PostMapping("/salles/modifier")
    public String modifierSalle(@RequestParam(name = "identifiant") String identifiant,
                                @RequestParam(name = "nom") String nom,
                                @RequestParam(name = "capacite") String capacite,
                                @RequestParam(name = "equipements", required = false) String equipements,
                                RedirectAttributes redirect) {
        return action(redirect, "Salle mise à jour.", () -> salles.modifierSalle(identifiant,
                new Salle(identifiant, nom, Integer.parseInt(capacite), parseEquipements(equipements))), "/salles");
    }

    @PostMapping("/salles/supprimer")
    public String supprimerSalle(@RequestParam(name = "identifiant") String identifiant, RedirectAttributes redirect) {
        return action(redirect, "Salle supprimée.", () -> salles.supprimerSalle(identifiant), "/salles");
    }

    @GetMapping("/enseignants")
    public String afficherEnseignants(Model model) {
        model.addAttribute("enseignants", enseignants.afficherTousLesEnseignants());
        return "enseignants";
    }

    @PostMapping("/enseignants/ajouter")
    public String ajouterEnseignant(@RequestParam(name = "identifiant") String identifiant,
                                    @RequestParam(name = "nom") String nom,
                                    @RequestParam(name = "email") String email, RedirectAttributes redirect) {
        return action(redirect, "Enseignant ajouté.", () ->
                enseignants.ajouterEnseignant(new Enseignant(identifiant, nom, email)), "/enseignants");
    }

    @GetMapping("/disponibilites")
    public String afficherDisponibilites(
            @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(name = "debut", required = false) String debut,
            @RequestParam(name = "fin", required = false) String fin, Model model) {
        model.addAttribute("date", date == null ? LocalDate.now() : date);
        model.addAttribute("debut", debut == null ? "09:00" : debut);
        model.addAttribute("fin", fin == null ? "10:00" : fin);
        if (date != null && debut != null && fin != null) {
            try {
                model.addAttribute("salles", reservations.rechercherSallesDisponibles(
                        date.atTime(LocalTime.parse(debut)), date.atTime(LocalTime.parse(fin))));
            } catch (DateTimeParseException | IllegalArgumentException exception) {
                model.addAttribute("erreur", "Saisissez des heures valides au format HH:mm et une fin après le début.");
            }
        }
        return "disponibilites";
    }

    @GetMapping("/reservations/nouvelle")
    public String nouvelleReservation(Model model) {
        model.addAttribute("salles", salles.afficherToutesLesSalles());
        model.addAttribute("enseignants", enseignants.afficherTousLesEnseignants());
        model.addAttribute("date", LocalDate.now());
        return "reservation-form";
    }

    @PostMapping("/reservations")
    public String creerReservation(@RequestParam(name = "salleId") String salleId,
                                   @RequestParam(name = "enseignantId") String enseignantId,
                                   @RequestParam(name = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                   @RequestParam(name = "debut") String debut,
                                   @RequestParam(name = "fin") String fin,
                                   RedirectAttributes redirect) {
        try {
            ReservationApplicationService.Confirmation confirmation = reservationService.creerEtNotifier(
                    salleId, enseignantId, date.atTime(LocalTime.parse(debut)), date.atTime(LocalTime.parse(fin)));
            EmailResult email = confirmation.resultatEmail();
            redirect.addFlashAttribute(email.envoye() ? "succes" : "info", email.message());
        } catch (DateTimeParseException | IllegalArgumentException exception) {
            redirect.addFlashAttribute("erreur", exception.getMessage() == null
                    ? "Vérifiez les dates et heures saisies (format HH:mm)." : exception.getMessage());
        }
        return "redirect:/reservations/nouvelle";
    }

    @GetMapping("/reservations")
    public String afficherReservations(Model model) {
        model.addAttribute("reservations", reservations.afficherToutesLesReservations());
        return "reservations";
    }

    @PostMapping("/reservations/annuler")
    public String annulerReservation(@RequestParam(name = "reservationId") String reservationId,
                                     RedirectAttributes redirect) {
        return action(redirect, "Réservation annulée.", () -> reservations.annulerReservation(reservationId),
                "/reservations");
    }

    @GetMapping("/mes-reservations")
    public String afficherMesReservations(@RequestParam(name = "enseignantId", required = false) String enseignantId,
                                          Model model) {
        model.addAttribute("enseignants", enseignants.afficherTousLesEnseignants());
        model.addAttribute("enseignantId", enseignantId);
        if (enseignantId != null && !enseignantId.isBlank()) {
            try {
                model.addAttribute("reservations", reservations.afficherReservationsEnseignant(enseignantId));
            } catch (IllegalArgumentException exception) {
                model.addAttribute("erreur", exception.getMessage());
            }
        }
        return "mes-reservations";
    }

    @PostMapping("/mes-reservations/annuler")
    public String annulerMaReservation(@RequestParam(name = "reservationId") String reservationId,
                                       @RequestParam(name = "enseignantId") String enseignantId,
                                       RedirectAttributes redirect) {
        String cible = "redirect:/mes-reservations?enseignantId=" + enseignantId;
        return action(redirect, "Votre réservation a été annulée.", () ->
                reservations.annulerReservationEnseignant(reservationId, enseignantId), cible);
    }

    private String action(RedirectAttributes redirect, String success, Runnable operation, String target) {
        try {
            operation.run();
            redirect.addFlashAttribute("succes", success);
        } catch (IllegalArgumentException exception) {
            redirect.addFlashAttribute("erreur", exception.getMessage());
        } catch (RuntimeException exception) {
            redirect.addFlashAttribute("erreur", "L'opération n'a pas pu être effectuée.");
        }
        return "redirect:" + target;
    }

    private List<String> parseEquipements(String equipements) {
        if (equipements == null || equipements.isBlank()) {
            return List.of();
        }
        return Arrays.stream(equipements.split(",")).map(String::trim).filter(value -> !value.isEmpty()).toList();
    }
}
