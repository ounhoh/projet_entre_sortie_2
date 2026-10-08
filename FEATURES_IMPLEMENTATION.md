# Documentation des Nouvelles Fonctionnalités

## Vue d'ensemble

L'application inclut plusieurs fonctionnalités avancées au-delà des processus de base (entrée/sortie/mobilité). Ce document documente toutes les fonctionnalités implémentées et disponibles dans l'application.

---

## 📢 1. Système de Notifications Avancé

### Description
Un système complet de notifications permettant de tenir informées les directions des changements d'état des processus et des tâches.

### Fonctionnalités principales

#### 1.1 Types de notifications
- **Alerte** : Notifications critiques (tâches en retard, problèmes urgents)
- **Information** : Notifications standard (processus lancés, tâches complétées)
- **Rappel** : Notifications de rappel planifiées

#### 1.2 Gestion des notifications
- Compteur de notifications non lues dans la navbar
- Marquage de notifications comme lues (individuels ou en masse)
- Suppression de notifications
- Filtrage par direction
- Recherche textuelle

#### 1.3 Interface utilisateur
- **Composant** : `Notification.tsx` dans `src/components/NavBar/`
- **Tableau** : `TableauNotification.tsx`
- Affichage groupé par agent avec expansion/collapse
- Animation des listes (`AnimatedList`, `AnimatedListItem`)

### Endpoints API

```typescript
// Récupération des notifications non lues
GET /api/notifications/{agentId}

// Marquage comme lu
PUT /api/notifications/{notificationId}/read
PUT /api/notifications/read-all/{agentId}

// Suppression
DELETE /api/notifications/{notificationId}

// Notifications de rappel
GET /api/notifications/rappel/{agentId}
POST /api/notifications/rappel
```

### Exemple d'utilisation
```typescript
import { notificationService } from 'src/service/notificationService';

// Récupérer les notifications non lues
const notifications = await notificationService.getUnread(agentId);

// Marquer comme lu
await notificationService.markAsRead(notificationId);

// Créer un rappel
await notificationService.createReminder({
  agentId,
  message: "Passage de mobilité interdomaine",
  level: "alerte"
});
```

---

## ⚙️ 2. Moteur de Règles (Rule Engine)

### Description
Un système déclaratif permettant de définir des actions complexes via JSON plutôt que du code.

### Fonctionnalités principales

#### 2.1 Actions supportées
- `CREATE_AGENT` : Créer un nouvel agent
- `ENVOYER_EMAIL` : Envoyer une notification par email
- `CHANGE_STATUT` : Changer le statut d'un processus
- `GENERER_GROUPE_TACHES` : Générer automatiquement des groupes de tâches
- `ACTIVER_AGENT` : Activer un agent (passage de "entrée" à "actif")

#### 2.2 Templating
Les actions supportent les variables dynamiques :
```json
{
  "type": "ENVOYER_EMAIL",
  "destinataire": "${processus.agent.email}",
  "sujet": "Processus ${processus.type} lancé",
  "corps": "L'agent ${processus.agent.prenom} a été ajouté le ${processus.dateCreation}"
}
```

#### 2.3 Fichiers de configuration
- Actions définies dans le contenu JSON des tâches
- Sérialisées via `com.fasterxml.jackson.databind.ObjectMapper`
- Extensible : ajouter de nouveaux handlers sans modifier le code core

### Exemple de configuration dans une tâche

```json
{
  "actions": [
    {
      "type": "GENERER_GROUPE_TACHES",
      "templates": ["ouverture_droits_dsiun", "ouverture_droits_dappi"],
      "processusId": "${processus.id}"
    },
    {
      "type": "ENVOYER_EMAIL",
      "destinataires": "${processus.agent.direction.emails}",
      "sujet": "Processus ouverture des droits lancé"
    }
  ]
}
```

### Implications pour la maintenance
- ✅ Flexibilité : Ajouter des actions sans redéployer
- ✅ Séparation des responsabilités : Domaine métier séparé de l'infra
- ✅ Testabilité : Actions indépendantes
- ✅ Réutilisabilité : Mêmes actions dans différents contextes

---

## 🔗 3. Système de Dépendances entre Tâches

### Description
Gestion des dépendances entre tâches avec blocage automatique et visualisation en arbre.

### Fonctionnalités principales

