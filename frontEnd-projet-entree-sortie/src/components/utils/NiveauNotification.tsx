import { Circle, CircleAlert, CircleCheck, CircleDashed, Info } from "lucide-react"
import { Badge } from "../ui/badge"
import { Button } from "../ui/button"


export const getBadgeAlerte = () => {
    return <Button variant={"ghost"} className="bg-red-500 hover:bg-red-600 text-white">
        <CircleAlert/>
    </Button>
}


export const getBadgeTache = () => {
    return <Button variant={"ghost"} className="bg-amber-500 hover:bg-amber-600 text-white">
       <Info/>
    </Button>
}


export const getBadgeInfo = () => {
    return <Button variant={"ghost"} className="bg-green-500 hover:bg-green-600 text-white">
       <Info/>
    </Button>
}