# Gestion des salles universitaires

## Lancer l'application

Prérequis : JDK 17 et Maven. Depuis la racine du projet :

```powershell
mvn javafx:run
```

Les salles et enseignants de démonstration sont chargés en mémoire au démarrage. Les données ne sont pas conservées après fermeture.

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
