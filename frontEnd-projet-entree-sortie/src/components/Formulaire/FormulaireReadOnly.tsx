import { Calendar, ImageIcon, NotebookPen, User, Building2 } from "lucide-react";
import { Label } from "../ui/label";
import type { TemplateTacheDTO } from "@/service/templateTacheService";
import { agentService } from "@/service/agentService";
import { useState, useEffect } from "react";

// Type pour un champ de formulaire défini dans le contenu
type ChampFormulaire = {
  type: 'text' | 'date' | 'file' | 'select' | 'liste' | 'texte' | 'email' | 'image' | 'input' | 'value';
  label: string;
  name: string;
  required?: boolean;
  placeholder?: string;
  options?: string[] | { accesBaseDonnees?: string; champs?: string[] };
  icon?: string;
  minDate?: string;
  // Propriétés spécifiques au type "value"
  baseDonnees?: string; // Table de la base de données (ex: "agent", "agentDirection")
  champs?: string[]; // Champs à récupérer depuis la base de données
  value?: string; // Template avec variables (ex: "${agent.email}")
};

// Fonction pour parser le contenu et extraire les champs (réutilisée depuis FormulaireDynamique)
function parseContenu(contenu: Record<string, any> | string | null | undefined): ChampFormulaire[] {
  if (!contenu) {
    return [];
  }

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
      // Propriétés pour le type "value"
      baseDonnees: field.baseDonnees,
      champs: field.champs,
      value: field.value,
    }));
  }
  
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
      // Propriétés pour le type "value"
      baseDonnees: field.baseDonnees,
      champs: field.champs,
      value: field.value,
    }));
  }

  Object.keys(contenuObj).forEach((key) => {
    if (key === 'actions' || key === 'fields' || key === 'champs') {
      return;
    }
    
    const value = contenuObj[key];
    if (typeof value === 'object' && value !== null && !Array.isArray(value)) {
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
          // Propriétés pour le type "value"
          baseDonnees: value.baseDonnees,
          champs: value.champs,
          value: value.value,
        });
      }
    } else if (typeof value === 'string' || typeof value === 'number') {
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
            const items = withoutBrackets.split(',').map(i => i.trim()).filter(i => i);
            normalized.push(...items);
          } else {
            normalized.push(item);
          }
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

