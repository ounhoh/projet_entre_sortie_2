import { useState, useMemo, useEffect } from "react";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "../../../ui/tabs";
import { BarreRecherche } from "@/components/recherche/Recherche";
import type { DependanceDTO, GroupeTacheDTO, TacheDTO } from "@/service/processusService";
import type { TemplateGroupeTacheDTO } from "@/service/templateProcessusService";
import { cn } from "@/lib/utils";
import { BookText, FileText, CheckCircle2, XCircle } from "lucide-react";
import { toast } from "sonner";
import { AnimatedList } from "../../../ui/animated-list";
import { ResizableHandle, ResizablePanel, ResizablePanelGroup } from "../../../ui/resizable";
import { Badge } from "../../../ui/badge";
import { Button } from "../../../ui/button";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "../../../ui/dialog";
import { FormulaireDynamique } from "../../../Formulaire/FormulaireDynamique";
import { FormulaireReadOnly } from "../../../Formulaire/FormulaireReadOnly";
import { tacheService } from "@/service/tacheService";
import { templateTacheService } from "@/service/templateTacheService";
import type { TemplateTacheDTO } from "@/service/templateTacheService";
import { Card, CardHeader, CardTitle, CardContent, CardFooter } from "../../../ui/card";
import { processusService, type ProcessDetailDTO } from "@/service/processusService";

interface InstanceGroupeTacheViewProps {
    groupes: GroupeTacheDTO[];
    templateGroupes: TemplateGroupeTacheDTO[];
    processusLibelle: string;
    statutActuelId: string;
    direction?:string;
    statutsProcessus?: Array<{ id: string; code: string; libelle: string }>; // Liste des statuts triés par ordre
    agentId?: string; // ID de l'agent pour récupérer les valeurs de type "value"
    agentRole?: string; // Rôle de l'agent du processus (pour affichages conditionnels)
    processusId?: string; // ID du processus pour recharger les données
    onDataUpdate?: (data: ProcessDetailDTO) => void; // Callback pour mettre à jour les données depuis le parent
    dependanceList?: DependanceDTO[];
}

const extractRequiredLabels = (contenu: any, formValues: Record<string, any> | undefined) => {
    if (!contenu || !formValues) return [];

    let contenuParsed: any = contenu;
    if (typeof contenu === "string") {
        try {
            contenuParsed = JSON.parse(contenu);
        } catch {
            return [];
        }
    }

    let champs: any[] = [];
    if (Array.isArray(contenuParsed)) {
        champs = contenuParsed;
    } else if (contenuParsed && typeof contenuParsed === "object") {
        if (Array.isArray(contenuParsed.champs)) {
            champs = contenuParsed.champs;
        } else if (Array.isArray(contenuParsed.fields)) {
            champs = contenuParsed.fields;
        } else {
            champs = Object.values(contenuParsed).filter(
                (val: any) => val && typeof val === "object" && (val.type || val.label)
            );
        }
    }

    const missingLabels: string[] = [];
    champs.forEach((champ: any) => {
        if (!champ?.required) return;
        const key = champ.name || champ.id;
        if (!key) return;
        const value = formValues[key];
        const valueStr = typeof value === "string" ? value.trim() : value;
        const isEmpty = !value || valueStr === "" || (Array.isArray(value) && value.length === 0);
        if (isEmpty && champ.label) {
            missingLabels.push(champ.label);
        }
    });

    return missingLabels;
};

// Mapping des statuts techniques -> libellés lisibles
const formatStatut = (statut: string) => {
    switch (statut) {
        case "aFaire":
            return "à faire";
        case "enCours":
            return "en cours";
        case "enAttente":
            return "en attente";
        case "fait":
            return "fait";
        case "bloque":
            return "bloqué";
        default:
            return statut;
    }
};

// Fonction pour obtenir les classes CSS selon le statut
const getStatutBadgeClasses = (statut: string): string => {
    switch (statut) {
        case "aFaire":
        case "enAttente":
            return "bg-blue-500 hover:bg-blue-600 text-white"; // Bleu pour à faire et en attente
        case "enCours":
            return "bg-amber-500 hover:bg-amber-600 text-white"; // Orange/Amber pour en cours
        case "fait":
            return "bg-green-500 hover:bg-green-600 text-white"; // Vert pour fait
        case "bloque":
            return "bg-red-500 hover:bg-red-600 text-white"; // Rouge pour bloqué
        default:
            return "bg-gray-500 hover:bg-gray-600 text-white";
    }
};

const STORAGE_KEY = "currentAgent";

