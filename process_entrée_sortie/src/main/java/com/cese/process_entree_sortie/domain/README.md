# Domaine (Backend)

Cette couche contient le modele metier pur: entites, value objects, services de domaine et erreurs.

## Contenu principal
- `Agent/`, `Direction/`, `Notification/`, `Processus/`, `Tache/`: agregats et services metier.
- `utils/ValueObject/`: value objects reutilisables (ex: identifiants, dates, etc.).
- `utils/error/`: erreurs metier et exceptions.

## Regles
- Pas de dependance vers `application` ou `infrastructure`.
- Logique metier concentree ici, testable sans framework.
