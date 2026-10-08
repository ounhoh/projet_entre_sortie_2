# Frontend Permission Mapping - React Components

## 1. ADDING ADVISORS/CONSEILLERS (Adding Advisors/Attachés)

### Component: `AjouterConseillerButton`
**File:** [src/components/NavBar/AjouterAgent.tsx](src/components/NavBar/AjouterAgent.tsx#L130)

**Which Directions Can Add Advisors:**
- `DAF` (Direction Administrative et Financière)
- `DSIUN` (Direction Système d'Information, Unité Numérique)

**Permission Check Code (Line 133-134):**
```typescript
const canAddConseiller = direction === "daf" || direction === "dsiun";
```

**Action When Permitted:**
- Button navigates to: `/process_entree?mode=conseiller`
- Route: [Creation Process Page](src/components/contenuPage/processus/)

**When Not Permitted:**
- Button returns `null` (not displayed)

---

## 2. ADDING AGENTS (Regular Employees)

### Component: `AjouterAgentButton`
**File:** [src/components/NavBar/AjouterAgent.tsx](src/components/NavBar/AjouterAgent.tsx#L92)

**Which Directions Can Add Agents:**
- `DRH` (Direction des Ressources Humaines)

**Permission Check Code (Line 95):**
```typescript
const canAddAgent = direction === "drh";
```

**Action When Permitted:**
- Button navigates to: `/process_entree`
- Route: [Process Page for Entry](src/components/contenuPage/processus/)

**When Not Permitted:**
- Button returns `null` (not displayed)

---

## 3. ADDING SERVICE PROVIDERS (Prestataires)

### Component: `AjouterPrestataireButton`
**File:** [src/components/NavBar/AjouterAgent.tsx](src/components/NavBar/AjouterAgent.tsx#L113)

**Which Directions Can Add Service Providers:**
- `DAPPI` (Direction des Affaires Publiques et Partenariats Institutionnels)
- `DSIUN` (Direction Système d'Information, Unité Numérique)
- `DAF` (Direction Administrative et Financière)

**Permission Check Code (Line 114):**
```typescript
const canAddPrestataire = direction === "dappi" || direction === "dsiun" || direction === "daf";
```

**Action When Permitted:**
- Button navigates to: `/process_entree?mode=prestataire`
- Route: [Process Page for Entry with Prestataire Mode](src/components/contenuPage/processus/)

**When Not Permitted:**
- Button returns `null` (not displayed)

---

## 4. LAUNCHING EXIT PROCESSES (Processus de Sortie)

### Component: Exit Process Button in Agent Detail Page
**File:** [src/components/contenuPage/agent/AgentDetailPage.tsx](src/components/contenuPage/agent/AgentDetailPage.tsx#L199)

**Exit Process Permissions Depend on Agent Type:**

#### For Regular Employees (not advisors or service providers):
- Only `DRH` can launch exit processes

**Permission Logic (Lines 199-202):**
```typescript
const canLaunchSortie = !isAncien && (
  (isPrestataireCible && (isDappi || isDsiun || isDaf)) ||
  (isConseillerCible && (isDafLike || isDsiunLike)) ||
  (!isPrestataireCible && !isConseillerCible && isDrh)
);
```

#### For Service Providers (Prestataires):
- Any direction that can ADD service providers can also launch exit processes:
  - `DAPPI`
  - `DSIUN`
  - `DAF`

#### For Advisors/Attachés (Conseillers):
- Only directions that can ADD advisors can launch exit processes:
  - `DAF`
  - `DSIUN`

**Direction Normalization (Lines 177-188):**
```typescript
const isDrh = direction === "drh";
const isDappi = dir === "dappi" || dir === "direction_appi" || dir === "d.a.p.p.i";
const isDsiun = dir === "dsiun" || dir === "direction_siun" || dir === "d.s.i.u.n";
const isDaf = dir === "daf" || dir === "direction_af" || dir === "d.a.f";
```

**Classification Logic (Lines 195-196):**
```typescript
const isPrestataireCible = isPrestataire({ role: agent.role, direction: agent.direction });
const isConseillerCible = isConseiller({ role: agent.role, direction: agent.direction });
```

Where:
- `isPrestataire()` checks if `role === "PRESTATAIRE"`
- `isConseiller()` checks if `role === "CONSEILLER"`

**Navigation Target:**
- Routes to: `/creation_process/{templateId}?mode={mode}`
- Uses template: `sortie_agent_creation` or first template of type `sortie`
- Mode parameter: `?mode=prestataire` or `?mode=conseiller` (if applicable)

---

## 5. UI BUTTON LAYOUT

### Dashboard Page Component
**File:** [src/components/tableau/tableau-Dashboard/TableauProcess.tsx](src/components/tableau/tableau-Dashboard/TableauProcess.tsx#L231)

**All four buttons are displayed conditionally in this grid:**
```
<AjouterAgentButton/>              // Shown only for DRH
<AjouterPrestataireButton/>         // Shown for DAPPI, DSIUN, DAF
<AjouterConseillerButton/>          // Shown for DAF, DSIUN
```

---

## 6. PERMISSION CHECK LOCATIONS (HARDCODED)

### Critical Files with Direction Checks:

| Location | File | Line(s) | Check Type |
|----------|------|---------|-----------|
| Add Advisor Button | [AjouterAgent.tsx](src/components/NavBar/AjouterAgent.tsx) | 133 | `direction === "daf" \|\| direction === "dsiun"` |
| Add Service Provider Button | [AjouterAgent.tsx](src/components/NavBar/AjouterAgent.tsx) | 114 | `direction === "dappi" \|\| direction === "dsiun" \|\| direction === "daf"` |
| Add Agent Button | [AjouterAgent.tsx](src/components/NavBar/AjouterAgent.tsx) | 95 | `direction === "drh"` |
| Launch Exit Process | [AgentDetailPage.tsx](src/components/contenuPage/agent/AgentDetailPage.tsx) | 199-202 | Complex logic with agent type classification |
| Exit Handler Check | [AgentDetailPage.tsx](src/components/contenuPage/agent/AgentDetailPage.tsx) | 208-210 | Duplicate permission checks |

---

## 7. SUPPORTING UTILITY FILES

### Current Agent Helper
**File:** [src/utils/currentAgent.ts](src/utils/currentAgent.ts)
- `getCurrentAgent()` - Retrieves agent info from localStorage
- `getCurrentAgentDisplay()` - Formats display with direction name

### Direction Display Helper
**File:** [src/utils/directionDisplay.ts](src/utils/directionDisplay.ts)
- `isPrestataire(agent)` - Checks if agent role is "PRESTATAIRE"
- `isConseiller(agent)` - Checks if agent role is "CONSEILLER"
- `getDirectionDisplay(agent)` - Formats direction for UI display

---

## 8. SUMMARY TABLE

| Action | DRH | DAPPI | DSIUN | DAF |
|--------|-----|-------|-------|-----|
| Add Agent | ✅ | ❌ | ❌ | ❌ |
| Add Service Provider | ❌ | ✅ | ✅ | ✅ |
| Add Advisor/Attaché | ❌ | ❌ | ✅ | ✅ |
| Launch Exit (Regular Employee) | ✅ | ❌ | ❌ | ❌ |
| Launch Exit (Service Provider) | ❌ | ✅ | ✅ | ✅ |
| Launch Exit (Advisor) | ❌ | ❌ | ✅ | ✅ |

---

## 9. KEY OBSERVATIONS

### Hardcoded Checks
All permission checks are **hardcoded in the frontend** using simple string comparisons on `currentAgent.direction`. There is:
- ❌ No centralized permission service
- ❌ No role-based access control (RBAC) system
- ❌ No backend validation visible in the frontend code

### Direction Normalization
Directions are normalized to lowercase before comparison (stored in `localStorage` as the current user's direction).

### Agent Classification
The system classifies agents into three types based on the `role` field:
1. **Regular Employee** (no special role) - Full name + direction
2. **Service Provider** (`role = "PRESTATAIRE"`) - Displayed as "Prestataire"
3. **Advisor/Attaché** (`role = "CONSEILLER"`) - Displayed as "Conseiller/Attaché"

Each type has different exit process permissions.

### Mode Parameter
When launching entry/exit processes, a `?mode` query parameter is passed:
- `mode=prestataire` - For service provider processes
- `mode=conseiller` - For advisor processes
- (default) - For regular employee processes

---

## 10. RELATED COMPONENTS

### Navigation Menu
**File:** [src/components/sidebar/AppSideBar.tsx](src/components/sidebar/AppSideBar.tsx)
- Line 16: "Processus d'entrée/sortie"
- Line 25: "Processus de Sortie"

### Route Configuration
**File:** [src/App.tsx](src/App.tsx)
- `/process_entree` - Entry process page
- `/creation_process/:templateId` - Process creation page
- `/agent/:agentId` - Agent detail page (where exit is launched from)
