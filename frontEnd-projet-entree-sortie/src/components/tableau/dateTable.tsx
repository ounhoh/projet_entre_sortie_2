"use client"
import type { ColumnDef } from "@tanstack/react-table"
import {
  flexRender,
  getCoreRowModel,
  getPaginationRowModel,
  getFilteredRowModel,
  useReactTable,
} from "@tanstack/react-table"

import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import { DataTablePagination } from "./pagination"
import { useEffect, useState, useRef, useLayoutEffect } from "react"

interface DataTableProps<TData, TValue> {
  columns: ColumnDef<TData, TValue>[]
  data: TData[]
  searchs:string
  onRowClick?: (row: TData) => void
}

export function DataTable<TData, TValue>({
  columns,
  data, 
  searchs,
  onRowClick
}: DataTableProps<TData, TValue> ) {

  const [globalFilter, setGlobalFilter] = useState("")
  const [pageSize, setPageSize] = useState(10)
  const [pageIndex, setPageIndex] = useState(0)
  const tableContainerRef = useRef<HTMLDivElement>(null)
  const headerRef = useRef<HTMLTableSectionElement>(null)
  const lastCalculatedPageSize = useRef<number>(10)
  const isCalculatingRef = useRef<boolean>(false)
  const lastAvailableHeight = useRef<number>(0)
  const lastContainerWidth = useRef<number>(0)
  const isPaginationChanging = useRef<boolean>(false)
  
  useEffect(() => {
    setGlobalFilter(searchs)
  }, [searchs])

  // Calculer la taille de page en fonction de la hauteur disponible
  useLayoutEffect(() => {
    let retryCount = 0
    const maxRetries = 10
    
    const calculatePageSize = () => {
      // Éviter les calculs simultanés ou pendant les changements de pagination
      if (isCalculatingRef.current || isPaginationChanging.current) return
      
      // Utiliser requestAnimationFrame pour s'assurer que le calcul se fait après le rendu
      requestAnimationFrame(() => {
        if (!tableContainerRef.current || !headerRef.current) {
          // Si les refs ne sont pas prêtes, réessayer
          if (retryCount < maxRetries) {
            retryCount++
            setTimeout(calculatePageSize, 100)
          }
          return
        }

        isCalculatingRef.current = true

        const container = tableContainerRef.current
        const header = headerRef.current
        
        // Utiliser getBoundingClientRect pour obtenir la taille réelle du conteneur
        // Cela donne une mesure plus stable que clientHeight
        const containerRect = container.getBoundingClientRect()
        const containerWidth = containerRect.width
        
        // Utiliser la hauteur du conteneur depuis getBoundingClientRect
        // Cette hauteur est plus stable et ne change pas avec le contenu
        const containerHeight = containerRect.height
        
        // Calculer la hauteur disponible (sans les lignes)
        const headerHeight = header.offsetHeight
        const padding = 32 // padding top + bottom approximatif
        const availableHeight = containerHeight - headerHeight - padding
        
        // Vérifier si c'est vraiment un changement de taille (pas juste un changement de contenu)
        // Tolérance de 10px pour éviter les recalculs dus aux changements de pagination
        const availableHeightChanged = Math.abs(availableHeight - lastAvailableHeight.current) > 10
        const widthChanged = Math.abs(containerWidth - lastContainerWidth.current) > 5
        
        // Si le conteneur n'a pas encore de hauteur, attendre
        if (containerHeight === 0 || availableHeight <= 0) {
          isCalculatingRef.current = false
          if (retryCount < maxRetries) {
            retryCount++
            setTimeout(calculatePageSize, 100)
          } else {
            // Si après plusieurs tentatives, utiliser une valeur par défaut
            if (lastCalculatedPageSize.current !== 10) {
              lastCalculatedPageSize.current = 10
              setPageSize(10)
            }
            isCalculatingRef.current = false
          }
          return
        }
        
        // Ne recalculer que si la hauteur disponible a vraiment changé (pas juste le contenu)
        if (!availableHeightChanged && !widthChanged && lastAvailableHeight.current > 0) {
          // La taille n'a pas changé, ne pas recalculer
          isCalculatingRef.current = false
          return
        }
        
        // Mettre à jour les références de taille
        lastAvailableHeight.current = availableHeight
        lastContainerWidth.current = containerWidth
        
        // Hauteur approximative d'une ligne (ajustez selon votre design)
        const rowHeight = 53 // Hauteur approximative d'une TableRow avec padding
        
        // Calculer le nombre de lignes qui peuvent tenir (minimum de 5 lignes)
        const calculatedPageSize = Math.max(5, Math.floor(availableHeight / rowHeight))
        
        // Ne mettre à jour que si la valeur a vraiment changé (tolérance de ±1 pour éviter les oscillations)
        if (Math.abs(calculatedPageSize - lastCalculatedPageSize.current) > 1) {
          lastCalculatedPageSize.current = calculatedPageSize
          setPageSize(calculatedPageSize)
        }
        
        retryCount = 0 // Réinitialiser le compteur après succès
        isCalculatingRef.current = false
      })
    }

    // Attendre un peu pour que le DOM soit complètement rendu
    const timeoutId = setTimeout(() => {
      calculatePageSize()
    }, 200)

    // Ne surveiller QUE les changements de taille de fenêtre (pas le conteneur)
    // Cela évite les recalculs dus aux changements de contenu (pagination)
    let windowResizeTimeout: ReturnType<typeof setTimeout>
    const handleWindowResize = () => {
      clearTimeout(windowResizeTimeout)
      windowResizeTimeout = setTimeout(() => {
        retryCount = 0
        // Réinitialiser les références pour forcer le recalcul
        lastAvailableHeight.current = 0
        lastContainerWidth.current = 0
        calculatePageSize()
      }, 150)
    }

    window.addEventListener('resize', handleWindowResize)

    return () => {
      clearTimeout(timeoutId)
      clearTimeout(windowResizeTimeout)
      window.removeEventListener('resize', handleWindowResize)
    }
  }, [data.length]) // Recalculer si les données changent
  
  const table = useReactTable({
    data,
    columns,
    state: {
      globalFilter,
      pagination: {
        pageIndex: pageIndex,
        pageSize: pageSize,
      },
    },
    onGlobalFilterChange: setGlobalFilter,
    onPaginationChange: (updater) => {
      if (typeof updater === 'function') {
        const newPagination = updater({ pageIndex, pageSize })
        setPageIndex(newPagination.pageIndex)
        setPageSize(newPagination.pageSize)
      } else {
        setPageIndex(updater.pageIndex)
        setPageSize(updater.pageSize)
      }
    },
    getCoreRowModel: getCoreRowModel(),
    getFilteredRowModel: getFilteredRowModel(),
    getPaginationRowModel: getPaginationRowModel(),
    globalFilterFn: (row, _columnId, filterValue) => {
      // Fonction personnalisée pour chercher dans les objets imbriqués
      const searchValue = String(filterValue).toLowerCase().trim()
      
      if (!searchValue) return true
      
      // Parcourir toutes les valeurs de la ligne
      const rowData = row.original as any
      
      // Fonction récursive pour chercher dans les objets
      const searchInObject = (obj: any): boolean => {
        if (obj === null || obj === undefined) return false
        
        if (typeof obj === 'string') {
          return obj.toLowerCase().includes(searchValue)
        }
        
        if (typeof obj === 'object') {
          return Object.values(obj).some(value => {
            if (typeof value === 'string') {
              return value.toLowerCase().includes(searchValue)
            }
            if (typeof value === 'object' && value !== null) {
              return searchInObject(value)
            }
            return false
          })
        }
        
        return String(obj).toLowerCase().includes(searchValue)
      }
      
      // Chercher dans toutes les colonnes
      return Object.values(rowData).some(value => searchInObject(value))
    },
  })

  // Réinitialiser la page à 0 quand les données changent ou quand le filtre change
  useEffect(() => {
    setPageIndex(0)
  }, [globalFilter, data.length])

  // Désactiver temporairement le recalcul pendant les changements de pagination
  useEffect(() => {
    isPaginationChanging.current = true
    const timeout = setTimeout(() => {
      isPaginationChanging.current = false
    }, 300) // Attendre 300ms après le changement de page
    
    return () => clearTimeout(timeout)
  }, [pageIndex])
  
  return (
    <div className="flex flex-col h-full max-h-full min-h-0">

    <div className="shrink-0">
    <DataTablePagination table={table}/>
    </div>
  
       <div 
        ref={tableContainerRef}
        className="overflow-auto rounded-md border flex flex-1 min-h-0"
      >
      <Table>
        <TableHeader ref={headerRef} className="bg-cese-bleu-azure sticky top-0 z-10">
              {table.getHeaderGroups().map((headerGroup) => (
            <TableRow key={headerGroup.id}>
              {headerGroup.headers.map((header) => {
                const align = (header.column.columnDef.meta as any)?.align || 'left'
                return (
                  <TableHead key={header.id} className={align === 'center' ? 'text-center' : align === 'right' ? 'text-right' : 'text-left'}>
                    {header.isPlaceholder
                      ? null
                      : flexRender(
                          header.column.columnDef.header,
                          header.getContext()
                        )}
                  </TableHead>
                )
              })}
            </TableRow>
          ))}
        </TableHeader>
        <TableBody>
          {table.getRowModel().rows?.length ? (
            table.getRowModel().rows.map((row) => (
              <TableRow
                key={row.id}
                data-state={row.getIsSelected() && "selected"}
                onClick={() => {
                  if (onRowClick) {
                    onRowClick(row.original)
                  }
                }}
                className={onRowClick ? "cursor-pointer hover:bg-accent/50 transition-colors" : ""}
              >
                {row.getVisibleCells().map((cell) => {
                  const align = (cell.column.columnDef.meta as any)?.align || 'left'
                  return (
                    <TableCell key={cell.id} className={align === 'center' ? 'text-center' : align === 'right' ? 'text-right' : 'text-left'}>
                      {flexRender(cell.column.columnDef.cell, cell.getContext())}
                    </TableCell>
                  )
                })}
              </TableRow>
            ))
          ) : (
            <TableRow>
              <TableCell colSpan={columns.length} className="h-24 text-center">
                Aucun résultat...
              </TableCell>
            </TableRow>
          )}
        </TableBody>
      </Table>
    </div>
  </div>

)
}