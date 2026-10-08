# Application (Backend)

Cette couche orchestre les cas d'usage. Elle expose des ports d'entree (interfaces) et s'appuie sur des ports de sortie pour acceder aux donnees.

## Contenu principal
- `port/in/`: API d'application (interfaces d'entree).
- `port/out/`: interfaces d'acces aux donnees et services externes.
- `service/`: implementations des cas d'usage.
- `dto/`: objets de transfert pour les entrees/sorties.

## Regles
- Depend uniquement du `domain`.
- Aucune reference directe aux frameworks ou a la persistence.
