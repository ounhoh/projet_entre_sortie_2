"use client"
import type { ColumnDef } from "@tanstack/react-table"
import { Button } from "@/components/ui/button"
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
import { ArrowLeft, ArrowRight } from "lucide-react"
import { Pagination, PaginationContent, PaginationItem, PaginationPrevious } from "@/components/ui/pagination"
import { DataTablePagination } from "./pagination"
import { useEffect, useState } from "react"
import { useNavigate } from "react-router-dom"

interface DataTableProps<TData, TValue> {
  columns: ColumnDef<TData, TValue>[]
  data: TData[]
  searchs:string
}

export function DataTableProcess<TData, TValue>({
  columns,
  data, 
  searchs,
  onRowClick
}: DataTableProps<TData, TValue> & { onRowClick?: (row: TData) => void } ) {

  const navigate = useNavigate()
  const [globalFilter, setGlobalFilter] = useState("")
  useEffect(() => {
    setGlobalFilter(searchs)
  }, [searchs])
  
  const table = useReactTable({
    data,
    columns,
    state: {
      globalFilter,
    },
    onGlobalFilterChange: setGlobalFilter,
    getCoreRowModel: getCoreRowModel(),
    getFilteredRowModel: getFilteredRowModel(),
    getPaginationRowModel: getPaginationRowModel(),
    globalFilterFn: (row, columnId, filterValue) => {
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

  
  return (
    <div className="flex flex-col h-full w-full">
      {/* Pagination au-dessus du tableau, alignée à droite */}
      <div className="shrink-0 flex justify-end mb-2">
        <DataTablePagination table={table}/>
      </div>

      {/* Tableau avec hauteur fixe et scroll */}
      <div className="overflow-auto rounded-md border flex-1 min-h-0">
        <Table>
          <TableHeader className="bg-cese-bleu-azure">
            {table.getHeaderGroups().map((headerGroup) => (
              <TableRow key={headerGroup.id}>
                {headerGroup.headers.map((header) => {
                  return (
                    <TableHead key={header.id}>
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
                  {row.getVisibleCells().map((cell) => (
                    <TableCell key={cell.id}>
                      {flexRender(cell.column.columnDef.cell, cell.getContext())}
                    </TableCell>
                  ))}
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