import React from "react";
import { Check } from "lucide-react";
import { cn } from "@/lib/utils";
import type { StatutProcessusDTO } from "@/service/templateProcessusService";
import {
    Breadcrumb,
    BreadcrumbItem,
    BreadcrumbLink,
    BreadcrumbList,
    BreadcrumbSeparator,
    BreadcrumbPage
} from "@/components/ui/breadcrumb";

interface ProcessStatusStepperProps {
    statuts: StatutProcessusDTO[];
    currentStatut?: string | null; // id, code ou libelle du statut actuel
}

export function ProcessStatusStepper({ statuts, currentStatut }: ProcessStatusStepperProps) {
    if (!statuts || statuts.length === 0) {
        return null;
    }

    // Trouver le statut actuel ou utiliser le premier par défaut
    const findCurrentStatutIndex = (): number => {
        if (!currentStatut) {
            return 0; // Premier statut par défaut
        }

        const index = statuts.findIndex(
            (statut) =>
                statut.id === currentStatut ||
                statut.code === currentStatut ||
                statut.libelle === currentStatut
        );

        return index >= 0 ? index : 0; // Si non trouvé, utiliser le premier
    };

    const currentIndex = findCurrentStatutIndex();

    return (
        <Breadcrumb className="w-full py-4">
            <BreadcrumbList className="flex items-center gap-0">
                {statuts.map((statut, index) => {
                    const isCompleted = index < currentIndex;
                    const isCurrent = index === currentIndex;
                    const isLast = index === statuts.length - 1;
                    
                    return (
                        <React.Fragment key={statut.id}>
                            <BreadcrumbItem className="relative shrink-0">
                                {/* Boîte du statut */}
                                <div className="relative">
                                    {isCompleted ? (
                                        <BreadcrumbLink 
                                            className={cn(
                                                "px-4 py-2 rounded-md border min-w-[120px] text-center inline-block relative",
                                                "bg-primary border-white text-white hover:text-white"
                                            )}
                                        >
                                            {statut.libelle}
                                            {/* Checkmark en haut à droite */}
                                            <div className="absolute -top-1 -right-1 bg-green-500 rounded-full p-0.5 flex items-center justify-center shadow-sm">
                                                <Check className="w-3 h-3 text-white" strokeWidth={3} />
                                            </div>
                                        </BreadcrumbLink>
                                    ) : isCurrent ? (
                                        <BreadcrumbPage 
                                            className={cn(
                                                "px-4 py-2 rounded-md border min-w-[120px] text-center inline-block",
                                                "bg-white border-primary text-primary font-medium"
                                            )}
                                        >
                                            {statut.libelle}
                                        </BreadcrumbPage>
                                    ) : (
                                        <BreadcrumbLink 
                                            className={cn(
                                                "px-4 py-2 rounded-md border min-w-[120px] text-center inline-block",
                                                "bg-white border-gray-200 text-gray-500 cursor-default"
                                            )}
                                        >
                                            {statut.libelle}
                                        </BreadcrumbLink>
                                    )}
                                </div>
                            </BreadcrumbItem>
                            
                            {/* Séparateur avec ligne et checkmark */}
                            {!isLast && (
                                <>
                                    <BreadcrumbSeparator className="relative flex-1 flex items-center mx-2 min-w-[60px]">
                                        {/* Ligne horizontale personnalisée */}
                                        <div className="flex-1 h-0.5 relative">
                                            {/* Partie complétée (bleue solide) */}
                                            {isCompleted && (
                                                <div className="absolute inset-0 bg-blue-600"></div>
                                            )}
                                            {/* Partie en cours/prochaine (gris pointillé) */}
                                            {!isCompleted && (
                                                <div className="absolute inset-0 border-t-2 border-dashed border-gray-300"></div>
                                            )}
                                        </div>
                                        
                                        
                                        {/* Masquer le chevron par défaut */}
                                        <span className="hidden" />
                                    </BreadcrumbSeparator>
                                </>
                            )}
                        </React.Fragment>
                    );
                })}
            </BreadcrumbList>
        </Breadcrumb>
    );
}
