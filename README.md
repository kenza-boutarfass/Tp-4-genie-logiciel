# Gestion des salles universitaires

## Lancer l'application

Prérequis : JDK 17 et Maven. Depuis la racine du projet :

```powershell
mvn javafx:run
```

Les salles et enseignants de démonstration sont chargés en mémoire au démarrage. Les données ne sont pas conservées après fermeture.

## Version web et mobile

L'interface web responsive fonctionne dans un navigateur sur ordinateur, tablette et téléphone :

```powershell
mvn spring-boot:run
```

Ouvrir ensuite `http://localhost:8080`. Pour tester depuis un téléphone connecté au même réseau Wi-Fi, remplacer `localhost` par l'adresse IP locale de l'ordinateur, par exemple `http://192.168.1.20:8080`. Le pare-feu de l'ordinateur doit autoriser les connexions entrantes sur le port 8080.

La version web et la version JavaFX sont deux façons distinctes de lancer l'application. Les données de démonstration restent en mémoire et sont réinitialisées à chaque démarrage. Cette version est destinée à un usage local/de démonstration ; une mise en ligne publique nécessite notamment une authentification, une base de données et une configuration de production.

## E-mail SMTP facultatif

Les réservations sont créées même si aucun serveur SMTP n'est configuré. Pour activer l'envoi de confirmations, définir les variables d'environnement suivantes avant de lancer Maven :

```powershell
$env:SMTP_HOST = "smtp.example.org"
$env:SMTP_PORT = "587"
$env:SMTP_USERNAME = "compte@example.org"
$env:SMTP_PASSWORD = "mot-de-passe-ou-jeton"
mvn javafx:run
```

Le port est facultatif et vaut `587` par défaut. Aucun secret n'est stocké dans le projet.

## Tests

```powershell
mvn test
```
