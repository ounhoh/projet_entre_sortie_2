
"use client"
import  type { ColumnDef } from "@tanstack/react-table"
import { Button } from "@/components/ui/button"
import { Calendar1, CalendarClock, Target, UserRoundPen, Users } from "lucide-react"
import { getIconEntree, getIconMobiliteInterne, getIconSortie } from "@/components/iconTypeProcessus/iconTypeProcessus"
import { Badge } from "@/components/ui/badge"
export type Agent = {
    nomPrenom:String
    direction:String
}

export type Process = {
    id: string,
    type: "entree" | "sortie" | "mobilite_interne",
    agent: Agent
    statut: string,
    dateEcheance:string,
    dateMobilite:string
}
export const getColumns = (dateHeaderLabel: string): ColumnDef<Process>[] => [
  {
    accessorKey: "type",
    header: () => {
      return (<Button variant="ghost">
        <Users/>
        Type
      </Button>)
    },
    cell: ({row}) => {
      const value = row.getValue("type") as Process["type"]
      return (
        <>
        {value==="entree" && getIconEntree()}
        {value==="sortie" && getIconSortie()}
        {value==="mobilite_interne" && getIconMobiliteInterne()}

        </>
      )
    }
  },
  {
    accessorKey: "agent",
    accessorFn: (row) => {
      // Retourne une chaîne recherchable qui combine nomPrenom et direction
      const agent = row.agent as { nomPrenom: string, direction: string }
      return `${agent.nomPrenom} ${agent.direction}`
    },
    header: ({column}) => {
      return (<Button variant="ghost" onClick={()=> column.toggleSorting(column.getIsSorted() === "asc") }>
        <UserRoundPen/>
        Utilisateur
      </Button>)
    },
    cell: ({row}) => {
       const agent = row.original.agent as { nomPrenom: string, direction : string}
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
    accessorKey: "statut",
    header: () => {
        return (<Button variant={"ghost"}><Target/>
        Statut
        </Button>)
    },
    cell: ({row}) => {
      const value = row.getValue("statut") as Process["statut"];
      return (
      <Badge>
       {value as string} 
      </Badge>)
    }
  },
  {
    accessorKey: "dateEcheance",
    header: () => {
      return (<Button variant={"ghost"}>
        <CalendarClock/>
        Date d'Echéance
      </Button>)
    },
  },
  {
    accessorKey: "dateMobilite",
    header: ({}) => {
      return (<Button variant={"ghost"}>

        <Calendar1/>
        {dateHeaderLabel}
      </Button>
      )
    },
  },
]