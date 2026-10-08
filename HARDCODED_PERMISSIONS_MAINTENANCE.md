# Hardcoded Permission Checks - Frontend

This document identifies all **hardcoded permission checks** in the frontend React code that restrict who can perform certain actions based on direction/department.

## 🔴 Critical: Hardcoded Permission Locations

### 1. Add Advisor/Attaché Button
**File:** [src/components/NavBar/AjouterAgent.tsx](src/components/NavBar/AjouterAgent.tsx)  
**Lines:** 130-145  
**Current Permissions:** DAF, DSIUN only

```typescript
export const AjouterConseillerButton = () => {
  const agent = getCurrentAgent();
  const direction = (agent.direction || "").trim().toLowerCase();
  const canAddConseiller = direction === "daf" || direction === "dsiun";  // ⚠️ LINE 133

  if (!canAddConseiller) {
    return null;
  }

  return (
    <Button asChild size="sm" className="rounded-lg w-fit">
      <Link to="/process_entree?mode=conseiller">
        <Plus />
        Ajouter des Conseillers/Attachés
      </Link>
    </Button>
  );
}
```

**To Add DAPPI:** Change line 133 to:
```typescript
const canAddConseiller = direction === "daf" || direction === "dsiun" || direction === "dappi";
```

---

### 2. Add Service Provider Button
**File:** [src/components/NavBar/AjouterAgent.tsx](src/components/NavBar/AjouterAgent.tsx)  
**Lines:** 113-127  
**Current Permissions:** DAPPI, DSIUN, DAF

```typescript
export const AjouterPrestataireButton = () => {
  const agent = getCurrentAgent();
  const direction = (agent.direction || "").trim().toLowerCase();
  const canAddPrestataire = direction === "dappi" || direction === "dsiun" || direction === "daf";  // ⚠️ LINE 114

  if (!canAddPrestataire) {
    return null;
  }

  return (
    <Button asChild size="sm" className="rounded-lg w-fit">
      <Link to="/process_entree?mode=prestataire">
        <Plus />
        Ajouter des Prestataires
      </Link>
    </Button>
  );
}
```

**To Modify:** Adjust line 114 to change which directions can add service providers.

---

### 3. Add Regular Employee Button
**File:** [src/components/NavBar/AjouterAgent.tsx](src/components/NavBar/AjouterAgent.tsx)  
**Lines:** 92-110  
**Current Permissions:** DRH only

```typescript
export const AjouterAgentButton = () => {
  const agent = getCurrentAgent();
  const direction = (agent.direction || "").trim().toLowerCase();
  const canAddAgent = direction === "drh";  // ⚠️ LINE 95

  if (!canAddAgent) {
    return null;
  }

  return (
    <Button asChild size="sm" className="rounded-lg w-fit">
      <Link to="/process_entree">
        <Plus />
        Ajouter des Agents
      </Link>
    </Button>
  );
}
```

**To Modify:** Change line 95 to add more directions.

---

### 4. Launch Exit Process Button (Agent Detail Page)
**File:** [src/components/contenuPage/agent/AgentDetailPage.tsx](src/components/contenuPage/agent/AgentDetailPage.tsx)  
**Lines:** 177-210

This is the **most complex permission check** as it depends on the agent type.

#### Direction Normalization (Lines 177-188):
```typescript
const isDrh = (currentAgent.direction || "").trim().toLowerCase() === "drh";
const isDappi = (() => {
  const dir = String(currentAgent.direction || "").trim().toLowerCase();
  return dir === "dappi" || dir === "direction_appi" || dir === "d.a.p.p.i";  // ⚠️ Accepts variants
})();
const isDsiun = (() => {
  const dir = String(currentAgent.direction || "").trim().toLowerCase();
  return dir === "dsiun" || dir === "direction_siun" || dir === "d.s.i.u.n";  // ⚠️ Accepts variants
})();
const isDaf = (() => {
  const dir = String(currentAgent.direction || "").trim().toLowerCase();
  return dir === "daf" || dir === "direction_af" || dir === "d.a.f";  // ⚠️ Accepts variants
})();
```

#### Agent Type Classification (Lines 195-196):
```typescript
const isPrestataireCible = isPrestataire({ role: agent.role, direction: agent.direction });
const isConseillerCible = isConseiller({ role: agent.role, direction: agent.direction });
```

Where `isPrestataire()` and `isConseiller()` are from [src/utils/directionDisplay.ts](src/utils/directionDisplay.ts):
```typescript
export function isPrestataire(agent: AgentLike | null | undefined): boolean {
  if (!agent) return false;
  const role = String(agent.role || "").trim().toUpperCase();
  return role === "PRESTATAIRE";  // ⚠️ Checks role field
}

export function isConseiller(agent: AgentLike | null | undefined): boolean {
  if (!agent) return false;
  const role = String(agent.role || "").trim().toUpperCase();
  return role === "CONSEILLER";  // ⚠️ Checks role field
}
```

