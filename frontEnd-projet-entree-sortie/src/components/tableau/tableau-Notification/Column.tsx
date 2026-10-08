"use client"
import  type { ColumnDef } from "@tanstack/react-table"
import { Button } from "@/components/ui/button"
import { UserRoundPen } from "lucide-react"
import { getIconEntree, getIconMobiliteInterne, getIconSortie } from "@/components/iconTypeProcessus/iconTypeProcessus"
import { Badge } from "@/components/ui/badge"
import { getBadgeAlerte, getBadgeInfo, getBadgeTache } from "@/components/utils/NiveauNotification"



export type Agent = {
    nomPrenom:String
    direction:String
}

export type Notifications = {
    id: string,
    Niveau: "tache" | "alerte" | "information" | "rappel",
    dateEnvoie:string,
    Direction:string,
    agent: Agent,
    Description:string,
    processusId?: string; // ID du processus lié à la notification (optionnel)
}


export const columns: ColumnDef<Notifications>[] = [
  {
    accessorKey: "Niveau",
    header:"Niveau", 
    cell: ({row}) => {
      const value = row.getValue("Niveau") as Notifications["Niveau"]
      return (
        <>
        {value==="tache" && getBadgeTache()}
        {value==="information" && getBadgeInfo()}
        {value==="alerte" && getBadgeAlerte()}

        </>
      )
    }
  },
  {
    accessorKey: "dateEnvoie",
    header:"Date d'envoie", 
  },
  {
    accessorKey: "agent",
    header: ({column}) => {
      return (<Button variant="ghost" onClick={()=> column.toggleSorting(column.getIsSorted() === "asc") }>
        <UserRoundPen/>
        Agent
      </Button>)
    },
    cell: ({row}) => {
       const agent = row.getValue("agent") as { nomPrenom: string, direction : string}
        return (<div className="grid grid-cols-1">
          <div className="font-bold truncate" title={agent.nomPrenom}>
            { agent.nomPrenom}
            </div>
          <div>
            {agent.direction}
          </div>
          </div>)
    }
  },
  {
    accessorKey: "Direction",
    header: "Direction",
    cell: ({row}) => {
       const direction = row.getValue("Direction") as string
        return (<Badge>{direction}</Badge>)
    }
  },
  {
    accessorKey: "Description",
    header:"Description"
}
]