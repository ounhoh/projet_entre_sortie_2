# Infrastructure (Backend)

Cette couche connecte l'application au monde exterieur: web, configuration, persistence.

## Contenu principal
- `web/controller/`: endpoints REST (adaptateurs entrants par les appels user).
- `persistence/`: entities JPA, repositories, adaptateurs sortants.
- `config/`: configuration Spring, CORS, securite, initialisation de donnees.

## Regles
- Implemente les ports `out` definis dans `application`.
- Peut dependre de frameworks (Spring, JPA).