#### 3.1 Fonctionnalités de base
- Création de dépendances directes : Tâche A → Tâche B
- Suppression de dépendances
- Blocage automatique des tâches dépendantes

#### 3.2 Visualisation
- **Arbre de dépendances** : Visualisation hiérarchique
- **Filtrage d'arbre** : Filtrer par direction ou statut
- **Récupération récursive** : Toutes les dépendances d'une tâche

#### 3.3 États de blocage
- `BLOQUE` : Tâche en attente de dépendances
- `PRET` : Tâche debloquée (dépendances résolues)
- `EN_COURS` : Tâche en cours
- `FAIT` : Tâche complétée

### Endpoints API

```typescript
// Gestion des dépendances
POST /api/dependances
DELETE /api/dependances/{dependanceId}

// Récupération
GET /api/dependances/processus/{processusId}
GET /api/dependances/tache/{tacheId}

// Visualisation
GET /api/dependances/processus/{processusId}/arbre
GET /api/dependances/processus/{processusId}/arbre-filtre?direction=DSIUN
```

### Organigramme d'une dépendance

```
Tâche A (entrée mail)
  ├── Tâche B (création accès PAO) ← Dépend de A
  └── Tâche C (paramétrage proxy) ← Dépend de A
      └── Tâche D (transmission IP) ← Dépend de C
```

Tant que Tâche A n'est pas complétée, les Tâches B et C restent bloquées.

---

## 👥 4. Système d'Affectations d'Agents

### Description
Historique complet des mouvements d'agents entre directions/services avec gestion des responsables et agents d'accueil.

### Fonctionnalités principales

#### 4.1 Données d'affectation
- Agent affecté
- Direction et Service
- Agent responsable de l'agent
- Agent en charge de l'accueil
- Dates de début et fin

#### 4.2 Fonctionnalités
- Création d'affectations
- Modification d'affectations existantes
- Historique complet par agent
- Récupération de l'affectation active
- Activation/Désactivation d'affectations
- Assignation de responsable
- Assignation d'agent d'accueil

### Endpoints API

```typescript
// Gestion
POST /api/affectations
PUT /api/affectations/{affectationId}
DELETE /api/affectations/{affectationId}

// Récupération
GET /api/affectations/agent/{agentId}
GET /api/affectations/agent/{agentId}/active
GET /api/affectations/historique/{agentId}

// Activation/Désactivation
PUT /api/affectations/{affectationId}/activate
PUT /api/affectations/{affectationId}/deactivate

// Assignation
PUT /api/affectations/{affectationId}/responsable/{responsableId}
PUT /api/affectations/{affectationId}/accueil/{accueilAgentId}
```

### Cas d'usage

**Arrivée d'un agent DSIUN :**
```json
{
  "agentId": "123",
  "direction": "DSIUN",
  "service": "Infrastructure",
  "dateDebut": "2026-03-15",
  "responsableId": "456",
  "accueilAgentId": "789"
}
```

---

## 📝 5. Formulaires Dynamiques Avancés

### Description
Système flexible de création de formulaires avec support de multiples types de champs et validations.

### Fonctionnalités principales

#### 5.1 Types de champs supportés (9 types)

| Type | Description | Exemple |
|------|-------------|---------|
| `text` | Champ texte simple | Nom, Prénom |
| `date` | Sélecteur de date | Date d'arrivée |
| `file` | Upload de fichier | CV, Justificatif |
| `select` | Dropdown simple | Direction, Service |
| `liste` | Sélection multiple | Compétences |
| `texte` | Longue texte (textarea) | Description, Commentaires |
| `email` | Champ email validé | Email professionnel |
| `image` | Upload d'image | Photo de profil |
| `input` | Champ texte numérique | Bureau, Extension |

#### 5.2 Validations supportées
```typescript
{
  "type": "text",
  "minLength": 3,
  "maxLength": 100,
  "pattern": "^[A-Za-z ]*$"  // Regex
}

{
  "type": "date",
  "minDate": "2026-01-01",
  "maxDate": "2026-12-31"
}

{
  "type": "file",
  "mimeType": ["application/pdf", "image/png"]
}

{
  "type": "email",
  "alreadyExist": true  // Vérification d'unicité
}
```

#### 5.3 Sources de données
- **Statiques** : Options codées en dur
- **Dynamiques** : Requête API pour récupérer options
- **Base de données** : Agents, Directions, Services

