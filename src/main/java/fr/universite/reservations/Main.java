package fr.universite.reservations;

import fr.universite.reservations.ui.ReservationApp;
import javafx.application.Application;

public final class Main {
    private Main() { }

    public static void main(String[] args) {
        Application.launch(ReservationApp.class, args);
    }
}
