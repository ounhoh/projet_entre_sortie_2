# Frontend - Projet Entree / Sortie

Application React + TypeScript (Vite) pour piloter les processus d'entree, sortie et mobilite interne.  
Elle consomme le backend via des services HTTP centralises dans `src/service`.

## Structure du dossier
- `src/components/`: composants UI et pages (tableaux, formulaires, layout, etc.).
- `src/service/`: services d'acces API (agents, processus, taches...).
- `src/utils/`: mappers et helpers de presentation.
- `src/lib/axios.ts`: client HTTP (baseURL, interceptors).
- `src/context/` et `src/hooks/`: etat global et hooks.

## Fonctionnement (haut niveau)
- Les pages composent des composants UI et appellent les services.
- Les services appellent l'API backend et retournent des DTOs.
- Les mappers adaptent les DTOs au format attendu par les composants.

## Signatures des methodes (services)

### `agentService`
```ts
getAgents(page?: number, size?: number, sortBy?: string, direction?: string): Promise<AgentDTO[]>
getAllAgents(size?: number, sortBy?: string, direction?: string): Promise<AgentDTO[]>
getAllAgentsActifs(size?: number, sortBy?: string, direction?: string): Promise<AgentDTO[]>
getAgentById(id: string): Promise<AgentDTO>
getAgentByCode(code: string): Promise<AgentDTO>
getAgentMateriel(id: string): Promise<Record<string, any>>
getAgentDroits(id: string): Promise<string[]>
getAgentMaterielOnly(id: string): Promise<string[]>
getAgentDiffusions(id: string): Promise<string[]>
getAffectationActive(id: string): Promise<Record<string, any> | null>
```
Fonctionnement: cache local des listes d'agents, normalisation des payloads "materiel" et filtrage des agents actifs.

### `directionService`
```ts
getDirections(): Promise<DirectionDTO[]>
```
Fonctionnement: recuperation de la liste des directions.

### `notificationService`
```ts
getNotificationsByAgent(agentId: string): Promise<NotificationFormatted[]>
getUnreadNotificationsByAgent(agentId: string): Promise<NotificationFormatted[]>
countUnreadNotifications(agentId: string): Promise<number>
markNotificationAsRead(notificationId: string): Promise<void>
markAllNotificationsRead(agentId: string): Promise<void>
deleteNotification(notificationId: string): Promise<void>
formatNotifications(notifications: NotificationDTO[]): Promise<NotificationFormatted[]>
```
Fonctionnement: transforme les notifications backend en format UI et hydrate les agents associes.

### `processusService`
```ts
getProcessusActifs(): Promise<ProcessusDTO[]>
getProcessusById(id: string): Promise<ProcessusDTO>
getProcessusDetails(id: string): Promise<ProcessDetailDTO>
getProcessusByAgent(agentId: string): Promise<ProcessusDTO[]>
getProcessusEnRetard(): Promise<ProcessusDTO[]>
startProcessus(id: string): Promise<ProcessusDTO>
cancelProcessus(id: string): Promise<ProcessusDTO>
completeProcessus(id: string): Promise<ProcessusDTO>
createProcessus(command: {
  agentId?: string
  templateProcessusId: string
  dateDebut?: string
  formulaireData?: Record<string, any>
}): Promise<ProcessusDTO>
```
Fonctionnement: accede aux processus, leurs details, et lance/annule/termine un processus.

### `roleService`
```ts
getAllRoles(): Promise<RoleDTO[]>
```
Fonctionnement: liste des roles disponibles.

### `tacheService`
```ts
getTachesByProcessus(processusId: string): Promise<TacheDTO[]>
getTacheById(id: string): Promise<TacheDTO>
completeTache(id: string): Promise<TacheDTO>
annulerValidationTache(id: string): Promise<TacheDTO>
submitFormulaire(id: string, contenujson: Record<string, any>): Promise<TacheDTO>
```
Fonctionnement: gestion des taches et soumission des formulaires.

### `templateProcessusService`
```ts
getAllTemplates(type?: string): Promise<TemplateProcessusDTO[]>
getTemplateById(id: string): Promise<TemplateProcessusDTO>
getTemplatesByType(type: string): Promise<TemplateProcessusDTO[]>
getTemplateByCode(code: string): Promise<TemplateProcessusDTO | null>
```
Fonctionnement: acces aux templates de processus par id, type ou code.

### `templateTacheService`
```ts
getTemplateTacheById(id: string): Promise<TemplateTacheDTO>
getTachesByGroupeTache(groupeId: string): Promise<TemplateTacheDTO[]>
```
Fonctionnement: acces aux templates de taches et au contenu des groupes.

## Liens vers la documentation par couche
- Domaine: `docs/domain/README.md`
- Application: `docs/application/README.md`
- Infrastructure: `docs/infrastructure/README.md`
