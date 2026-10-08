import { Circle, CircleCheck, CircleDashed } from "lucide-react"
import { Badge } from "../ui/badge"


export const getBadgeAfaire = () => {
    return <Badge className="bg-blue-500 hover:bg-blue-600 text-white">
        <Circle/>
        à faire
    </Badge>
}


export const getBadgeEnCours = () => {
    return <Badge className="bg-amber-500 hover:bg-amber-600">
        <CircleDashed/>
        en cours 
    </Badge>
}


export const getBadgeFait = () => {
    return <Badge className="bg-green-500 hover:bg-green-600">
        <CircleCheck/>
        fait! 
    </Badge>
}