// Composant pour afficher une valeur de type "value" (lecture seule depuis la base de données)
function ValueFieldReadOnly({ 
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

        // Récupérer les données selon baseDonnees
        let data: any = null;
        let materielEtDroit: any = null;
        
        if (champ.baseDonnees === 'agent') {
          data = await agentService.getAgentById(agentId);
        } else if (champ.baseDonnees === 'agentDirection') {
          // Pour agentDirection, on récupère d'abord l'agent
          const agent = await agentService.getAgentById(agentId);
          data = agent;
          
          // Si on cherche materielEtDroit, on le récupère via l'API dédiée
          if (champ.champs && champ.champs.some(f => f.includes('materielEtDroit'))) {
            try {
              const materiel = await agentService.getAgentMateriel(agentId);
              // materiel est un objet avec une propriété agentMaterileEtDroit (Map)
              // Note: le nom de la propriété est "agentMaterileEtDroit" (avec une faute d'orthographe dans le backend)
              materielEtDroit = materiel.agentMaterileEtDroit || materiel;
            } catch (err) {
              console.warn('Impossible de récupérer materielEtDroit:', err);
            }
          }
        }

        if (data || materielEtDroit) {
          // Extraire la valeur selon les champs spécifiés
          let extractedValue: any = null;
          
          // Si on a un template value, extraire le chemin depuis le template
          if (champ.value) {
            // Extraire le chemin depuis le template (ex: "${agentDirection.materielEtDroit.droitAgent}" -> "droitAgent")
            const templateMatch = champ.value.match(/\$\{[^}]+\}/);
            if (templateMatch) {
              const fullPath = templateMatch[0].replace(/\$\{|\}/g, ''); // "agentDirection.materielEtDroit.droitAgent"
              const pathParts = fullPath.split('.');
              
              // Si le chemin commence par "agentDirection.materielEtDroit", utiliser materielEtDroit
              if (pathParts[0] === 'agentDirection' && pathParts[1] === 'materielEtDroit' && materielEtDroit) {
                // Naviguer dans materielEtDroit pour extraire le reste du chemin
                extractedValue = materielEtDroit;
                console.log('[ValueFieldReadOnly] Chemin d\'extraction:', pathParts);
                console.log('[ValueFieldReadOnly] materielEtDroit:', materielEtDroit);
                for (let i = 2; i < pathParts.length; i++) {
                  if (extractedValue && typeof extractedValue === 'object') {
                    console.log(`[ValueFieldReadOnly] Extraction étape ${i}: cherchant "${pathParts[i]}" dans:`, extractedValue);
                    extractedValue = extractedValue[pathParts[i]];
                    console.log(`[ValueFieldReadOnly] Valeur après extraction:`, extractedValue);
                    if (extractedValue === undefined) break;
                  } else {
                    extractedValue = undefined;
                    break;
                  }
                }
                // Fallback: si droitAgent n'existe pas, essayer droit
                if (extractedValue === undefined && pathParts.length > 2 && pathParts[pathParts.length - 1] === 'droitAgent' && typeof materielEtDroit === 'object') {
                  extractedValue = materielEtDroit['droit'];
                  console.log('[ValueFieldReadOnly] Fallback: utilisation de "droit" au lieu de "droitAgent":', extractedValue);
                }
                console.log('[ValueFieldReadOnly] Valeur finale extraite:', extractedValue);
              } else if (pathParts[0] === 'agent' && data) {
                // Pour les chemins commençant par "agent", utiliser data
                extractedValue = data;
                for (let i = 1; i < pathParts.length; i++) {
                  if (extractedValue && typeof extractedValue === 'object') {
                    extractedValue = extractedValue[pathParts[i]];
                    if (extractedValue === undefined) break;
                  } else {
                    extractedValue = undefined;
                    break;
                  }
                }
              }
            }
          } else {
            // Sinon, utiliser la logique originale avec champ.champs
            extractedValue = data || materielEtDroit;
            for (const field of champ.champs) {
              if (extractedValue && typeof extractedValue === 'object') {
                // Gérer les chemins imbriqués comme "materielEtDroit.droitAgent"
                const parts = field.split('.');
                for (const part of parts) {
                  extractedValue = extractedValue[part];
                  if (extractedValue === undefined) break;
                }
              } else {
                extractedValue = undefined;
                break;
              }
            }
          }

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
                droitAgentValue = extractedValue.join(', ');
              } else if (extractedValue !== null && extractedValue !== undefined) {
                droitAgentValue = String(extractedValue);
              } else {
                // Fallback: essayer d'extraire "droit" directement
                if (typeof materielEtDroit === 'object' && 'droit' in materielEtDroit) {
                  const droitValue = materielEtDroit['droit'];
                  droitAgentValue = Array.isArray(droitValue) ? droitValue.join(', ') : String(droitValue || '');
                  console.log('[ValueFieldReadOnly] Fallback: utilisation de "droit" depuis materielEtDroit:', droitAgentValue);
                }
              }
              console.log('[ValueFieldReadOnly] droitAgentValue final:', droitAgentValue);
              templateValue = templateValue.replace(/\$\{agentDirection\.materielEtDroit\.droitAgent\}/g, droitAgentValue);
            }
            setLoadedValue(templateValue);
          } else {
            // Si extractedValue est un tableau, l'afficher comme une liste
            if (Array.isArray(extractedValue)) {
              const normalizedList = normalizeList(extractedValue);
              setLoadedValue(normalizedList.length > 0 ? normalizedList.join(', ') : 'Aucun élément');
            } else {
              setLoadedValue(extractedValue !== null && extractedValue !== undefined ? String(extractedValue) : 'Non disponible');
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

// Composant pour afficher un champ en lecture seule
function ChampFormReadOnly({ 
  champ, 
  value,
  allValues,
  agentId
}: { 
  champ: ChampFormulaire; 
  value: any;
  allValues?: Record<string, any>; // Toutes les valeurs pour accéder aux libellés
  agentId?: string; // ID de l'agent pour récupérer les valeurs de type "value"
}) {
  // Si c'est un champ de type "value", utiliser le composant ValueFieldReadOnly
  if (champ.type === 'value') {
    return <ValueFieldReadOnly champ={champ} agentId={agentId} />;
  }
  const getIcon = () => {
    const iconName = champ.icon?.toLowerCase() || '';
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

  const formatValue = (): string => {
    if (value === null || value === undefined || value === '') {
      return 'Non renseigné';
    }

    // Pour les listes, joindre les éléments
    if (Array.isArray(value)) {
      return value.join(', ');
    }

    // Pour les dates, formater si possible
    if (champ.type === 'date' && typeof value === 'string') {
      try {
        const date = new Date(value);
        return date.toLocaleDateString('fr-FR');
      } catch (e) {
        return value;
      }
    }

    // Pour les selects, afficher le libellé si disponible (stocké dans {name}_libelle)
    // Sinon, afficher l'ID ou la valeur brute
    if (champ.type === 'select' && allValues) {
      const libelleKey = `${champ.name}_libelle`;
      const libelle = allValues[libelleKey];
      if (libelle && typeof libelle === 'string') {
        return libelle;
      }
    }

    // Pour les autres champs, afficher directement la valeur
    return String(value);
  };

  return (
    <div className="flex flex-col gap-2">
      <div className="flex items-center gap-2">
        {getIcon()}
        <Label className="text-sm font-medium">
          {champ.label}
          {champ.required && <span className="text-red-500 ml-1">*</span>}
        </Label>
      </div>
      <div className="flex h-10 w-full rounded-md border border-input bg-muted/50 px-3 py-2 text-sm text-foreground">
        {formatValue()}
      </div>
    </div>
  );
}

// Composant principal pour afficher un formulaire en lecture seule
export function FormulaireReadOnly({ 
  templateTache,
  filledValues,
  agentId
}: { 
  templateTache: TemplateTacheDTO | null;
  filledValues?: Record<string, any>;
  agentId?: string; // ID de l'agent pour récupérer les valeurs de type "value"
}) {
  if (!templateTache) {
    return null;
  }

  // Parser le contenu (peut être un objet ou une chaîne JSON)
  const contenuParsed = typeof templateTache.contenu === 'string' 
    ? (() => {
        try {
          return JSON.parse(templateTache.contenu);
        } catch (e) {
          console.error('Erreur lors du parsing du contenu:', e, templateTache.contenu);
          return {};
        }
      })()
    : templateTache.contenu || {};

  const champs = parseContenu(contenuParsed);

  // Extraire les valeurs remplies depuis filledValues ou depuis le contenu de la tâche
  const values = filledValues || {};

  return (
    <div className="space-y-4">
      {champs.length === 0 ? (
        <p className="text-muted-foreground text-sm">
          Aucun champ défini dans le template.
        </p>
      ) : (
        champs.map((champ) => (
          <ChampFormReadOnly
            key={champ.name}
            champ={champ}
            value={values[champ.name]}
            allValues={values}
            agentId={agentId}
          />
        ))
      )}
    </div>
  );
}
