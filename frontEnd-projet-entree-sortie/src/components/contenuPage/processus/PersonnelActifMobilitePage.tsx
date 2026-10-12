import { useEffect, useMemo, useState } from "react";
import { agentService } from "@/service/agentService";
import type { AgentDTO } from "@/service/processusService";
import { Button } from "@/components/ui/button";
import { Retour } from "@/components/utils/Retour";
import { ArrowLeftRight } from "lucide-react";

type TypePersonnel = "Agent" | "Prestataire" | "Conseiller";

function determinerType(agent: AgentDTO): TypePersonnel {
    const role = String(agent.role ?? "").trim().toUpperCase();

    if (role === "PRESTATAIRE") return "Prestataire";
    if (role.includes("CONSEILLER")) return "Conseiller";

    return "Agent";
}

function libelleBouton(type: TypePersonnel): string {
    switch (type) {
        case "Prestataire":
            return "Basculer ce prestataire en mobilité";
        case "Conseiller":
            return "Basculer ce conseiller en mobilité";
        default:
            return "Basculer cet agent en mobilité";
    }
}

export default function PersonnelActifMobilitePage() {
    const [agents, setAgents] = useState<AgentDTO[]>([]);
    const [recherche, setRecherche] = useState("");
    const [typeSelectionne, setTypeSelectionne] = useState("tous");
    const [directionSelectionnee, setDirectionSelectionnee] = useState("toutes");
    const [chargement, setChargement] = useState(true);
    const [erreur, setErreur] = useState<string | null>(null);

    useEffect(() => {
        let annule = false;

        async function chargerAgents() {
            try {
                setChargement(true);
                setErreur(null);

                const tousLesAgents = await agentService.getAllAgents();
                const agentsActifs = tousLesAgents.filter(
                    (agent) =>
                        String(agent.etatAgent ?? "").trim().toLowerCase() === "actif"
                );

                if (!annule) {
                    setAgents(agentsActifs);
                }
            } catch (cause) {
                console.error("Erreur lors du chargement des employés actifs :", cause);

                if (!annule) {
                    setErreur("Impossible de charger la liste du personnel actif.");
                }
            } finally {
                if (!annule) {
                    setChargement(false);
                }
            }
        }

        chargerAgents();

        return () => {
            annule = true;
        };
    }, []);

    const directions = useMemo(
        () =>
            Array.from(
                new Set(
                    agents
                        .map((agent) => agent.direction?.trim())
                        .filter((direction): direction is string => Boolean(direction))
                )
            ).sort((a, b) => a.localeCompare(b, "fr")),
        [agents]
    );

    const agentsFiltres = useMemo(() => {
        const terme = recherche.trim().toLowerCase();

        return agents.filter((agent) => {
            const correspondAuType =
                typeSelectionne === "tous" ||
                determinerType(agent) === typeSelectionne;

            const correspondALaDirection =
                directionSelectionnee === "toutes" ||
                agent.direction === directionSelectionnee;

            const correspondALaRecherche =
                !terme ||
                [
                    agent.nom,
                    agent.prenom,
                    agent.email,
                    agent.direction,
                    determinerType(agent),
                ].some((valeur) =>
                    String(valeur ?? "").toLowerCase().includes(terme)
                );

            return (
                correspondAuType &&
                correspondALaDirection &&
                correspondALaRecherche
            );
        });
    }, [agents, recherche, typeSelectionne, directionSelectionnee]);

    return (
        <main className="flex h-full min-h-0 flex-col gap-5 overflow-auto bg-slate-50 p-6 lg:p-8 dark:bg-background">
            <header className="flex flex-col gap-4 border-b border-slate-200 pb-4 dark:border-border">
                <div>
                    <Retour to="/dashboard" />
                </div>

                <div>
                    <h2 className="text-xl font-semibold tracking-tight text-slate-900 dark:text-foreground">
                        Personnel actif
                    </h2>
                    <p className="mt-1 text-sm text-slate-600 dark:text-muted-foreground">
                        Sélectionne un agent, prestataire ou conseiller pour une future mobilité interne.
                    </p>
                </div>
            </header>

            <div className="flex flex-col gap-3 md:flex-row md:flex-wrap md:items-end">
                <label className="flex min-w-[220px] flex-1 flex-col gap-1 text-xs font-medium text-slate-600 dark:text-muted-foreground">
                    Rechercher
                    <input
                        type="search"
                        value={recherche}
                        onChange={(event) => setRecherche(event.target.value)}
                        placeholder="Nom, email ou direction..."
                        className="h-10 w-full border border-slate-300 bg-white px-3 text-sm font-normal text-slate-900 outline-none focus-visible:ring-2 focus-visible:ring-ring dark:border-border dark:bg-background dark:text-foreground"
                    />
                </label>

                <label className="flex min-w-[180px] flex-col gap-1 text-xs font-medium text-slate-600 dark:text-muted-foreground">
                    Type de personnel
                    <select
                        value={typeSelectionne}
                        onChange={(event) => setTypeSelectionne(event.target.value)}
                        className="h-10 border border-slate-300 bg-white px-3 text-sm font-normal text-slate-900 dark:border-border dark:bg-background dark:text-foreground"
                    >
                        <option value="tous">Tous les types</option>
                        <option value="Agent">Agent</option>
                        <option value="Prestataire">Prestataire</option>
                        <option value="Conseiller">Conseiller</option>
                    </select>
                </label>

                <label className="flex min-w-[180px] flex-col gap-1 text-xs font-medium text-slate-600 dark:text-muted-foreground">
                    Direction
                    <select
                        value={directionSelectionnee}
                        onChange={(event) => setDirectionSelectionnee(event.target.value)}
                        className="h-10 border border-slate-300 bg-white px-3 text-sm font-normal text-slate-900 dark:border-border dark:bg-background dark:text-foreground"
                    >
                        <option value="toutes">Toutes les directions</option>
                        {directions.map((direction) => (
                            <option key={direction} value={direction}>
                                {direction}
                            </option>
                        ))}
                    </select>
                </label>
            </div>

            {chargement && (
                <p className="text-sm text-muted-foreground">
                    Chargement du personnel...
                </p>
            )}

            {erreur && (
                <p role="alert" className="text-sm text-red-700 dark:text-red-300">
                    {erreur}
                </p>
            )}

            {!chargement && !erreur && agents.length === 0 && (
                <p className="text-sm text-muted-foreground">
                    Aucun membre du personnel actif n’a été trouvé.
                </p>
            )}

            {!chargement && !erreur && agents.length > 0 && agentsFiltres.length === 0 && (
                <p className="text-sm text-muted-foreground">
                    Aucun résultat ne correspond aux critères sélectionnés.
                </p>
            )}

            {!chargement && !erreur && agentsFiltres.length > 0 && (
                <div className="overflow-x-auto border border-slate-200 bg-white dark:border-border dark:bg-card">
                    <table className="w-full min-w-[850px] table-fixed border-collapse text-left text-sm">
                        <colgroup>
                            <col className="w-[18%]" />
                            <col className="w-[14%]" />
                            <col className="w-[18%]" />
                            <col className="w-[26%]" />
                            <col className="w-[24%]" />
                        </colgroup>
                        <thead className="bg-slate-100 text-xs uppercase tracking-wide text-slate-600 dark:bg-muted dark:text-muted-foreground">
                            <tr>
                                <th scope="col" className="px-4 py-3 font-semibold">Nom</th>
                                <th scope="col" className="px-4 py-3 font-semibold">Type</th>
                                <th scope="col" className="px-4 py-3 font-semibold">Direction</th>
                                <th scope="col" className="px-4 py-3 font-semibold">Email</th>
                                <th scope="col" className="px-3 py-3 text-right font-semibold">Basculer en mobilité interne</th>
                            </tr>
                        </thead>
                        <tbody>
                            {agentsFiltres.map((agent) => {
                                const type = determinerType(agent);

                                return (
                                    <tr
                                        key={agent.id}
                                        className="border-t border-slate-200 dark:border-border"
                                    >
                                        <td className="px-4 py-3 font-medium text-slate-900 dark:text-foreground">
                                            {agent.prenom} {agent.nom}
                                        </td>
                                        <td className="px-4 py-3">{type}</td>
                                        <td className="px-4 py-3">{agent.direction || "—"}</td>
                                        <td className="px-4 py-3">{agent.email || "—"}</td>
                                        <td className="px-4 py-3 text-center">
                                            <Button
                                                type="button"
                                                size="icon"
                                                className="bg-amber-500 text-white hover:bg-amber-600"
                                                aria-label={libelleBouton(type)}
                                                title={libelleBouton(type)}
                                            >
                                                <ArrowLeftRight />
                                            </Button>
                                        </td>
                                    </tr>
                                );
                            })}
                        </tbody>
                    </table>

                    <p className="border-t border-slate-200 px-4 py-3 text-xs text-slate-500 dark:border-border dark:text-muted-foreground">
                        {agentsFiltres.length} membre{agentsFiltres.length > 1 ? "s" : ""} actif
                        {agentsFiltres.length > 1 ? "s" : ""}
                    </p>
                </div>
            )}
        </main>
    );
}