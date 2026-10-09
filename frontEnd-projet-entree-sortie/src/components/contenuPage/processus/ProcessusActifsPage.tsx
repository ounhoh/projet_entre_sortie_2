import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { processusService } from "@/service/processusService";
import type { ProcessusDTO } from "@/service/processusService";
import type { Process } from "@/components/tableau/tableau-Dashboard/Column";
import TableauProcess from "@/components/tableau/tableau-Dashboard/TableauProcess";
import { mapProcessusToProcess } from "@/utils/processusMapper";

type ProcessType = Process["type"];

function isProcessType(value: string | undefined): value is ProcessType {
    return (
        value === "entree" ||
        value === "sortie" ||
        value === "mobilite_interne"
    );
}

const labels: Record<ProcessType, string> = {
    entree: "Entrée",
    sortie: "Sortie",
    mobilite_interne: "Mobilité interne",
};

export default function ProcessusActifsPage() {
    const { type } = useParams<{ type: string }>();
    const [processus, setProcessus] = useState<Process[]>([]);
    const [search, setSearch] = useState("");
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const processType = isProcessType(type) ? type : null;

    useEffect(() => {
        if (!processType) {
            setLoading(false);
            return;
        }

        let cancelled = false;

        async function loadProcessus() {
            setLoading(true);
            setError(null);

            try {
                const response: ProcessusDTO[] =
                    await processusService.getProcessusActifs();

                const mapped = response
                    .map(mapProcessusToProcess)
                    .filter((processus) => processus.type === processType);

                if (!cancelled) {
                    setProcessus(mapped);
                }
            } catch (cause) {
                console.error("Erreur lors du chargement des processus :", cause);

                if (!cancelled) {
                    setError("Impossible de charger les processus. Réessaie plus tard.");
                    setProcessus([]);
                }
            } finally {
                if (!cancelled) {
                    setLoading(false);
                }
            }
        }

        loadProcessus();

        return () => {
            cancelled = true;
        };
    }, [processType]);

    if (!processType) {
        return <p className="p-8">Type de processus invalide.</p>;
    }

    if (loading) {
        return <p className="p-8">Chargement des processus...</p>;
    }

    if (error) {
        return <p className="p-8 text-red-500">{error}</p>;
    }

    return (
        <div className="flex h-full min-h-0 flex-col p-8">
            <h2 className="mb-4 text-xl font-bold">
                Processus d&apos;{processType === "entree" ? "entrée" : labels[processType]}
                {" "}en cours
            </h2>

            {processus.length === 0 ? (
                <p>Aucun processus actif de type {labels[processType]}.</p>
            ) : (
                <TableauProcess
                    data={processus}
                    search={search}
                    onSearchChange={setSearch}
                    fixedType={processType}
                />
            )}
        </div>
    );
}