#### Permission Logic (Lines 199-202):
```typescript
const canLaunchSortie = !isAncien && (
  (isPrestataireCible && (isDappi || isDsiun || isDaf)) ||  // ⚠️ Service providers: DAPPI, DSIUN, DAF
  (isConseillerCible && (isDafLike || isDsiunLike)) ||      // ⚠️ Advisors: DAF, DSIUN
  (!isPrestataireCible && !isConseillerCible && isDrh)      // ⚠️ Employees: DRH only
);
```

#### Handler Permission Checks (Lines 208-210):
```typescript
const handleLancerSortie = async () => {
  if (!agent) return;
  // Vérifier les droits selon le type d'agent
  if (isPrestataireCible && !(isDappi ||isDsiun || isDaf)) return;  // ⚠️ Duplicate check
  if (isConseillerCible && !(isDafLike || isDsiunLike)) return;     // ⚠️ Duplicate check
  if (!isPrestataireCible && !isConseillerCible && !isDrh) return;  // ⚠️ Duplicate check
  
  // ... rest of handler
}
```

---

## 📊 Permission Values by Role-Type

### Regular Employee (role != "PRESTATAIRE" && role != "CONSEILLER")
- **Can Launch Exit:** DRH only

### Service Provider (role === "PRESTATAIRE")
- **Can Launch Exit:** DAPPI, DSIUN, DAF

### Advisor/Attaché (role === "CONSEILLER")
- **Can Launch Exit:** DAF, DSIUN only

---

## 🔧 Direction String Normalization

All code normalizes directions to **lowercase** before comparison. The following variations are recognized:

| Direction | Accepted Values |
|-----------|-----------------|
| DAPPI | `"dappi"`, `"direction_appi"`, `"d.a.p.p.i"` |
| DSIUN | `"dsiun"`, `"direction_siun"`, `"d.s.i.u.n"` |
| DAF | `"daf"`, `"direction_af"`, `"d.a.f"` |
| DRH | `"drh"` only |

**Found in:** [src/components/contenuPage/agent/AgentDetailPage.tsx](src/components/contenuPage/agent/AgentDetailPage.tsx), lines 178-188

---

## 🔌 Data Source

Current user's direction is retrieved from **localStorage** via:
```typescript
const agent = getCurrentAgent();
const direction = (agent.direction || "").trim().toLowerCase();
```

**File:** [src/utils/currentAgent.ts](src/utils/currentAgent.ts)
```typescript
export function getCurrentAgent(): CurrentAgentInfo {
  try {
    const raw = localStorage.getItem("currentAgent");
    if (!raw) return {};
    const parsed = JSON.parse(raw) as CurrentAgentInfo;
    return parsed || {};
  } catch {
    return {};
  }
}
```

⚠️ **Security Note:** This client-side permission check is **NOT ENFORCED** on the backend. A user could manually modify localStorage to claim any direction.

---

## ✅ TODO: Future Hardening

1. **Implement Backend Permission Validation**
   - Verify user's actual direction/role server-side before processing any requests
   - All entry/exit process creation should validate permissions

2. **Implement RBAC System**
   - Consider moving permissions to a configuration service instead of hardcoding
   - Centralize permission rules for maintenance

3. **Add Permission Service**
   - Create `src/service/permissionService.ts` to centralize all permission checks
   - Replace all hardcoded direction strings with constants

4. **Audit Trail**
   - Log who adds agents/processes to detect unauthorized modifications

---

## 📋 Files to Modify When Changing Permissions

1. **Button Visibility:** [src/components/NavBar/AjouterAgent.tsx](src/components/NavBar/AjouterAgent.tsx)
   - Lines 95, 114, 133

2. **Exit Process Logic:** [src/components/contenuPage/agent/AgentDetailPage.tsx](src/components/contenuPage/agent/AgentDetailPage.tsx)
   - Lines 177-210 (direction normalization + permission checks)

3. **Direction Display (if needed):** [src/utils/directionDisplay.ts](src/utils/directionDisplay.ts)
   - Only if you change how agent types are classified

---

## 🧪 Testing Directions

Default test direction in [src/App.tsx](src/App.tsx#L11):
```typescript
export const user = {
    nom:"Marcel Pecqueux",
    direction: "DSIUN",  // ⚠️ Test user is DSIUN
    avatar: "MP",
    id: "02d86861-b9e7-45e9-903b-93e9efc7a304"
};
```

To test different directions:
1. Modify this value in App.tsx, or
2. Modify `localStorage.currentAgent` in browser DevTools to `{"direction": "daf", ...}`
