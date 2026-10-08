import { useState, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import { toast } from "sonner";
import { BarreRecherche } from "@/components/recherche/Recherche";
import type { TemplateGroupeTacheDTO, TemplateProcessusDTO } from "@/service/templateProcessusService";
import type { TemplateTacheDTO } from "@/service/templateTacheService";
import { cn } from "@/lib/utils";
import { BookText, FileText } from "lucide-react";
import { AnimatedList } from "../../../ui/animated-list";
import { ResizableHandle, ResizablePanel, ResizablePanelGroup } from "../../../ui/resizable";
import { Separator } from "../../../ui/separator";
import { Button } from "../../../ui/button";
import { FormulaireDynamique } from "../../../Formulaire/FormulaireDynamique";
import { CheckCircle2 } from "lucide-react";
import { processusService } from "@/service/processusService";

interface GroupeTacheViewProps {
    groupes: TemplateGroupeTacheDTO[];
    templateLibelle: string;
    premierStatutId?: string;
    templateId?: string;
    template?: TemplateProcessusDTO;
    prefillValues?: Record<string, any>;
    prefillAgentId?: string;
    modePrestataire?: boolean;
    modeConseiller?: boolean;
}

export function GroupeTacheView({ groupes, templateLibelle, premierStatutId, templateId, template, prefillValues, prefillAgentId, modePrestataire, modeConseiller }: GroupeTacheViewProps) {
    const [search, setSearch] = useState("");
    const [selectedGroupeId, setSelectedGroupeId] = useState<string | null>(null);
    const [formData, setFormData] = useState<Record<string, any>>({});
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [formValidation, setFormValidation] = useState<{ isValid: boolean; errors: string[] }>({ isValid: false, errors: [] });
    const navigate = useNavigate();

    const normalize = (value: any) =>
        String(value || "")
            .trim()
            .toLowerCase()
            .normalize('NFD')
            .replace(/[\u0300-\u036f]/g, '');
    const isSortieRhLabel = (value: any) => {
        const v = normalize(value);
        return v.includes("sortie") && v.includes("agent") && (v.includes("rh") || v.includes("dappi") || v.includes("daf"));
    };

    const getGroupeLibelle = (groupe: TemplateGroupeTacheDTO) => {
        if (modePrestataire && groupe.code === 'form_agent_creation_rh') {
            return "Formulaire de création d'un prestataire";
        }
        if (modeConseiller && groupe.code === 'form_agent_creation_rh') {
            return "Formulaire de création d'un conseiller/attaché";
        }
        // Sortie prestataire (même logique que création prestataire : libellés spécifiques DAPPI)
        if (modePrestataire) {
            const code = normalize(groupe.code);
            const lib = normalize(groupe.libelle);
            if ((code.includes('sortie') && (code.includes('rh') || lib.includes('rh') || lib.includes('dappi'))) || isSortieRhLabel(groupe.libelle)) {
                return "Formulaire de sortie d'un prestataire";
            }
        }
        // Sortie conseiller/attaché (même logique que création conseiller/attaché : libellés spécifiques DAF)
        if (modeConseiller) {
            const code = normalize(groupe.code);
            const lib = normalize(groupe.libelle);
            if ((code.includes('sortie') && (code.includes('rh') || lib.includes('rh') || lib.includes('daf'))) || isSortieRhLabel(groupe.libelle)) {
                return "Formulaire de sortie d'un conseiller/attaché";
            }
        }
        return groupe.libelle;
    };

    const getTacheLibelle = (tache: TemplateTacheDTO) => {
        if (modePrestataire && tache.code === 'form_agent_creation_rh') {
            return "Formulaire de création d'un prestataire";
        }
        if (modeConseiller && tache.code === 'form_agent_creation_rh') {
            return "Formulaire de création d'un conseiller/attaché";
        }
        if (modePrestataire) {
            const code = normalize(tache.code);
            const lib = normalize(tache.libelle);
            if ((code.includes('sortie') && (code.includes('rh') || lib.includes('rh') || lib.includes('dappi'))) || isSortieRhLabel(tache.libelle)) {
                return "Formulaire de sortie d'un prestataire";
            }
        }
        if (modeConseiller) {
            const code = normalize(tache.code);
            const lib = normalize(tache.libelle);
            if ((code.includes('sortie') && (code.includes('rh') || lib.includes('rh') || lib.includes('daf'))) || isSortieRhLabel(tache.libelle)) {
                return "Formulaire de sortie d'un conseiller/attaché";
            }
        }
        return tache.libelle;
    };

    const getTacheDescription = (tache: TemplateTacheDTO) => {
        if (modePrestataire && tache.code === 'form_agent_creation_rh') {
            return "Tâche de création d'un prestataire";
        }
        if (modeConseiller && tache.code === 'form_agent_creation_rh') {
            return "Tâche de création d'un conseiller/attaché";
        }
        if (modePrestataire) {
            const code = normalize(tache.code);
            const lib = normalize(tache.libelle);
            const desc = normalize(tache.description);
            if (
                (code.includes('sortie') && (code.includes('rh') || lib.includes('rh') || lib.includes('dappi') || desc.includes('rh') || desc.includes('dappi'))) ||
                isSortieRhLabel(tache.description) ||
                isSortieRhLabel(tache.libelle)
            ) {
                return "Tâche de sortie d'un prestataire";
            }
        }
        if (modeConseiller) {
            const code = normalize(tache.code);
            const lib = normalize(tache.libelle);
            const desc = normalize(tache.description);
            if (
                (code.includes('sortie') && (code.includes('rh') || lib.includes('rh') || lib.includes('daf') || desc.includes('rh') || desc.includes('daf'))) ||
                isSortieRhLabel(tache.description) ||
                isSortieRhLabel(tache.libelle)
            ) {
                return "Tâche de sortie d'un conseiller/attaché";
            }
        }
        return tache.description;
    };
    
    // Filtrer les groupes pour n'afficher que ceux du premier statut
    const groupesFiltresParStatut = useMemo(() => {
        if (!premierStatutId) return groupes;
        return groupes.filter((groupe) => groupe.statutProcessusId === premierStatutId);
    }, [premierStatutId, groupes]);

    // Filtrer les groupes selon la recherche
    const groupesFiltres = useMemo(() => {
        if (!search.trim()) return groupesFiltresParStatut;
        const searchLower = search.toLowerCase();
        return groupesFiltresParStatut.filter(
            (groupe) =>
                groupe.libelle.toLowerCase().includes(searchLower) ||
                groupe.code.toLowerCase().includes(searchLower) ||
                (groupe.tacheDTOList && groupe.tacheDTOList.some((tache: any) =>
                    tache.libelle?.toLowerCase().includes(searchLower) ||
                    tache.description?.toLowerCase().includes(searchLower)
                ))
        );
    }, [search, groupesFiltresParStatut]);

    // Trouver le groupe sélectionné
    const selectedGroupe = groupes.find((g) => g.id === selectedGroupeId);
    const taches = selectedGroupe?.tacheDTOList || [];

    // Fonction pour valider une tâche et créer le processus
    const handleValidateTache = async (tache: TemplateTacheDTO) => {
        // Vérifier que le template est de type "entree" (via le code)
        if (!templateId || !template) {
            toast.error('Erreur: Template manquant');
            return;
        }

        // Vérifier que le template est de type "entree" ou "sortie" (le code contient "entrée", "entree" ou "sortie")
        const codeLower = template.code.toLowerCase();
        const isEntreeTemplate = codeLower.includes('entrée') || codeLower.includes('entree');
        const isSortieTemplate = codeLower.includes('sortie');
        if (!isEntreeTemplate && !isSortieTemplate) {
            toast.error('Cette fonctionnalité est uniquement disponible pour les processus d\'entrée ou de sortie');
            return;
        }

        // Vérifier qu'il n'y a qu'une seule tâche avec formulaire (simplifié : on vérifie juste si elle a du contenu)
        const tachesAvecFormulaire = taches.filter((t: TemplateTacheDTO) => {
            return t.contenu && (
                (typeof t.contenu === 'string' && t.contenu.trim().length > 0) ||
                (typeof t.contenu === 'object' && t.contenu !== null && Object.keys(t.contenu).length > 0)
            );
        });

        if (tachesAvecFormulaire.length !== 1 || tachesAvecFormulaire[0].id !== tache.id) {
            toast.error('Veuillez remplir la seule tâche avec formulaire pour continuer');
            return;
        }

        // Récupérer les données du formulaire pour cette tâche
        const tacheFormData = formData[tache.id] || {};
        
        // Valider le formulaire
        if (!formValidation.isValid) {
            const requiredLabels = formValidation.errors
                .filter(err => err.startsWith('Le champ "') && err.endsWith('" est requis'))
                .map(err => err.replace('Le champ "', '').replace('" est requis', '').trim())
                .filter(Boolean);
            const otherLabels = formValidation.errors
                .filter(err => err.startsWith('Le champ "') && !err.endsWith('" est requis'))
                .map(err => err.replace('Le champ "', '').replace(/".*$/, '').trim())
                .filter(Boolean);
            if (requiredLabels.length > 0) {
                toast.error("Éléments à remplir", {
                    description: `Éléments à remplir: ${requiredLabels.join(', ')}`
                });
            }
            if (otherLabels.length > 0) {
                toast.error("Champs à modifier", {
                    description: `Champs à modifier: ${otherLabels.join(', ')}`
                });
            }
            return;
        }

        try {
            setIsSubmitting(true);

            // Convertir le rôle vers le format attendu par le backend (AGENT, MANAGER, ADMIN, PRESTATAIRE, CONSEILLERATTACHE)
            let roleValue = tacheFormData.role;
            if (typeof roleValue === 'string') {
                // Si c'est déjà en majuscules, on garde, sinon on convertit
                roleValue = roleValue.toUpperCase();
            }

            // Préparer les données du formulaire pour l'API
            const formulaireData = {
                ...tacheFormData,
                ...(roleValue ? { role: roleValue } : {})
            };

            // Pour le mode prestataire, forcer le rôle à PRESTATAIRE
            if (modePrestataire) {
                formulaireData.role = 'PRESTATAIRE';
            }

            // Pour le mode conseiller/attaché, forcer le rôle à CONSEILLERATTACHE
            if (modeConseiller) {
                formulaireData.role = 'CONSEILLER';
            }

            // Appeler l'API pour créer le processus
            await processusService.createProcessus({
                templateProcessusId: templateId,
                formulaireData: formulaireData
            });

            toast.success("Tâche validée", {
                description: `${tache.libelle} validée`
            });

            // Rediriger vers le dashboard
            navigate('/dashboard');
        } catch (error) {
            console.error('Erreur lors de la création du processus:', error);
            toast.error("Tâche non validée", {
                description: "Erreur lors de la validation de la tâche. Veuillez réessayer."
            });
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <ResizablePanelGroup orientation="horizontal" className="h-full min-h-0">
            <ResizablePanel defaultSize={30} minSize={20} className="min-h-0">
            {/* Panneau gauche - Liste des groupes */}
            <div className="h-full border-r flex flex-col min-h-0 bg-white dark:bg-transparent">
                {/* En-tête avec titre */}
                <div className="p-4 border-b">
                    <h2 className="text-lg font-semibold">{templateLibelle}</h2>
                </div>

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
                                const tacheCount = groupe.tacheDTOList?.length || 0;
                                const groupeColor = 'rgb(59, 130, 246)'; // blue
                                
                                return (
                                    <figure
                                        key={groupe.id}
                                        onClick={() => setSelectedGroupeId(groupe.id)}
                                        className={cn(
                                            "relative min-h-fit w-full min-w-[250px] flex-1 cursor-pointer overflow-hidden rounded-2xl p-4",
                                            // animation styles
                                            "transition-all duration-200 ease-in-out hover:scale-[103%]",
                                            // light styles
                                            "bg-white [box-shadow:0_0_0_1px_rgba(0,0,0,.03),0_2px_4px_rgba(0,0,0,.05),0_12px_24px_rgba(0,0,0,.05)]",
                                            // dark styles
                                            "transform-gpu dark:bg-transparent dark:[box-shadow:0_-20px_80px_-20px_#ffffff1f_inset] dark:backdrop-blur-md dark:[border:1px_solid_rgba(255,255,255,.1)]",
                                            // selected style
                                            isSelected && "ring-2 ring-primary ring-offset-2"
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
                                                <div className="flex flex-col overflow-hidden flex-1 min-w-0">
                                                            <figcaption className="flex flex-row items-start">
                                                        <span className={cn(
                                                            "text-base sm:text-lg font-semibold leading-tight line-clamp-2",
                                                            isSelected ? "text-primary" : "dark:text-white"
                                                        )}>
                                                                    {getGroupeLibelle(groupe)}
                                                        </span>
                                                    </figcaption>
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
            <div className="h-full flex flex-col min-h-0 bg-white dark:bg-transparent">
                {selectedGroupe ? (
                    <>
                        {/* En-tête du panneau droit */}
                        <div className="p-4 border-b flex items-center justify-between">
                            <div>
                                <h3 className="text-lg font-semibold">{getGroupeLibelle(selectedGroupe)}</h3>
                                <p className="text-sm text-muted-foreground">{selectedGroupe.code}</p>
                            </div>
                        </div>

                        {/* Liste des tâches */}
                        <div className="flex-1 overflow-y-auto p-6">
                            {taches.length === 0 ? (
                                <div className="text-center text-muted-foreground">
                                    <BookText className="size-12 mx-auto mb-4 opacity-50" />
                                    <p>Aucune tâche dans ce groupe</p>
                                </div>
                            ) : (
                                <div className="space-y-8">
                                    <AnimatedList>
                                        {taches.map((tache: TemplateTacheDTO, index: number) => {
                                            const showStepNumber = taches.length > 1;
                                            const stepNumber = index + 1;
                                            
                                            return (
                                                <div key={`tache-${tache.id}-${index}`} className="flex gap-6">
                                                    {/* Numéro d'étape à gauche (si plus d'une tâche) */}
                                                    {showStepNumber && (
                                                        <div className="shrink-0">
                                                            <div className="bg-gray-800 dark:bg-gray-700 text-white rounded-lg px-4 py-3 min-w-[80px] text-center font-semibold text-lg">
                                                                Step {stepNumber}
                                                            </div>
                                                        </div>
                                                    )}
                                                    
                                                    {/* Carte de la tâche */}
                                                    <div className={cn(
                                                        "flex-1 bg-white dark:bg-gray-900 rounded-xl border border-gray-200 dark:border-gray-800 shadow-sm overflow-hidden",
                                                        "transition-all duration-200 hover:shadow-md"
                                                    )}>
                                                        {/* En-tête avec icône et nom */}
                                                        <div className="p-6 pb-4">
                                                            <div className="flex items-center gap-3">
                                                                <div className="size-12 rounded-xl bg-blue-100 dark:bg-blue-900 flex items-center justify-center shrink-0">
                                                                    <BookText className="size-6 text-blue-600 dark:text-blue-400" />
                                                                </div>
                                                                <div className="flex-1 min-w-0">
                                                                    <h4 className="text-lg font-semibold dark:text-white">{getTacheLibelle(tache)}</h4>
                                                                    <p className="text-xs text-muted-foreground mt-0.5">Code: {tache.code}</p>
                                                                </div>
                                                            </div>
                                                        </div>
                                                        
                                                        <Separator />
                                                        
                                                        {/* Description */}
                                                        {getTacheDescription(tache) && (
                                                            <>
                                                                <div className="p-6 pt-4">
                                                                    <p className="text-sm text-muted-foreground leading-relaxed">
                                                                        {getTacheDescription(tache)}
                                                                    </p>
                                                                </div>
                                                                <Separator />
                                                            </>
                                                        )}
                                                        
                                                        {/* Contenu/Formulaire - afficher le formulaire si la tâche a du contenu */}
                                                        {tache.contenu && (
                                                            <>
                                                                <div className="p-6">
                                                                    <FormulaireDynamique 
                                                                        templateTache={tache}
                                                                        onFormDataChange={(data) => {
                                                                            setFormData(prev => ({
                                                                                ...prev,
                                                                                [tache.id]: data
                                                                            }));
                                                                        }}
                                                                        onValidationChange={(isValid, errors) => {
                                                                            setFormValidation({ isValid, errors });
                                                                        }}
                                                                        initialValues={prefillValues}
                                                                        agentId={prefillAgentId}
                                                                        modePrestataire={modePrestataire}
                                                                        modeConseiller={modeConseiller}
                                                                    />
                                                                </div>
                                                                <Separator />
                                                            </>
                                                        )}
                                                        
                                                        {/* Bouton Valider */}
                                                        <div className="p-6 pt-4">
                                                            <Button 
                                                                className="w-full" 
                                                                size="lg"
                                                                disabled={isSubmitting}
                                                                onClick={async () => {
                                                                    await handleValidateTache(tache);
                                                                }}
                                                            >
                                                                <CheckCircle2 className="size-4 mr-2" />
                                                                {isSubmitting ? 'Validation...' : 'Valider la tâche'}
                                                            </Button>
                                                        </div>
                                                    </div>
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
    );
}
