# Configuration des Handlers - Éléments à Ajouter

## 📋 Résumé

Ce document liste tous les éléments que vous devez ajouter/configurer pour que les handlers fonctionnent correctement.

## 🔧 1. Migration de la Base de Données

### A. Ajouter la colonne `is_direction_concernee` à `template_groupe_tache`

```sql
ALTER TABLE template_groupe_tache 
ADD COLUMN is_direction_concernee BOOLEAN NOT NULL DEFAULT false;
```

**Note :** Cette colonne permet de marquer les groupes de tâches qui doivent être associés à la direction du nouvel agent plutôt qu'à la direction du template.

### B. Vérifier que la colonne existe dans `agent_materiel_et_droit`

La table `agent_materiel_et_droit` doit exister avec les colonnes :
- `id` (UUID)
- `agent_id` (UUID)
- `materiel_et_droit` (TEXT/JSON)

Si elle n'existe pas, elle sera créée automatiquement par Hibernate avec `ddl-auto: update`.

## 📝 2. Configuration des Templates de Tâches

### Structure JSON du contenu de la tâche

Dans le contenu JSON de votre tâche de formulaire (dans `template_tache.contenu`), vous devez ajouter une section `actions` :

```json
{
  "fields": [
    {
      "type": "text",
      "name": "nom_agent",
      "label": "Nom",
      "required": true
    },
    {
      "type": "text",
      "name": "prenom_agent",
      "label": "Prénom",
      "required": true
    },
    {
      "type": "text",
      "name": "email_agent",
      "label": "Email",
      "required": true
    },
    {
      "type": "select",
      "name": "role",
      "label": "Rôle",
      "options": ["Agent", "Manager", "Admin"],
      "required": true
    },
    {
      "type": "select",
      "name": "direction",
      "label": "Direction",
      "accesBaseDonnees": "directions",
      "required": true
    },
    {
      "type": "text",
      "name": "fonction",
      "label": "Fonction",
      "required": true
    },
    {
      "type": "date",
      "name": "date_arrivee",
      "label": "Date d'arrivée",
      "required": true
    },
    {
      "type": "text",
      "name": "materiel",
      "label": "Matériel (JSON)",
      "placeholder": "{\"ordinateur\": \"en_possession\", \"badge\": \"rendu\"}"
    }
  ],
  "actions": [
    {
      "type": "CREATE_AGENT",
      "condition": null,
      "params": {
        "nom": "${formulaire.nom_agent}",
        "prenom": "${formulaire.prenom_agent}",
        "email": "${formulaire.email_agent}",
        "role": "${formulaire.role}",
        "etatAgent": "${processus.typeProcessus}"
      }
    },
    {
      "type": "CREATE_AGENT_DIRECTION",
      "condition": null,
      "params": {
        "agentId": "${agent.id}",
        "directionId": "${formulaire.direction}",
        "dateArrivee": "${formulaire.date_arrivee}"
      }
    },
    {
      "type": "CREATE_AGENT_AFFECTATION",
      "condition": null,
      "params": {
        "agentId": "${agent.id}",
        "directionId": "${agentDirection.directionId}",
        "fonction": "${formulaire.fonction}"
      }
    },
    {
      "type": "CREATE_AGENT_MATERIEL",
      "condition": null,
      "params": {
        "agentId": "${agent.id}",
        "materielData": "${formulaire.materiel}"
      }
    }
  ]
}
```

## 🗄️ 3. Configuration des Templates de Groupes de Tâches

### Marquer les groupes concernés par la direction de l'agent

Pour les groupes de tâches qui doivent être associés à la direction du nouvel agent (et non à la direction du template), vous devez :

1. **Dans la base de données**, mettre `is_direction_concernee = true` pour ces groupes :

```sql
UPDATE template_groupe_tache 
SET is_direction_concernee = true 
WHERE code_template = 'nom_du_groupe';
```

2. **Ou lors de la création** via le code, utiliser le Builder :

```java
TemplateGroupeTache groupe = TemplateGroupeTache.Builder()
    .withId(UUID.randomUUID())
    .withCodeTemplate("nom_du_groupe")
    .withIsDirectionConcernee(true)  // ← Important
    .build();
```

## 🔄 4. Ordre d'Exécution des Actions

**IMPORTANT :** L'ordre des actions dans le tableau `actions` est crucial car chaque action peut dépendre des résultats des actions précédentes.

### Ordre recommandé :

1. **CREATE_AGENT** (première action)
   - Crée l'agent et stocke `${agent.id}` dans le contexte

2. **CREATE_AGENT_DIRECTION** (deuxième action)
   - Crée la direction de l'agent
   - Stocke `${agentDirection.directionId}` dans le contexte

3. **CREATE_AGENT_AFFECTATION** (troisième action)
   - Utilise `${agent.id}` et `${agentDirection.directionId}`

