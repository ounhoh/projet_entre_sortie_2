import React, { useState, useEffect, useMemo, useCallback, useRef } from "react";
import { Label } from "../ui/label";
import { InputGroup, InputGroupInput, InputGroupAddon } from "../ui/input-group";
import { Combobox, type ComboboxOption } from "../ui/combobox";
import { DatePicker } from "../ui/date-picker";
import { Alert, AlertDescription, AlertTitle } from "../ui/alert";
import { Button } from "../ui/button";
import { Calendar, ImageIcon, NotebookPen, User, Building2, AlertCircle, X, Upload, Plus } from "lucide-react";
import type { TemplateTacheDTO } from "@/service/templateTacheService";
import { directionService } from "@/service/directionService";
import { agentService } from "@/service/agentService";
import { roleService } from "@/service/roleService";
import { getCurrentAgent } from "@/utils/currentAgent";

// Type pour un champ de formulaire défini dans le contenu
type ChampFormulaire = {
  type: 'text' | 'date' | 'file' | 'select' | 'liste' | 'texte' | 'email' | 'image' | 'input' | 'value' | 'etat_changement';
  label: string;
  name: string;
  required?: boolean;
  placeholder?: string;
  options?: string[] | { accesBaseDonnees?: string; champs?: string[] }; // Pour les selects
  icon?: string; // Nom de l'icône à utiliser
  minDate?: string; // Pour les dates
  maxDate?: string; // Pour les dates
  minLength?: number; // Pour les champs texte
  maxLength?: number; // Pour les champs texte
  pattern?: string; // Pour les champs texte (regex pattern)
  mimeType?: string[]; // Pour les champs file/image (types MIME acceptés)
  addableToList?: boolean; // Si true, permet d'ajouter les valeurs à une liste
  alreadyExist?: {
    type?: string;
    baseDonnees?: string;
    champs?: string[];
    contenu?: string[];
  };
  // Propriétés spécifiques au type "value"
  baseDonnees?: string; // Table de la base de données (ex: "agent", "agentDirection")
  champs?: string[]; // Champs à récupérer depuis la base de données
  value?: string; // Template avec variables (ex: "${agent.email}")
  // Propriétés spécifiques au type "etat_changement"
  etat?: string[]; // Liste des états (ex: ["en_cours", "en_possession"])
};

// Type pour les options chargées depuis la base de données
type OptionDonnees = {
  id: string;
  libelle: string;
  code?: string;
};

// Fonction pour parser le contenu et extraire les champs
function parseContenu(contenu: Record<string, any> | string | null | undefined): ChampFormulaire[] {
  // Si le contenu est null, undefined ou vide
  if (!contenu) {
    return [];
  }

  // Si le contenu est une chaîne JSON, le parser
  let contenuObj: Record<string, any>;
  if (typeof contenu === 'string') {
    try {
      contenuObj = JSON.parse(contenu);
    } catch (e) {
      console.error('Erreur lors du parsing du contenu JSON:', e, contenu);
      return [];
    }
  } else if (typeof contenu === 'object') {
    contenuObj = contenu;
  } else {
    return [];
  }

  const champs: ChampFormulaire[] = [];
  
  // Le contenu peut être structuré de différentes manières
  // Format 0: Le contenu est directement un array de champs
  if (Array.isArray(contenuObj)) {
    return contenuObj.map((field: any) => ({
      type: field.type || 'text',
      label: field.label || field.libelle || field.name || '',
      name: field.name || field.id || field.key || '',
      required: field.required || false,
      placeholder: field.placeholder,
      options: field.options,
      icon: field.icon,
      minDate: field.minDate || field.validation?.minDate,
      maxDate: field.maxDate || field.validation?.maxDate,
      minLength: field.validation?.minLength,
      maxLength: field.validation?.maxLength,
      pattern: field.validation?.pattern,
      mimeType: field.validation?.mimeType,
      addableToList: field.addableToList || false,
      alreadyExist: field.alreadyExist,
      // Propriétés pour le type "value"
      baseDonnees: field.baseDonnees,
      champs: field.champs,
      value: field.value,
      // Propriétés pour le type "etat_changement"
      etat: field.etat,
    }));
  }
  
  // Format 1: Array de champs dans fields ou champs
  if (Array.isArray(contenuObj.fields) || Array.isArray(contenuObj.champs)) {
    const fields = contenuObj.fields || contenuObj.champs;
    return fields.map((field: any) => ({
      type: field.type || 'text',
      label: field.label || field.libelle || field.name || '',
      name: field.name || field.id || field.key || '',
      required: field.required || false,
      placeholder: field.placeholder,
      options: field.options,
      icon: field.icon,
      minDate: field.minDate || field.validation?.minDate,
      maxDate: field.maxDate || field.validation?.maxDate,
      minLength: field.validation?.minLength,
      maxLength: field.validation?.maxLength,
      pattern: field.validation?.pattern,
      mimeType: field.validation?.mimeType,
      addableToList: field.addableToList || false,
      alreadyExist: field.alreadyExist,
      // Propriétés pour le type "value"
      baseDonnees: field.baseDonnees,
      champs: field.champs,
      value: field.value,
      // Propriétés pour le type "etat_changement"
      etat: field.etat,
    }));
  }

  // Format 2: Map simple avec des objets comme valeurs
  Object.keys(contenuObj).forEach((key) => {
    // Ignorer les clés qui ne sont pas des champs (comme "actions")
    if (key === 'actions' || key === 'fields' || key === 'champs') {
      return;
    }
    
    const value = contenuObj[key];
    if (typeof value === 'object' && value !== null && !Array.isArray(value)) {
      // Si c'est un objet avec des propriétés de champ
      if (value.type || value.label || value.libelle) {
        champs.push({
          type: value.type || 'text',
          label: value.label || value.libelle || key,
          name: key,
          required: value.required || false,
          placeholder: value.placeholder,
          options: value.options,
      icon: value.icon,
      minDate: value.minDate || value.validation?.minDate,
      maxDate: value.maxDate || value.validation?.maxDate,
      minLength: value.validation?.minLength,
      maxLength: value.validation?.maxLength,
      pattern: value.validation?.pattern,
      mimeType: value.validation?.mimeType,
      addableToList: value.addableToList || false,
      alreadyExist: value.alreadyExist,
      // Propriétés pour le type "value"
      baseDonnees: value.baseDonnees,
      champs: value.champs,
      value: value.value,
        });
      }
    } else if (typeof value === 'string' || typeof value === 'number') {
      // Si c'est une valeur simple, créer un champ texte par défaut
      champs.push({
        type: 'text',
        label: key.charAt(0).toUpperCase() + key.slice(1).replace(/([A-Z])/g, ' $1'),
        name: key,
        required: false,
        placeholder: `Entrer ${key}`,
      });
    }
  });

  return champs;
}

// Fonction helper pour normaliser une liste qui peut contenir des strings formatées comme "[item1, item2]"
function normalizeList(list: (string | any)[]): string[] {
  if (!Array.isArray(list) || list.length === 0) {
    return [];
  }
  
  const normalized: string[] = [];
  
  for (const item of list) {
    if (typeof item === 'string') {
      // Si la string ressemble à une liste JSON (commence par [ et se termine par ])
      const trimmed = item.trim();
      const withoutEdgeBrackets = trimmed.replace(/^\[/, '').replace(/\]$/, '').trim();
      if (trimmed.startsWith('[') && trimmed.endsWith(']')) {
        try {
          // Essayer de parser comme JSON
          const parsed = JSON.parse(trimmed);
          if (Array.isArray(parsed)) {
            // Ajouter chaque élément du tableau parsé
            normalized.push(...parsed.map(String));
          } else {
            normalized.push(item);
          }
        } catch {
          // Si le parsing échoue, essayer de splitter manuellement
          const withoutBrackets = trimmed.slice(1, -1).trim();
          if (withoutBrackets) {
            const items = withoutBrackets
              .split(/[\n,]+/)
              .map(i => i.trim())
              .filter(i => i);
            normalized.push(...items);
          } else {
            normalized.push(item);
          }
        }
      } else if (trimmed.startsWith('[') || trimmed.endsWith(']')) {
        if (withoutEdgeBrackets) {
          normalized.push(withoutEdgeBrackets);
        }
      } else {
        normalized.push(item);
      }
    } else {
      normalized.push(String(item));
    }
  }
  
  return normalized;
}

function normalizeListInput(value: any): string[] {
  if (value === null || value === undefined) {
    return [];
  }
  if (Array.isArray(value)) {
    return normalizeList(value);
  }
  if (typeof value === 'string') {
    return normalizeList([value]);
  }
  if (typeof value === 'object') {
    return Object.keys(value).map(String).filter(Boolean);
  }
  return normalizeList([String(value)]);
}

