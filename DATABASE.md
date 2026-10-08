# Base de données
## Collecte des données
Cette étape est la première dans le processus de création d'une base de données avec la méthode Merise. Sa valeur est donc très importante car c'est cette collecte des données qui va être à la base de tout le reste du travail. Ainsi, pour effectuer cette collecte, nous avons dû analyser au maximum toutes les informations qui nous ont été données. Cela se traduit par une lecture pointilleuse de tous les éléments présents sur la maquette et le diagramme de cas d'utilisation ainsi que la description du projet.

### Identification des acteurs
Acteur
Rôle
Direction
remplir leurs tâche
DRH/ADMIN
créer et remplir leurs tâches
Agent
doit physiquement participer au tâche

### 2.Collecte des documents existant
Document
Description
Donnée principal
Formulaire d’information de l’agent
élément contenant des informations sur l’agent, tant sur le plan personnel que professionnel
état, direction gérant le formulaire, contenue (ensembles des informations à soumettre obligatoire comme optionnelle) ,date d’échéance
Tâche de Direction
tâche que doit réaliser une direction
état, nom, description, direction
Notification
notification pour information une direction
niveau, date d’envoie, agent concernée, direction ayant envoyée la notification, description de la notification
Ensemble de tâche
état regroupant un ensemble de tâche à faire
statue, nom, date d’échéance, ensemble des tâches, direction

### Observation et entretien
Informations à noter pour chaque élément :
- Visualisation de l'état du processus tant entrée que sortie et mobilité interne
- Processus passant d'un statut à un autre, entrée et mobilité interne: (initialisation via un formulaire, formulaire de la direction concernée, réalisation des tâches, fin), sortie (initialisation via un formulaire, réalisation des tâches et fin)
- Date d'échéance à respecter
- Identification d'un agent selon les éléments mis dans son formulaire: nom, prénom, direction et fonction
- Une fois la création ou la mobilité interne de l'agent effectue, on le met dans la liste des agents actifs et si l'agent décide de partir, on le met dans la liste des anciens agents
- La personne qui commence la procédure est la RH/ADMIN et uniquement pour le cas de sortie
### Fiche de relevé des informations
Formulaire entrée -RH
Elément
Information
Nom du de la procédure
Formulaire d’entrée -RH
Acteur concernée
RH
Support Envisagé
Formulaire web
Objectif
Remplir les informations personnel du nouvel agent
Donnée relevée
date d’entrée, date de sortie, nom de l’agent, prénom de l’agent, date d’arrivée, date de départ, statut

Formulaire de sortie -RH
Elément
Information
Nom du de la procédure
Formulaire de sortie -RH
Acteur concernée
RH
Support Envisagé
Formulaire web
Objectif
remplissage des information personnel de sortie de l’agent
Donnée relevée
nom, prénom, date de sortie, direction statut

Formulaire d’entrée - direction concernée
Elément
Information
Nom du de la procédure
Formulaire d’entrée
Acteur concernée
Direction concernée
Support Envisagé
Formulaire web
Objectif
remplissage des information professionel de de l’agent
Donnée relevée
fonction, service, responsable, agent chargé de l’acceuil, application, application, liste des diffusions, matériels nécessaire, direction à mettre dans le processus

Tâche
Elément
Information


Nom du de la procédure
Tâche


Acteur concernée
Direction concernée par la tâche


Support Envisagé
dématérialisé


Objectif
réalisé la tâche


Donnée relevée
nom, statut, description, état, contenu



Ensemble de tâche
Elément
Information
Nom du de la procédure
Ensemble de tâche
Acteur concernée
Direction concernée par cet ensemble des tâches
Support Envisagé
dématérialisé
Objectif
réalisé l’ensemble des tâches dans cet ensemble
Donnée relevée
nom, statut, date d’échéance

Notification
Elément
Information
Nom du de la procédure
Notification
Acteur concernée
Direction concernée par notification
Support Envisagé
dématérialisé
Objectif
direction concernée est obtenu l’information
Donnée relevée
niveau, date d’envoie, direction expéditrice, description de la notification

Processus
Elément
Information
Nom du de la procédure
Entrée/sortie/Mobilité Interne
Acteur concernée
toutes les directions appelées
Support Envisagé
dématérialisé
Objectif
que l’agent est tout a sa disposition pour sortir ou rentrée dans l’établissement
Donnée relevée
agent, statue, type de processus, date de création du processus, l’ensemble des tâches, et

### Synthèse des données
Entité Potentielle
Attributs trouvés
Agent
nom, prénom, date_d’arrivée, date_départ, statut, direction, service, fonction
processus
type(entrée/sortie/mobilité interne), statut, date_création, agent_concernée
Tâche
nom, description, contenu,statut, direction_concernée, groupe_tâche, date_échéance_groupe, type(formulaire/tâche), ordre
Notification
niveau, date_envoie, direction_expéditrice, direction_destinataire, description, agent_concernée, statut_lecture , type_evenement(tache finie,processus_lance,delai_depasse)
Direction
nom, code,role
Role
role

## Dictionnaire des données
[voir document dans dossier documents/bdd]
## MCD
[voir document dans dossier documents/bdd] (à utiliser sur draw.io)

## MLD
[voir document dans dossier documents/bdd] (à utiliser sur draw;io)

## Normalisation de MLD
1FN (Première Forme Normale) Pour qu'une table soit en 1NF, elle doit respecter les conditions suivantes : ● Les colonnes doivent avoir des noms uniques. ● Les valeurs dans chaque colonne doivent être atomiques, c'est-à-dire indivisibles.
2FN (Deuxième Forme Normale) Pour être en 2FN :
● La table doit déjà être en 1FN. ● Tous les attributs non-clés doivent être entièrement fonctionnellement dépendants de toute la clé primaire et non une partie de la clé primaire
3FN (Troisième Forme Normale) Pour être en 3FN :
● La table doit être en 2FN. ● Elle ne doit pas avoir de dépendances transitives, où un attribut non-clé dépend d'un autre attribut non-clé.
## MPD
[voir document dans dossier documents/bdd]

