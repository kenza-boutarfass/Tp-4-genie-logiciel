package fr.universite.reservations.service;

import fr.universite.reservations.model.Reservation;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.time.format.DateTimeFormatter;
import java.util.Properties;

public final class SmtpEmailService implements EmailService {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm");

    @Override
    public EmailResult notifierConfirmation(Reservation reservation) {
        String host = System.getenv("SMTP_HOST");
        String username = System.getenv("SMTP_USERNAME");
        String password = System.getenv("SMTP_PASSWORD");
        String port = System.getenv().getOrDefault("SMTP_PORT", "587");
        if (host == null || host.isBlank() || username == null || username.isBlank()
                || password == null || password.isBlank()) {
            return new EmailResult(false, "Réservation enregistrée. L'e-mail réel nécessite SMTP_HOST, SMTP_USERNAME et SMTP_PASSWORD.");
        }

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.connectiontimeout", "10000");
        properties.put("mail.smtp.timeout", "10000");
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(reservation.getEnseignant().getEmail()));
            message.setSubject("Confirmation de réservation de salle");
            message.setText("Bonjour " + reservation.getEnseignant().getNom() + ",\n\n"
                    + "Votre réservation de la salle " + reservation.getSalle().getNom() + " est confirmée.\n"
                    + "Début : " + reservation.getDebut().format(DATE) + "\n"
                    + "Fin : " + reservation.getFin().format(DATE) + "\n\n"
                    + "Gestion des salles universitaires");
            Transport.send(message);
            return new EmailResult(true, "Un e-mail de confirmation a été envoyé à " + reservation.getEnseignant().getEmail() + ".");
        } catch (Exception exception) {
            return new EmailResult(false,
                    "Réservation enregistrée, mais l'e-mail n'a pas pu être envoyé. Vérifiez les paramètres SMTP et la connexion.");
        }
    }
}