4. **CREATE_AGENT_MATERIEL** (quatrième action)
   - Utilise `${agent.id}`

## 📊 5. Format du Matériel

Le champ `materiel` dans le formulaire doit être au format JSON :

```json
{
  "ordinateur": "en_possession",
  "badge": "rendu",
  "telephone": "en_cours"
}
```

Les valeurs possibles pour le statut :
- `"en_possession"` : L'agent a le matériel
- `"rendu"` : Le matériel a été rendu
- `"en_cours"` : Le matériel est en cours de traitement/attribution

## 📧 6. Format de la Liste de Diffusion

La liste de diffusion (`AgentDiffusion`) est stockée comme un **texte avec séparateur** dans la base de données.

### Format en Base de Données

- **Type** : `TEXT` (chaîne de caractères)
- **Séparateur par défaut** : virgule (`,`)
- **Format** : `"element1,element2,element3"`

### Format sur le Front-End

Sur le front-end, vous pouvez avoir une **liste d'éléments** (par exemple, une liste d'emails ou de groupes) qui sera convertie en texte avec séparateur.

**Exemple :**
```typescript
// Front-end : Liste d'éléments
const listeDiffusion = ["email1@example.com", "email2@example.com", "groupe@example.com"];

// Conversion en texte pour la base de données
const textePourBDD = listeDiffusion.join(","); 
// Résultat : "email1@example.com,email2@example.com,groupe@example.com"
```

### Utilisation dans les Actions JSON

Dans le contenu JSON de votre tâche de formulaire, vous pouvez utiliser :

**Option 1 : Texte avec séparateur (recommandé)**
```json
{
  "type": "text",
  "name": "liste_diffusion",
  "label": "Liste de diffusion",
  "placeholder": "email1@example.com,email2@example.com"
}
```

**Option 2 : JSON Array (sera converti automatiquement)**
```json
{
  "type": "text",
  "name": "liste_diffusion",
  "label": "Liste de diffusion",
  "placeholder": "[\"email1@example.com\", \"email2@example.com\"]"
}
```

**Action dans le JSON :**
```json
{
  "type": "CREATE_AGENT_DIFFUSION",
  "params": {
    "agentId": "${agent.id}",
    "listeDiffusion": "${formulaire.liste_diffusion}",
    "separateur": ","  // Optionnel, par défaut: ","
  }
}
```

### Conversion Front-End ↔ Backend

**Front-end → Backend :**
```typescript
// Si vous avez une liste d'éléments
const elements = ["email1", "email2", "email3"];
const texte = elements.join(","); // "email1,email2,email3"
// Envoyer `texte` dans le formulaire
```

**Backend → Front-end :**
```typescript
// Si vous recevez un texte depuis le backend
const texte = "email1,email2,email3";
const elements = texte.split(","); // ["email1", "email2", "email3"]
// Afficher `elements` dans votre liste
```

### Séparateur Personnalisé

Vous pouvez utiliser un séparateur différent de la virgule :

```json
{
  "type": "CREATE_AGENT_DIFFUSION",
  "params": {
    "agentId": "${agent.id}",
    "listeDiffusion": "${formulaire.liste_diffusion}",
    "separateur": ";"  // Utiliser le point-virgule comme séparateur
  }
}
```

**Note :** Le séparateur est stocké dans la base de données, donc vous pouvez utiliser différents séparateurs pour différents agents si nécessaire.

## ⚠️ 6. Points d'Attention

### A. Création du Processus

Le processus est créé **AVANT** l'exécution des règles. Les règles s'exécutent lors de la **complétion de la tâche de formulaire**, pas lors de la création du processus.

**Flux actuel :**
1. `CreateProcessusFromTemplateService.createProcessu()` crée le processus
2. L'utilisateur remplit le formulaire
3. L'utilisateur clique sur "Valider la tâche"
4. `CompleteTacheService.completeTache()` est appelé
5. Les règles sont exécutées via `RuleEngine.executeRules()`

### B. Direction du Processus

La direction du processus (`directionConcerneeId`) est mise à jour automatiquement si :
- Au moins un groupe a `isDirectionConcernee = true`
- Une `AgentDirection` existe pour l'agent

La direction sera alors remplacée par celle de l'agent.

### C. Expressions Disponibles

Dans les paramètres des actions, vous pouvez utiliser :

- `${formulaire.nom_champ}` : Valeur d'un champ du formulaire
- `${agent.id}` : ID de l'agent créé (après CREATE_AGENT)
- `${agentDirection.directionId}` : ID de la direction de l'agent (après CREATE_AGENT_DIRECTION)
- `${processus.typeProcessus}` : Type du processus (entree, sortie, mobiliteInterne)
- `${processus.agentId}` : ID de l'agent du processus
- `${tache.id}` : ID de la tâche complétée

## 🧪 7. Tests

