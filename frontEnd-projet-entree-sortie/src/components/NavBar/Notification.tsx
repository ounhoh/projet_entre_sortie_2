
import { Button } from "@/components/ui/button"
import { BellRing } from "lucide-react";
import { AnimatedShinyText } from "../ui/animated-shiny-text";
import { Sheet, SheetContent, SheetDescription, SheetHeader, SheetTitle, SheetTrigger } from "../ui/sheet";
import { getCurrentAgentDisplay } from "@/utils/currentAgent";
import { Separator } from "@/components/ui/separator"
import TableauNotification from "../tableau/tableau-Notification/TableauNotification";

export const NotificationButton = () => {
    const user = getCurrentAgentDisplay();
    return (
        <Sheet>
            <SheetTrigger asChild>

        <Button className="rounded-full" variant={"outline"}>
        <BellRing/>
            notification
        </Button>
            </SheetTrigger>
            <SheetContent className="w-[600px] max-w-[90vw] overflow-hidden flex flex-col">
                <SheetHeader className="shrink-0">
                    <SheetTitle>
                        {user.nom}
                    </SheetTitle>
                    <SheetDescription>
                        {user.direction}
                    </SheetDescription>
                </SheetHeader>
                <Separator
                    orientation="horizontal"
                    className="h-4 shrink-0"
                />
                <div className="flex-1 min-h-0 overflow-hidden">
                    <TableauNotification/>
                </div>
            </SheetContent>
        </Sheet>
    );
}
