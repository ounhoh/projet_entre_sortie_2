

import { Separator } from "@/components/ui/separator"
import { SidebarTrigger } from "@/components/ui/sidebar"
import { NotificationButton } from "./Notification"
import { AnimatedThemeToggler } from "../ui/animated-theme-toggler";


const DarkMode = () =>
{
  return (<AnimatedThemeToggler/>);
}
export function SiteHeader({titrePage, description}: {titrePage: string, description: string}) {
  return (
    <header className="flex h-16 shrink-0 items-center gap-2 border-b ">
      <div className="flex w-full items-center gap-1 px-4 lg:gap-2 lg:px-6">
        <SidebarTrigger className="-ml-1" />
        <Separator
          orientation="vertical"
          className="mx-2 h-4"
        />
        <div className="">
        <h1 className="font-medium">{ titrePage } </h1>
        <h2 className="hidden lg:block text-sm text-muted-foreground">{ description}</h2>
        </div>
        <div className="ml-auto flex items-center gap-2">
          <DarkMode />
          <NotificationButton />
        </div>
      </div>
    </header>
  )
}
