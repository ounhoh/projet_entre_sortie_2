"use client"

import * as React from "react"
import { format, addWeeks, addMonths, addYears } from "date-fns"
import { fr as dateFnsFr } from "date-fns/locale"
import { fr as dayPickerFr } from "react-day-picker/locale"
import { Calendar as CalendarIcon, X } from "lucide-react"

import { cn } from "@/lib/utils"
import { Calendar } from "@/components/ui/calendar"
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover"
import { InputGroup, InputGroupInput, InputGroupAddon, InputGroupButton } from "@/components/ui/input-group"

// Fonction pour parser les formats spéciaux de date
function parseDateValue(value: string): Date | undefined {
  if (!value) return undefined
  
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  
  // Format "now" -> date actuelle
  if (value.toLowerCase() === "now") {
    return today
  }
  
  // Format "nombre-year", "nombre-week", "nombre-month"
  // ou "-year", "-week", "-month" (nombre par défaut = 1)
  // ou "year", "week", "month" (nombre par défaut = 1, sans tiret)
  const relativeMatchWithDash = value.match(/^(-?\d+)?-(year|week|month)$/i)
  const relativeMatchWithoutDash = value.match(/^(year|week|month)$/i)
  
  if (relativeMatchWithDash) {
    const numberStr = relativeMatchWithDash[1]
    const unit = relativeMatchWithDash[2].toLowerCase()
    const number = numberStr ? parseInt(numberStr, 10) : 1
    
    switch (unit) {
      case "week":
        return addWeeks(today, number)
      case "month":
        return addMonths(today, number)
      case "year":
        return addYears(today, number)
      default:
        return today
    }
  }
  
  if (relativeMatchWithoutDash) {
    const unit = relativeMatchWithoutDash[1].toLowerCase()
    const number = 1 // Par défaut, nombre = 1 si pas de tiret
    
    switch (unit) {
      case "week":
        return addWeeks(today, number)
      case "month":
        return addMonths(today, number)
      case "year":
        return addYears(today, number)
      default:
        return today
    }
  }
  
  // Format YYYY-MM-DD (date normale)
  if (value.match(/^\d{4}-\d{2}-\d{2}$/)) {
    const [year, month, day] = value.split('-').map(Number)
    return new Date(year, month - 1, day)
  }
  
  // Essayer de parser comme date normale
  const parsedDate = new Date(value)
  if (!isNaN(parsedDate.getTime())) {
    return parsedDate
  }
  
  return undefined
}

interface DatePickerProps {
  value?: string
  onChange?: (value: string) => void
  placeholder?: string
  required?: boolean
  min?: string
  max?: string
  disabled?: boolean
  className?: string
}

