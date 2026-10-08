# Guide des écrans

## Table des matières
1. [Dashboard des processus](#dashboard)
2. [Détail d'un processus](#detail-processus)
3. [Liste des agents actifs](#liste-agents)
4. [Liste des anciens agents](#anciens-agents)

---

## Dashboard


## 🏠 Dashboard des processus 

### Informations générales
- **Route** : `/dashboard`
- **Composant** : `src/components/dashboard/`
- **Accès** : Tous les utilisateurs connectés
- **Rôle** : Page d'accueil - Vue d'ensemble de tous les processus

### Aperçu visuel
[Screenshot du dashboard]

### Structure de la page

#### Header
- Titre : "Dashboard - Vue d'ensemble des processus d'entrée/sorties d'un agent"
- Boutons d'action pour les directions autorisées :
  - "Ajouter des Agents" (visible uniquement pour DRH)
  - "Ajouter des Prestataires" (visible pour DAPPI, DSIUN, DAF)
  - "Ajouter des Conseillers/Attachés" (visible pour DAF, DSIUN)

#### Filtres (au-dessus de la liste)
- **Type de processus** : Dropdown (Tous / Entrée / Sortie / Mobilité interne)
- **Recherche** : Barre de texte (recherche par nom/prénom d'agent, direction, statut)
- **Tri alphabétique** : Toggle A-Z / Z-A (optionnel)
- **Filtrage par rôle** : Dropdown (Tous / Agents / Conseillers / Prestataires)

#### Liste des processus
Format : Tableau

**Colonnes du tableau** :
| Colonne | Contenu | Exemple |
|---------|---------|---------|
| Agent | Nom + Prénom + Direction | "Dupont Jean / DAPPI" |
| Type | Badge coloré | 🟢 Entrée / 🔴 Sortie / 🟡 Mobilité |
| Statut | Badge coloré | "Ouverture des droits" |
| Date d'Échéance | Date | 15/04/2026 |
| Date de Mobilisation (arrivée/sortie) | Date | 15/03/2026 |


**Interactions** :
- Clic sur une ligne → Redirige vers `/processus/:id`


#### Pagination
- Si + de 10 processus : pagination sous format de page

---

### Appels API utilisés

#### 1. Récupération de tous les processus
```typescript
// Au chargement de la page
GET /api/processes
Query params optionnels : 
  ?type=ENTREE
  &status=OUVERTURE_DROITS
  &direction=DSIUN
  &search=Dupont

Réponse :
{
  "data": [
    {
      "id": "123",
      "type": "ENTREE",
      "status": "OUVERTURE_DROITS",
      "agent": {
        "nom": "Dupont",
        "prenom": "Jean",
        "direction": "DSIUN"
      },
      "dateArrivee": "2026-03-15",
      "tasksCompleted": 3,
      "tasksTotal": 5
    },
    // ... autres processus
  ],
  "message": "Success",
  "timestamp": "2026-01-29T10:00:00"
}
```

#### 2. Création d'un nouveau processus (DRH uniquement)
```typescript
// Quand on clique sur "Nouveau processus"
POST /api/processes

Body :
{
  "type": "ENTREE",  // ou "SORTIE" ou "MOBILITE"
  "agent": {
    "nom": "Martin",
    "prenom": "Sophie",
    "email": "sophie.martin@cese.fr",
    "direction": "DAF"
  },
  "dateArrivee": "2026-04-01",  // ou dateDepart pour une sortie
  "commentaire": "Stage 6 mois"
}

Réponse :
{
  "data": {
    "id": "456",
    "status": "FORMULAIRE_RH",
    // ... processus créé
  },
  "message": "Processus créé avec succès"
}
```

---

### Services utilisés
**Fichier** : `src/services/processService.ts`
```typescript
export const processService = {
  // Liste tous les processus avec filtres optionnels
  getAll: (filters?: ProcessFilters) => 
    api.get<Process[]>('/processes', { params: filters }),
  
  // Crée un nouveau processus
  create: (data: CreateProcessDto) => 
    api.post<Process>('/processes', data),
};
```

---

### Composants réutilisables utilisés

- `<ProcessCard>` : Carte d'un processus (si affichage en grille)
- `<ProcessTable>` : Tableau des processus (si affichage en tableau)
- `<FilterBar>` : Barre de filtres
- `<NewProcessDialog>` : Modal de création (Shadcn Dialog)
- `<StatusBadge>` : Badge coloré pour le statut
- `<ProcessTypeBadge>` : Badge pour le type (Entrée/Sortie/Mobilité)

---

### États React gérés
```typescript
const [processes, setProcesses] = useState<Process[]>([]);
const [loading, setLoading] = useState(true);
const [filters, setFilters] = useState<ProcessFilters>({
  type: null,
  status: null,
  direction: null,
  search: ''
});
const [isNewProcessDialogOpen, setIsNewProcessDialogOpen] = useState(false);
```

---

### Règles métier spécifiques

- ⚠️ **Seuls les agents DRH** voient le bouton "Nouveau processus"
- Les processus clôturés apparaissent en grisé ou avec un badge spécifique
- La barre de progression se calcule : `tasksCompleted / tasksTotal * 100`
- Tri par défaut : processus les plus récents en premier

---

### Cas d'erreur à gérer

1. **API inaccessible** : Afficher message "Impossible de charger les processus. Réessayez."
2. **Aucun processus** : Afficher "Aucun processus en cours" avec illustration
3. **Échec création** : Toast d'erreur "La création du processus a échoué"

---

---

## 📋 Détail d'un processus {#detail-processus}

### Informations générales
- **Route** : `/process/:id`
- **Composant** : `src/pages/ProcessDetail.tsx`
- **Accès** : Tous les utilisateurs
- **Rôle** : Afficher le détail d'un processus et gérer les tâches

### Aperçu visuel
[Screenshot ou description]

### Structure de la page

#### Section 1 : Informations de l'agent
**Bloc en haut de page - Card ou Panel**

Affiche :
- Photo (si disponible) ou avatar avec initiales
- Nom complet : "Jean Dupont"
- Email : jean.dupont@cese.fr
- Direction : DSIUN
- Type de processus : Badge "Entrée" / "Sortie" / "Mobilité"
- Date concernée :
  - Si entrée : "Date d'arrivée : 15/03/2026"
  - Si sortie : "Date de départ : 20/04/2026"
- Statut actuel du processus : Badge coloré avec le statut

**Si l'utilisateur est DRH** : Bouton "Modifier les infos agent"

---

#### Section 2 : Progression globale

**Barre de progression visuelle**
```
[████████░░░░░░░░] 8/12 tâches complétées (67%)
```

**Timeline des statuts** (optionnel mais joli)
```
✅ Formulaire RH      → Terminé le 10/01/2026
✅ Formulaire droits  → Terminé le 15/01/2026
🔄 Ouverture droits   → En cours
⏳ Clôture            → En attente
```

---

#### Section 3 : Liste des tâches par direction

**Format : Accordéons ou Cartes pliables**

Exemple pour une direction :
```
┌─────────────────────────────────────────────────────┐
│ 📁 DRH - Direction des Ressources Humaines          │
│    2/3 tâches complétées                            │
│                                                     │
│ ✅ Formulaire d'initialisation                      │
│    Complétée le 10/01/2026 par Marie Legrand        │
│                                                     │
│ ✅ Préparation du contrat                           │
│    Complétée le 12/01/2026 par Marie Legrand        │
│                                                     │
│ ⏳ Archivage du dossier                             │
│    [Bouton : Marquer comme terminée]                │
│    (visible uniquement si je suis agent DRH)        │
└─────────────────────────────────────────────────────┘
```

**Pour chaque direction** :
- DSIUN (Direction Système d'Information et Usages Numériques)
- DAPPI (Direction de l'Accueil, du Patrimoine et de la Prévention Incendie)
- DAF (Direction Administrative et Financière)
- DICI (Direction de l'Information et de la Communication Institutionnelle)
- DSC (Direction des Sessions et de la Communication)
- DRH (Direction des Ressources Humaines)
- Direction concernée (celle de l'agent)

**Icônes de statut des tâches** :
- ✅ Terminée (vert)
- 🔄 En cours (bleu)
- ⏳ À faire (gris)
- ⚠️ En retard (rouge - si date limite dépassée)

**Informations par tâche** :
- Nom de la tâche : "Créer le badge d'accès"
- Description (optionnel - si existe)
- Statut
- Date de complétion (si terminée)
- Qui l'a validée (si terminée)
- Bouton d'action :
  - Si tâche À FAIRE et que je suis de cette direction → "Marquer comme terminée"
  - Si tâche avec formulaire → "Remplir le formulaire"

---

#### Section 4 : Historique / Timeline (optionnel)

Liste chronologique des événements :
```
📅 29/01/2026 10:30 - Process créé par Marie Legrand (DRH)
📅 29/01/2026 11:00 - Tâche "Formulaire RH" validée par Marie Legrand
📅 30/01/2026 09:15 - Tâche "Formulaire droits" validée par Paul Durand (DSIUN)
📅 30/01/2026 14:20 - Tâche "Créer badge" validée par Sophie Martin (DAPPI)
```

---

#### Section 5 : Commentaires (optionnel - si implémenté)

Zone de discussion entre directions :
```
[Zone texte pour ajouter un commentaire]
[Bouton : Envoyer]

Historique :
💬 Marie Legrand (DRH) - 29/01 11:45
   "L'agent aura besoin d'un ordinateur portable pour télétravail"
   
💬 Paul Durand (DSIUN) - 29/01 14:20
   "Ok, MacBook Pro commandé"
```

---

### Appels API utilisés

#### 1. Récupération du détail du processus
```typescript
GET /api/processes/:id

Réponse :
{
  "data": {
    "id": "123",
    "type": "ENTREE",
    "status": "OUVERTURE_DROITS",
    "agent": {
      "id": "456",
      "nom": "Dupont",
      "prenom": "Jean",
      "email": "jean.dupont@cese.fr",
      "direction": "DSIUN",
      "photo": "https://..."
    },
    "dateArrivee": "2026-03-15",
    "dateCreation": "2026-01-29T10:00:00",
    "creePar": "Marie Legrand"
  }
}
```

#### 2. Récupération des tâches du processus
```typescript
GET /api/processes/:id/tasks

Réponse :
{
  "data": [
    {
      "id": "1",
      "nom": "Formulaire RH",
      "description": "Remplir les informations initiales",
      "status": "TERMINEE",
      "direction": "DRH",
      "dateCompletee": "2026-01-10T14:30:00",
      "completeeParNom": "Marie Legrand",
      "completeeParEmail": "marie.legrand@cese.fr"
    },
    {
      "id": "2",
      "nom": "Créer badge d'accès",
      "description": null,
      "status": "A_FAIRE",
      "direction": "DAPPI",
      "dateCompletee": null,
      "completeeParNom": null,
      "completeeParEmail": null
    },
    // ... autres tâches
  ]
}
```

#### 3. Marquer une tâche comme terminée
```typescript
PUT /api/tasks/:taskId/complete

Body : {}  // ou { "commentaire": "..." } si on permet des commentaires

Réponse :
{
  "data": {
    "id": "2",
    "status": "TERMINEE",
    "dateCompletee": "2026-01-29T15:45:00",
    "completeeParNom": "Sophie Martin"
  },
  "message": "Tâche validée avec succès"
}
```

#### 4. Modifier les infos de l'agent (DRH uniquement)
```typescript
PUT /api/processes/:id/agent

Body :
{
  "nom": "Dupont",
  "prenom": "Jean",
  "email": "jean.dupont@cese.fr",
  "dateArrivee": "2026-03-20"  // date modifiée
}

Réponse :
{
  "data": { ... },
  "message": "Informations mises à jour"
}
```

---

### Services utilisés
**Fichier** : `src/services/processService.ts` + `src/services/taskService.ts`
```typescript
// processService.ts
export const processService = {
  getById: (id: string) => 
    api.get<Process>(`/processes/${id}`),
  
  getTasks: (processId: string) => 
    api.get<Task[]>(`/processes/${processId}/tasks`),
    
  updateAgent: (processId: string, data: UpdateAgentDto) =>
    api.put<Process>(`/processes/${processId}/agent`, data),
};

// taskService.ts
export const taskService = {
  complete: (taskId: string) => 
    api.put<Task>(`/tasks/${taskId}/complete`),
};
```

---

### Composants réutilisables

- `<AgentInfoCard>` : Card avec infos de l'agent
- `<ProgressBar>` : Barre de progression
- `<StatusTimeline>` : Timeline des statuts (optionnel)
- `<DirectionTasksAccordion>` : Accordéon des tâches d'une direction
- `<TaskItem>` : Item d'une tâche (avec bouton validation)
- `<StatusBadge>` : Badge de statut

---

### États React gérés
```typescript
const { id } = useParams(); // ID du processus depuis l'URL
const [process, setProcess] = useState<Process | null>(null);
const [tasks, setTasks] = useState<Task[]>([]);
const [loading, setLoading] = useState(true);
const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
```

---

### Règles métier spécifiques

- ⚠️ **Je ne peux valider QUE les tâches de ma direction**
  - Si je suis agent DSIUN → je vois uniquement le bouton sur les tâches DSIUN
- Quand je valide une tâche :
  1. Appel API pour marquer la tâche terminée
  2. Rechargement des tâches pour voir le changement
  3. Toast de succès : "Tâche validée ✅"
  4. Si c'était la dernière tâche → Le statut du processus passe à "PROCESSUS_CLOTURE" automatiquement
- **Modification des infos agent** : Uniquement DRH + uniquement si processus pas encore clôturé

---

### Cas d'erreur

1. **Processus introuvable** : Afficher "Processus #123 introuvable" + bouton retour dashboard
2. **Échec validation tâche** : Toast "Impossible de valider la tâche. Réessayez."
3. **Pas les droits** : Bouton "Valider" désactivé avec tooltip "Vous n'êtes pas autorisé"

---

---

## 👥 Liste des agents actifs {#liste-agents}

### Informations générales
- **Route** : `/agents` ou `/agents/active`
- **Composant** : `src/pages/AgentsList.tsx`
- **Accès** : Tous les utilisateurs
- **Rôle** : Voir tous les agents actuellement en poste au CESE

### Structure de la page

#### Header
- Titre : "Agents actifs"
- Badge : Nombre total d'agents actifs (ex: "247 agents")

#### Filtres / Recherche
- **Recherche** : Input texte (nom, prénom, email)
- **Direction** : Dropdown (Toutes / DRH / DSIUN / ...)
- **Tri** : Dropdown (Nom A-Z / Nom Z-A / Date d'arrivée)

#### Liste des agents

**Format tableau** :
| Nom | Direction | Email | Date d'arrivée | Actions |
|-----|-----------|-------|----------------|---------|
| Dupont Jean | DSIUN | jean.dupont@cese.fr | 15/03/2024 | 👁️ |
| Martin Sophie | DAF | sophie.martin@cese.fr | 01/09/2023 | 👁️ |

Ou **Format cartes** (grid) :
```
┌───────────────────────┐  ┌───────────────────────┐
│ 👤 Jean Dupont        │  │ 👤 Sophie Martin      │
│ DSIUN                 │  │ DAF                   │
│ jean.dupont@cese.fr   │  │ sophie.martin@cese.fr │
│ Arrivé le 15/03/2024  │  │ Arrivée le 01/09/2023 │
│ [Voir détails]        │  │ [Voir détails]        │
└───────────────────────┘  └───────────────────────┘
```

**Au clic sur "Voir détails"** :
- Modal/Dialog avec plus d'infos :
  - Nom complet, email, téléphone
  - Direction, fonction
  - Date d'arrivée
  - Processus d'entrée associé (lien vers `/process/:id`)

---

### Appels API
```typescript
GET /api/agents?status=ACTIF

Query params optionnels :
  ?search=Dupont
  &direction=DSIUN
  &sort=nom_asc

Réponse :
{
  "data": [
    {
      "id": "456",
      "nom": "Dupont",
      "prenom": "Jean",
      "email": "jean.dupont@cese.fr",
      "direction": "DSIUN",
      "fonction": "Développeur",
      "dateArrivee": "2024-03-15",
      "processEntreeId": "123"  // ID du processus d'entrée
    },
    // ...
  ],
  "total": 247
}
```

---

### Services utilisés
```typescript
// src/services/agentService.ts
export const agentService = {
  getActive: (filters?: AgentFilters) => 
    api.get<Agent[]>('/agents', { 
      params: { status: 'ACTIF', ...filters } 
    }),
};
```

---

---

## 🗃️ Liste des anciens agents {#anciens-agents}

### Informations générales
- **Route** : `/agents/old` ou `/agents/inactive`
- **Composant** : `src/pages/OldAgentsList.tsx`
- **Accès** : Tous les utilisateurs
- **Rôle** : Archivage - Agents ayant quitté le CESE

### Différence avec liste agents actifs

**Presque identique** mais :
- Affiche les agents avec `status = INACTIF`
- Colonne supplémentaire : "Date de sortie"
- Titre : "Anciens agents" avec badge du nombre total

#### Appels API
```typescript
GET /api/agents?status=INACTIF

Réponse : même structure mais avec dateDepart au lieu de dateArrivee
```

---

---

## 🎨 **Éléments transversaux à tous les écrans**

### Navigation (présente sur tous les écrans)

**Sidebar ou Header avec menu** :
- 🏠 Dashboard
- 📋 Processus (même lien que Dashboard)
- 👥 Agents actifs
- 🗃️ Anciens agents
- ⚙️ Paramètres (si applicable)
- 👤 Mon profil

### Composants communs utilisés partout

- `<Navbar>` : Barre de navigation
- `<Sidebar>` : Menu latéral (optionnel)
- `<Breadcrumb>` : Fil d'Ariane (ex: Dashboard > Processus #123)
- `<LoadingSpinner>` : Loader pendant chargement des données
- `<EmptyState>` : Illustration "Aucun résultat"
- `<Toast>` : Notifications (succès/erreur)

---

---

## 🔔 **Système de notifications (si implémenté)**

### Cloche de notifications (en haut à droite)

Badge avec nombre de notifications non lues : 🔔 (3)

**Au clic** : Dropdown avec liste :
```
📬 Nouvelle tâche assignée
   "Créer badge d'accès pour Jean Dupont"
   Il y a 5 min

📬 Tâche validée
   "Paul Durand a validé 'Préparer matériel'"
   Il y a 1h

[Voir toutes les notifications]
```

**Appel API** :
```typescript
GET /api/notifications?unread=true

Réponse :
{
  "data": [
    {
      "id": "1",
      "message": "Nouvelle tâche assignée : Créer badge pour Jean Dupont",
      "type": "TASK_ASSIGNED",
      "lu": false,
      "dateCreation": "2026-01-29T10:00:00",
      "processId": "123",
      "taskId": "456"
    },
    // ...
  ]
}
```

**Marquer comme lue** :
```typescript
PUT /api/notifications/:id/read
```

---

---

## ✅ **Checklist complète SCREENS.md**

- [ ] Dashboard : structure + filtres + appels API + services
- [ ] Détail processus : sections + tâches par direction + validation
- [ ] Liste agents actifs : tableau + recherche + API
- [ ] Liste anciens agents : idem agents actifs
- [ ] Navigation commune à tous les écrans
- [ ] Composants réutilisables listés
- [ ] Système de notifications (si applicable)
- [ ] États React pour chaque page
- [ ] Règles métier spécifiques (qui peut faire quoi)
- [ ] Cas d'erreur à gérer

---

---

# 🛠️ **CODE_GUIDE.md - Contenu complet**

---

## **Structure du fichier**
```markdown
# Guide de code

## Table des matières
1. [Conventions de nommage](#conventions)
2. [Structure du projet](#structure)
3. [Frontend - React/TypeScript](#frontend)
4. [Backend - Spring Boot](#backend)
5. [Communication API](#api)
6. [Gestion des erreurs](#erreurs)
7. [Bonnes pratiques](#bonnes-pratiques)
8. [Points d'attention](#points-attention)

---

## 🏷️ Conventions de nommage {#conventions}

### Frontend (JavaScript/TypeScript)

| Type | Convention | Exemple |
|------|------------|---------|
| **Composants React** | PascalCase | `Dashboard.tsx`, `ProcessCard.tsx` |
| **Hooks custom** | camelCase + préfixe `use` | `useProcesses.ts`, `useAuth.ts` |
| **Services** | camelCase | `processService.ts`, `agentService.ts` |
| **Types/Interfaces** | PascalCase | `Process`, `Agent`, `Task` |
| **Constantes** | SCREAMING_SNAKE_CASE | `API_BASE_URL`, `MAX_FILE_SIZE` |
| **Fonctions** | camelCase | `fetchProcesses()`, `validateForm()` |
| **Variables** | camelCase | `isLoading`, `processData` |

### Backend (Java)

| Type | Convention | Exemple |
|------|------------|---------|
| **Classes** | PascalCase | `ProcessService`, `AgentController` |
| **Méthodes** | camelCase | `createProcess()`, `findById()` |
| **Variables** | camelCase | `processId`, `agentList` |
| **Constantes** | SCREAMING_SNAKE_CASE | `MAX_TASKS_PER_PROCESS` |
| **Packages** | snake_case minuscule | `com.cese.entree_sortie.domain.model` |
| **Interfaces** | PascalCase (pas de préfixe I) | `ProcessRepository`, `TaskService` |
| **Enums** | PascalCase | `ProcessStatus`, `TaskStatus`, `ProcessType` |

---

## 📁 Structure du projet {#structure}

### Frontend
```
src/
├── components/              # Composants réutilisables
│   ├── atoms/              # Composants atomiques (Shadcn)
│   │   ├── Button.tsx
│   │   ├── Input.tsx
│   │   └── Badge.tsx
│   ├── molecules/          # Composants composés
│   │   ├── ProcessCard.tsx
│   │   ├── TaskItem.tsx
│   │   └── FilterBar.tsx
│   └── organisms/          # Sections complètes
│       ├── ProcessTable.tsx
│       ├── DirectionTasksSection.tsx
│       └── Navbar.tsx
│
├── pages/                  # Pages principales (routes)
│   ├── Dashboard.tsx
│   ├── ProcessDetail.tsx
│   ├── AgentsList.tsx
│   └── OldAgentsList.tsx
│
├── services/               # Appels API
│   ├── api.ts             # Config Axios + intercepteurs
│   ├── processService.ts
│   ├── taskService.ts
│   ├── agentService.ts
│   └── notificationService.ts
│
├── types/                  # Types TypeScript
│   ├── process.types.ts
│   ├── task.types.ts
│   ├── agent.types.ts
│   └── api.types.ts
│
├── hooks/                  # Hooks custom
│   ├── useProcesses.ts
│   ├── useTasks.ts
│   └── useAuth.ts         # Pour plus tard
│
├── utils/                  # Fonctions utilitaires
│   ├── dateFormatter.ts   # Format des dates
│   ├── statusHelper.ts    # Traductions de statuts
│   └── validators.ts      # Validation de formulaires
│
├── constants/              # Constantes globales
│   ├── api.constants.ts   # URLs, endpoints
│   ├── status.constants.ts # Statuts possibles
│   └── directions.constants.ts # Liste des directions
│
├── App.tsx                 # Composant racine
├── main.tsx                # Point d'entrée
└── router.tsx              # Configuration des routes
```

**Pourquoi cette structure ?**
- **Atomic Design** : atoms → molecules → organisms (scalabilité)
- **Séparation des responsabilités** : 1 fichier = 1 rôle
- **Facile à naviguer** : On sait où chercher

---

### Backend (Architecture Hexagonale)
```
com.cese.entree_sortie/
│
├── domain/                      # ❤️ CŒUR MÉTIER (ne dépend de rien)
│   ├── model/                   # Entités métier
│   │   ├── Agent.java
│   │   ├── Process.java
│   │   ├── Task.java
│   │   └── Notification.java
│   │
│   ├── enums/                   # Énumérations
│   │   ├── ProcessStatus.java
│   │   ├── ProcessType.java
│   │   ├── TaskStatus.java
│   │   └── Direction.java
│   │
│   └── port/                    # Interfaces (contrats)
│       ├── in/                  # Ports d'entrée (use cases)
│       │   ├── ProcessService.java
│       │   ├── TaskService.java
│       │   └── AgentService.java
│       │
│       └── out/                 # Ports de sortie (repositories)
│           ├── ProcessRepository.java
│           ├── TaskRepository.java
│           └── AgentRepository.java
│
├── application/                 # 🧠 LOGIQUE MÉTIER
│   └── service/                 # Implémentation des use cases
│       ├── ProcessServiceImpl.java
│       ├── TaskServiceImpl.java
│       └── AgentServiceImpl.java
│
└── infrastructure/              # 🔌 ADAPTERS (accès externe)
    ├── web/                     # API REST (Controllers)
    │   ├── ProcessController.java
    │   ├── TaskController.java
    │   ├── AgentController.java
    │   └── dto/                 # Data Transfer Objects
    │       ├── CreateProcessDto.java
    │       ├── UpdateAgentDto.java
    │       └── ProcessResponseDto.java
    │
    ├── persistence/             # Base de données (JPA)
    │   ├── entity/              # Entités JPA (mapping BDD)
    │   │   ├── ProcessEntity.java
    │   │   ├── TaskEntity.java
    │   │   └── AgentEntity.java
    │   │
    │   ├── repository/          # Repositories JPA
    │   │   ├── ProcessJpaRepository.java
    │   │   ├── TaskJpaRepository.java
    │   │   └── AgentJpaRepository.java
    │   │
    │   └── adapter/             # Implémentation des ports
    │       ├── ProcessRepositoryAdapter.java
    │       ├── TaskRepositoryAdapter.java
    │       └── AgentRepositoryAdapter.java
    │
    └── config/                  # Configuration Spring
        ├── CorsConfig.java
        ├── JpaConfig.java
        └── BeanConfiguration.java
```

**Règles d'or de l'hexagonal** :
1. Le **domain** ne dépend de RIEN (pas de Spring, pas de JPA)
2. Les dépendances vont toujours **vers le domain** :
```
   Infrastructure → Application → Domain