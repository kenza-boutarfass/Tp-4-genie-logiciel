package fr.universite.reservations.ui;

import fr.universite.reservations.model.Enseignant;
import fr.universite.reservations.model.Reservation;
import fr.universite.reservations.model.Salle;
import fr.universite.reservations.model.StatutReservation;
import fr.universite.reservations.service.EmailResult;
import fr.universite.reservations.service.GestionEnseignants;
import fr.universite.reservations.service.GestionReservations;
import fr.universite.reservations.service.GestionSalles;
import fr.universite.reservations.service.ReservationApplicationService;
import fr.universite.reservations.service.SmtpEmailService;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public final class ReservationApp extends Application {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final GestionSalles salles = new GestionSalles();
    private final GestionEnseignants enseignants = new GestionEnseignants();
    private final GestionReservations reservations = new GestionReservations(salles, enseignants);
    private final ReservationApplicationService reservationService =
            new ReservationApplicationService(reservations, new SmtpEmailService());
    private final Label notification = new Label("Bienvenue dans l'espace de gestion des salles.");
    private VBox pageHost;
    private Stage stage;

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        initialiserDonnees();
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-root");
        root.setLeft(creerNavigation());
        VBox main = new VBox(18);
        main.setPadding(new Insets(28, 34, 30, 34));
        HBox heading = new HBox();
        heading.setAlignment(Pos.CENTER_LEFT);
        VBox titles = new VBox(5);
        Label title = new Label("Gestion des salles universitaires");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Administration et réservations des espaces d'enseignement");
        subtitle.getStyleClass().add("muted");
        titles.getChildren().addAll(title, subtitle);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button quit = new Button("Quitter");
        quit.getStyleClass().add("button-quiet");
        quit.setOnAction(event -> stage.close());
        heading.getChildren().addAll(titles, spacer, quit);
        notification.getStyleClass().add("notification");
        pageHost = new VBox();
        VBox.setVgrow(pageHost, Priority.ALWAYS);
        main.getChildren().addAll(heading, notification, pageHost);
        root.setCenter(main);

        Scene scene = new Scene(root, 1280, 800);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        primaryStage.setTitle("Gestion des salles universitaires");
        primaryStage.setMinWidth(980);
        primaryStage.setMinHeight(650);
        primaryStage.setScene(scene);
        afficherSalles();
        primaryStage.show();
    }

    private void initialiserDonnees() {
        salles.ajouterSalle(new Salle("S-101", "Amphithéâtre 101", 120,
                List.of("Projecteur", "Tableau", "Microphone")));
        salles.ajouterSalle(new Salle("S-204", "Salle de cours 204", 36,
                List.of("Projecteur", "Tableau blanc")));
        salles.ajouterSalle(new Salle("S-305", "Salle informatique 305", 28,
                List.of("Ordinateurs", "Projecteur")));
        enseignants.ajouterEnseignant(new Enseignant("ENS-001", "Camille Durand", "camille.durand@universite.fr"));
        enseignants.ajouterEnseignant(new Enseignant("ENS-002", "Alexandre Martin", "alexandre.martin@universite.fr"));
    }

    private Node creerNavigation() {
        VBox nav = new VBox(8);
        nav.setPadding(new Insets(28, 16, 22, 16));
        nav.getStyleClass().add("sidebar");
        Label brand = new Label("UNIVERSITÉ\nRéservations");
        brand.getStyleClass().add("brand");
        Label adminSection = new Label("ADMINISTRATION");
        adminSection.getStyleClass().add("nav-section");
        Label teacherSection = new Label("ENSEIGNANTS");
        teacherSection.getStyleClass().add("nav-section");
        Button rooms = navButton("Salles", this::afficherSalles);
        Button teachers = navButton("Enseignants", this::afficherEnseignants);
        Button allReservations = navButton("Toutes les réservations", this::afficherToutesReservations);
        Button availability = navButton("Disponibilités", this::afficherDisponibilites);
        Button create = navButton("Créer une réservation", this::afficherCreationReservation);
        Button mine = navButton("Mes réservations / historique", this::afficherMesReservations);
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        Label footer = new Label("Gestion institutionnelle");
        footer.getStyleClass().add("nav-footer");
        nav.getChildren().addAll(brand, adminSection, rooms, teachers, allReservations,
                teacherSection, availability, create, mine, spacer, footer);
        nav.setPrefWidth(245);
        return nav;
    }

    private Button navButton(String text, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().add("nav-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setOnAction(event -> action.run());
        return button;
    }

    private void setPage(String title, String hint, Node body) {
        Label heading = new Label(title);
        heading.getStyleClass().add("section-title");
        Label description = new Label(hint);
        description.getStyleClass().add("muted");
        VBox page = new VBox(16, heading, description, body);
        page.getStyleClass().add("page");
        VBox.setVgrow(body, Priority.ALWAYS);
        pageHost.getChildren().setAll(page);
    }

    private void afficherSalles() {
        TextField id = field("Ex. S-401");
        TextField name = field("Nom ou numéro");
        TextField capacity = field("Capacité");
        TextField equipment = field("Séparer par des virgules");
        GridPane form = formGrid();
        addField(form, 0, "Identifiant", id);
        addField(form, 1, "Nom", name);
        addField(form, 2, "Capacité", capacity);
        addField(form, 3, "Équipements", equipment);
        TableView<Salle> table = salleTable();
        Button add = primaryButton("Ajouter la salle");
        Button update = secondaryButton("Enregistrer les modifications");
        Button delete = dangerButton("Supprimer");
        Button clear = secondaryButton("Vider le formulaire");
        HBox actions = new HBox(9, add, update, delete, clear);
        add.setOnAction(event -> safeAction(() -> {
            salles.ajouterSalle(readSalle(id, name, capacity, equipment));
            afficherSalles();
            success("La salle a été ajoutée.");
        }));
        table.getSelectionModel().selectedItemProperty().addListener((obs, previous, selected) -> {
            if (selected == null) return;
            id.setText(selected.getIdentifiant());
            id.setDisable(true);
            name.setText(selected.getNom());
            capacity.setText(String.valueOf(selected.getCapacite()));
            equipment.setText(String.join(", ", selected.getEquipements()));
        });
        update.setOnAction(event -> safeAction(() -> {
            String selectedId = id.getText().trim();
            salles.modifierSalle(selectedId, readSalle(id, name, capacity, equipment));
            afficherSalles();
            success("Les informations de la salle ont été mises à jour.");
        }));
        delete.setOnAction(event -> {
            Salle selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { error("Sélectionnez une salle à supprimer."); return; }
            if (!confirmer("Supprimer la salle " + selected.getNom() + " ?")) return;
            safeAction(() -> {
                if (!reservations.afficherReservationsSalle(selected.getIdentifiant()).isEmpty()) {
                    throw new IllegalArgumentException("Cette salle est référencée par l'historique et ne peut pas être supprimée.");
                }
                salles.supprimerSalle(selected.getIdentifiant());
                afficherSalles();
                success("La salle a été supprimée.");
            });
        });
        clear.setOnAction(event -> {
            id.clear(); id.setDisable(false); name.clear(); capacity.clear(); equipment.clear();
            table.getSelectionModel().clearSelection();
        });

        TextField minCapacity = field("Capacité minimale");
        TextField equipmentQuery = field("Équipement recherché");
        Button searchCapacity = secondaryButton("Capacité ≥");
        Button searchEquipment = secondaryButton("Équipement");
        Button showAll = secondaryButton("Afficher toutes");
        searchCapacity.setOnAction(event -> safeAction(() ->
                table.setItems(FXCollections.observableArrayList(salles.rechercherParCapacite(
                        Integer.parseInt(minCapacity.getText().trim()))))));
        searchEquipment.setOnAction(event -> safeAction(() ->
                table.setItems(FXCollections.observableArrayList(salles.rechercherParEquipement(equipmentQuery.getText())))));
        showAll.setOnAction(event -> table.setItems(FXCollections.observableArrayList(salles.afficherToutesLesSalles())));
        HBox filters = new HBox(9, minCapacity, searchCapacity, equipmentQuery, searchEquipment, showAll);
        filters.setAlignment(Pos.CENTER_LEFT);
        VBox body = new VBox(14, card("Informations de la salle", form, actions),
                card("Recherche", filters), table);
        VBox.setVgrow(table, Priority.ALWAYS);
        setPage("Gestion des salles", "Ajouter, modifier, rechercher et consulter les espaces universitaires.", body);
        table.setItems(FXCollections.observableArrayList(salles.afficherToutesLesSalles()));
    }

    private Salle readSalle(TextField id, TextField name, TextField capacity, TextField equipment) {
        int places;
        try { places = Integer.parseInt(capacity.getText().trim()); }
        catch (NumberFormatException exception) { throw new IllegalArgumentException("La capacité doit être un nombre entier."); }
        List<String> features = Arrays.stream(equipment.getText().split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
        return new Salle(id.getText(), name.getText(), places, features);
    }

    private void afficherEnseignants() {
        TextField id = field("Ex. ENS-003");
        TextField name = field("Nom complet");
        TextField email = field("nom@universite.fr");
        GridPane form = formGrid();
        addField(form, 0, "Identifiant", id);
        addField(form, 1, "Nom", name);
        addField(form, 2, "Adresse e-mail", email);
        Button add = primaryButton("Ajouter l'enseignant");
        add.setOnAction(event -> safeAction(() -> {
            enseignants.ajouterEnseignant(new Enseignant(id.getText(), name.getText(), email.getText()));
            afficherEnseignants();
            success("L'enseignant a été ajouté.");
        }));
        TableView<Enseignant> table = new TableView<>();
        table.getColumns().add(column("Identifiant", "identifiant", 180));
        table.getColumns().add(column("Nom", "nom", 270));
        table.getColumns().add(column("Adresse e-mail", "email", 360));
        table.setItems(FXCollections.observableArrayList(enseignants.afficherTousLesEnseignants()));
        setPage("Gestion des enseignants", "Créer un enseignant et consulter le répertoire disponible.", new VBox(14,
                card("Nouvel enseignant", form, add), table));
    }

    private void afficherDisponibilites() {
        DatePicker date = new DatePicker(LocalDate.now());
        TextField start = field("09:00");
        TextField end = field("10:00");
        Button search = primaryButton("Rechercher les salles libres");
        TableView<Salle> table = salleTable();
        search.setOnAction(event -> safeAction(() -> {
            LocalDateTime from = dateTime(date, start);
            LocalDateTime to = dateTime(date, end);
            table.setItems(FXCollections.observableArrayList(reservations.rechercherSallesDisponibles(from, to)));
            success(table.getItems().size() + " salle(s) disponible(s) pour cette période.");
        }));
        HBox controls = new HBox(12, fieldGroup("Date", date), fieldGroup("Début (HH:mm)", start),
                fieldGroup("Fin (HH:mm)", end), search);
        controls.setAlignment(Pos.BOTTOM_LEFT);
        setPage("Disponibilités", "Les résultats tiennent compte de la date, de la période et des réservations confirmées.",
                new VBox(14, card("Période souhaitée", controls), table));
    }

    private void afficherCreationReservation() {
        ComboBox<Salle> room = new ComboBox<>(FXCollections.observableArrayList(salles.afficherToutesLesSalles()));
        ComboBox<Enseignant> teacher = new ComboBox<>(FXCollections.observableArrayList(enseignants.afficherTousLesEnseignants()));
        room.setPromptText("Choisir une salle");
        teacher.setPromptText("Choisir un enseignant");
        DatePicker date = new DatePicker(LocalDate.now());
        TextField start = field("09:00");
        TextField end = field("10:00");
        GridPane form = formGrid();
        addField(form, 0, "Salle", room);
        addField(form, 1, "Enseignant", teacher);
        addField(form, 2, "Date", date);
        addField(form, 3, "Heure de début", start);
        addField(form, 4, "Heure de fin", end);
        Button create = primaryButton("Confirmer la réservation");
        create.setOnAction(event -> safeAction(() -> {
            if (room.getValue() == null || teacher.getValue() == null) {
                throw new IllegalArgumentException("Sélectionnez une salle et un enseignant.");
            }
            ReservationApplicationService.Confirmation confirmation = reservationService.creerEtNotifier(
                    room.getValue().getIdentifiant(), teacher.getValue().getIdentifiant(),
                    dateTime(date, start), dateTime(date, end));
            EmailResult email = confirmation.resultatEmail();
            afficherCreationReservation();
            status(email.message(), email.envoye());
        }));
        setPage("Créer une réservation", "La réservation est confirmée uniquement si la période est valide et libre.",
                card("Détails de la réservation", form, create));
    }

    private void afficherToutesReservations() {
        TableView<Reservation> table = reservationTable();
        Button details = secondaryButton("Voir les détails");
        Button cancel = dangerButton("Annuler la réservation");
        details.setOnAction(event -> afficherDetails(table.getSelectionModel().getSelectedItem()));
        cancel.setOnAction(event -> {
            Reservation selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { error("Sélectionnez une réservation."); return; }
            if (!confirmer("Annuler la réservation de " + selected.getSalle().getNom() + " ?")) return;
            safeAction(() -> {
                reservations.annulerReservation(selected.getIdentifiant());
                afficherToutesReservations();
                success("La réservation a été annulée. La période est à nouveau disponible.");
            });
        });
        table.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) afficherDetails(table.getSelectionModel().getSelectedItem());
        });
        table.setItems(FXCollections.observableArrayList(reservations.afficherToutesLesReservations()));
        setPage("Toutes les réservations", "Vue administration : réservations confirmées et annulées.", new VBox(12,
                new HBox(9, details, cancel), table));
    }

    private void afficherMesReservations() {
        ComboBox<Enseignant> teacher = new ComboBox<>(FXCollections.observableArrayList(enseignants.afficherTousLesEnseignants()));
        teacher.setPromptText("Identifier l'enseignant");
        TableView<Reservation> table = reservationTable();
        Button load = primaryButton("Consulter mes réservations");
        Button details = secondaryButton("Voir les détails");
        Button cancel = dangerButton("Annuler ma réservation");
        load.setOnAction(event -> safeAction(() -> {
            if (teacher.getValue() == null) throw new IllegalArgumentException("Sélectionnez l'enseignant à identifier.");
            table.setItems(FXCollections.observableArrayList(
                    reservations.afficherReservationsEnseignant(teacher.getValue().getIdentifiant())));
            success("Historique chargé, y compris les réservations annulées.");
        }));
        details.setOnAction(event -> afficherDetails(table.getSelectionModel().getSelectedItem()));
        cancel.setOnAction(event -> {
            Reservation selected = table.getSelectionModel().getSelectedItem();
            if (selected == null || teacher.getValue() == null) { error("Sélectionnez une réservation et identifiez l'enseignant."); return; }
            if (!confirmer("Annuler votre réservation ?")) return;
            safeAction(() -> {
                reservations.annulerReservationEnseignant(selected.getIdentifiant(), teacher.getValue().getIdentifiant());
                table.setItems(FXCollections.observableArrayList(
                        reservations.afficherReservationsEnseignant(teacher.getValue().getIdentifiant())));
                success("Votre réservation a été annulée.");
            });
        });
        table.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) afficherDetails(table.getSelectionModel().getSelectedItem());
        });
        setPage("Mes réservations et historique",
                "Choisissez votre identité pour consulter uniquement vos réservations, passées ou annulées.",
                new VBox(12, card("Identification", new HBox(12, teacher, load)), new HBox(9, details, cancel), table));
    }

    private TableView<Salle> salleTable() {
        TableView<Salle> table = new TableView<>();
        table.getColumns().add(column("Identifiant", "identifiant", 150));
        table.getColumns().add(column("Salle", "nom", 240));
        table.getColumns().add(column("Capacité", "capacite", 110));
        TableColumn<Salle, String> equipment = new TableColumn<>("Équipements");
        equipment.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                String.join(", ", data.getValue().getEquipements())));
        equipment.setPrefWidth(360);
        table.getColumns().add(equipment);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        return table;
    }

    private TableView<Reservation> reservationTable() {
        TableView<Reservation> table = new TableView<>();
        TableColumn<Reservation, String> id = new TableColumn<>("Identifiant");
        id.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getIdentifiant()));
        id.setPrefWidth(190);
        TableColumn<Reservation, String> room = new TableColumn<>("Salle");
        room.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getSalle().getNom()));
        room.setPrefWidth(150);
        TableColumn<Reservation, String> teacher = new TableColumn<>("Enseignant");
        teacher.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getEnseignant().getNom()));
        teacher.setPrefWidth(190);
        TableColumn<Reservation, String> start = new TableColumn<>("Début");
        start.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDebut().format(DATE_TIME)));
        TableColumn<Reservation, String> end = new TableColumn<>("Fin");
        end.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getFin().format(DATE_TIME)));
        TableColumn<Reservation, String> status = new TableColumn<>("Statut");
        status.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getStatut() == StatutReservation.CONFIRMEE ? "Confirmée" : "Annulée"));
        status.setCellFactory(column -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                getStyleClass().removeAll("status-confirmed", "status-cancelled");
                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(value);
                    getStyleClass().add(value.equals("Confirmée") ? "status-confirmed" : "status-cancelled");
                }
            }
        });
        table.getColumns().addAll(List.of(id, room, teacher, start, end, status));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        return table;
    }

    private void afficherDetails(Reservation reservation) {
        if (reservation == null) { error("Sélectionnez une réservation pour afficher ses détails."); return; }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails de la réservation");
        alert.setHeaderText("Réservation " + reservation.getIdentifiant());
        alert.setContentText("Salle : " + reservation.getSalle().getNom() + " (" + reservation.getSalle().getIdentifiant() + ")\n"
                + "Enseignant : " + reservation.getEnseignant().getNom() + " — " + reservation.getEnseignant().getEmail() + "\n"
                + "Début : " + reservation.getDebut().format(DATE_TIME) + "\n"
                + "Fin : " + reservation.getFin().format(DATE_TIME) + "\n"
                + "Statut : " + (reservation.getStatut() == StatutReservation.CONFIRMEE ? "Confirmée" : "Annulée"));
        alert.showAndWait();
    }

    private GridPane formGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(9);
        return grid;
    }

    private void addField(GridPane grid, int row, String label, Node control) {
        Label caption = new Label(label);
        caption.getStyleClass().add("field-label");
        grid.add(caption, 0, row);
        grid.add(control, 1, row);
        if (control instanceof Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
            GridPane.setHgrow(region, Priority.ALWAYS);
        }
    }

    private TextField field(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.getStyleClass().add("text-input");
        return field;
    }

    private VBox fieldGroup(String title, Node control) {
        Label label = new Label(title);
        label.getStyleClass().add("field-label");
        VBox group = new VBox(5, label, control);
        return group;
    }

    private VBox card(String title, Node... content) {
        Label heading = new Label(title);
        heading.getStyleClass().add("card-title");
        VBox card = new VBox(12);
        card.getStyleClass().add("card");
        card.getChildren().add(heading);
        card.getChildren().addAll(content);
        return card;
    }

    private Button primaryButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("button-primary");
        return button;
    }

    private Button secondaryButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("button-secondary");
        return button;
    }

    private Button dangerButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("button-danger");
        return button;
    }

    private <T> TableColumn<T, ?> column(String title, String property, double width) {
        TableColumn<T, Object> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setPrefWidth(width);
        return column;
    }

    private LocalDateTime dateTime(DatePicker date, TextField time) {
        if (date.getValue() == null) throw new IllegalArgumentException("Veuillez sélectionner une date.");
        try {
            return LocalDateTime.of(date.getValue(), LocalTime.parse(time.getText().trim()));
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Saisissez une heure valide au format HH:mm.");
        }
    }

    private boolean confirmer(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message,
                javafx.scene.control.ButtonType.OK, javafx.scene.control.ButtonType.CANCEL);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        return alert.showAndWait().orElse(javafx.scene.control.ButtonType.CANCEL) == javafx.scene.control.ButtonType.OK;
    }

    private void safeAction(Runnable action) {
        try { action.run(); }
        catch (IllegalArgumentException exception) { error(exception.getMessage()); }
        catch (RuntimeException exception) { error("L'opération n'a pas pu être effectuée. Vérifiez les données saisies."); }
    }

    private void success(String message) { status(message, true); }

    private void status(String message, boolean positive) {
        notification.setText(message);
        notification.getStyleClass().removeAll("notification-success", "notification-warning");
        notification.getStyleClass().add(positive ? "notification-success" : "notification-warning");
    }

    private void error(String message) {
        notification.setText(message);
        notification.getStyleClass().removeAll("notification-success", "notification-warning");
        notification.getStyleClass().add("notification-warning");
    }
}