function normalizeMaterielPayload(payload: any): Record<string, any> {
  if (!payload || typeof payload !== 'object') {
    return {};
  }
  const normalized = payload.agentMaterileEtDroit || payload.agentMaterielEtDroit || payload;
  if (normalized && typeof normalized === 'object' && !Array.isArray(normalized)) {
    if (!('materiels' in normalized) && 'materiel' in normalized) {
      const materielValue = (normalized as Record<string, any>).materiel;
      if (Array.isArray(materielValue)) {
        (normalized as Record<string, any>).materiels = normalizeList(materielValue);
      } else if (materielValue && typeof materielValue === 'object') {
        (normalized as Record<string, any>).materiels = Object.keys(materielValue).map(String);
      }
    }
  }
  return normalized;
}

function getValueAtPath(source: any, path: string): any {
  if (!source || typeof source !== 'object') {
    return undefined;
  }
  const parts = path.split('.').filter(Boolean);
  let current: any = source;
  for (const part of parts) {
    if (current && typeof current === 'object') {
      current = current[part];
    } else {
      return undefined;
    }
  }
  return current;
}

function normalizeMaterielPath(path: string): string {
  if (path.startsWith('materielEtDroit.')) {
    return path.replace('materielEtDroit.', '');
  }
  if (path.startsWith('materielAgent.')) {
    return path.replace('materielAgent.', '');
  }
  return path;
}

function extractValueFromPath(data: any, path: string[]): any {
  if (!data || !path || path.length === 0) {
    return data;
  }
  return path.reduce((current, key) => {
    if (current == null) return undefined;
    return current[key];
  }, data);
}

function normalizeMaterielPathSegment(segment: string): string[] {
  if (segment === 'materiel' || segment === 'materielAgent' || segment === 'material') {
    return ['materiels', 'materiel', 'materials'];
  }
  if (segment === 'droit_materiel' || segment === 'droits' || segment === 'droit' || segment === 'droitAgent') {
    return ['droits', 'droit', 'droitAgent'];
  }
  if (segment === 'diffusion' || segment === 'listeDiffusion') {
    return ['listeDiffusion', 'diffusion', 'diffusionAgent'];
  }
  return [segment];
}

function buildCandidatePaths(path: string[], baseDonnees?: string): string[][] {
  if (!path || path.length === 0) return [];
  const isMaterielBase = baseDonnees === 'agent_droit_materiel' || baseDonnees === 'agentMaterielEtDroit';
  const isDiffusionBase = baseDonnees === 'agent_diffusion' || baseDonnees === 'agentDiffusion';

  if (!isMaterielBase && !isDiffusionBase) {
    return [path];
  }

  const candidates: string[][] = [[]];
  path.forEach((segment) => {
    const options = isDiffusionBase
      ? normalizeMaterielPathSegment(segment)
      : normalizeMaterielPathSegment(segment);
    const next: string[][] = [];
    candidates.forEach(existing => {
      options.forEach(option => {
        next.push([...existing, option]);
      });
    });
    candidates.splice(0, candidates.length, ...next);
  });

  return candidates;
}

function extractAgentFallbackValue(data: any, path: string[], baseDonnees?: string): any {
  if (baseDonnees !== 'agent') {
    return undefined;
  }
  if (!data || !path || path.length === 0) {
    return undefined;
  }
  const lastKey = path[path.length - 1];
  const fallbackMap: Record<string, string> = {
    date_depart: 'dateSortie',
    date_sortie: 'dateSortie',
    date_arrivee: 'dateArrivee',
  };
  const fallbackKey = fallbackMap[lastKey];
  if (!fallbackKey) {
    return undefined;
  }
  const fallbackPath = [...path.slice(0, -1), fallbackKey];
  return extractValueFromPath(data, fallbackPath);
}

function normalizeDateValue(value: any): string | undefined {
  if (value === null || value === undefined || value === '') {
    return undefined;
  }
  if (value instanceof Date && !isNaN(value.getTime())) {
    const year = value.getFullYear();
    const month = String(value.getMonth() + 1).padStart(2, '0');
    const day = String(value.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
  if (typeof value === 'string') {
    const trimmed = value.trim();
    if (!trimmed) return undefined;
    if (/^\d{4}-\d{2}-\d{2}$/.test(trimmed)) {
      return trimmed;
    }
    const parsed = new Date(trimmed);
    if (!isNaN(parsed.getTime())) {
      const year = parsed.getFullYear();
      const month = String(parsed.getMonth() + 1).padStart(2, '0');
      const day = String(parsed.getDate()).padStart(2, '0');
      return `${year}-${month}-${day}`;
    }
  }
  return undefined;
}

function normalizeText(value: string): string {
  return value
    .toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/[^a-z0-9]/g, '');
}

function findBestOptionMatch(options: OptionDonnees[], extracted: any): OptionDonnees | undefined {
  if (!extracted) return undefined;
  const extractedText = normalizeText(String(extracted));
  if (!extractedText) return undefined;

  let best: OptionDonnees | undefined = undefined;
  let bestScore = 0;

  for (const option of options) {
    const libelle = option.libelle ? normalizeText(option.libelle) : '';
    const code = option.code ? normalizeText(option.code) : '';
    const id = option.id ? normalizeText(option.id) : '';

    let score = 0;
    if (libelle === extractedText || code === extractedText || id === extractedText) {
      score = 100;
    } else if (libelle.includes(extractedText) || code.includes(extractedText)) {
      score = 80;
    } else if (extractedText.includes(libelle) || extractedText.includes(code)) {
      score = 70;
    } else if (libelle && extractedText && libelle[0] === extractedText[0]) {
      score = 30;
    }

    if (score > bestScore) {
      bestScore = score;
      best = option;
    }
  }

  return bestScore >= 60 ? best : undefined;
}

// Composant pour afficher une valeur de type "value" (lecture seule depuis la base de données)
function ValueField({ 
  champ, 
  agentId 
}: { 
  champ: ChampFormulaire; 
  agentId?: string;
}) {
  const [loadedValue, setLoadedValue] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchValue = async () => {
      if (!champ.baseDonnees || !champ.champs || !agentId) {
        setLoading(false);
        return;
      }

      try {
        setLoading(true);
        setError(null);
        console.log('[ValueField] Champ:', {
          name: champ.name,
          label: champ.label,
          baseDonnees: champ.baseDonnees,
          champs: champ.champs,
          valueTemplate: champ.value,
          agentId
        });

        // Récupérer les données selon baseDonnees
        let data: any = null;
        let materielEtDroit: any = null;
        switch (champ.baseDonnees) {
          case 'agent':
            data = await agentService.getAgentById(agentId);
            break;
          case 'agentDirection':
            data = await agentService.getAgentById(agentId);
            break;
          case 'agentMaterielEtDroit':
            try {
              const materielRaw = await agentService.getAgentMateriel(agentId);
              materielEtDroit = normalizeMaterielPayload(materielRaw);
              data = materielEtDroit;
              console.log('[ValueField] materielEtDroit extrait:', materielEtDroit);
            } catch (err) {
              console.warn('Impossible de récupérer materielEtDroit:', err);
            }
            break;
          case 'agentDiffusion':
            try {
              const diffusions = await agentService.getAgentDiffusions(agentId);
              data = {
                listeDiffusion: diffusions,
                diffusion: diffusions,
                diffusionAgent: diffusions,
              };
              console.log('[ValueField] diffusions extraites:', diffusions);
            } catch (err) {
              console.warn('Impossible de récupérer les diffusions:', err);
            }
            break;
        }

        if (data) {
          // Extraire la valeur selon les champs spécifiés
          let extractedValue: any = null;
          
          // Si on a un template value, extraire le chemin depuis le template
          if (champ.value) {
            // Extraire le chemin depuis le template (ex: "${agentDirection.materielEtDroit.droitAgent}" -> "droitAgent")
            const templateMatch = champ.value.match(/\$\{[^}]+\}/);
            if (templateMatch) {
              const fullPath = templateMatch[0].replace(/\$\{|\}/g, ''); // "agentDirection.materielEtDroit.droitAgent"
              const pathParts = fullPath.split('.');
              
              const pathWithoutPrefixes = [...pathParts];
              if (pathParts[0] === 'agent') {
                pathWithoutPrefixes.shift();
              } else if (pathParts[0] === 'agentDirection' && pathParts[1] === 'materielEtDroit') {
                pathWithoutPrefixes.splice(0, 2);
              } else if (pathParts[0] === 'agentMaterielEtDroit') {
                pathWithoutPrefixes.shift();
              } else if (pathParts[0] === 'agentDiffusion') {
                pathWithoutPrefixes.shift();
              }
              const normalizedPath = pathWithoutPrefixes.join('.');
              extractedValue = normalizedPath ? getValueAtPath(data, normalizedPath) : data;
            }
          } else {
            // Sinon, utiliser la logique originale avec champ.champs
            extractedValue = data;
            for (const field of champ.champs) {
              const normalizedField = champ.baseDonnees === 'agentMaterielEtDroit'
                ? normalizeMaterielPath(field)
                : field;
              extractedValue = getValueAtPath(extractedValue, normalizedField);
            }
          }

          console.log('[ValueField] extractedValue:', extractedValue);

          // Si on a un template avec variables, remplacer les variables
          if (champ.value && extractedValue !== null && extractedValue !== undefined) {
            let templateValue = champ.value;
            // Remplacer les variables du template
            if (data) {
              templateValue = templateValue.replace(/\$\{agent\.email\}/g, data.email || '');
              templateValue = templateValue.replace(/\$\{agent\.nom\}/g, data.nom || '');
              templateValue = templateValue.replace(/\$\{agent\.prenom\}/g, data.prenom || '');
            }
            // Remplacer la variable materielEtDroit.droitAgent (ou droit en fallback)
            if (materielEtDroit) {
              let droitAgentValue = '';
              if (Array.isArray(extractedValue)) {
                // Normaliser la liste : si c'est une liste avec une seule string qui contient des éléments séparés par des virgules
                const normalizedList = normalizeList(extractedValue);
                droitAgentValue = normalizedList.join(', ');
              } else if (extractedValue !== null && extractedValue !== undefined) {
                droitAgentValue = String(extractedValue);
              } else {
                // Fallback: essayer d'extraire "droit" directement
                if (typeof materielEtDroit === 'object' && 'droit' in materielEtDroit) {
                  const droitValue = materielEtDroit['droit'];
                  if (Array.isArray(droitValue)) {
                    const normalizedList = normalizeList(droitValue);
                    droitAgentValue = normalizedList.join(', ');
                  } else {
                    droitAgentValue = String(droitValue || '');
                  }
                  console.log('[ValueField] Fallback: utilisation de "droit" depuis materielEtDroit:', droitAgentValue);
                }
              }
              console.log('[ValueField] droitAgentValue final:', droitAgentValue);
              templateValue = templateValue.replace(/\$\{agentDirection\.materielEtDroit\.droitAgent\}/g, droitAgentValue);
            }
            setLoadedValue(templateValue);
          } else {
            // Si extractedValue est un tableau, l'afficher comme une liste
            if (Array.isArray(extractedValue)) {
              const normalizedList = normalizeList(extractedValue);
              setLoadedValue(normalizedList.length > 0 ? normalizedList.join(', ') : 'Aucun');
            } else if (extractedValue && typeof extractedValue === 'object') {
              // Afficher un objet (ex: materiel: { pc: en_possession })
              const entries = Object.entries(extractedValue);
              if (entries.length === 0) {
                setLoadedValue('Aucun');
              } else {
                const formatted = entries.map(([key, val]) => `${key}: ${val}`);
                setLoadedValue(formatted.join(', '));
              }
            } else if (typeof extractedValue === 'string') {
              const trimmed = extractedValue.trim();
              if (trimmed.startsWith('[') && trimmed.endsWith(']')) {
                const normalizedList = normalizeList([trimmed]);
                setLoadedValue(normalizedList.length > 0 ? normalizedList.join(', ') : 'Aucun');
              } else {
                setLoadedValue(trimmed || 'Aucun');
              }
            } else {
              setLoadedValue(extractedValue !== null && extractedValue !== undefined ? String(extractedValue) : 'Aucun');
            }
          }
        } else {
          setLoadedValue('Non disponible');
        }
      } catch (err) {
        console.error('Erreur lors de la récupération de la valeur:', err);
        setError('Erreur lors du chargement');
        setLoadedValue(null);
      } finally {
        setLoading(false);
      }
    };

    fetchValue();
  }, [champ.baseDonnees, champ.champs, champ.value, agentId]);

  // Si loadedValue est une liste d'éléments (séparés par des virgules), les afficher comme une liste
  const displayValue = loadedValue || 'Non disponible';
  const isList = displayValue.includes(',') && !displayValue.startsWith('${');

  return (
    <div className={`flex ${isList ? 'flex-col' : 'items-center'} w-full rounded-md border border-input bg-muted/50 px-3 py-2 text-sm text-foreground ${isList ? 'py-3' : 'h-10'}`}>
      {loading ? (
        <span className="text-muted-foreground">Chargement...</span>
      ) : error ? (
        <span className="text-destructive">{error}</span>
      ) : isList ? (
        <div className="flex flex-col gap-1">
          {displayValue.split(',').map((item, index) => (
            <div key={index} className="flex items-center gap-2">
              <span className="w-2 h-2 rounded-full bg-primary"></span>
              <span>{item.trim()}</span>
            </div>
          ))}
        </div>
      ) : (
        <span>{displayValue}</span>
      )}
    </div>
  );
}