Pour tester les handlers :

1. **Créer un template de processus** avec une tâche de formulaire
2. **Ajouter les actions** dans le contenu JSON de la tâche
3. **Marquer les groupes** avec `isDirectionConcernee = true` si nécessaire
4. **Créer un processus** depuis le template
5. **Remplir le formulaire** et valider
6. **Vérifier** que :
   - L'agent est créé
   - L'AgentDirection est créée
   - L'AgentAffectation est créée
   - L'AgentMaterielEtDroit est créé
   - La direction du processus est mise à jour si nécessaire

## 📌 8. Checklist Finale

- [ ] Migration SQL exécutée pour `is_direction_concernee`
- [ ] Actions JSON ajoutées dans le contenu des tâches de formulaire
- [ ] Groupes de tâches marqués avec `isDirectionConcernee = true` si nécessaire
- [ ] Format JSON du matériel correct dans le formulaire
- [ ] Ordre des actions respecté dans le tableau `actions`
- [ ] Tests effectués pour vérifier le fonctionnement

## 🔍 9. Dépannage

### Problème : Les règles ne s'exécutent pas

**Vérifier :**
- La tâche a bien une section `actions` dans son contenu JSON
- La tâche est bien de type formulaire (vérifier dans le template)
- Les logs montrent "Exécution de X règles pour la tâche..."

### Problème : Erreur "Impossible de déterminer l'ID de l'agent"

**Cause :** L'action `CREATE_AGENT` n'a pas été exécutée avant ou a échoué.

**Solution :** Vérifier que `CREATE_AGENT` est la première action et qu'elle s'exécute correctement.

### Problème : La direction du processus n'est pas mise à jour

**Vérifier :**
- Au moins un groupe a `isDirectionConcernee = true`
- L'action `CREATE_AGENT_DIRECTION` a été exécutée avec succès
- Les logs montrent "Mise à jour de la direction du processus..."

### Problème : Format JSON du matériel invalide

**Vérifier :**
- Le JSON est valide (utiliser un validateur JSON)
- Les clés sont des strings
- Les valeurs sont des strings (statuts)

## 📚 10. Exemple Complet

Voici un exemple complet de contenu JSON pour une tâche d'entrée d'agent :

```json
{
  "fields": [
    {"type": "text", "name": "nom_agent", "label": "Nom", "required": true, "icon": "user"},
    {"type": "text", "name": "prenom_agent", "label": "Prénom", "required": true, "icon": "user"},
    {"type": "text", "name": "email_agent", "label": "Email", "required": true, "icon": "user"},
    {"type": "select", "name": "role", "label": "Rôle", "options": ["Agent", "Manager", "Admin"], "required": true},
    {"type": "select", "name": "direction", "label": "Direction", "accesBaseDonnees": "directions", "required": true, "icon": "building"},
    {"type": "text", "name": "fonction", "label": "Fonction", "required": true},
    {"type": "date", "name": "date_arrivee", "label": "Date d'arrivée", "required": true, "icon": "calendar"},
    {"type": "text", "name": "materiel", "label": "Matériel (JSON)", "placeholder": "{\"ordinateur\": \"en_possession\", \"badge\": \"rendu\"}"}
  ],
  "actions": [
    {
      "type": "CREATE_AGENT",
      "condition": null,
      "params": {
        "nom": "${formulaire.nom_agent}",
        "prenom": "${formulaire.prenom_agent}",
        "email": "${formulaire.email_agent}",
        "role": "${formulaire.role}",
        "etatAgent": "${processus.typeProcessus}"
      }
    },
    {
      "type": "CREATE_AGENT_DIRECTION",
      "condition": null,
      "params": {
        "agentId": "${agent.id}",
        "directionId": "${formulaire.direction}",
        "dateArrivee": "${formulaire.date_arrivee}"
      }
    },
    {
      "type": "CREATE_AGENT_AFFECTATION",
      "condition": null,
      "params": {
        "agentId": "${agent.id}",
        "directionId": "${agentDirection.directionId}",
        "fonction": "${formulaire.fonction}"
      }
    },
    {
      "type": "CREATE_AGENT_MATERIEL",
      "condition": null,
      "params": {
        "agentId": "${agent.id}",
        "materielData": "${formulaire.materiel}"
      }
    }
  ]
}
```

## ✅ Résumé des Éléments à Ajouter

1. **Migration SQL** : Ajouter `is_direction_concernee` à `template_groupe_tache`
2. **JSON Actions** : Ajouter la section `actions` dans le contenu des tâches de formulaire
3. **Configuration Groupes** : Marquer les groupes avec `isDirectionConcernee = true` si nécessaire
4. **Format Matériel** : S'assurer que le champ matériel est au format JSON valide

Une fois ces éléments en place, les handlers s'exécuteront automatiquement lors de la complétion des tâches de formulaire !
