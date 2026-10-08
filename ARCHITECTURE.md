
# Architecture de l'application

## Contexte métier
L'application remplace le processus actuel par mail/téléphone pour :
- Centraliser les processus (entrée/sortie/mobilité)
- Tracer l'avancement des tâches par direction
- Notifier automatiquement les directions
## Architecture technique

### Frontend (React + TS)
```
src/
├── components/        # Composants réutilisables (Atomic Design)
│   ├── contenuPage/         # Contenu des différentes pages
│   ├── Formulaire/         # Représentation des formulaires
│   ├── iconTypeProcessus/       # Différentes icônes représentant un processus
│   ├── Login/         # Page Login
│   ├── NavBar/         # Éléments de la NavBar
│   ├── Recherche/         # Éléments pour la recherche (barre de recherche,...)
│   ├── sidebar/         # Éléments de la sidebar 
│   ├── tableau/         # Tableaux des différentes pages
│   ├── Tache/         # Éléments des tâches (à supprimer car ancienne méthode)
│   ├── ui/             # Boutons, inputs (Shadcn)
│   ├── utils/         # Boutons Retour, badge de statut de tâche, niveau notification
├── service/          # Appels API (axios)
│   ├── roleService.ts         
│   ├── tacheService.ts
│   ├── processusService.ts
│   ├── templateProcessusService.ts
│   ├── templateTacheService.ts
│   ├── directionService.ts
│   ├── agentService.ts
│   └── notificationService.ts
├── lib/             # Éléments pour configurer axios et tailwind
├── hooks/             # Hooks custom
├── types/             # Types TypeScript
└── utils/             # Fonctions utilitaires
```

### Backend (Spring Boot - Hexagonal)
```
com.cese.entree_sortie/
├── domain/           # Cœur métier
│   ├── model/        # Agent, Process, Task, Notification
├── application/      # Cas d'usage
│   ├── dto/          # Modèle pour transférer les données 
│   ├── service/      # Logique métier
│   └── port/         # Interfaces (repositories, services)
└── infrastructure/   # Adapters
│   ├── config/       # Configuration Spring
│   ├── persistence/  # JPA repositories
│   └── web/          # Controllers REST
```


## Communication Frontend ↔ Backend
- Base URL : `http://localhost:8080/api` (dev)
- Axios configuré dans `services/api.ts`
- Format : JSON
- Authentification : *À venir (JWT/SSO)*
## Schéma visuel de l'application

## Choix techniques

### Frontend : React + TypeScript
**Pourquoi ?**
- React : Composants réutilisables, très utilisé, et simple à manier
- TypeScript : Typage fort pour éviter erreurs sur les statuts de processus


### Styling : Tailwind + Shadcn
**Pourquoi ?**
- Tailwind : Rapidité de dev, pas de CSS custom à maintenir
- Shadcn : Composants accessibles prêts à l'emploi (boutons, dialogs, toast...)

### Backend : Spring Boot + Architecture Hexagonale
**Pourquoi ?**
- Spring Boot : Standard Java, robuste pour applications métier
- Hexagonal : Séparation claire domaine métier / infra → facilite tests et évolutions
- JPA : Mapping objet-relationnel simplifié

### Base de données : PostgreSQL
**Pourquoi ?**
- Relationnel : Données structurées (Agents, Processus, Tâches liées)
## Modèles de données

Principales entités :

**Agent**
- id, nom, prénom, email, direction, statut (ACTIF/INACTIF)



**Processus**
- id, type (ENTREE/SORTIE/MOBILITE), statut, dateCreation, dateArrivee/Sortie
- Lié à : 1 Agent

**Tache** 
- id, nom, description, statut (A_FAIRE/EN_COURS/TERMINEE), contenu, TYPE (FORMULAIRE/TACHE)
- Lié à : 1 GroupeTache


**GroupeTache** 
- id, nom, description, statut (En_ATTENTE/EN_COURS/TERMINEE), direction
- Lié à : 1 Process, 1 Direction

**Notification**
- id, message, dateEnvoi, lu (boolean)
- Lié à : 1 Agent, 1 Task

👉 Voir DATABASE.md pour les informations détaillées de la base de donnée
## Workflow détaillé : Processus d'ENTRÉE

