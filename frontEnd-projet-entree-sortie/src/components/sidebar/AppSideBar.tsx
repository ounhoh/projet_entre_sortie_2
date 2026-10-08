import { History, House, Pencil, UserCheck, UserRound, Users, UsersRound } from "lucide-react"
import { Sidebar, SidebarContent, SidebarFooter, SidebarGroup, SidebarGroupContent, SidebarGroupLabel, SidebarHeader, SidebarMenu, SidebarMenuBadge, SidebarMenuButton, SidebarMenuItem, SidebarProvider, SidebarSeparator } from "../ui/sidebar"
import { NavUser } from "./Foot"
import { Link} from "react-router-dom"
import { getCurrentAgentDisplay } from "@/utils/currentAgent"
import logo from "@/assets/logo-CESE-blanc-EXE_baseline.png"

export const Menuitems = [
    {
        title: "Accueil",
        url: "/dashboard",
        icon:House,
        titrePage:"Agents",
        badge:0,
        hasSub:true,
        description:"Vue sur l'ensemble des processus d'entrée/sortie",
        subItems:[
            {
         title:"Process Entrée",
         titrePage: "Processus d'Entrée",
         url: "/dashboard/list_process_entree",
         description:"Vue sur l'ensemble des processus d'entrée"
        },   {
         title:"Process Sortie",
         titrePage: "Processus de Sortie",
         url:"/dashboard/list_process_sortie",
         description:"Vue sur l'ensemble des processus de sortie"
        },   {
         title:"Mobilité Interne",
         url:"/dashboard/list_mobilite_interne",
         titrePage: "Mobilté Interne",
         description:"Vue sur l'ensemble des mobilités internes"
        },
    ]

    },
    {
        title:"Liste des Utilisateurs",
        url: "/liste",
        icon:UsersRound,
        hasSub:false,
        titrePage:"Liste des utilisateurs",
        description: "Vue sur l'ensemble des utilisateurs",
        subItems: []
    },
    {
        title:"Historique",
        url:"/historique",
        icon:History,
        hasSub:false,
        titrePage:"Ensemble des anciens utilisateurs",
        description: "vue sur l'ensemble des anciens utilisateurs",
        subItems: []
    },
    {
        title:"Éditeur de processus",
        url:"/processus-editor",
        icon:Pencil,
        hasSub:false,
        titrePage:"Éditeur de processus",
        description: "Éditer un processus",
        subItems: []
    }
]
const AppSideBar = () => {
    const user = getCurrentAgentDisplay();
    return <Sidebar collapsible="icon" variant={"inset"}  >
        <SidebarHeader className="w-fit h-fit">
            <div>

            <Link to={"/dashboard"}/>
                        <img src={logo} width={180} height={180} alt="logo"/>
            </div>
        </SidebarHeader>
        <SidebarContent>
            <SidebarGroup>
                <SidebarGroupLabel></SidebarGroupLabel>
                <SidebarGroupContent>
                    <SidebarMenu>
                        { Menuitems.map (item => (
                            <SidebarMenuItem key={item.title}>
                                <SidebarMenuButton asChild>
                                    <Link to={item.url}>
                                        <item.icon />
                                        <span className="font-bold">{item.title}</span>
                                    </Link>
                                </SidebarMenuButton>
                                {item.title==="Agents" && (
                                    <SidebarMenuBadge className=" bg-cese-rouge-secondaire text-white">{item.badge}</SidebarMenuBadge>
                                )}
                            </SidebarMenuItem>
                        ))
                        }
                    </SidebarMenu>
                </SidebarGroupContent>
            </SidebarGroup>
        </SidebarContent>
        <SidebarFooter>
        <SidebarSeparator/>
            <NavUser user={user} />
        </SidebarFooter>
    </Sidebar>
}

export default AppSideBar