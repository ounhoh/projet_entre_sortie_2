# Projet Entrée / Sortie.

## À propos

Dans une volonté de standardiser le processus d'entrée/sortie actuel des agents, la DSIUN s'est proposée d'offrir une application web dédiée au traitement de l'entrée, la sortie et la mobilité interne des agents. Vous trouverez dans ce dépôt toutes les informations nécessaires afin de découvrir et apporter votre contribution au projet.

## Table des matières
- [À  propos]()
- [🔗 Prérequis]()
- [Installation]()
- [🎯 Utilisation]()
- [Contribution]()
- [Construit avec]()
- [📜 Documentation]()
- [📥 Démarrage rapide]()
- [📥 Liens utiles]()

## Prérequis
  - Node.js
  - npm
  - Java 21+ (backend Spring Boot)
  - PostgreSQL
  - Git
  - Vite
  - Maven (Optionnel)
### Documentation
Backend:
  -  [Architecture Hexagonale](https://blog.octo.com/architecture-hexagonale-trois-principes-et-un-exemple-dimplementation)
  - [Spring Boot](https://spring.io/projects/spring-boot)
Frontend: 
  -  [Shadcn](https://ui.shadcn.com/) (pour les composants ui)
  - [magicui](https://magicui.design/) (pour les composants avec effets)
  - [tweakcn](https://tweakcn.com/) (pour les couleurs et la typographie du site)

### Backend
##### 1) Configurer la base de données PostgreSQL
      - DB: "projet_entre_sortie"
      - user: "entre_sortie_user"
      - password: "user"
#### 2) Lancer le backend
    - cd process_entrée_sortie
    - mvnw spring-boot:run (sans Maven)
    - mvn spring-boot:run  (avec Maven)
### Frontend
##### Lancer le Frontend
    - cd frontEnd-projet-entre-sortie
    - npm run dev
## Installation

Installer du frontEnd avec npm

```bash
  npm install frontEnd-projet-entre-sortie
  cd frontEnd-projet-entre-sortie
```
    
## Utilisation

1) Démarrer le backend et le frontend (voir section Installation).
2) Ouvrir l'application dans le navigateur: `http://localhost:5173`.
3) Accéder aux écrans principaux:
   - Tableau de bord des processus
   - Détail d'un agent
   - Détail d'un processus
   - Suivi des tâches et formulaires
   - Notifications et rappels
4) Créer ou démarrer un processus depuis un template (entrée, sortie, mobilité interne).
5) Suivre l'avancement et finaliser les tâches.
## Contributions
-- Maquettage, création de la base de données, développement de la base de l'application et choix de la charte graphique des couleurs --
- Marcel Pecqueux
- Henri Eke Priso

-- Continuité du développement, développement des nouvelles fonctionnalités, évolution de l'application, finalisation d'une première version entièrement fonctionnelle et déploiement --
- Matthew Launay

## Construit avec

### Langages & Frameworks

#### Backend
- Java 21
- Spring Boot (Web, Security, Data JPA)
- PostgreSQL
- Springdoc OpenAPI (Swagger)

#### Frontend
- React
- TypeScript
- Vite
- Tailwind CSS
- shadcn/ui + Radix UI
- React Router
- ANouvelles fonctionnalités

L'application inclut plusieurs fonctionnalités avancées :

- **Système de notifications** : Notifications avec niveaux (alerte, information, rappel), compteur de notifications non lues, marquage en masse
- **Moteur de règles** : Actions déclaratives en JSON pour exécution flexible des transitions de processus
- **Dépendances de tâches** : Arbre de dépendances, blocage automatique des tâches dépendantes
- **Affectations d'agents** : Historique complet des mouvements, suivi des responsables et agents d'accueil
- **Formulaires dynamiques** : Support de 9 types de champs avec validations complexes
- **Recherche et filtrage avancés** : Multi-colonnes, tri alphabétique, filtrage par rôle
- **Modes de processus** : Création de conseillers/attachés et prestataires en plus des agents réguliers
- **Anciens agents** : Gestion séparée des agents sortis

Pour plus de détails, voir FEATURES_IMPLEMENTATION.md et la section Documentation ci-dessous.

## Démarrage rapide
1. Architecture → voir ARCHITECTURE.md
2. Base de données → voir DATABASE.md
3. Guide des écrans → voir SCREEN.md
4. Nouvelles fonctionnalités → voir FEATURES_IMPLEMENTATION.md
5. Permissions frontend → voir FRONTEND_PERMISSIONS_MAPPING.md
## Liens utiles
- Maquettes écran Figma : https://www.figma.com/design/u9H2zjApFRb3oYlobKoddk/wireframe-entr%C3%A9e-sortie?node-id=1-2&t=ApZupikDs4TAqr8p-1
- Diagrammes cas d'utilisation : voir Diagrammes de cas d'utilisation/
- Modèles de donnéesgrammes cas d'utilisation : voir /docs/annexes/
- MCD : voir DATABASE.md