export function InstanceGroupeTacheView({ 
    groupes, 
    templateGroupes, 
    processusLibelle, 
    statutActuelId,
    direction,
    statutsProcessus = [],
    agentId,
    agentRole,
    processusId,
    onDataUpdate,
    dependanceList = [],
}: InstanceGroupeTacheViewProps) {
    const [search, setSearch] = useState("");
    const [selectedGroupeId, setSelectedGroupeId] = useState<string | null>(null);
    const [selectedDirection, setSelectedDirection] = useState<string | null>(null);
    const [selectedGroupeIdAvancement, setSelectedGroupeIdAvancement] = useState<string | null>(null);
    const [formData, setFormData] = useState<Record<string, any>>({});
    const [formValidationByTache, setFormValidationByTache] = useState<Record<string, { isValid: boolean; errors: string[] }>>({});
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [templateTachesMap, setTemplateTachesMap] = useState<Map<string, TemplateTacheDTO>>(new Map());
    const [isAnnulerDialogOpen, setIsAnnulerDialogOpen] = useState(false);
    const [pendingAnnulerTache, setPendingAnnulerTache] = useState<TacheDTO | null>(null);
    const [annulerImpacts, setAnnulerImpacts] = useState<Array<{ id: string; libelle: string; direction: string }>>([]);

    const isPrestataireProcess = useMemo(() => {
        const normalized = String(agentRole || '').trim().toLowerCase();
        return normalized.includes('prestataire');
    }, [agentRole]);

    const isConseillerProcess = useMemo(() => {
        const normalized = String(agentRole || '').trim().toLowerCase();
        return normalized.includes('conseiller');
    }, [agentRole]);

    const normalize = (value: any) =>
        String(value || "")
            .trim()
            .toLowerCase()
            .normalize('NFD')
            .replace(/[\u0300-\u036f]/g, '');
    const includesAll = (value: any, terms: string[]) => {
        const v = normalize(value);
        return terms.every(t => v.includes(t));
    };

    const getGroupeLibelle = (groupe: GroupeTacheDTO) => {
        if (isPrestataireProcess && groupe.code === 'agent_form_concenrnee') {
            return "Groupe de tâches de création d'un prestataire";
        }
        if (isConseillerProcess && groupe.code === 'agent_form_concenrnee') {
            return "Groupe de tâches de création d'un conseiller/attaché";
        }
        // Sortie prestataire : renommer les groupes pour clarifier le prestataire
        if (isPrestataireProcess) {
            if (includesAll(groupe.libelle, ["groupe", "taches", "sortie", "agent"])) {
                return "Groupe de tâches de sortie d'un prestataire";
            }
        }
        // Sortie conseiller : renommer les groupes pour clarifier le conseiller
        if (isConseillerProcess) {
            if (includesAll(groupe.libelle, ["groupe", "taches", "sortie", "agent"])) {
                return "Groupe de tâches de sortie d'un conseiller/attaché";
            }
        }
        return groupe.libelle;
    };

    const getTacheLibelle = (tache: TacheDTO) => {
        if (isPrestataireProcess && tache.code === 'form_agent_creation_concernee') {
            return "Formulaire de création d'un prestataire pour la direction concernée";
        }
        if (isConseillerProcess && tache.code === 'form_agent_creation_concernee') {
            return "Formulaire de création d'un conseiller/attaché pour la direction concernée";
        }
        if (isPrestataireProcess) {
            // Sortie - premier formulaire (DAPPI)
            if (
                includesAll(tache.libelle, ["formulaire", "sortie", "agent", "rh"]) ||
                includesAll(tache.libelle, ["formulaire", "sortie", "agent", "dappi"])
            ) {
                return "Formulaire de sortie d'un prestataire";
            }
            // Sortie - formulaire direction concernée
            if (includesAll(tache.libelle, ["formulaire", "sortie", "agent", "direction"])) {
                return "Formulaire de sortie d'un prestataire pour la direction concernée";
            }
        }
        if (isConseillerProcess) {
            // Sortie - premier formulaire (DAF)
            if (
                includesAll(tache.libelle, ["formulaire", "sortie", "agent", "rh"]) ||
                includesAll(tache.libelle, ["formulaire", "sortie", "agent", "daf"])
            ) {
                return "Formulaire de sortie d'un conseiller/attaché";
            }
            // Sortie - formulaire direction concernée
            if (includesAll(tache.libelle, ["formulaire", "sortie", "agent", "direction"])) {
                return "Formulaire de sortie d'un conseiller/attaché pour la direction concernée";
            }
        }
        return tache.libelle;
    };

    const getTacheDescription = (tache: TacheDTO) => {
        if (!isPrestataireProcess && !isConseillerProcess) return tache.description;
        if (includesAll(tache.description, ["tache", "sortie", "agent", "rh"])) {
            return "Tâche de sortie d'un prestataire";
        }
        if (includesAll(tache.description, ["tache", "sortie", "agent", "rh"])) {
            return "Tâche de sortie d'un conseiller/attaché";
        }
        if (includesAll(tache.description, ["tache", "sortie", "agent", "dappi"])) {
            return "Tâche de sortie d'un prestataire";
        }
        if (includesAll(tache.description, ["tache", "sortie", "agent", "daf"])) {
            return "Tâche de sortie d'un conseiller/attaché";
        }
        return tache.description;
    };

    const shouldApplyPrestataireModeForTache = (tache: TacheDTO) => {
        if (!isPrestataireProcess) return false;
        const code = normalize(tache.code);
        const lib = normalize(tache.libelle);
        // Cibler uniquement les formulaires "RH/DAPPI" de sortie (et éviter ceux "direction concernée")
        if (code.includes('concer')) return false;
        if (code.includes('sortie') && (code.includes('rh') || lib.includes('rh') || lib.includes('dappi'))) return true;
        if (includesAll(lib, ["formulaire", "sortie", "agent", "rh"])) return true;
        if (includesAll(lib, ["formulaire", "sortie", "agent", "dappi"])) return true;
        return false;
    };

    const shouldApplyConseillerModeForTache = (tache: TacheDTO) => {
        if (!isConseillerProcess) return false;
        const code = normalize(tache.code);
        const lib = normalize(tache.libelle);
        // Cibler uniquement les formulaires "RH/DAF" de sortie (et éviter ceux "direction concernée")
        if (code.includes('concer')) return false;
        if (code.includes('sortie') && (code.includes('rh') || lib.includes('rh') || lib.includes('daf'))) return true;
        if (includesAll(lib, ["formulaire", "sortie", "agent", "rh"])) return true;
        if (includesAll(lib, ["formulaire", "sortie", "agent", "daf"])) return true;
        return false;
    };
    
    const currentAgentDirection = useMemo(() => {
        try {
            const raw = localStorage.getItem(STORAGE_KEY);
            if (!raw) return null;
            const parsed = JSON.parse(raw) as { direction?: string | null };
            return parsed?.direction || null;
        } catch {
            return null;
        }
    }, []);

    const groupesFiltresParDirection = useMemo(() => {
        if (!currentAgentDirection) {
            return groupes;
        }

        const normalize = (value?: string | null) =>
            (value || "").trim().toLowerCase();

        const directionAgent = normalize(currentAgentDirection);
        const directionProcess = normalize(direction);

        const templateGroupesMap = new Map<string, TemplateGroupeTacheDTO>();
        templateGroupes.forEach((tg) => {
            templateGroupesMap.set(tg.id, tg);
        });

        return groupes.filter((groupe) => {
            const templateGroupe = templateGroupesMap.get(groupe.templateId);
            if (!templateGroupe) return false;

            if (templateGroupe.isDirectionConcernee) {
                return directionProcess && directionProcess === directionAgent;
            }

            const codeDirection = normalize(templateGroupe.codeDirection);
            const libelleDirection = normalize(templateGroupe.libelleDirection);
            return (
                (codeDirection && codeDirection === directionAgent) ||
                (libelleDirection && libelleDirection === directionAgent)
            );
        });
    }, [groupes, templateGroupes, currentAgentDirection, direction]);

    // Filtrer les groupes pour n'afficher que ceux du statut actuel
    // En comparant le templateId des groupes instances avec les templates groupes
    const groupesFiltresParStatut = useMemo(() => {
        if (!statutActuelId) return groupesFiltresParDirection;
        
        // Créer un map des template groupes par leur ID et statutProcessusId
        const templateGroupesMap = new Map<string, TemplateGroupeTacheDTO>();
        templateGroupes.forEach(tg => {
            templateGroupesMap.set(tg.id, tg);
        });
        
        // Filtrer les groupes instances dont le templateId correspond à un template groupe
        // avec le statutProcessusId égal au statut actuel
        return groupesFiltresParDirection.filter((groupe) => {
            const templateGroupe = templateGroupesMap.get(groupe.templateId);
            return templateGroupe && templateGroupe.statutProcessusId === statutActuelId;
        });
    }, [statutActuelId, groupesFiltresParDirection, templateGroupes]);

    // Fonction pour obtenir l'ordre de tri des statuts
    const getStatutOrder = (statut: string): number => {
        switch (statut) {
            case 'enCours':
                return 1;
            case 'enAttente':
                return 2;
            case 'fait':
                return 3;
            default:
                return 4; // Autres statuts en dernier
        }
    };

    // Filtrer et trier les groupes selon la recherche et le statut
    const groupesFiltres = useMemo(() => {
        let groupesFiltres = groupesFiltresParStatut;
        
        // Filtrer selon la recherche si nécessaire
        if (search.trim()) {
            const searchLower = search.toLowerCase();
            groupesFiltres = groupesFiltresParStatut.filter(
                (groupe) =>
                    groupe.libelle.toLowerCase().includes(searchLower) ||
                    groupe.code.toLowerCase().includes(searchLower) ||
                    (groupe.taches && groupe.taches.some((tache: TacheDTO) =>
                        tache.libelle?.toLowerCase().includes(searchLower) ||
                        tache.description?.toLowerCase().includes(searchLower)
                    ))
            );
        }
        
        // Trier les groupes : enCours -> enAttente -> fait
        return [...groupesFiltres].sort((a, b) => {
            const orderA = getStatutOrder(a.statut);
            const orderB = getStatutOrder(b.statut);
            return orderA - orderB;
        });
    }, [search, groupesFiltresParStatut]);

    // Trouver le groupe sélectionné
    const selectedGroupe = groupesFiltresParDirection.find((g) => g.id === selectedGroupeId);
    // Inverser l'ordre des tâches pour afficher de haut en bas (première tâche en haut)
    const taches = selectedGroupe?.taches ? [...selectedGroupe.taches].reverse() : [];

    // Trouver l'ordre du statut actuel dans la liste des statuts
    const ordreStatutActuel = useMemo(() => {
        if (!statutsProcessus || statutsProcessus.length === 0) {
            // Si pas de liste de statuts, on affiche tous les groupes (comportement par défaut)
            return Infinity;
        }
        const index = statutsProcessus.findIndex(s => s.id === statutActuelId);
        return index >= 0 ? index : Infinity;
    }, [statutsProcessus, statutActuelId]);

    // Filtrer les groupes pour l'onglet Avancement : seulement ceux du statut actuel et des statuts précédents
    const groupesFiltresParOrdreStatut = useMemo(() => {
        if (ordreStatutActuel === Infinity) {
            // Si on ne peut pas déterminer l'ordre, afficher tous les groupes
            return groupes;
        }

        // Créer un map des template groupes par leur ID et statutProcessusId
        const templateGroupesMap = new Map<string, TemplateGroupeTacheDTO>();
        templateGroupes.forEach(tg => {
            templateGroupesMap.set(tg.id, tg);
        });

        // Créer un map des statuts par leur ID pour trouver rapidement l'ordre
        const statutsMap = new Map<string, number>();
        statutsProcessus.forEach((statut, index) => {
            statutsMap.set(statut.id, index);
        });

        // Filtrer les groupes dont le statutProcessusId a un ordre <= ordreStatutActuel
        return groupes.filter((groupe) => {
            const templateGroupe = templateGroupesMap.get(groupe.templateId);
            if (!templateGroupe) return false;
            
            const ordreStatutGroupe = statutsMap.get(templateGroupe.statutProcessusId);
            // Si le statut du groupe n'est pas dans la liste, on ne l'affiche pas
            if (ordreStatutGroupe === undefined) return false;
            
            // Afficher seulement les groupes du statut actuel et des statuts précédents
            return ordreStatutGroupe <= ordreStatutActuel;
        });
    }, [groupes, templateGroupes, statutsProcessus, ordreStatutActuel]);

    // Grouper les groupes par leur direction (codeDirection du template)
    const groupesParDirection = useMemo(() => {
        const groupesMap = new Map<string, GroupeTacheDTO[]>();
        const DIRECTION_CONCERNEE = "Direction concernée";
        
        // Créer un map des template groupes par leur ID
        const templateGroupesMap = new Map<string, TemplateGroupeTacheDTO>();
        templateGroupes.forEach(tg => {
            templateGroupesMap.set(tg.id, tg);
        });
        
        // Grouper les groupes par leur codeDirection (utiliser le libellé pour l'affichage)
        groupesFiltresParOrdreStatut.forEach((groupe) => {
            const templateGroupe = templateGroupesMap.get(groupe.templateId);
            
            // Si le groupe est "direction concernée", le mettre dans la direction spéciale
            if (templateGroupe?.isDirectionConcernee) {
                if (!groupesMap.has(DIRECTION_CONCERNEE)) {
                    groupesMap.set(DIRECTION_CONCERNEE, []);
                }
                groupesMap.get(DIRECTION_CONCERNEE)!.push(groupe);
            } else {
                // Sinon, utiliser le libellé de la direction si disponible, sinon le code, sinon la direction du processus
                const dirKey = templateGroupe?.libelleDirection || templateGroupe?.codeDirection || direction || "Direction";
                
                if (!groupesMap.has(dirKey)) {
                    groupesMap.set(dirKey, []);
                }
                groupesMap.get(dirKey)!.push(groupe);
            }
        });
        
        // Si aucun groupe n'a été trouvé, créer une entrée par défaut
        if (groupesMap.size === 0) {
            const dir = direction ?? "Direction";
            groupesMap.set(dir, groupesFiltresParOrdreStatut);
        }
        
        return groupesMap;
    }, [templateGroupes, groupesFiltresParOrdreStatut, direction]);

    // Extraire les directions uniques depuis les groupes par direction
    const directions = useMemo(() => {
        const dirSet = new Set<string>(groupesParDirection.keys());
        
        // Si aucune direction trouvée, utiliser la direction du processus ou un libellé par défaut
        if (dirSet.size === 0) {
            if (direction) {
                dirSet.add(direction);
            } else {
                dirSet.add("Direction");
            }
        }
        
        // Trier les directions en mettant "Direction concernée" en premier
        const sortedDirs = Array.from(dirSet).sort((a, b) => {
            if (a === "Direction concernée") return -1;
            if (b === "Direction concernée") return 1;
            return a.localeCompare(b);
        });

        return sortedDirs;
    }, [groupesParDirection, direction]);

    // Groupes affichés pour l'onglet Avancement
    const groupesAvancement = useMemo(() => {
        const dir = selectedDirection ?? (direction ?? "Direction");
        return groupesParDirection.get(dir) ?? [];
    }, [selectedDirection, direction, groupesParDirection]);

    const selectedGroupeAvancement = groupesAvancement.find((g) => g.id === selectedGroupeIdAvancement);
    // Inverser l'ordre des tâches pour afficher de haut en bas (première tâche en haut)
    const tachesAvancement = selectedGroupeAvancement?.taches ? [...selectedGroupeAvancement.taches].reverse() : [];

    // Charger les templates des tâches formulaire
    useEffect(() => {
        const loadTemplates = async () => {
            const newMap = new Map<string, TemplateTacheDTO>();
            
            // Pour chaque tâche formulaire dans les groupes affichés
            for (const groupe of groupesAvancement) {
                const templateGroupe = templateGroupes.find(tg => tg.id === groupe.templateId);
                if (templateGroupe && templateGroupe.tacheDTOList) {
                    for (const tache of groupe.taches || []) {
                        if ((tache.type === 'formulaire' || tache.type === 'FORMULAIRE') && !newMap.has(tache.id)) {
                            // Chercher le template correspondant par code ou id
                            const templateTache = templateGroupe.tacheDTOList.find(
                                (tt: any) => (tt.code && tt.code === tache.code) || (tt.id && tt.id === tache.id)
                            );
                            if (templateTache && templateTache.id) {
                                // Charger le template complet depuis l'API
                                try {
                                    const fullTemplate = await templateTacheService.getTemplateTacheById(templateTache.id);
                                    newMap.set(tache.id, fullTemplate);
                                } catch (error) {
                                    console.error(`Erreur lors du chargement du template pour la tâche ${tache.id}:`, error);
                                }
                            }
                        }
                    }
                }
            }
            
            setTemplateTachesMap(newMap);
        };
        
        if (groupesAvancement.length > 0) {
            loadTemplates();
        }
    }, [groupesAvancement, templateGroupes]);

    const getDirectionLabelForGroupe = (groupe: GroupeTacheDTO): string => {
        const templateGroupe = templateGroupes.find(tg => tg.id === groupe.templateId);
        if (templateGroupe?.isDirectionConcernee) {
            return "Direction concernée";
        }
        return templateGroupe?.libelleDirection || templateGroupe?.codeDirection || direction || "Direction";
    };

    const computeAnnulationImpacts = (tache: TacheDTO) => {
        if (!dependanceList || dependanceList.length === 0) {
            return [];
        }

        const graph = new Map<string, string[]>();
        dependanceList.forEach((dep) => {
            graph.set(dep.sourceTacheId, dep.cibleTacheIds || []);
        });

        const visited = new Set<string>();
        const stack = [...(graph.get(tache.id) || [])];
        while (stack.length > 0) {
            const current = stack.pop();
            if (!current || visited.has(current)) {
                continue;
            }
            visited.add(current);
            const next = graph.get(current);
            if (next && next.length > 0) {
                stack.push(...next);
            }
        }

        if (visited.size === 0) {
            return [];
        }

        const groupeDuTache = groupes.find(g => g.taches?.some(t => t.id === tache.id));
        const impacted: Array<{ id: string; libelle: string; direction: string }> = [];

        groupes.forEach((groupe) => {
            const isSameGroupe = groupeDuTache?.id && groupe.id === groupeDuTache.id;
            const directionLabel = getDirectionLabelForGroupe(groupe);
            (groupe.taches || []).forEach((t) => {
                if (!visited.has(t.id)) return;
                if (isSameGroupe) return;
                if (t.statut === 'aFaire') return;
                impacted.push({
                    id: t.id,
                    libelle: t.libelle,
                    direction: directionLabel,
                });
            });
        });

        return impacted;
    };

    // Fonction pour valider une tâche
    const handleValidateTache = async (tache: TacheDTO) => {
        try {
            setIsSubmitting(true);
            
            // Sauvegarder le statut actuel du processus avant la validation
            const ancienStatutId = statutActuelId;
            
            // D'abord, soumettre le formulaire pour sauvegarder les réponses dans "reponses"
            const formDataForTache = formData[tache.id];
            const validation = formValidationByTache[tache.id];
            if (validation && !validation.isValid) {
                const requiredLabels = validation.errors
                    .filter(err => err.startsWith('Le champ "') && err.endsWith('" est requis'))
                    .map(err => err.replace('Le champ "', '').replace('" est requis', '').trim())
                    .filter(Boolean);
                const otherLabels = validation.errors
                    .filter(err => err.startsWith('Le champ "') && !err.endsWith('" est requis'))
                    .map(err => err.replace('Le champ "', '').replace(/".*$/, '').trim())
                    .filter(Boolean);

                const fallbackLabels = requiredLabels.length > 0
                    ? requiredLabels
                    : extractRequiredLabels(tache.contenu, formDataForTache);
                if (fallbackLabels.length > 0) {
                    toast.error("Éléments à remplir", {
                        description: `Éléments à remplir: ${fallbackLabels.join(', ')}`
                    });
                }

                if (otherLabels.length > 0) {
                    toast.error("Champs à modifier", {
                        description: `Champs à modifier: ${otherLabels.join(', ')}`
                    });
                }
                return;
            }
            if (!validation) {
                const requiredLabels = extractRequiredLabels(tache.contenu, formDataForTache);
                if (requiredLabels.length > 0) {
                    toast.error("Éléments à remplir", {
                        description: `Éléments à remplir: ${requiredLabels.join(', ')}`
                    });
                    return;
                }
            }
            if (formDataForTache) {
                // Construire le contenuJSON avec les réponses et la structure existante
                const contenujson = {
                    ...(tache.contenu || {}), // Conserver la structure (champs, actions)
                    ...formDataForTache // Ajouter les réponses de l'utilisateur
                };
                await tacheService.submitFormulaire(tache.id, contenujson);
            }
            
            // Ensuite, compléter la tâche (qui va exécuter les règles avec les données du formulaire)
            await tacheService.completeTache(tache.id);
            toast.success("Tâche validée", { description: `${getTacheLibelle(tache)} validée` });
            
            // Recharger les données du processus pour voir si le statut a changé
            if (processusId) {
                try {
                    const updatedDetails = await processusService.getProcessusDetails(processusId);
                    const nouveauStatutId = updatedDetails.processInfo.statut.id;
                    
                    // Si le statut n'a pas changé, mettre à jour les données localement
                    if (nouveauStatutId === ancienStatutId && onDataUpdate) {
                        onDataUpdate(updatedDetails);
                    } else {
                        // Si le statut a changé, recharger la page
                        window.location.reload();
                    }
                } catch (reloadError) {
                    console.error('Erreur lors du rechargement des données:', reloadError);
                    // En cas d'erreur, recharger la page par sécurité
                    window.location.reload();
                }
            } else {
                // Si pas de processusId, recharger la page par défaut
                window.location.reload();
            }
        } catch (error) {
            console.error('Erreur lors de la validation de la tâche:', error);
            toast.error("Tâche non validée", {
                description: "Erreur lors de la validation de la tâche. Veuillez réessayer."
            });
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleAnnulerValidationRequest = (tache: TacheDTO) => {
        const impacts = computeAnnulationImpacts(tache);
        if (impacts.length === 0) {
            handleAnnulerValidation(tache);
            return;
        }
        setAnnulerImpacts(impacts);
        setPendingAnnulerTache(tache);
        setIsAnnulerDialogOpen(true);
    };

    // Fonction pour annuler la validation d'une tâche
    const handleAnnulerValidation = async (tache: TacheDTO) => {
        try {
            setIsSubmitting(true);
            
            // Sauvegarder le statut actuel du processus avant l'annulation
            const ancienStatutId = statutActuelId;
            
            // Annuler la validation (remet toutes les tâches du groupe à "à faire")
            await tacheService.annulerValidationTache(tache.id);
            
            // Recharger les données du processus pour voir si le statut a changé
            if (processusId) {
                try {
                    const updatedDetails = await processusService.getProcessusDetails(processusId);
                    const nouveauStatutId = updatedDetails.processInfo.statut.id;
                    
                    // Si le statut n'a pas changé, mettre à jour les données localement
                    if (nouveauStatutId === ancienStatutId && onDataUpdate) {
                        onDataUpdate(updatedDetails);
                    } else {
                        // Si le statut a changé, recharger la page
                        window.location.reload();
                    }
                } catch (reloadError) {
                    console.error('Erreur lors du rechargement des données:', reloadError);
                    // En cas d'erreur, recharger la page par sécurité
                    window.location.reload();
                }
            } else {
                // Si pas de processusId, recharger la page par défaut
                window.location.reload();
            }
        } catch (error) {
            console.error('Erreur lors de l\'annulation de la validation:', error);
            alert('Erreur lors de l\'annulation de la validation. Veuillez réessayer.');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <>
        <Dialog
            open={isAnnulerDialogOpen}
            onOpenChange={(open) => {
                setIsAnnulerDialogOpen(open);
                if (!open) {
                    setPendingAnnulerTache(null);
                    setAnnulerImpacts([]);
                }
            }}
        >
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>Annuler la validation de la tâche</DialogTitle>
                    <DialogDescription>
                        {annulerImpacts.length > 0
                            ? "L'annulation va entraîner un changement d'état sur les tâches suivantes (dans d'autres groupes)."
                            : "Aucune dépendance dans d'autres groupes n'a été détectée. Voulez-vous continuer ?"}
                    </DialogDescription>
                </DialogHeader>
                {annulerImpacts.length > 0 && (
                    <div className="max-h-60 overflow-y-auto space-y-2 text-sm">
                        {annulerImpacts.map((impact) => (
                            <div key={impact.id} className="flex items-center justify-between gap-3 border rounded-md px-3 py-2">
                                <span className="font-medium">{impact.libelle}</span>
                                <span className="text-muted-foreground">{impact.direction}</span>
                            </div>
                        ))}
                    </div>
                )}
                <DialogFooter>
                    <Button
                        type="button"
                        variant="outline"
                        onClick={() => setIsAnnulerDialogOpen(false)}
                    >
                        Annuler
                    </Button>
                    <Button
                        type="button"
                        variant="destructive"
                        onClick={() => {
                            if (pendingAnnulerTache) {
                                setIsAnnulerDialogOpen(false);
                                handleAnnulerValidation(pendingAnnulerTache);
                            }
                        }}
                        disabled={isSubmitting}
                    >
                        Confirmer
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
        <Tabs defaultValue="taches" className="h-full">
            <div className="p-4 border-b">
                <div className="flex items-center justify-between gap-4">
                    <div className="flex flex-col gap-2 flex-1 min-w-0">
                        <h2 className="text-lg font-semibold">{processusLibelle}</h2>
                        {direction && (
                            <p className="text-sm text-muted-foreground">Direction: {direction}</p>
                        )}
                    </div>
                    <TabsList className="shrink-0">
                        <TabsTrigger 
                            value="taches" 
                            className="data-[state=active]:bg-primary data-[state=active]:text-white dark:data-[state=active]:bg-primary dark:data-[state=active]:text-white transition-colors duration-200 ease-in-out"
                        >
                            Tâches
                        </TabsTrigger>
                        <TabsTrigger 
                            value="avancement" 
                            className="data-[state=active]:bg-primary data-[state=active]:text-white dark:data-[state=active]:bg-primary dark:data-[state=active]:text-white transition-colors duration-200 ease-in-out"
                        >
                            Avancement
                        </TabsTrigger>
                    </TabsList>
                </div>
            </div>

            <TabsContent value="taches" className="h-full">
                <ResizablePanelGroup orientation="horizontal" className="h-full min-h-0">
                    <ResizablePanel defaultSize={30} minSize={20} className="min-h-0">
                        {/* Panneau gauche - Liste des groupes */}
                        <div className="h-full border-r flex flex-col min-h-0 bg-white dark:bg-transparent">
                        {/* Barre de recherche */}
                        <div className="p-4 border-b">
                            <BarreRecherche value={search} onChange={setSearch} />
                        </div>

                        {/* Liste des groupes */}
                        <div className="flex-1 overflow-y-auto p-4">
                            {groupesFiltres.length === 0 ? (
                                <div className="text-sm text-muted-foreground text-center">
                                    Aucun groupe trouvé
                                </div>
                            ) : (
                                <div className="flex flex-wrap gap-4">
                                    {groupesFiltres.map((groupe) => {
                                        const isSelected = selectedGroupeId === groupe.id;
                                        const tacheCount = groupe.taches?.length || 0;
                                        const groupeColor = 'rgb(59, 130, 246)'; // blue
                                        
                                        return (
                                            <figure
                                                key={groupe.id}
                                                onClick={() => setSelectedGroupeId(groupe.id)}
                                                className={cn(
                                                    "relative min-h-fit w-full min-w-[250px] flex-1 overflow-visible rounded-2xl p-4",
                                                    // animation styles
                                                    "transition-all duration-200 ease-in-out",
                                                    // light styles
                                                    "bg-white [box-shadow:0_0_0_1px_rgba(0,0,0,.03),0_2px_4px_rgba(0,0,0,.05),0_12px_24px_rgba(0,0,0,.05)]",
                                                    // dark styles
                                                    "transform-gpu dark:bg-transparent dark:[box-shadow:0_-20px_80px_-20px_#ffffff1f_inset] dark:backdrop-blur-md dark:[border:1px_solid_rgba(255,255,255,.1)]",
                                                    // selected style
                                                    isSelected && "ring-2 ring-primary ring-offset-2",
                                                    // always clickable
                                                    "cursor-pointer hover:scale-[103%]"
                                                )}
                                            >
                                                <div className="flex flex-col gap-3">
                                                    <div className="flex flex-row items-start gap-4">
                                                        <div
                                                            className="flex size-14 items-center justify-center rounded-2xl shrink-0"
                                                            style={{
                                                                backgroundColor: groupeColor + '15', // 15% opacity
                                                            }}
                                                        >
                                                            <FileText className="size-7" style={{ color: groupeColor }} />
                                                        </div>
                                                        <div className="flex flex-col flex-1 min-w-0">
                                                            <div className="flex items-start justify-between gap-2 min-w-0">
                                                                <span className={cn(
                                                                    "text-base sm:text-lg font-semibold leading-tight line-clamp-2 flex-1 min-w-0 break-words",
                                                                    isSelected ? "text-primary" : "dark:text-white"
                                                                )}>
                                                                    {getGroupeLibelle(groupe)}
                                                                </span>
                                                                {/* Statut du groupe en haut à droite */}
                                                                <Badge className={cn(
                                                                    "shrink-0 whitespace-nowrap",
                                                                    getStatutBadgeClasses(groupe.statut as string)
                                                                )}>{formatStatut(groupe.statut as string)}</Badge>
                                                            </div>
                                                            <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
                                                                Code: {groupe.code}
                                                            </p>
                                                        </div>
                                                    </div>
                                                    
                                                    <div className="flex flex-col gap-2 pt-2 border-t border-gray-100 dark:border-gray-800">
                                                        <div className="flex items-center gap-2">
                                                            <div className="flex items-center gap-2 text-sm">
                                                                <BookText className="size-4 text-muted-foreground" />
                                                                <span className="font-medium dark:text-white/90">Tâches:</span>
                                                            </div>
                                                            <span className="text-sm text-muted-foreground">{tacheCount} tâche{tacheCount > 1 ? 's' : ''}</span>
                                                        </div>
                                                    </div>
                                                </div>
                                            </figure>
                                        );
                                    })}
                                </div>
                            )}
                        </div>
                        </div>
                    </ResizablePanel>
                    <ResizableHandle withHandle />
                    <ResizablePanel defaultSize={70} minSize={40} className="min-h-0">
                        {/* Panneau droit - Détails des tâches */}
                        <div className="h-full flex flex-col min-h-0 bg-background">
                        {selectedGroupe ? (
                            <>
                                {/* En-tête du panneau droit */}
                                <div className="p-4 border-b flex items-center justify-between">
                                    <div>
                                        <h3 className="text-lg font-semibold">{getGroupeLibelle(selectedGroupe)}</h3>
                                        <p className="text-sm text-muted-foreground">{selectedGroupe.code}</p>
                                    </div>
                                    <Badge className={cn(
                                        "shrink-0",
                                        getStatutBadgeClasses(selectedGroupe.statut as string)
                                    )}>
                                        {formatStatut(selectedGroupe.statut as string)}
                                    </Badge>
                                </div>

                                {/* Liste des tâches */}
                                <div className="flex-1 overflow-y-auto p-6 relative">
                                    {taches.length === 0 ? (
                                        <div className="text-center text-muted-foreground">
                                            <BookText className="size-12 mx-auto mb-4 opacity-50" />
                                            <p>Aucune tâche dans ce groupe</p>
                                        </div>
                                    ) : (
                                        <div className="space-y-8">
                                            <AnimatedList>
                                                {taches.map((tache: TacheDTO, index: number) => {
                                                    const showStepNumber = taches.length > 1;
                                                    // Le numéro d'étape : puisque les tâches sont inversées pour l'affichage,
                                                    // la première affichée (en haut) correspond à la dernière de l'ordre original
                                                    // Donc on utilise taches.length - index pour avoir le bon numéro d'étape
                                                    // Exemple: [tache1, tache2, tache3] → inversé [tache3, tache2, tache1]
                                                    // tache3 (index 0) → Étape 3, tache2 (index 1) → Étape 2, tache1 (index 2) → Étape 1
                                                    const stepNumber = taches.length - index;
                                                    const isTacheAFaire = tache.statut === 'aFaire';
                                                    
                                                    return (
                                                        <div key={`tache-${tache.id}-${index}`} className="flex gap-6">
                                                            {/* Numéro d'étape à gauche (si plus d'une tâche) */}
                                                            {showStepNumber && (
                                                                <div className="shrink-0">
                                                                    <div className={cn(
                                                                        "text-white rounded-lg px-4 py-3 min-w-[80px] text-center font-semibold text-lg",
                                                                        isTacheAFaire 
                                                                            ? "bg-gray-400 dark:bg-gray-500" 
                                                                            : "bg-primary dark:bg-primary"
                                                                    )}>
                                                                        Étape {stepNumber}
                                                                    </div>
                                                                </div>
                                                            )}
                                                            
                                                            {/* Carte de la tâche (Shadcn Card) */}
                                                            <Card className={cn(
                                                                "flex-1 shadow-sm",
                                                                isTacheAFaire && "opacity-60"
                                                            )}>
                                                                <CardHeader className="pb-4">
                                                                    <div className="flex items-start justify-between gap-4">
                                                                        <div className="flex items-start gap-3 flex-1 min-w-0">
                                                                            <div className="size-12 rounded-xl bg-blue-100 dark:bg-blue-900 flex items-center justify-center shrink-0">
                                                                                <BookText className="size-6 text-blue-600 dark:text-blue-400" />
                                                                            </div>
                                                                            <div className="flex-1 min-w-0">
                                                                                <CardTitle className="text-lg font-semibold dark:text-white break-words whitespace-normal">
                                                                                    {getTacheLibelle(tache)}
                                                                                </CardTitle>
                                                                                {tache.dateEcheance && (
                                                                                    <p className="text-xs text-muted-foreground mt-0.5">
                                                                                        Échéance : {new Date(tache.dateEcheance).toLocaleDateString('fr-FR', { 
                                                                                            day: '2-digit', 
                                                                                            month: '2-digit', 
                                                                                            year: 'numeric' 
                                                                                        })}
                                                                                    </p>
                                                                                )}
                                                                            </div>
                                                                        </div>
                                                                {/* Statut de la tâche en haut à droite */}
                                                                <Badge className={cn(
                                                                    "shrink-0 ml-2",
                                                                    getStatutBadgeClasses(tache.statut as string)
                                                                )}>
                                                                    {formatStatut(tache.statut as string)}
                                                                </Badge>
                                                                    </div>
                                                                </CardHeader>
                                                                
                                                                <CardContent className="space-y-4">
                                                                    {/* Description */}
                                                                    {tache.description && (
                                                                        <p className="text-sm text-muted-foreground leading-relaxed">
                                                                            {getTacheDescription(tache)}
                                                                        </p>
                                                                    )}
                                                                    
                                                                    {/* Contenu/Formulaire - seulement si la tâche a du contenu avec des champs de formulaire */}
                                                                    {(() => {
                                                                        // Parser le contenu si c'est une chaîne JSON
                                                                        let contenuParsed: any = null;
                                                                        if (tache.contenu) {
                                                                            if (typeof tache.contenu === 'string') {
                                                                                try {
                                                                                    contenuParsed = JSON.parse(tache.contenu);
                                                                                } catch (e) {
                                                                                    console.error('Erreur lors du parsing du contenu:', e);
                                                                                    contenuParsed = null;
                                                                                }
                                                                            } else {
                                                                                contenuParsed = tache.contenu;
                                                                            }
                                                                        }
                                                                        
                                                                        // Vérifier si le contenu parsé contient des champs de formulaire
                                                                        const hasFormContent = contenuParsed && 
                                                                            (Array.isArray(contenuParsed) ||
                                                                             (typeof contenuParsed === 'object' && 
                                                                              Object.keys(contenuParsed).length > 0 &&
                                                                              (Array.isArray(contenuParsed.fields) || 
                                                                               Array.isArray(contenuParsed.champs) ||
                                                                               Object.values(contenuParsed).some((val: any) => 
                                                                                   typeof val === 'object' && val !== null && (val.type || val.label)
                                                                               ))));
                                                                        
                                                                        return hasFormContent ? (
                                                                            <div className={cn(
                                                                                isTacheAFaire && "pointer-events-none opacity-50"
                                                                            )}>
                                                                                <FormulaireDynamique
                                                                                    templateTache={tache as unknown as TemplateTacheDTO}
                                                                                    agentId={agentId}
                                                                                    readOnly={tache.statut === 'fait'}
                                                                                    modePrestataire={shouldApplyPrestataireModeForTache(tache)}
                                                                                    modeConseiller={shouldApplyConseillerModeForTache(tache)}
                                                                                    onFormDataChange={(data) => {
                                                                                        setFormData(prev => ({
                                                                                            ...prev,
                                                                                            [tache.id]: data
                                                                                        }));
                                                                                    }}
                                                                                    onValidationChange={(isValid, errors) => {
                                                                                        setFormValidationByTache(prev => ({
                                                                                            ...prev,
                                                                                            [tache.id]: { isValid, errors }
                                                                                        }));
                                                                                    }}
                                                                                />
                                                                            </div>
                                                                        ) : null;
                                                                    })()}
                                                                </CardContent>
                                                                
                                                                <CardFooter className="pt-0">
                                                                    {/* Bouton Valider ou Annuler */}
                                                                    <div className="w-full pt-2 border-t border-border">
                                                                        {tache.statut === 'fait' ? (
                                                                            <Button 
                                                                                className="w-full mt-2" 
                                                                                size="lg"
                                                                                variant="outline"
                                                                                disabled={isSubmitting}
                                                                                onClick={async () => {
                                                                                    handleAnnulerValidationRequest(tache);
                                                                                }}
                                                                            >
                                                                                <XCircle className="size-4 mr-2" />
                                                                                {isSubmitting ? 'Annulation...' : 'Annuler la validation'}
                                                                            </Button>
                                                                        ) : (
                                                                            <Button 
                                                                                className="w-full mt-2" 
                                                                                size="lg"
                                                                                disabled={isSubmitting || tache.statut === 'fait' || isTacheAFaire}
                                                                                onClick={async () => {
                                                                                    await handleValidateTache(tache);
                                                                                }}
                                                                            >
                                                                                <CheckCircle2 className="size-4 mr-2" />
                                                                                {isSubmitting ? 'Validation...' : isTacheAFaire ? 'Tâche non disponible' : 'Valider la tâche'}
                                                                            </Button>
                                                                        )}
                                                                    </div>
                                                                </CardFooter>
                                                            </Card>
                                                        </div>
                                                    );
                                                })}
                                            </AnimatedList>
                                        </div>
                                    )}
                                </div>
                            </>
                        ) : (
                            <div className="flex-1 flex items-center justify-center">
                                <div className="text-center text-muted-foreground">
                                    <FileText className="size-16 mx-auto mb-4 opacity-50" />
                                    <p className="text-lg font-medium">Sélectionnez un groupe de tâches</p>
                                    <p className="text-sm mt-2">Cliquez sur un groupe dans la liste pour voir ses tâches</p>
                                </div>
                            </div>
                        )}
                        </div>
                    </ResizablePanel>
                </ResizablePanelGroup>
            </TabsContent>

            <TabsContent value="avancement" className="h-full">
                <div className="flex h-full min-h-0">
                    {/* Colonne 1 : Directions - Taille fixe */}
                    <div className="w-64 border-r flex flex-col min-h-0 bg-white dark:bg-transparent shrink-0">
                        <div className="p-4 border-b shrink-0">
                            <h3 className="text-base font-semibold">Directions</h3>
                            <p className="text-xs text-muted-foreground">Filtrer les groupes par direction</p>
                        </div>
                        <div className="flex-1 overflow-y-auto p-2 space-y-2 min-h-0">
                            {directions.map((dir) => {
                                const isActive = (selectedDirection ?? direction ?? "Direction") === dir;
                                return (
                                    <button
                                        key={dir}
                                        className={cn(
                                            "w-full text-left px-3 py-2 rounded-lg transition-colors",
                                            isActive
                                                ? "bg-primary text-white"
                                                : "bg-muted hover:bg-muted/70 dark:text-white"
                                        )}
                                        onClick={() => {
                                            setSelectedDirection(dir);
                                            setSelectedGroupeIdAvancement(null);
                                        }}
                                    >
                                        {dir}
                                    </button>
                                );
                            })}
                        </div>
                    </div>

                    {/* Colonne 2 : Groupes de tâches - Taille fixe */}
                    <div className="w-80 border-r flex flex-col min-h-0 bg-white dark:bg-transparent shrink-0 overflow-hidden">
                        <div className="p-4 border-b shrink-0">
                            <h3 className="text-base font-semibold">Groupes de tâches</h3>
                            <p className="text-xs text-muted-foreground">
                                Sélectionnez un groupe pour voir ses tâches
                            </p>
                        </div>
                        <div className="flex-1 overflow-y-auto p-4 space-y-3 min-h-0">
                            {groupesAvancement.length === 0 ? (
                                <p className="text-sm text-muted-foreground">Aucun groupe pour cette direction</p>
                            ) : (
                                groupesAvancement.map((groupe) => {
                                    const isSelected = selectedGroupeIdAvancement === groupe.id;
                                    return (
                                        <button
                                            key={groupe.id}
                                            className={cn(
                                                "w-full text-left p-3 rounded-lg border transition-all",
                                                isSelected
                                                    ? "border-primary ring-2 ring-primary/30"
                                                    : "border-border hover:border-primary/50"
                                            )}
                                            onClick={() => setSelectedGroupeIdAvancement(groupe.id)}
                                        >
                                            <div className="flex items-start justify-between gap-2">
                                                <div className="flex-1 min-w-0">
                                                    <p className="font-semibold break-words whitespace-normal">{getGroupeLibelle(groupe)}</p>
                                                    <p className="text-xs text-muted-foreground break-words whitespace-normal">
                                                        {groupe.code}
                                                    </p>
                                                </div>
                                                <Badge className={cn(
                                                    "shrink-0 mt-0.5",
                                                    getStatutBadgeClasses(groupe.statut as string)
                                                )}>
                                                    {formatStatut(groupe.statut as string)}
                                                </Badge>
                                            </div>
                                        </button>
                                    );
                                })
                            )}
                        </div>
                    </div>

                    {/* Colonne 3 : Détails des tâches - Taille flexible mais avec scroll */}
                    <div className="flex-1 flex flex-col min-h-0 bg-background overflow-hidden">
                        <div className="p-4 border-b shrink-0">
                            <h3 className="text-base font-semibold">Détails des tâches</h3>
                            <p className="text-xs text-muted-foreground">
                                Liste des tâches du groupe sélectionné
                            </p>
                        </div>
                        <div className="flex-1 overflow-y-auto p-6 min-h-0">
                            {selectedGroupeAvancement ? (
                                <div className="space-y-6">
                                    <div className="flex items-center justify-between">
                                        <div>
                                            <h4 className="text-lg font-semibold">{getGroupeLibelle(selectedGroupeAvancement)}</h4>
                                            <p className="text-sm text-muted-foreground">{selectedGroupeAvancement.code}</p>
                                        </div>
                                        <Badge className={cn(
                                            "shrink-0",
                                            getStatutBadgeClasses(selectedGroupeAvancement.statut as string)
                                        )}>
                                            {formatStatut(selectedGroupeAvancement.statut as string)}
                                        </Badge>
                                    </div>

                                    <div className="space-y-4">
                                        {tachesAvancement.length === 0 ? (
                                            <p className="text-sm text-muted-foreground">Aucune tâche</p>
                                        ) : (
                                            tachesAvancement.map((tache) => {
                                                const isFormulaire = tache.type === 'formulaire' || tache.type === 'FORMULAIRE';
                                                // Utiliser reponses si disponible, sinon contenu (pour compatibilité)
                                                const reponsesTache = tache.reponses || tache.contenu || {};
                                                const hasReponses = reponsesTache && Object.keys(reponsesTache).length > 0;
                                                
                                                // Afficher les réponses si c'est un formulaire ET qu'il y a des réponses
                                                // Maintenant on affiche même pour les tâches en cours
                                                const shouldShowFormulaireFields = isFormulaire && hasReponses;
                                                
                                                return (
                                                    <div
                                                        key={tache.id}
                                                        className="p-4 rounded-lg border bg-white dark:bg-card"
                                                    >
                                                        <div className="flex items-start justify-between gap-4 mb-3">
                                                            <div className="flex-1 min-w-0 pr-2">
                                                                <p className="font-medium break-words whitespace-normal text-sm sm:text-base">{getTacheLibelle(tache)}</p>
                                                                {tache.description && (
                                                                    <p className="text-xs text-muted-foreground break-words whitespace-normal mt-1">
                                                                        {tache.description}
                                                                    </p>
                                                                )}
                                                            </div>
                                                            <Badge className={cn(
                                                                "shrink-0 mt-0.5 ml-2",
                                                                getStatutBadgeClasses(tache.statut as string)
                                                            )}>
                                                                {formatStatut(tache.statut as string)}
                                                            </Badge>
                                                        </div>
                                                        
                                                        {/* Afficher les réponses du formulaire si disponibles (maintenant pour toutes les tâches, même en cours) */}
                                                        {shouldShowFormulaireFields && (
                                                            <div className="mt-4 pt-4 border-t">
                                                                <p className="text-sm font-semibold mb-3 text-muted-foreground">Réponses du formulaire :</p>
                                                                {templateTachesMap.has(tache.id) ? (
                                                                    <FormulaireReadOnly
                                                                        templateTache={templateTachesMap.get(tache.id) || null}
                                                                        filledValues={reponsesTache}
                                                                        agentId={agentId}
                                                                    />
                                                                ) : (
                                                                    <p className="text-sm text-muted-foreground">Chargement du formulaire...</p>
                                                                )}
                                                            </div>
                                                        )}
                                                    </div>
                                                );
                                            })
                                        )}
                                    </div>
                                </div>
                            ) : (
                                <div className="flex items-center justify-center h-full">
                                    <div className="text-center text-muted-foreground">
                                        <FileText className="size-16 mx-auto mb-4 opacity-50" />
                                        <p className="text-lg font-medium">Sélectionnez un groupe</p>
                                        <p className="text-sm mt-2">Choisissez un groupe dans la liste pour voir ses tâches</p>
                                    </div>
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            </TabsContent>
        </Tabs>
        </>
    );
}