Exemple de source dynamique :
```json
{
  "type": "select",
  "options": "${api:agents?role=DIRECTEUR&direction=DSIUN}",
  "displayField": "prenom nom",
  "valueField": "id"
}
```

#### 5.4 Types spéciaux
- `value` : Réparation d'auto-valeurs depuis la base de données
- `etat_changement` : Suivi des changements d'état d'agent

### Composants

```typescript
// Affichage et édition
import FormulaireDynamique from '@/components/Formulaire/FormulaireDynamique';

// Lecture seule
import FormulaireReadOnly from '@/components/Formulaire/FormulaireReadOnly';
```

---

## 🔍 6. Recherche et Filtrage Avancés

### Description
Système complet de recherche multi-colonnes avec filtrage par rôle et tri personnalisé.

### Fonctionnalités principales

#### 6.1 Recherche
- Recherche multi-colonnes sur objets imbriqués
- Case-insensitive avec trim automatique
- Recherche en texte libre combinée à des filtres

#### 6.2 Filtrage
- **Type de processus** : Entrée/Sortie/Mobilité
- **Rôle d'agent** : Agent/Conseiller/Prestataire/Administrateur
- **Direction**
- **Statut de tâche ou processus**

#### 6.3 Tri
- **Alphabétique A-Z / Z-A** : Toggle pour inverser l'ordre
- **Par date** : Processus les plus récents en premier
- **Par statut** : Regroupement par statut

#### 6.4 Pagination
- Calcul automatique de la taille des pages
- Navigation fluide
- Nombre de résultats par page configurable

### Exemple d'API

```typescript
GET /api/processes?search=Dupont&type=ENTREE&direction=DSIUN&status=OUVERTURE_DROITS&sort=dateCreation:DESC&page=0&size=10
```

---

## 👨‍💼 7. Modes de Processus (Conseiller/Prestataire)

### Description
Support de création de processus spécifiques aux conseillers/attachés et prestataires.

### Fonctionnalités principales

#### 7.1 Trois types d'agents
- **Agent régulier** : Créé par DRH, avec contrat standard
- **Conseiller/Attaché** : Créé par DAF/DSIUN, statut temporaire
- **Prestataire** : Créé par DAPPI/DSIUN/DAF, contrats externes

#### 7.2 Routes associées
- `/process_entree?mode=conseiller` : Création de conseiller
- `/process_entree?mode=prestataire` : Création de prestataire
- `/process_entree` : Création d'agent régulier (défaut)

#### 7.3 Différences dans les processus
- Formulaires adapta au type d'agent
- Permissions de lancement de sortie spécifiques
- Statuts et états différents

### Permissions de lancement sortie

| Type d'agent | Directions autorisées |
|------------|---------------------|
| Agent régulier | DRH |
| Conseiller | DAF, DSIUN |
| Prestataire | DAPPI, DSIUN, DAF |

---

## 📊 8. Système d'Avancement

### Description
Calcul automatique de l'avancement d'un processus et des groupes de tâches.

### Fonctionnalités principales

#### 8.1 Métriques
- Pourcentage d'avancement global du processus
- Nombre de tâches complétées / total
- Avancement par groupe de tâches
- Avancement par direction

#### 8.2 Endpoints API

```typescript
// Avancement global d'un processus
GET /api/avancement/processus/{processusId}

// Avancement d'un groupe de tâches
GET /api/avancement/groupe/{groupeTacheId}

Response:
{
  "processusId": "123",
  "totalTaches": 12,
  "tachesCompletes": 8,
  "pourcentage": 66.67,
  "grouped": {
    "DSIUN": { "total": 3, "completes": 2, "pourcentage": 66.67 },
    "DAPPI": { "total": 3, "completes": 2, "pourcentage": 66.67 },
    "DAF": { "total": 3, "completes": 2, "pourcentage": 66.67 },
    "DRH": { "total": 3, "completes": 2, "pourcentage": 66.67 }
  }
}
```

#### 8.3 Mise à jour automatique
- Recalcul lors de chaque complétion de tâche
- Affichage temps réel dans l'UI
- Progression visuelle avec barres de progression

---

## 👴 9. Gestion des Anciens Agents

### Description
Affichage et gestion séparée des agents qui ont quitté l'organisation.

### Fonctionnalités principales

