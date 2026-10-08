# Guide d'utilisation de DataInitializer

## 📋 Qu'est-ce que DataInitializer ?

`DataInitializer` est une classe Spring Boot qui initialise automatiquement les données de base nécessaires au fonctionnement de votre application. Elle s'exécute **automatiquement** au démarrage de l'application.

## 🚀 Comment ça fonctionne ?

### Principe

1. **Implémentation de `CommandLineRunner`** : Cette interface Spring Boot permet d'exécuter du code après le démarrage complet de l'application.

2. **Vérification avant création** : La classe vérifie si les données existent déjà (`count() == 0`) avant de les créer, évitant ainsi les doublons.

3. **Exécution automatique** : Aucune action manuelle n'est nécessaire, tout se fait automatiquement au démarrage.

### Données initialisées

#### 1. Rôles (table `role`)
- **Agent** (valeur: 1, code: "AGENT")
- **Manager** (valeur: 2, code: "MANAGER")
- **Admin** (valeur: 3, code: "ADMIN")

**UUIDs fixes :**
- Agent: `550e8400-e29b-41d4-a716-446655440001`
- Manager: `550e8400-e29b-41d4-a716-446655440002`
- Admin: `550e8400-e29b-41d4-a716-446655440003`

#### 2. Directions (table `direction`)
- **DIR001** - Direction des Ressources Humaines
- **DIR002** - Direction Informatique
- **DIR003** - Direction Financière
- **DIR004** - Direction Juridique

**UUIDs fixes :**
- DIR001: `660e8400-e29b-41d4-a716-446655440001`
- DIR002: `660e8400-e29b-41d4-a716-446655440002`
- DIR003: `660e8400-e29b-41d4-a716-446655440003`
- DIR004: `660e8400-e29b-41d4-a716-446655440004`

## 📝 Utilisation

### Démarrage normal

1. **Lancez votre application Spring Boot** normalement :
   ```bash
   mvn spring-boot:run
   ```
   ou via votre IDE.

2. **Vérifiez les logs** : Vous devriez voir dans la console :
   ```
   Démarrage de l'initialisation des données...
   Création des rôles...
   Rôle 'Agent' créé (valeur: 1)
   Rôle 'Manager' créé (valeur: 2)
   Rôle 'Admin' créé (valeur: 3)
   Création des directions...
   Direction 'DIR001 - Direction des Ressources Humaines' créée
   ...
   Initialisation des données terminée.
   ```

3. **Vérification dans la base de données** : Connectez-vous à PostgreSQL et vérifiez :
   ```sql
   SELECT * FROM role;
   SELECT * FROM direction;
   ```

### Premier démarrage vs démarrages suivants

- **Premier démarrage** : Les données sont créées automatiquement.
- **Démarrages suivants** : Les données existent déjà, donc rien n'est créé (log : "Les rôles/directions existent déjà").

## ⚙️ Personnalisation

### Ajouter d'autres données

Pour ajouter d'autres données (ex: statuts de processus, types de tâches), modifiez la méthode `run()` :

```java
@Override
public void run(String... args) {
    logger.info("Démarrage de l'initialisation des données...");
    
    initialiserRoles();
    initialiserDirections();
    initialiserStatutsProcessus(); // Nouvelle méthode
    initialiserAutresDonnees();   // Autre méthode
    
    logger.info("Initialisation des données terminée.");
}

private void initialiserStatutsProcessus() {
    // Votre logique ici
}
```

### Désactiver l'initialisation

Si vous voulez désactiver temporairement l'initialisation :

1. **Option 1** : Commentez l'annotation `@Component` :
   ```java
   // @Component
   public class DataInitializer implements CommandLineRunner {
   ```

2. **Option 2** : Ajoutez une condition dans `application.yaml` :
   ```yaml
   app:
     data-initializer:
       enabled: false
   ```
   Et modifiez la classe pour vérifier cette propriété.

### Modifier les données existantes

Si vous modifiez les UUIDs ou les valeurs dans le code, vous devrez :
1. Supprimer les données existantes de la base
2. Relancer l'application

Ou modifier directement en base de données.

## 🔍 Vérification dans Swagger

Une fois l'application démarrée, vous pouvez vérifier les données via Swagger :

1. **Ouvrez Swagger UI** : `http://localhost:8080/swagger-ui/index.html`

2. **Testez les endpoints** :
   - `GET /api/directions` → Devrait retourner les 4 directions créées
   - Pour les rôles, vous devrez vérifier directement en base (pas de contrôleur REST pour les rôles)

## ⚠️ Points importants

1. **UUIDs fixes** : Les UUIDs sont fixes pour faciliter les tests. Si vous avez besoin de les modifier, changez-les dans le code.

2. **Idempotence** : L'initialisation est idempotente (peut être exécutée plusieurs fois sans effet de bord) grâce à la vérification `count() == 0`.

3. **Transaction** : Les opérations sont exécutées dans des transactions Spring. En cas d'erreur, tout est annulé.

4. **Ordre d'exécution** : L'initialisation se fait APRÈS la création des tables par Hibernate (grâce à `ddl-auto: update`).

## 🐛 Dépannage

### Les données ne sont pas créées

1. Vérifiez les logs pour voir s'il y a des erreurs
2. Vérifiez que la classe est bien annotée `@Component`
3. Vérifiez que la base de données est accessible
4. Vérifiez que les tables existent (Hibernate doit les avoir créées)

### Erreur de contrainte unique

Si vous obtenez une erreur de contrainte unique, c'est que les données existent déjà avec des UUIDs différents. Supprimez-les manuellement ou modifiez les UUIDs dans le code.

### Les logs ne s'affichent pas

Vérifiez la configuration des logs dans `application.yaml` ou `logback.xml`.

## 📚 Ressources

- [Spring Boot CommandLineRunner Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.spring-application.command-line-runner)
- [Spring Data JPA Documentation](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
