import { DropdownMenu, DropdownMenuArrow, DropdownMenuContent, DropdownMenuGroup, DropdownMenuItem, DropdownMenuLabel, DropdownMenuSeparator, DropdownMenuTrigger } from "@radix-ui/react-dropdown-menu"
import { Sidebar, SidebarMenu, SidebarMenuButton, SidebarMenuItem, useSidebar } from "../ui/sidebar"
import { Avatar } from "@radix-ui/react-avatar"
import { EllipsisVertical, LogOut, User } from "lucide-react"
import { Link } from "react-router-dom"


export function NavUser( {
    user,
}: {
    user: {
        nom:string,
        direction:string
        avatar:string
    }
}) {
    return (<SidebarMenu>
        <SidebarMenuItem>
            <DropdownMenu>
                <DropdownMenuTrigger asChild>
                    <SidebarMenuButton size="lg">
                        <User/>
                        <div className="grid flex-1 text-left text-sm leading-tight">
                            <span className="truncate font-bold">{user.nom}</span>
                            <span className="text-muted-foreground truncate text-xs text-white font-bold">
                            {user.direction}
                            </span>
                        </div>
                        <EllipsisVertical className="ml-auto size-4"/>
                    </SidebarMenuButton>
                </DropdownMenuTrigger>
                <DropdownMenuContent
                  side="right"
                  className="w-[--radix-popper-anchor-width] bg-cese-bleu-secondaire w-fit rounded-lg h-fit"
                >
                  <DropdownMenuItem className="px-3 py-2.5">
                                <Link to="/">
                               <span>
                                Déconnexion
                                </span>
                                </Link>
                  </DropdownMenuItem>
                </DropdownMenuContent>

            </DropdownMenu>
        </SidebarMenuItem>
    </SidebarMenu>)
}