#### 9.1 Identification
- État `ancien` : Agents sortis de l'organisation
- Détection via `etatAgent === "ancien"` ou `dateSortie`
- Séparation automatique de la liste active

#### 9.2 Interface
- **Composant** : `TableauAncienAgent.tsx`
- Mêmes filtres/tri que la liste active
- Distinction visuelle (grisé, badge "Ancien")
- Accès en lecture seule après sortie

#### 9.3 Données conservées
- Historique complet d'affectations
- Tous les processus de sortie
- Notifications historiques

---

## 📋 10. Services Avancés

### 10.1 Recherche d'agents (`SearchAgentsService`)

```typescript
POST /api/agents/search

{
  "searchTerm": "Jean",
  "role": "CONSEILLER",
  "direction": "DSIUN",
  "limit": 100
}

Response: Agent[]
```

### 10.2 Gestion de rôles et emails

```typescript
// Modifier le rôle d'un agent
PUT /api/agents/{agentId}/role
{ "role": "CONSEILLER" }

// Modifier l'email d'un agent
PUT /api/agents/{agentId}/email
{ "email": "nouveau@cese.fr" }
```

### 10.3 Service d'édition de templates

- Édition complète des templates processus
- Gestion des groupes et statuts
- Duplication pour versioning
- Activation/Désactivation

### 10.4 Groupes de tâches prêts à démarrer

```typescript
GET /api/groupes/prets-a-demarrer/{processusId}
GET /api/groupes/bloques/{processusId}
```

---

## 🔒 11. Permissions Frontend (Hardcodées)

### Description
Système de permissions basé sur la direction pour contrôler l'accès aux fonctionnalités.

### Permissions actuelles

#### 11.1 Ajout d'agents
- **DRH** peut ajouter des agents réguliers

#### 11.2 Ajout de prestataires
- **DAPPI**, **DSIUN**, **DAF** peuvent ajouter des prestataires

#### 11.3 Ajout de conseillers/attachés
- **DAF**, **DSIUN** peuvent ajouter des conseillers/attachés
- **SG** (récemment ajouté) peut aussi ajouter des conseillers

#### 11.4 Lancement de sortie
- **Agents réguliers** : Seul DRH
- **Prestataires** : DAPPI, DSIUN, DAF
- **Conseillers** : DAF, DSIUN

### Fichiers concernés

- `src/components/NavBar/AjouterAgent.tsx`
- `src/components/contenuPage/agent/AgentDetailPage.tsx`

### ⚠️ Note de maintenance
Ces permissions sont actuellement hardcodées. Pour une maintenance future :
- Considérer une approche data-driven
- Centraliser les configurations de permissions
- Voir `HARDCODED_PERMISSIONS_MAINTENANCE.md` pour détails

---

## 📚 Intégrateurs &  Mappers Utilitaires

### 10.1 Mappers
- **agentMapper** : Conversion DTO ↔ Affichage
- **processusMapper** : Normalisation des données
- **directionDisplay** : Affichage formaté des directions

### 10.2 Utilitaires
- **currentAgent** : Gestion de l'agent connecté
- Extraction et parsing des matériaux d'agent
- Cache 5-min sur les énumérations

---

## 🔄 Workflow complet d'exemple

### Processus d'entrée d'un conseiller

1. **DAF lance une création de conseiller**
   - Clique sur "Ajouter des Conseillers/Attachés"
   - Remplit formulaire (mode=conseiller)
   - ✅ Affectation créée

2. **Système de notifications**
   - Tous les groupes de tâches reçoivent une notification
   - Badge "1 notification non lue" s'affiche

3. **Gestion des dépendances**
   - Certaines tâches sont bloquées par d'autres
   - ⛓️ Indicateur visuel du blocage

4. **Avancement en temps réel**
   - Barre de progression se met à jour
   - Calcul automatique du pourcentage

5. **Clôture du processus**
   - Moteur de règles exécute les actions finales
   - Agent passe à l'état "actif"
   - Notifications envoyées à tous

---

## 📖 Documentation complémentaire

- **ARCHITECTURE.md** : Architecture complète de l'application
- **FRONTEND_PERMISSIONS_MAPPING.md** : Détails des permissions frontend
- **HARDCODED_PERMISSIONS_MAINTENANCE.md** : Guide de maintenance des permissions
- **MOTEUR_REGLES_GAINS.md** : Documentation détaillée du moteur de règles
