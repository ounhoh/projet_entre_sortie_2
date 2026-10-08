import { cn } from "@/lib/utils";
import { type TemplateProcessusDTO } from "@/service/templateProcessusService";
import { useNavigate } from "react-router-dom";
import { FileText, CheckCircle2, Target } from "lucide-react";


export const getElement = (
  template: TemplateProcessusDTO,
  navigate: ReturnType<typeof useNavigate>,
  modePrestataire?: boolean,
  modeConseiller?: boolean
) => {
    const groupeCount = template.groupeTacheDTOS?.length || 0;
    const statutCount = template.statutProcessusDTOList?.length || 0;
    const typeColor = template.code.includes('ENTREE') 
        ? 'rgb(59, 130, 246)' // blue
        : template.code.includes('MOBILITE')
        ? 'rgb(147, 51, 234)' // purple
        : 'rgb(34, 197, 94)'; // green
    return (
        <figure
        key={template.id}
        onClick={() => {
          const basePath = `/creation_process/${template.id}`;
          const search = modePrestataire ? '?mode=prestataire' : '';
          const searchConseiller = modeConseiller ? '?mode=conseiller' : '';
          navigate(`${basePath}${search}${searchConseiller}`);
        }}
        className={cn(
            "relative min-h-fit w-full min-w-[300px] max-w-[400px] flex-1 cursor-pointer overflow-hidden rounded-2xl p-4",
            // animation styles
            "transition-all duration-200 ease-in-out hover:scale-[103%]",
            // light styles
            "bg-white [box-shadow:0_0_0_1px_rgba(0,0,0,.03),0_2px_4px_rgba(0,0,0,.05),0_12px_24px_rgba(0,0,0,.05)]",
            // dark styles
            "transform-gpu dark:bg-transparent dark:[box-shadow:0_-20px_80px_-20px_#ffffff1f_inset] dark:backdrop-blur-md dark:[border:1px_solid_rgba(255,255,255,.1)]"
        )}
    >
        <div className="flex flex-col gap-3">
            <div className="flex flex-row items-start gap-4">
                <div
                    className="flex size-14 items-center justify-center rounded-2xl shrink-0"
                    style={{
                        backgroundColor: typeColor + '15', // 15% opacity
                    }}
                >
<FileText className="size-7" style={{ color: typeColor }} />
                </div>
                <div className="flex flex-col overflow-hidden flex-1 min-w-0">
                    <figcaption className="flex flex-row items-start">
                        <span className="text-base sm:text-lg font-semibold leading-tight dark:text-white line-clamp-3">{template.libelle}</span>
                    </figcaption>
                    <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
                        Code: {template.code}
                    </p>
                </div>
            </div>
            
            <div className="flex flex-col gap-2 pt-2 border-t border-gray-100 dark:border-gray-800">
                <div className="flex items-center gap-2">
                    <div className="flex items-center gap-2 text-sm">
                        <Target className="size-4 text-muted-foreground" />
                        <span className="font-medium dark:text-white/90">Groupes de tâches:</span>
                    </div>
                    <span className="text-sm text-muted-foreground">{groupeCount} groupe{groupeCount > 1 ? 's' : ''}</span>
                </div>
                <div className="flex items-center gap-2">
                    <div className="flex items-center gap-2 text-sm">
                        <CheckCircle2 className="size-4 text-muted-foreground" />
                        <span className="font-medium dark:text-white/90">Statuts:</span>
                    </div>
                    <span className="text-sm text-muted-foreground">{statutCount} statut{statutCount > 1 ? 's' : ''}</span>
                </div>
            </div>
        </div>
    </figure>
    );
}