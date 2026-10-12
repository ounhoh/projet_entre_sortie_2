import { ArrowLeftRight, UserMinus, UserPlus, Users } from "lucide-react"
import { Button } from "../ui/button"
import type { JSX } from "react"


export const getIconEntree= (onClick?: () => void) : JSX.Element => {
    return (<Button className="bg-green-500" size={"icon"} onClick={onClick}>
        <UserPlus/>
    </Button>)
}

export const getIconSortie = (onClick?: () => void) : JSX.Element => {
    return (<Button className="bg-red-500" size={"icon"} onClick={onClick}>
        <UserMinus/>
    </Button>)
}

export const getIconMobiliteInterne = (onClick?: () => void): JSX.Element => {
    return (
        <Button
            className="bg-amber-500"
            size="icon"
            onClick={onClick}
        >
            <ArrowLeftRight />
        </Button>
    );
};

export const getIconAllProcess = (onClick?: () => void) : JSX.Element  =>{
    return (
    <Button className="bg-blue-500" size={"icon"} onClick={onClick}>
        <Users/>
    </Button>)
}