export function DatePicker({
  value,
  onChange,
  placeholder = "Sélectionner une date",
  required = false,
  min,
  max,
  disabled = false,
  className,
}: DatePickerProps) {
  const [open, setOpen] = React.useState(false)
  
  // Log pour vérifier les props reçues
  React.useEffect(() => {
    console.log('[DatePicker] Props reçues:', { min, max, value })
  }, [min, max, value])
  
  // Convertir la valeur string en Date
  // Support des formats spéciaux : "now", "nombre-year/week/month", et YYYY-MM-DD
  const date = React.useMemo(() => {
    if (!value) return undefined
    return parseDateValue(value)
  }, [value])
  
  // Convertir min en Date si fourni (peut aussi être un format relatif)
  const minDate = React.useMemo(() => {
    if (!min) return undefined
    const parsed = parseDateValue(min)
    if (parsed) {
      parsed.setHours(0, 0, 0, 0)
    }
    console.log('[DatePicker] minDate parsée:', { min, parsed: parsed?.toISOString() })
    return parsed
  }, [min])

  // Convertir max en Date si fourni (peut aussi être un format relatif)
  const maxDate = React.useMemo(() => {
    if (!max) {
      console.log('[DatePicker] maxDate: undefined (max prop est vide)')
      return undefined
    }
    console.log('[DatePicker] Tentative de parsing maxDate:', { max, type: typeof max })
    const parsed = parseDateValue(max)
    if (parsed) {
      parsed.setHours(23, 59, 59, 999) // Fin de journée pour maxDate
      console.log('[DatePicker] maxDate parsée avec succès:', { 
        max, 
        parsed: parsed.toISOString(),
        parsedDate: parsed.toLocaleDateString('fr-FR')
      })
    } else {
      console.log('[DatePicker] maxDate: échec du parsing', { 
        max, 
        type: typeof max,
        value: String(max)
      })
    }
    return parsed
  }, [max])

  const handleSelect = React.useCallback((selectedDate: Date | undefined) => {
    if (!onChange) return;
    
    // Si selectedDate est undefined, c'est que l'utilisateur a cliqué sur la date déjà sélectionnée
    // pour la désélectionner
    if (!selectedDate) {
      onChange("")
      setOpen(false)
      return
    }
    
    // Normaliser la date pour éviter les problèmes d'heure
    const normalizedDate = new Date(selectedDate)
    normalizedDate.setHours(0, 0, 0, 0)
    
    // Vérifier si c'est la même date que celle déjà sélectionnée
    if (date && normalizedDate.getTime() === date.getTime()) {
      // Si c'est la même date, la désélectionner
      onChange("")
      setOpen(false)
      return
    }
    
    // Quand l'utilisateur sélectionne une date via le calendrier,
    // on retourne toujours le format YYYY-MM-DD (date absolue)
    const formattedDate = format(normalizedDate, "yyyy-MM-dd")
    
    // Appeler onChange directement
    onChange(formattedDate)
    
    // Fermer le popover immédiatement après la sélection
    setOpen(false)
  }, [onChange, date])
  
  const handleClear = React.useCallback((e: React.MouseEvent) => {
    e.preventDefault()
    e.stopPropagation()
    if (onChange && !disabled) {
      onChange("")
    }
  }, [onChange, disabled])

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <div className={className}>
        <InputGroup>
          <InputGroupAddon align="inline-start">
            <CalendarIcon className="w-4 h-4" />
          </InputGroupAddon>
          <PopoverTrigger asChild>
            <InputGroupInput
              type="text"
              placeholder={placeholder}
              required={required}
              value={date ? format(date, "dd/MM/yyyy", { locale: dateFnsFr }) : ""}
              readOnly
              disabled={disabled}
              className={cn(
                "cursor-pointer",
                !date && "text-muted-foreground"
              )}
            />
          </PopoverTrigger>
          {date && !required && (
            <InputGroupButton
              type="button"
              variant="ghost"
              size="xs"
              disabled={disabled}
              className="cursor-pointer hover:bg-destructive/10"
              onClick={handleClear}
              title="Effacer la date"
            >
              <X className="w-4 h-4" />
            </InputGroupButton>
          )}
          <InputGroupButton
            type="button"
            variant="ghost"
            size="xs"
            disabled={disabled}
            className="cursor-pointer"
            onClick={(e) => {
              e.preventDefault()
              e.stopPropagation()
              if (!disabled) {
                setOpen((prev) => !prev)
              }
            }}
          >
            <CalendarIcon className="w-4 h-4" />
          </InputGroupButton>
        </InputGroup>
      </div>
      <PopoverContent 
        className="w-auto p-0" 
        align="start"
      >
        {(() => {
          const disabledConfig: any = 
            minDate || maxDate
              ? {
                  ...(minDate && { before: minDate }),
                  ...(maxDate && { after: maxDate }),
                }
              : undefined
          
          console.log('[DatePicker] Calendar disabled config:', {
            minDate: minDate?.toISOString(),
            maxDate: maxDate?.toISOString(),
            disabledConfig,
            hasBefore: !!disabledConfig?.before,
            hasAfter: !!disabledConfig?.after
          })
          
          return (
            <Calendar
              mode="single"
              selected={date}
              onSelect={handleSelect}
              locale={dayPickerFr}
              disabled={disabledConfig}
              initialFocus
            />
          )
        })()}
      </PopoverContent>
    </Popover>
  )
}
