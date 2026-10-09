import { ContenuProcessPage } from "./components/contenuPage/processus/creation_process/contenuProcess";
import { ContenuCreationProcessPage } from "./components/contenuPage/processus/creation_process/contenuCreationProcess";
import Dashboard from "./components/contenuPage/dashboard/Dashboard";
import { SiteHeader } from "./components/NavBar/SiteHeader";

import AppSideBar from "./components/sidebar/AppSideBar";
import TableauListeAgent from "./components/tableau/tableau-Agents/TableauListeAgent";
import TableauAncienAgent from "./components/tableau/Tableau-AncienAgent/TableauAncienAgent";
import { SidebarInset, SidebarProvider } from "./components/ui/sidebar";
import ProcessusDetailPage from "./components/contenuPage/processus/contenu_process/ProcessusDetailPage";
import AgentDetailPage from "./components/contenuPage/agent/AgentDetailPage";
import { ProcessusEditorPage } from "./components/contenuPage/processus/ProcessusEditor";
import ProcessusActifsPage from "./components/contenuPage/processus/ProcessusActifsPage";

type SidebarProps = { open: boolean; 
    setOpen: React.Dispatch<React.SetStateAction<boolean>>; };

export default function Home({open , setOpen} : SidebarProps) {
    return (
      <SidebarProvider open={open} onOpenChange={setOpen}>
            <AppSideBar/>
            <SidebarInset>
            <SiteHeader titrePage="Dashboard" description="Vue sur l'ensemble des processus d'entrée/sortie"/>
            <Dashboard />

            </SidebarInset>
        </SidebarProvider>
    )
}

export  function ListeAgent({open , setOpen} : SidebarProps)
{
    return (
      <SidebarProvider open={open} onOpenChange={setOpen}>
            <AppSideBar/>
                <SidebarInset className="flex flex-col min-h-0"> 
                <SiteHeader titrePage="Liste des utilisateurs" description="Vue sur l'ensemble des utilisateurs"/>
                <div className="flex-1 min-h-0">
                    <TableauListeAgent/>
                </div>
                </SidebarInset>
        </SidebarProvider>
    )
}


export  function Historique({open , setOpen} : SidebarProps)
{
    return (
      <SidebarProvider open={open} onOpenChange={setOpen}>
            <AppSideBar/>
                <SidebarInset className="flex flex-col min-h-0">
                <SiteHeader titrePage="Historique" description="Vue sur l'ensemble des anciens utilisateurs"/>
                <div className="flex-1 min-h-0">
                    <TableauAncienAgent/>
                </div>
                </SidebarInset>
        </SidebarProvider>
    )
}




type ProcessPageProps = SidebarProps & { type?: "entree" | "sortie" };

export  function ProcessPage({open , setOpen, type = "entree"} : ProcessPageProps)
{
    const isSortie = type === "sortie";
    return (
      <SidebarProvider open={open} onOpenChange={setOpen}>
            <AppSideBar/>
                <SidebarInset className="flex flex-col min-h-0">
                <SiteHeader
                    titrePage={isSortie ? "Processus de Sortie" : "Processus d'Entrée et de Mobilité Interne"}
                    description={isSortie ? "Modèles de processus de sortie" : "Vue sur l'ensemble des processus d'entrée et de mobilité interne"}
                />
                <div className="flex-1 min-h-0 overflow-auto">
                    <ContenuProcessPage type={type}/>
                </div>
                </SidebarInset>
        </SidebarProvider>
    )
}


export function CreationProcessPage({open , setOpen} : SidebarProps)
{
    return (
      <SidebarProvider open={open} onOpenChange={setOpen}>
            <AppSideBar/>
                <SidebarInset className="flex flex-col min-h-0">
                <SiteHeader titrePage="Création de processus" description="Créer un nouveau processus à partir d'un template"/>
                <ContenuCreationProcessPage/>
                </SidebarInset>
        </SidebarProvider>
    )
}


export function ProcessusDetailPageComponent({open , setOpen} : SidebarProps)
{
    return (
      <SidebarProvider open={open} onOpenChange={setOpen}>
            <AppSideBar/>
            <SidebarInset className="flex flex-col min-h-0">
            <SiteHeader titrePage="Détail du processus" description="Vue sur les détails d'un processus"/>
            <ProcessusDetailPage/>
            </SidebarInset>
        </SidebarProvider>
    )
}

export function AgentDetailPageComponent({open , setOpen} : SidebarProps)
{
    return (
      <SidebarProvider open={open} onOpenChange={setOpen}>
            <AppSideBar/>
            <SidebarInset className="flex flex-col min-h-0">
            <SiteHeader titrePage="Détail de l'utilisateur" description="Vue sur les informations d'un utilisateur"/>
            <AgentDetailPage/>
            </SidebarInset>
        </SidebarProvider>
    )
}


export  function ProcessusEditor({open , setOpen} : SidebarProps)
{
    return (
      <SidebarProvider open={open} onOpenChange={setOpen}>
            <AppSideBar/>
                <SidebarInset className="flex flex-col min-h-0"> 
                <SiteHeader titrePage="Éditeur de processus" description="Éditer un processus"/>
                <div className="flex-1 min-h-0">
                    <ProcessusEditorPage/>
                </div>
                </SidebarInset>
        </SidebarProvider>
    )
}

export function ProcessusActifsPageComponent({ open, setOpen }: SidebarProps) {
    return (
        <SidebarProvider open={open} onOpenChange={setOpen}>
            <AppSideBar />
            <SidebarInset className="flex flex-col min-h-0">
                <SiteHeader
                    titrePage="Processus en cours"
                    description="Sélectionne un processus pour consulter ses tâches"
                />
                <div className="flex-1 min-h-0 overflow-auto">
                    <ProcessusActifsPage />
                </div>
            </SidebarInset>
        </SidebarProvider>
    );
}