// Composant pour gérer un champ de type "etat_changement"
function EtatChangementField({
  champ,
  value: _value,
  onChange,
  agentId,
  readOnly = false,
}: {
  champ: ChampFormulaire;
  value: any;
  onChange: (name: string, value: any) => void;
  agentId?: string;
  readOnly?: boolean;
}) {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [localState, setLocalState] = useState<Record<string, string>>({});

  // Les états définis dans le champ (ex: ["en_cours", "en_possession"])
  const etats = champ.etat || [];
  const premierEtat = etats[0] || '';
  const autresEtats = etats.slice(1);

  useEffect(() => {
    const fetchData = async () => {
      if (!champ.baseDonnees || !champ.champs || !agentId) {
        setLoading(false);
        return;
      }

      try {
        setLoading(true);
        setError(null);
        console.log('[EtatChangementField] Champ:', {
          name: champ.name,
          label: champ.label,
          baseDonnees: champ.baseDonnees,
          champs: champ.champs,
          etats,
          agentId
        });

        // Récupérer les données depuis agentMaterielEtDroit
        if (champ.baseDonnees === 'agentMaterielEtDroit') {
          const materiel = await agentService.getAgentMateriel(agentId);
          const materielEtDroit = normalizeMaterielPayload(materiel);
          
          console.log('[EtatChangementField] Materiel récupéré:', materiel);
          console.log('[EtatChangementField] materielEtDroit:', materielEtDroit);
          console.log('[EtatChangementField] materielEtDroit clés disponibles:', Object.keys(materielEtDroit || {}));
          console.log('[EtatChangementField] champ.champs:', champ.champs);

          // Extraire les données selon le chemin (ex: materielAgent)
          let extractedData: any = materielEtDroit;
          if (champ.champs && champ.champs.length > 0) {
            for (const field of champ.champs) {
              const normalizedField = normalizeMaterielPath(field);
              console.log('[EtatChangementField] Extraction du champ:', normalizedField);
              extractedData = getValueAtPath(extractedData, normalizedField);
              console.log(`[EtatChangementField] Valeur trouvée:`, extractedData);
              if (extractedData === undefined) break;
            }
          }
          
          console.log('[EtatChangementField] extractedData final:', extractedData);

          // Si extractedData est undefined, essayer d'utiliser "material" comme fallback
          if (extractedData === undefined && materielEtDroit && typeof materielEtDroit === 'object') {
            // Essayer plusieurs clés possibles pour les matériels
            extractedData = materielEtDroit['material'] || 
                          materielEtDroit['materielAgent'] || 
                          materielEtDroit['materiel'] ||
                          materielEtDroit['materiels'] ||
                          materielEtDroit['materials'];
            console.log('[EtatChangementField] Fallback - tentative avec différentes clés:', {
              material: materielEtDroit['material'],
              materielAgent: materielEtDroit['materielAgent'],
              materiel: materielEtDroit['materiel'],
              materiels: materielEtDroit['materiels'],
              materials: materielEtDroit['materials'],
              result: extractedData
            });
          }

          // Si c'est une Map (objet), la convertir en Record<string, string>
          if (Array.isArray(extractedData)) {
            const normalized = normalizeList(extractedData);
            const dataMap: Record<string, string> = {};
            normalized.forEach((item) => {
              dataMap[item] = premierEtat;
            });
            console.log('[EtatChangementField] dataMap créé depuis liste:', dataMap);
            setLocalState(dataMap);
            console.log('[EtatChangementField] localState initial:', dataMap);
          } else if (extractedData && typeof extractedData === 'object') {
            const dataMap: Record<string, string> = {};
            for (const [key, val] of Object.entries(extractedData)) {
              dataMap[key] = String(val || premierEtat);
            }
            console.log('[EtatChangementField] dataMap créé:', dataMap);
            setLocalState(dataMap);
            console.log('[EtatChangementField] localState initial:', dataMap);
          } else {
            console.warn('[EtatChangementField] Aucun matériel trouvé. extractedData:', extractedData);
            console.warn('[EtatChangementField] Structure complète de materielEtDroit:', JSON.stringify(materielEtDroit, null, 2));
            // Si aucun matériel n'est trouvé, initialiser avec un état vide
            setLocalState({});
          }
        }
      } catch (err) {
        console.error('Erreur lors de la récupération des données:', err);
        setError('Erreur lors du chargement');
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [champ.baseDonnees, champ.champs, agentId, premierEtat]);

  // Gérer le changement d'état d'un élément
  const handleEtatChange = (elementName: string, nouvelEtat: string) => {
    if (readOnly) {
      return;
    }
    const newState = { ...localState };
    newState[elementName] = nouvelEtat;
    setLocalState(newState);
    
    // Mettre à jour la valeur du formulaire
    // Le format attendu par l'action Update_AGENT_MATERIEL est une Map des états
    // (le backend convertit désormais vers une liste de matériels)
    onChange(champ.name, newState);
  };

  // Organiser les éléments par état
  const elementsParEtat: Record<string, string[]> = {};
  etats.forEach(etat => {
    elementsParEtat[etat] = [];
  });

  Object.entries(localState).forEach(([elementName, etat]) => {
    if (elementsParEtat[etat]) {
      elementsParEtat[etat].push(elementName);
    }
  });

  if (loading) {
    return (
      <div className="w-full rounded-md border border-input bg-muted/50 px-3 py-2 h-10 flex items-center">
        <span className="text-muted-foreground">Chargement...</span>
      </div>
    );
  }

  if (error) {
    return (
      <div className="w-full rounded-md border border-input bg-muted/50 px-3 py-2 h-10 flex items-center">
        <span className="text-destructive">{error}</span>
      </div>
    );
  }

  // Si aucun matériel n'a été trouvé
  if (Object.keys(localState).length === 0 && !loading) {
    return (
      <div className="w-full space-y-4">
        <Alert>
          <AlertCircle className="h-4 w-4" />
          <AlertTitle>Aucun matériel trouvé</AlertTitle>
          <AlertDescription>
            Aucun matériel n'a été trouvé pour cet agent. Les matériels doivent être créés 
            via l'action CREATE_AGENT_MATERIEL avant de pouvoir gérer leurs états.
          </AlertDescription>
        </Alert>
      </div>
    );
  }

  // Vérifier si tous les éléments sont passés du premier état
  const elementsPremierEtat = elementsParEtat[premierEtat] || [];
  const isAllMoved = elementsPremierEtat.length === 0 && Object.keys(localState).length > 0;

  return (
    <div className="w-full space-y-4">
      <div className="flex flex-col gap-4">
        {etats.map((etat, index) => {
          const elements = elementsParEtat[etat] || [];
          const isPremierEtat = index === 0;

          return (
            <div
              key={etat}
              className={`w-full rounded-md border p-4 ${
                isPremierEtat && elements.length > 0
                  ? 'border-destructive bg-destructive/5'
                  : 'border-input bg-card'
              }`}
            >
              <div className="flex items-center justify-between mb-2">
                <h4 className="font-medium text-sm capitalize">
                  {etat.replace('_', ' ')}
                </h4>
                <span className="text-xs text-muted-foreground">
                  ({elements.length})
                </span>
              </div>
              <div className="space-y-2 min-h-[100px]">
                {elements.length === 0 ? (
                  <p className="text-xs text-muted-foreground text-center py-4">
                    Aucun élément
                  </p>
                ) : (
                  elements.map((elementName) => (
                    <div
                      key={elementName}
                      className="flex items-center justify-between p-2 bg-background rounded border"
                    >
                      <span className="text-sm">{elementName}</span>
                      {!isPremierEtat && (
                      <Button
                          type="button"
                          variant="ghost"
                          size="sm"
                          className="h-6 w-6 p-0"
                          onClick={() => handleEtatChange(elementName, premierEtat)}
                          title="Remettre au premier état"
                        disabled={readOnly}
                        >
                          ←
                        </Button>
                      )}
                    </div>
                  ))
                )}
              </div>
              {isPremierEtat && elements.length > 0 && (
                <div className="mt-2 space-y-1">
                  {autresEtats.map((autreEtat) => (
                    <Button
                      key={autreEtat}
                      type="button"
                      variant="outline"
                      size="sm"
                      className="w-full text-xs"
                      onClick={() => {
                        // Déplacer tous les éléments du premier état vers cet autre état
                        const newState = { ...localState };
                        elements.forEach((elementName) => {
                          newState[elementName] = autreEtat;
                        });
                        setLocalState(newState);
                        onChange(champ.name, newState);
                      }}
                      disabled={readOnly}
                    >
                      → {autreEtat.replace('_', ' ')}
                    </Button>
                  ))}
                </div>
              )}
            </div>
          );
        })}
      </div>
      {!isAllMoved && Object.keys(localState).length > 0 && (
        <Alert>
          <AlertCircle className="h-4 w-4" />
          <AlertTitle>Attention</AlertTitle>
          <AlertDescription>
            Tous les éléments doivent être passés de l'état "{premierEtat.replace('_', ' ')}" 
            pour pouvoir valider la tâche.
          </AlertDescription>
        </Alert>
      )}
    </div>
  );
}

// Composant pour un champ de formulaire
function ChampForm({ champ, value, onChange, loadedOptions, agentId, readOnly = false }: { 
  champ: ChampFormulaire; 
  value: any;
  onChange: (name: string, value: any) => void;
  loadedOptions?: OptionDonnees[];
  agentId?: string;
  readOnly?: boolean;
}) {
  const [patternError, setPatternError] = useState<string | null>(null);
  const [inputValue, setInputValue] = useState<string>(''); // Valeur temporaire pour l'input
  const [listeElements, setListeElements] = useState<string[]>(() => {
    // Initialiser avec la valeur existante si c'est un tableau, sinon tableau vide
    if (Array.isArray(value)) {
      return value;
    }
    return value ? [value] : [];
  });

  // Synchroniser la liste avec la valeur si elle change depuis l'extérieur (pour addableToList)
  useEffect(() => {
    if (champ.addableToList) {
      if (Array.isArray(value)) {
        setListeElements(value);
      } else if (value && typeof value === 'string') {
        setListeElements([value]);
      } else if (!value) {
        setListeElements([]);
      }
    }
  }, [value, champ.addableToList]);

  // Valider le pattern si présent
  const validatePattern = (inputValue: string) => {
    if (!champ.pattern || !inputValue) {
      setPatternError(null);
      return true;
    }

    try {
      const regex = new RegExp(champ.pattern);
      const isValid = regex.test(inputValue);
      
      if (!isValid) {
        // Message d'erreur plus convivial selon le pattern
        let errorMessage = "Le format saisi n'est pas valide.";
        
        // Messages spécifiques pour certains patterns courants
        if (champ.pattern.includes('@lecese\\.fr')) {
          errorMessage = "L'adresse email doit se terminer par @lecese.fr";
        } else if (champ.pattern.includes('email')) {
          errorMessage = "Le format de l'adresse email n'est pas valide.";
        } else {
          errorMessage = `Le format saisi n'est pas valide. Format attendu : ${champ.pattern}`;
        }
        
        setPatternError(errorMessage);
      } else {
        setPatternError(null);
      }
      
      return isValid;
    } catch (e) {
      // Si le pattern n'est pas une regex valide, on ignore
      setPatternError(null);
      return true;
    }
  };

  const handleChange = (newValue: string) => {
    onChange(champ.name, newValue);
    // Effacer l'erreur lors du changement pour permettre à l'utilisateur de corriger
    if (patternError) {
      setPatternError(null);
    }
  };

  const getIcon = () => {
    const iconName = champ.type?.toLowerCase() || '';
    if (iconName.includes('calendar') || iconName.includes('date')) {
      return <Calendar className="w-4 h-4" />;
    }
    if (iconName.includes('image') || iconName.includes('file')) {
      return <ImageIcon className="w-4 h-4" />;
    }
    if (iconName.includes('user') || iconName.includes('person')) {
      return <User className="w-4 h-4" />;
    }
    if (iconName.includes('building') || iconName.includes('direction')) {
      return <Building2 className="w-4 h-4" />;
    }
    return <NotebookPen className="w-4 h-4" />;
  };

  const renderField = () => {
    // Si c'est un champ de type "value", utiliser le composant ValueField
    if (champ.type === 'value') {
      return <ValueField champ={champ} agentId={agentId} />;
    }

    // Si c'est un champ de type "etat_changement", utiliser le composant EtatChangementField
    if (champ.type === 'etat_changement') {
      return (
        <EtatChangementField
          champ={champ}
          value={value}
          onChange={onChange}
          agentId={agentId}
          readOnly={readOnly}
        />
      );
    }

    // Normaliser les types du backend vers les types du frontend
    const normalizedType = champ.type === 'texte' ? 'text' : 
                          champ.type === 'image' ? 'file' : 
                          champ.type;
    
    switch (normalizedType) {
      case 'date':
        return (
          <DatePicker
            value={value || ''}
            onChange={(newValue) => onChange(champ.name, newValue)}
            placeholder={champ.placeholder || 'Sélectionner une date'}
            required={champ.required}
            min={champ.minDate}
            max={champ.maxDate}
            disabled={readOnly}
          />
        );
      case 'file': {
        const [fileError, setFileError] = useState<string | null>(null);
        const fileInputRef = React.useRef<HTMLInputElement>(null);
        
        // Gérer les fichiers (peut être un File ou un tableau de Files)
        const currentFiles = Array.isArray(value) ? value : (value ? [value] : []);
        const isImageType = champ.type === 'image' || (champ.mimeType && champ.mimeType.some(mt => mt.startsWith('image/')));
        
        // Créer et nettoyer les URLs d'objets pour éviter les fuites mémoire
        const [fileUrls, setFileUrls] = useState<Map<number, string>>(new Map());
        
        // Créer une clé stable pour les fichiers basée sur leur nom et taille
        const filesKey = currentFiles.map((file: any, index: number) => 
          file instanceof File ? `${index}-${file.name}-${file.size}` : `${index}-${String(file)}`
        ).join('|');
        
        useEffect(() => {
          const urls = new Map<number, string>();
          currentFiles.forEach((file: any, index: number) => {
            if (file instanceof File) {
              urls.set(index, URL.createObjectURL(file));
            }
          });
          setFileUrls(urls);
          
          return () => {
            urls.forEach(url => URL.revokeObjectURL(url));
          };
        }, [filesKey]); // Utiliser filesKey au lieu de currentFiles pour une comparaison stable
        
        const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
          const files = Array.from(e.target.files || []);
          if (files.length === 0) return;
          
          // Valider le type MIME si spécifié
          if (champ.mimeType && champ.mimeType.length > 0) {
            const invalidFiles = files.filter(file => !champ.mimeType!.includes(file.type));
            if (invalidFiles.length > 0) {
              const allowedTypes = champ.mimeType.join(', ');
              setFileError(`Type de fichier non autorisé. Types acceptés : ${allowedTypes}`);
              return;
            }
          }
          
          setFileError(null);
          
          // Ajouter les nouveaux fichiers aux fichiers existants
          const newFiles = [...currentFiles, ...files];
          onChange(champ.name, newFiles.length === 1 ? newFiles[0] : newFiles);
          
          // Réinitialiser l'input pour permettre de sélectionner le même fichier à nouveau
          if (fileInputRef.current) {
            fileInputRef.current.value = '';
          }
        };
        
        const handleRemoveFile = (index: number) => {
          const newFiles = currentFiles.filter((_: any, i: number) => i !== index);
          onChange(champ.name, newFiles.length === 0 ? null : (newFiles.length === 1 ? newFiles[0] : newFiles));
        };
        
        const handleButtonClick = () => {
          fileInputRef.current?.click();
        };
        
        return (
          <div className="space-y-2">
            <div className="flex items-center gap-2">
              <input
                ref={fileInputRef}
                type="file"
                accept={champ.mimeType ? champ.mimeType.join(',') : undefined}
                multiple={false}
                className="hidden"
                onChange={handleFileSelect}
                disabled={readOnly}
              />
              <Button
                type="button"
                variant="outline"
                onClick={handleButtonClick}
                className="flex items-center gap-2"
                disabled={readOnly}
              >
                <Upload className="w-4 h-4" />
                {isImageType ? 'Ajouter une image' : 'Ajouter un fichier'}
              </Button>
            </div>
            
            {fileError && (
              <Alert variant="destructive">
                <AlertCircle className="h-4 w-4" />
                <AlertTitle>Erreur</AlertTitle>
                <AlertDescription>{fileError}</AlertDescription>
              </Alert>
            )}
            
            {currentFiles.length > 0 && (
              <div className="space-y-2">
                {currentFiles.map((file: File, index: number) => {
                  const fileUrl = fileUrls.get(index) || null;
                  const fileName = file instanceof File ? file.name : 'Fichier';
                  
                  return (
                    <div key={index} className="flex items-center gap-2 p-2 border rounded-md">
                      {isImageType && fileUrl ? (
                        <img 
                          src={fileUrl} 
                          alt={fileName}
                          className="w-16 h-16 object-cover rounded"
                        />
                      ) : (
                        <div className="w-16 h-16 flex items-center justify-center bg-muted rounded">
                          <ImageIcon className="w-8 h-8 text-muted-foreground" />
                        </div>
                      )}
                      <div className="flex-1 min-w-0">
                        <p className="text-sm font-medium truncate">{fileName}</p>
                        {file instanceof File && (
                          <p className="text-xs text-muted-foreground">
                            {(file.size / 1024).toFixed(2)} KB
                          </p>
                        )}
                      </div>
                      <Button
                        type="button"
                        variant="ghost"
                        size="icon"
                        onClick={() => handleRemoveFile(index)}
                        className="shrink-0"
                        disabled={readOnly}
                      >
                        <X className="w-4 h-4" />
                      </Button>
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        );
      }
      case 'liste':
        // Composant pour gérer une liste d'éléments avec ajout/suppression
        const listeValue = Array.isArray(value) ? value : (value ? [value] : []);
        return (
          <div className="space-y-2">
            <div className="space-y-2">
              {listeValue.map((item: string, index: number) => (
                <div key={index} className="flex gap-2 items-center">
                  <InputGroup className="flex-1">
                    <InputGroupAddon align="inline-start">
                      {getIcon()}
                    </InputGroupAddon>
                    <InputGroupInput
                      type="text"
                      placeholder={champ.placeholder || `Élément ${index + 1}`}
                      value={item || ''}
                      onChange={(e) => {
                        const newListe = [...listeValue];
                        newListe[index] = e.target.value;
                        onChange(champ.name, newListe);
                      }}
                    disabled={readOnly}
                    />
                  </InputGroup>
                  <button
                    type="button"
                    onClick={() => {
                      const newListe = listeValue.filter((_: any, i: number) => i !== index);
                      onChange(champ.name, newListe.length > 0 ? newListe : []);
                    }}
                    className="px-3 py-2 text-sm bg-destructive text-destructive-foreground rounded-md hover:bg-destructive/90"
                    disabled={readOnly}
                  >
                    Supprimer
                  </button>
                </div>
              ))}
            </div>
            <button
              type="button"
              onClick={() => {
                const newListe = [...listeValue, ''];
                onChange(champ.name, newListe);
              }}
              className="px-4 py-2 text-sm bg-primary text-primary-foreground rounded-md hover:bg-primary/90"
              disabled={readOnly}
            >
              + Ajouter
            </button>
          </div>
        );
      case 'select':
        // Si loadedOptions est fourni, utiliser les données de la base
        if (loadedOptions && loadedOptions.length > 0) {
          const comboboxOptions: ComboboxOption[] = loadedOptions.map((option) => ({
            value: option.id,
            label: option.libelle,
          }));
          
          return (
            <Combobox
              options={comboboxOptions}
              value={value || ''}
              onValueChange={(newValue) => onChange(champ.name, newValue)}
              placeholder={champ.placeholder || 'Sélectionner...'}
              searchPlaceholder="Rechercher..."
              emptyText="Aucun résultat trouvé."
              disabled={readOnly}
            />
          );
        }
        // Sinon, utiliser les options statiques
        if (Array.isArray(champ.options)) {
          const comboboxOptions: ComboboxOption[] = champ.options.map((option) => ({
            value: option,
            label: option,
          }));
          
          return (
            <Combobox
              options={comboboxOptions}
              value={value || ''}
              onValueChange={(newValue) => onChange(champ.name, newValue)}
              placeholder={champ.placeholder || 'Sélectionner...'}
              searchPlaceholder="Rechercher..."
              emptyText="Aucun résultat trouvé."
              disabled={readOnly}
            />
          );
        }
        // Si aucune option n'est disponible
        return (
          <Combobox
            options={[]}
            value={value || ''}
            onValueChange={(newValue) => onChange(champ.name, newValue)}
            placeholder={champ.placeholder || 'Sélectionner...'}
            searchPlaceholder="Rechercher..."
            emptyText="Aucune option disponible."
            disabled={true}
          />
        );
      case 'email':
        return (
          <div className="space-y-2">
            <InputGroup>
              <InputGroupAddon align="inline-start">
                {getIcon()}
              </InputGroupAddon>
              <InputGroupInput
                type="email"
                placeholder={champ.placeholder}
                required={champ.required}
                value={value || ''}
                onChange={(e) => handleChange(e.target.value)}
                onBlur={(e) => {
                  // Valider uniquement si une valeur a été saisie
                  if (champ.pattern && e.target.value && e.target.value.trim() !== '') {
                    validatePattern(e.target.value);
                  } else if (!e.target.value || e.target.value.trim() === '') {
                    // Effacer l'erreur si le champ est vide
                    setPatternError(null);
                  }
                }}
                disabled={readOnly}
              />
            </InputGroup>
            {patternError && (
              <Alert variant="destructive">
                <AlertCircle className="h-4 w-4" />
                <AlertTitle>Format invalide</AlertTitle>
                <AlertDescription>{patternError}</AlertDescription>
              </Alert>
            )}
          </div>
        );
      case 'input':
      case 'text':
      default:
        // Pour les champs de type "input", activer automatiquement la fonctionnalité d'ajout à la liste
        // Pour "text", activer seulement si addableToList est explicitement défini à true
        const shouldEnableAddToList = champ.type === 'input' || champ.addableToList === true;
        
        // Si le champ permet d'ajouter des éléments à une liste
        if (shouldEnableAddToList) {
          const handleAddToList = () => {
            const trimmedValue = inputValue.trim();
            if (trimmedValue === '') return;

            // Valider le pattern si présent
            if (champ.pattern) {
              try {
                const regex = new RegExp(champ.pattern);
                if (!regex.test(trimmedValue)) {
                  setPatternError("Le format saisi n'est pas valide.");
                  return;
                }
              } catch (e) {
                // Pattern invalide, on ignore
              }
            }

            // Ajouter à la liste
            const nouvelleListe = [...listeElements, trimmedValue];
            setListeElements(nouvelleListe);
            setInputValue(''); // Réinitialiser l'input
            setPatternError(null);
            
            // Notifier le changement (on envoie un tableau)
            onChange(champ.name, nouvelleListe);
          };

          const handleRemoveFromList = (index: number) => {
            const nouvelleListe = listeElements.filter((_, i) => i !== index);
            setListeElements(nouvelleListe);
            onChange(champ.name, nouvelleListe.length > 0 ? nouvelleListe : []);
          };

          const handleKeyPress = (e: React.KeyboardEvent<HTMLInputElement>) => {
            if (e.key === 'Enter') {
              e.preventDefault();
              handleAddToList();
            }
          };

          return (
            <div className="space-y-2">
              <div className="flex items-center gap-2">
                <InputGroup className="flex-1">
                  <InputGroupAddon align="inline-start">
                    {getIcon()}
                  </InputGroupAddon>
                  <InputGroupInput
                    type="text"
                    placeholder={champ.placeholder}
                    required={champ.required}
                    minLength={champ.minLength}
                    maxLength={champ.maxLength}
                    pattern={champ.pattern}
                    value={inputValue}
                    onChange={(e) => {
                      setInputValue(e.target.value);
                      if (patternError) {
                        setPatternError(null);
                      }
                    }}
                    onKeyPress={handleKeyPress}
                    onBlur={(e) => {
                      // Valider uniquement si une valeur a été saisie
                      if (champ.pattern && e.target.value && e.target.value.trim() !== '') {
                        validatePattern(e.target.value);
                      } else if (!e.target.value || e.target.value.trim() === '') {
                        // Effacer l'erreur si le champ est vide
                        setPatternError(null);
                      }
                    }}
                    disabled={readOnly}
                  />
                </InputGroup>
                <Button
                  type="button"
                  variant="outline"
                  onClick={handleAddToList}
                  className="shrink-0"
                  title="Ajouter à la liste"
                  disabled={readOnly}
                >
                  <Plus className="w-4 h-4" />
                </Button>
              </div>
              
              {patternError && (
                <Alert variant="destructive">
                  <AlertCircle className="h-4 w-4" />
                  <AlertTitle>Format invalide</AlertTitle>
                  <AlertDescription>{patternError}</AlertDescription>
                </Alert>
              )}

              {/* Liste des éléments ajoutés */}
              {listeElements.length > 0 && (
                <div className="space-y-2 mt-2">
                  {listeElements.map((element, index) => (
                    <div 
                      key={index} 
                      className="flex items-center gap-2 p-2 border rounded-md bg-muted/50"
                    >
                      <span className="flex-1 text-sm">{element}</span>
                      <Button
                        type="button"
                        variant="ghost"
                        size="icon"
                        onClick={() => handleRemoveFromList(index)}
                        className="shrink-0 h-7 w-7"
                        title="Supprimer"
                      disabled={readOnly}
                      >
                        <X className="w-4 h-4" />
                      </Button>
                    </div>
                  ))}
                </div>
              )}
            </div>
          );
        }

        // Comportement normal pour les inputs sans liste
        return (
          <div className="space-y-2">
            <InputGroup>
              <InputGroupAddon align="inline-start">
                {getIcon()}
              </InputGroupAddon>
              <InputGroupInput
                type="text"
                placeholder={champ.placeholder}
                required={champ.required}
                minLength={champ.minLength}
                maxLength={champ.maxLength}
                pattern={champ.pattern}
                value={value || ''}
                onChange={(e) => handleChange(e.target.value)}
                onBlur={(e) => {
                  // Valider uniquement si une valeur a été saisie
                  if (champ.pattern && e.target.value && e.target.value.trim() !== '') {
                    validatePattern(e.target.value);
                  } else if (!e.target.value || e.target.value.trim() === '') {
                    // Effacer l'erreur si le champ est vide
                    setPatternError(null);
                  }
                }}
                disabled={readOnly}
              />
            </InputGroup>
            {patternError && (
              <Alert variant="destructive">
                <AlertCircle className="h-4 w-4" />
                <AlertTitle>Format invalide</AlertTitle>
                <AlertDescription>{patternError}</AlertDescription>
              </Alert>
            )}
          </div>
        );
    }
  };

  return (
    <div className="flex flex-col gap-2">
      <Label>
        {champ.label}
        {champ.required && <span className="text-red-500 ml-1">*</span>}
      </Label>
      {renderField()}
    </div>
  );
}

// Composant principal du formulaire dynamique
export function FormulaireDynamique({ 
  templateTache,
  initialValues,
  onFormDataChange,
  onValidationChange,
  agentId,
  readOnly = false,
  modePrestataire = false,
  modeConseiller = false,
}: { 
  templateTache: TemplateTacheDTO | null;
  initialValues?: Record<string, any>;
  onFormDataChange?: (formData: Record<string, any>) => void;
  onValidationChange?: (isValid: boolean, errors: string[]) => void;
  agentId?: string; // ID de l'agent pour récupérer les valeurs de type "value"
  readOnly?: boolean;
  modePrestataire?: boolean;
  modeConseiller?: boolean;
}) {
  const [formData, setFormData] = useState<Record<string, any>>(() => initialValues || {});
  const [loadedOptions, setLoadedOptions] = useState<Record<string, OptionDonnees[]>>({});
  const isInitialMount = useRef(true);
  const onFormDataChangeRef = useRef(onFormDataChange);
  const onValidationChangeRef = useRef(onValidationChange);
  const lastValidationRef = useRef<{ isValid: boolean; errors: string[] } | null>(null);

  // Mettre à jour la référence de onFormDataChange sans déclencher de re-render
  useEffect(() => {
    onFormDataChangeRef.current = onFormDataChange;
  }, [onFormDataChange]);

  // Mettre à jour la référence de onValidationChange sans déclencher de re-render
  useEffect(() => {
    onValidationChangeRef.current = onValidationChange;
  }, [onValidationChange]);

  // Parser le contenu une seule fois avec useMemo
  const contenuParsed = useMemo(() => {
    if (!templateTache) return {};
    
    if (typeof templateTache.contenu === 'string') {
      try {
        return JSON.parse(templateTache.contenu);
      } catch (e) {
        return {};
      }
    }
    return templateTache.contenu || {};
  }, [templateTache?.contenu]);

  // Parser les champs une seule fois avec useMemo
  const champs = useMemo(() => {
    return parseContenu(contenuParsed);
  }, [contenuParsed]);

  // Initialiser formData uniquement quand initialValues change
  // Ne pas écraser les valeurs pré-remplies (alreadyExist)
  useEffect(() => {
    if (!initialValues || Object.keys(initialValues).length === 0) {
      return;
    }
    setFormData(prev => {
      const next = { ...prev };
      champs.forEach(champ => {
        if (initialValues[champ.name] !== undefined) {
          const current = next[champ.name];
          if (current === undefined || current === null || current === '') {
            next[champ.name] = initialValues[champ.name];
          }
        }
      });
      return next;
    });
    isInitialMount.current = true; // Marquer comme initialisation
  }, [initialValues, champs]);

  // Appeler onFormDataChange après que formData a été mis à jour (mais pas lors de l'initialisation)
  useEffect(() => {
    if (isInitialMount.current) {
      isInitialMount.current = false;
      return; // Ne pas appeler onFormDataChange lors de l'initialisation
    }
    
    if (onFormDataChangeRef.current) {
      onFormDataChangeRef.current(formData);
    }
  }, [formData]); // Ne plus inclure onFormDataChange dans les dépendances

  if (!templateTache) {
    return null;
  }

  // Charger les options depuis la base de données pour les selects
  useEffect(() => {
    // Créer une liste stable des noms de champs qui nécessitent des options
    const selectChamps = champs.filter(
      champ => champ.type === 'select' && 
      champ.options && 
      typeof champ.options === 'object' && 
      !Array.isArray(champ.options) &&
      (champ.options as { accesBaseDonnees?: string }).accesBaseDonnees
    );
    
    if (selectChamps.length === 0) {
      return;
    }

    const loadOptions = async () => {
      const optionsToLoad: Record<string, Promise<OptionDonnees[]>> = {};
      const immediateResults: Record<string, OptionDonnees[]> = {};

      selectChamps.forEach((champ) => {
        const options = champ.options as { accesBaseDonnees?: string; champs?: string[] };
        if (options.accesBaseDonnees) {
          switch (options.accesBaseDonnees) {
            case 'direction':
              if (modePrestataire && champ.name === 'direction') {
                optionsToLoad[champ.name] = directionService.getDirections().then(directions => {
                  const current = getCurrentAgent();
                  const currentDirection = String(current.direction || "").trim().toUpperCase();

                  const target = directions.find(dir => {
                    const code = String(dir.code || '').toLowerCase();
                    const libelle = String(dir.libelle || '').toUpperCase();
                    const normalizedCurrent = currentDirection.replace(/\./g, "").replace(/\s+/g, "");
                    const normalizedLibelle = libelle.replace(/\./g, "").replace(/\s+/g, "");

                    return (
                      normalizedLibelle === normalizedCurrent ||
                      code === normalizedCurrent.toLowerCase()
                    );
                  });

                  if (!target) {
                    // Si on ne trouve pas la direction de l'utilisateur, ne pas casser le select:
                    // retourner toutes les directions (le forcing plus bas ne s'appliquera
                    // que si on a exactement 1 option).
                    return directions.map(dir => ({ id: dir.id, libelle: dir.libelle, code: dir.code }));
                  }

                  return [{
                    id: target.id,
                    libelle: 'Prestataire (liaison avec les directions du CESE)',
                    code: target.code,
                  }];
                });
              } else if (modeConseiller && champ.name === 'direction') {
                optionsToLoad[champ.name] = directionService.getDirections().then(directions => {
                  const current = getCurrentAgent();
                  const currentDirection = String(current.direction || "").trim().toUpperCase();

                  const target = directions.find(dir => {
                    const code = String(dir.code || '').toLowerCase();
                    const libelle = String(dir.libelle || '').toUpperCase();
                    const normalizedCurrent = currentDirection.replace(/\./g, "").replace(/\s+/g, "");
                    const normalizedLibelle = libelle.replace(/\./g, "").replace(/\s+/g, "");

                    return (
                      normalizedLibelle === normalizedCurrent ||
                      code === normalizedCurrent.toLowerCase()
                    );
                  });

                  if (!target) {
                    // Si on ne trouve pas la direction de l'utilisateur, ne pas casser le select:
                    // retourner toutes les directions (le forcing plus bas ne s'appliquera
                    // que si on a exactement 1 option).
                    return directions.map(dir => ({ id: dir.id, libelle: dir.libelle, code: dir.code }));
                  }

                  return [{
                    id: target.id,
                    libelle: 'Conseiller/Attaché (liaison avec les directions du CESE)',
                    code: target.code,
                  }];
                });
              } else {
                optionsToLoad[champ.name] = directionService.getDirections().then(directions =>
                  directions.map(dir => ({ id: dir.id, libelle: dir.libelle, code: dir.code }))
                );
              }
              break;
            case 'agent':
              optionsToLoad[champ.name] = agentService.getAllAgents(600).then(agents => {
                console.log('[Select agent] agents récupérés:', agents);
                return agents.filter(agent=> agent.etatAgent === 'actif').map(agent => ({ 
                  id: agent.id, 
                  libelle: `${agent.nom} ${agent.prenom}`, 
                  code: agent.code 
                }));
              });
              break;
            case 'role':
              if (modePrestataire) {
                immediateResults[champ.name] = [{
                  id: 'PRESTATAIRE',
                  libelle: 'Prestataire',
                  code: 'PRESTATAIRE',
                }];
              } else if (modeConseiller) {
                immediateResults[champ.name] = [{
                  id: 'CONSEILLER',
                  libelle: 'Conseiller/Attaché',
                  code: 'CONSEILLER',
                }];
              } else {
                optionsToLoad[champ.name] = roleService.getAllRoles().then(roles => {
                  // Sur les formulaires RH de création ou sortie d'un agent (flux classique), 
                  // ne pas proposer "Prestataire" (réservé au mode prestataire DAPPI) 
                  // et ne pas proposer "Conseiller" (réservé au mode conseiller DAF)
                  const shouldFilterSpecialRoles =
                    champ.name === 'role' && (templateTache?.code === 'form_agent_creation_rh' || templateTache?.code === 'form_agent_sortie_rh');
                  const filtered = shouldFilterSpecialRoles
                    ? roles.filter(r => {
                      const code = String(r.code || '').toUpperCase();
                      return code !== 'PRESTATAIRE' && code !== 'CONSEILLER';
                    })
                    : roles;
                  return filtered.map(role => ({ id: role.id, libelle: role.libelle, code: role.code }));
                });
              }
              break;
            default:
              break;
          }
        }
      });

      const results: Record<string, OptionDonnees[]> = { ...immediateResults };
      await Promise.all(
        Object.entries(optionsToLoad).map(async ([name, promise]) => {
          results[name] = await promise;
        })
      );
      setLoadedOptions(results);
    };

    loadOptions();
  }, [champs, modePrestataire, modeConseiller]); // Dépendance sur les champs parsés, le mode prestataire et le mode conseiller

  // En mode prestataire, forcer la direction à la direction de l'utilisateur connecté
  // (affichée comme "Prestataire (liaison avec les directions du CESE)")
  useEffect(() => {
    if (!modePrestataire) return;
    const hasDirectionField = champs.some(c => c.name === 'direction' && c.type === 'select');
    if (!hasDirectionField) return;
    const directionOptions = loadedOptions['direction'];
    if (!directionOptions || directionOptions.length !== 1) return;
    const forced = directionOptions[0];
    if (!forced?.id) return;

    setFormData(prev => {
      if (prev.direction === forced.id) {
        // S'assurer que le libellé est présent pour l'affichage en lecture seule
        if (prev['direction_libelle'] === forced.libelle) return prev;
        return { ...prev, direction_libelle: forced.libelle };
      }
      return {
        ...prev,
        direction: forced.id,
        direction_libelle: forced.libelle,
      };
    });
  }, [modePrestataire, champs, loadedOptions]);

  // En mode conseiller, forcer la direction à la direction de l'utilisateur connecté
  // (affichée comme "Conseiller/Attaché (liaison avec les directions du CESE)")
  useEffect(() => {
    if (!modeConseiller) return;
    const hasDirectionField = champs.some(c => c.name === 'direction' && c.type === 'select');
    if (!hasDirectionField) return;
    const directionOptions = loadedOptions['direction'];
    if (!directionOptions || directionOptions.length !== 1) return;
    const forced = directionOptions[0];
    if (!forced?.id) return;

    setFormData(prev => {
      if (prev.direction === forced.id) {
        // S'assurer que le libellé est présent pour l'affichage en lecture seule
        if (prev['direction_libelle'] === forced.libelle) return prev;
        return { ...prev, direction_libelle: forced.libelle };
      }
      return {
        ...prev,
        direction: forced.id,
        direction_libelle: forced.libelle,
      };
    });
  }, [modeConseiller, champs, loadedOptions]);

  // En mode prestataire, forcer le rôle à PRESTATAIRE (affiché comme "Prestataire")
  useEffect(() => {
    if (!modePrestataire) return;
    const hasRoleField = champs.some(c => c.name === 'role' && c.type === 'select');
    if (!hasRoleField) return;
    const roleOptions = loadedOptions['role'];
    if (!roleOptions || roleOptions.length !== 1) return;
    const forced = roleOptions[0];
    if (!forced?.id) return;

    setFormData(prev => {
      if (prev.role === forced.id) {
        if (prev['role_libelle'] === forced.libelle) return prev;
        return { ...prev, role_libelle: forced.libelle };
      }
      return {
        ...prev,
        role: forced.id,
        role_libelle: forced.libelle,
      };
    });
  }, [modePrestataire, champs, loadedOptions]);

  // En mode conseiller, forcer le rôle à CONSEILLER (affiché comme "Conseiller/Attaché")
  useEffect(() => {
    if (!modeConseiller) return;
    const hasRoleField = champs.some(c => c.name === 'role' && c.type === 'select');
    if (!hasRoleField) return;
    const roleOptions = loadedOptions['role'];
    if (!roleOptions || roleOptions.length !== 1) return;
    const forced = roleOptions[0];
    if (!forced?.id) return;

    setFormData(prev => {
      if (prev.role === forced.id) {
        if (prev['role_libelle'] === forced.libelle) return prev;
        return { ...prev, role_libelle: forced.libelle };
      }
      return {
        ...prev,
        role: forced.id,
        role_libelle: forced.libelle,
      };
    });
  }, [modeConseiller, champs, loadedOptions]);

  // Pré-remplir depuis alreadyExist uniquement si un agent est fourni
  useEffect(() => {
    if (!agentId) {
      return;
    }
    const champsAvecAlready = champs.filter(champ => champ.alreadyExist?.baseDonnees);
    if (champsAvecAlready.length === 0) {
      return;
    }

    let cancelled = false;
    const loadAlreadyExist = async () => {
      const dataCache: Record<string, any> = {};
      const getBaseData = async (base: string) => {
        if (dataCache[base] !== undefined) {
          return dataCache[base];
        }
        let data: any = null;
        if (base === 'agent' || base === 'agentDirection') {
          data = await agentService.getAgentById(agentId);
        } else if (base === 'affectation') {
          data = await agentService.getAffectationActive(agentId);
        } else if (base === 'agent_diffusion' || base === 'agentDiffusion') {
          data = await agentService.getAgentDiffusions(agentId);
        } else if (base === 'agent_droit_materiel' || base === 'agentMaterielEtDroit') {
          data = await agentService.getAgentMateriel(agentId);
        }
        dataCache[base] = data;
        return data;
      };

      const updates: Record<string, any> = {};
      for (const champ of champsAvecAlready) {
        const already = champ.alreadyExist;
        if (!already?.baseDonnees) {
          continue;
        }
        let baseData = await getBaseData(already.baseDonnees);
        if (
          already.baseDonnees === 'agent_droit_materiel' ||
          already.baseDonnees === 'agentMaterielEtDroit'
        ) {
          baseData = normalizeMaterielPayload(baseData);
        }
        if (!baseData) {
          continue;
        }

        const path = [...(already.contenu || []), ...(already.champs || [])];
        const candidatePaths = buildCandidatePaths(path, already.baseDonnees);
        let extracted = undefined;
        for (const candidate of candidatePaths) {
          extracted = extractValueFromPath(baseData, candidate);
          if (extracted !== undefined) {
            break;
          }
        }
        if (extracted === undefined) {
          const fallback = extractAgentFallbackValue(baseData, path, already.baseDonnees);
          if (fallback !== undefined) {
            extracted = fallback;
          }
        }
        if (
          extracted === undefined &&
          (already.baseDonnees === 'agent_diffusion' || already.baseDonnees === 'agentDiffusion') &&
          Array.isArray(baseData)
        ) {
          extracted = baseData;
        }
        if (extracted === undefined || extracted === null || extracted === '') {
          continue;
        }

        if (champ.type === 'liste') {
          const normalizedList = normalizeListInput(extracted);
          if (normalizedList.length === 0) {
            continue;
          }
          extracted = normalizedList;
        }

        if (champ.type === 'date') {
          const normalizedDate = normalizeDateValue(extracted);
          if (!normalizedDate) {
            continue;
          }
          extracted = normalizedDate;
        }

        if (champ.type === 'select' && !loadedOptions[champ.name]) {
          // Attendre le chargement des options pour mapper correctement l'ID
          continue;
        }

        if (champ.type === 'select' && loadedOptions[champ.name]) {
          const options = loadedOptions[champ.name];
          const extractedCandidates: string[] = [];
          if (extracted && typeof extracted === 'object' && !Array.isArray(extracted)) {
            if (extracted.id) extractedCandidates.push(String(extracted.id));
            if (extracted.code) extractedCandidates.push(String(extracted.code));
            if (extracted.libelle) extractedCandidates.push(String(extracted.libelle));
          } else if (extracted !== undefined && extracted !== null) {
            extractedCandidates.push(String(extracted));
          }
          const extractedUpper = extractedCandidates.map(value => value.toUpperCase());
          let matched = options.find(opt =>
            extractedCandidates.includes(String(opt.id)) ||
            extractedCandidates.includes(String(opt.code)) ||
            extractedCandidates.includes(String(opt.libelle)) ||
            extractedUpper.includes(String(opt.code || '').toUpperCase())
          );
          if (!matched) {
            matched = findBestOptionMatch(options, extractedCandidates[0]);
          }
          if (matched) {
            extracted = matched.id;
            updates[`${champ.name}_libelle`] = matched.libelle;
          }
        }

        updates[champ.name] = extracted;
      }

      if (!cancelled && Object.keys(updates).length > 0) {
        setFormData(prev => {
          const next = { ...prev };
          Object.entries(updates).forEach(([name, value]) => {
            const current = next[name];
            if (current === undefined || current === null || current === '') {
              next[name] = value;
            }
          });
          return next;
        });
      }
    };

    loadAlreadyExist();
    return () => {
      cancelled = true;
    };
  }, [agentId, champs, loadedOptions]);

  const handleChange = useCallback((name: string, value: any) => {
    setFormData(prevFormData => {
      const updatedData: Record<string, any> = {
        ...prevFormData,
        [name]: value, // Toujours stocker l'ID dans la clé principale (pour compatibilité avec les règles)
      };

      // Pour les selects, enrichir avec le libellé si disponible
      const champ = champs.find(c => c.name === name);
      if (champ && champ.type === 'select' && value && loadedOptions[name]) {
        const option = loadedOptions[name].find(opt => opt.id === value || opt.code === value);
        if (option) {
          // Stocker le libellé dans une clé séparée pour l'affichage en lecture seule
          updatedData[`${name}_libelle`] = option.libelle;
        }
      }

      return updatedData;
    });
  }, [champs, loadedOptions]); // Inclure champs et loadedOptions pour avoir accès aux libellés

  // Valider le formulaire et notifier le parent uniquement si la validation a changé
  useEffect(() => {
    // Ne pas valider lors de l'initialisation
    if (isInitialMount.current) {
      return;
    }
    
    // Calculer la validation directement dans le useEffect pour éviter les dépendances instables
    const errors: string[] = [];
    
    champs.forEach((champ) => {
      const value = formData[champ.name];
      const valueStr = typeof value === 'string' ? value.trim() : value;
      
      // Vérifier les champs requis
      if (champ.required && (!value || valueStr === '' || (Array.isArray(value) && value.length === 0))) {
        errors.push(`Le champ "${champ.label}" est requis`);
        return;
      }
      
      // Si le champ est vide et non requis, on passe
      if (!value || valueStr === '') {
        return;
      }
      
      // Validation de la longueur pour les champs texte
      if (champ.type === 'text' || champ.type === 'input' || champ.type === 'email' || champ.type === 'texte') {
        const strValue = String(valueStr);
        if (champ.minLength && strValue.length < champ.minLength) {
          errors.push(`Le champ "${champ.label}" doit contenir au moins ${champ.minLength} caractères`);
        }
        if (champ.maxLength && strValue.length > champ.maxLength) {
          errors.push(`Le champ "${champ.label}" ne doit pas dépasser ${champ.maxLength} caractères`);
        }
        // Validation du pattern
        if (champ.pattern) {
          try {
            const regex = new RegExp(champ.pattern);
            if (!regex.test(strValue)) {
              let errorMessage = `Le format du champ "${champ.label}" n'est pas valide`;
              if (champ.pattern.includes('@lecese\\.fr')) {
                errorMessage = `Le champ "${champ.label}" doit se terminer par @lecese.fr`;
              }
              errors.push(errorMessage);
            }
          } catch (e) {
            // Pattern invalide, on ignore
          }
        }
      }
    });
    
    const isValid = errors.length === 0;
    const validation = { isValid, errors };
    
    // Comparer avec la dernière validation pour éviter les appels inutiles
    const lastValidation = lastValidationRef.current;
    if (lastValidation && 
        lastValidation.isValid === validation.isValid && 
        JSON.stringify(lastValidation.errors) === JSON.stringify(validation.errors)) {
      return; // La validation n'a pas changé, ne pas notifier
    }
    
    // Mettre à jour la référence
    lastValidationRef.current = validation;
    
    // Notifier le parent uniquement si la validation a changé
    if (onValidationChangeRef.current) {
      onValidationChangeRef.current(validation.isValid, validation.errors);
    }
  }, [formData, champs]); // Dépendre directement de formData et champs au lieu de validateForm

  return (
    <div className="space-y-4">
      {champs.length === 0 ? (
        <p className="text-muted-foreground">
          Aucun champ défini dans le template.
        </p>
      ) : (
        champs.map((champ) => (
          <ChampForm
            key={champ.name}
            champ={champ}
            value={formData[champ.name]}
            onChange={handleChange}
            loadedOptions={loadedOptions[champ.name]}
            agentId={agentId}
            readOnly={readOnly}
          />
        ))
      )}
    </div>
  );
}