### Étape 1 : Création du processus (RH)
1. Agent RH clique le bouton "ajouter un agent" puis sur le type de processus
2. Remplit formulaire :
   - image de l'agent
   - Nom, prénom, email du futur agent
   - Direction d'affectation, numéro du bureau
   - Date d'arrivée prévue, date de départ prévue
   - role (pour l'instant)
3. Frontend → `POST /api/processes`
```json
   {
     "type": "ENTREE",
     "agent": { "nom": "Dupont", "prenom": "Jean", ... },
     ...
     "dateArrivee": "2026-03-15",
     ...
     "direction": "DSIUN"
   }
```
4. **Backend** :
   - Crée l'entité `Process` avec statut = `Initialisation` (avec les infos du formulaire)
   - Crée l'entité `Agent` avec l'etat = `entrée`  (avec les infos du formulaire)
   - Crée l'entité `AgentDirection` (avec les infos du formulaire)
   - Génère automatiquement la tâche "Formulaire RH" assignée à DRH et la complète
   automatiquement par le formulaire rempli précédemment
   - **Change automatiquement** le statut du processus → `Formulaire des droits`
   - Génère le groupe de tâche pour le formulaire de l'agent assignée à la Direction concernée

### Étape 2 : Formulaire de la direction concernée
5. Agent de la direction concernée (ex: DSIUN) se connecte
6. Clique sur le processus -> Clique sur le groupe de tache 
7. Clique → Remplit formulaire (fonction, applications nécessaires, matériel...)
8. Frontend → `PUT /api/tasks/{taskId}/complete`
9. **Backend** :
   - Marque la tâche comme `fait`
   - **Change automatiquement** le statut du processus → `Ouverture des droits`
   - **Génère toutes les Groupes de taches et taches des 5 directions** :
     * DSIUN : "Création de l'adresse mail", "Matériel de l'agent"
     * DAPPI : "Création du badge", "Clé du bureau à donner pour l'agent"
     * DAF : "Rémunération de l'agent", "Droits de la cantine"
     * DICI : 
     * DRH : "Virtualia et Organigramme"
   - **Envoie notifications** à chaque direction (à faire pour plus tard)

### Étape 3 : Réalisation des tâches (5 directions)
10. Chaque direction valide sa tâche au fur et à mesure
11. À chaque validation → `PUT /api/tasks/{taskId}/complete`
12. **Backend vérifie** : tous les groupes de tâches terminés ?
    - Non → Processus reste en `Ouverture des droits`
    - **Oui → Change automatiquement** → `Processus clôturé`

### Étape 4 : Clôture automatique
13. Le jour de la date d'arrivée (`dateArrivee`)
14. **backend** (tâche planifiée) :
    - Vérifie les processus avec `dateArrivee <= aujourd'hui`
    - Si `dateArrivee <= aujourd'hui`, change le statut de l'agent : `entrée` → `actif` et l'agent apparaît maintenant dans "Liste des agents actifs"
    - Sinon, attendre que `dateArrivee <= aujourd'hui` et recommencer la vérification
---

### Transitions de statut automatiques (IMPORTANT)

| Événement déclencheur | Statut AVANT | Statut APRÈS |
|-----------------------|--------------|--------------|
| Création du processus | `Initialisation` | `Initialisation` |
| Validation formulaire RH | `Initialisation` |  `Formulaire des droits` |
| Validation formulaire direction |  `Formulaire des droits` | `Ouverture des droits` |
| Toutes tâches terminées | `Ouverture des droits` | `Processus cloturé` |
| Date d'arrivée atteinte | Agent `entrée` | Agent `actif` |

⚠️ **Ces transitions sont gérées par le backend** 
→ Le frontend ne fait que déclencher les actions, le backend gère la logique métier
## Workflow détaillé : Processus de Sortie

### Étape 1 : Création du processus (RH)
1. Agent RH clique sur un agent de la liste des agents, Clique -> agent à lancer le Processus
2. Clique -> Bouton "lancer le processus de sortie"
3. Remplit formulaire :
   - image de l'agent
   - Nom, prénom, email du futur agent
   - Direction d'affectation, numéro du bureau
   - Date d'arrivée prévue, date de départ prévue
   - role (pour l'instant)
4. Frontend → `POST /api/processes`
```json
   {
     "type": "SORTIE",
     "agent": { "nom": "Dupont", "prenom": "Jean", ... },
     ...
     "dateDepart": "2026-03-15",
     ...
     "direction": "DSIUN"
   }
```
5. **Backend** :
   - Crée l'entité `Process` avec statut = `Initialisation/Formulaire des droits` (avec les infos du formulaire)
   - Modifie l'entité `Agent` avec l'etat = `sortie`  (avec les infos du formulaire)
   - Modifie l'entité `AgentDirection` (avec les infos du formulaire)
   - Génère automatiquement la tâche "Formulaire RH" assignée à DRH et le complète
   automatiquement par le formulaire remplit précedemment
   - **Change automatiquement** le statut du processus → `Formulaire des accès`
   - Génère le groupe de tâche pour le formulaire de l'agent assignée à la Direction concernée

### Étape 2 : Formulaire de la direction concernée
5. Agent de la direction concernée (ex: DSIUN) se connecte
6. Clique sur le processus -> Clique sur le groupe de tache 
7. Clique → Remplit formulaire (fonction, applications nécessaires, matériel...)
8. Frontend → `PUT /api/tasks/{taskId}/complete`
9. **Backend** :
   - Marque la tâche comme `fait`
   - **Change automatiquement** le statut du processus → `Retrait des droits`
   - **Génère toutes les Groupes de taches et taches des 5 directions** :
     * DSIUN : "Droits et Adresse mail", "Matériel de l'agent"
     * DAPPI : "Requea", "Clé du bureau à récupérer pour l'agent"
     * DAF : "Traitement du matériel non rendu"
     * DICI : 
     * DRH : "Virtualia et Organigramme"
   - **Envoie notifications** à chaque direction (à faire pour plus tard)

### Étape 3 : Réalisation des tâches (5 directions)
10. Chaque direction valide sa tâche au fur et à mesure
11. À chaque validation → `PUT /api/tasks/{taskId}/complete`
12. **Backend vérifie** : toutes les groupes de tâches terminés ?
    - Non → Processus reste en `Retrait des droits`
    - **Oui → Change automatiquement** → `Processus cloturé`

### Étape 4 : Clôture automatique
13. Le jour de la date d'arrivée (`dateArrivee`)
14. **backend** (tâche planifiée) :
    - Vérifie les processus avec `dateDepart <= aujourd'hui`
    - Si `dateArrivee <= aujourd'hui` Change le statut de l'agent : `sortie` → `ancien` et l'agent apparaît maintenant dans "Liste des anciens agents"
    - Sinon, on attent que:  `dateDepart <= aujourd'hui` et on recommence la vérification
---

### Transitions de statut automatiques (IMPORTANT)

| Événement déclencheur | Statut AVANT | Statut APRÈS |
|-----------------------|--------------|--------------|
| Création du processus | `Initialisation/Formulaire des droits` | `Initialisation/Formulaire des droits` |
| Validation formulaire RH | `Initialisation/Formulaire des droits` |  `Formulaire des accès` |
| Validation formulaire direction |  `Formulaire des accès` | `Retrait des droits` |
| Toutes tâches terminées | `Retrait des droits` | `Processus cloturé` |
| Date de sortie atteinte | Agent `sortie` | Agent `ancien` |

⚠️ **Ces transitions sont gérées par le backend** 
→ Le frontend ne fait que déclencher les actions, le backend gère la logique métier
## Environnements

### Développement (local)
- Frontend : `http://localhost:5174` (Vite dev server)
- Backend : `http://localhost:8080` (Spring Boot)
- Base de données : localhost:5432 (PostgreSQL)

### Production (si applicable)
- URL frontend : [À faire]
- URL backend : [À faire]
- Base de données : [À faire]

### Variables d'environnement

**Frontend** (`.env`)
```
VITE_API_BASE_URL=http://localhost:8080/api
```

**Backend** (`application.yml`)
```properties
spring:
  application:
    name: process_entree_sortie
  datasource:
    url: jdbc:postgresql://localhost:5432/projet_entre_sortie
    username: entre_sortie_user
    password: user
    driver-class-name: org.postgresql.Driver
    
  # Configuration JPA/Hibernate
  jpa:
    hibernate:
      ddl-auto: update  # create, create-drop, update, validate, none
    show-sql: true      # Affiche les requêtes SQL dans la console
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true  # Formatte les requêtes SQL pour la lisibilité
        jdbc:
          use_streams_for_binary: false  # Évite l'utilisation des Large Objects (OID)
    
  # Configuration du serveur
server:
  port: 8080
  error:
    include-message: always
    include-binding-errors: always

processus:
  agentEtat:
    schedulerDelay: 3000 # 30 secondes en millisecondes
```

⚠️ **Ne jamais commit les fichiers `.env` ou `application.yml` avec des vrais credentials**
→ Utiliser `application-example.properties` avec des valeurs factices
## Sécurité

### État actuel
⚠️ **Aucune authentification implémentée pour le moment**
- Pas de login/mot de passe
- Pas de JWT
- Tous les endpoints sont publics

### Prévu pour plus tard
- **SSO (Single Sign-On)** : Connexion via le système d'authentification du CESE
- **Rôles utilisateurs** :
  - `ADMIN` : DRH (peut créer des processus)
  - `DIRECTION` : Agents des directions (peut valider les tâches de sa direction)
  - `LECTURE` : Autres agents (consultation uniquement)
- **JWT** : Token d'authentification pour sécuriser les appels API

### Points d'attention pour l'implémentation future
- Ajouter un intercepteur Axios pour injecter le token JWT
- Protéger les endpoints Spring avec `@PreAuthorize`
- Gérer le refresh token pour éviter les déconnexions