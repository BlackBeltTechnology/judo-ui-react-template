# Table Element Specification

**Domain:** Visual Elements / Tables  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `runtime-model/07-table-model.md`  
**Blocks:** Data table component  

## Overview

Table element displays collections with sorting, filtering, pagination, and row actions.

## Component Interface

```typescript
interface DataTableProps {
  model: TableModel;
  service: any;
  onRowAction?: (action: string, row: any) => void;
}

function DataTable({ model, service, onRowAction }: DataTableProps) {
  const [data, setData] = useState<any[]>([]);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(model.defaultPageSize);
  const [sortBy, setSortBy] = useState<SortConfig | null>(null);
  const [filters, setFilters] = useState<Record<string, any>>({});
  const [selected, setSelected] = useState<string[]>([]);
  
  useEffect(() => {
    fetchData();
  }, [page, pageSize, sortBy, filters]);
  
  const fetchData = async () => {
    const query: QueryCustomizer = {
      _seek: { page, limit: pageSize },
      _orderBy: sortBy ? [sortBy] : undefined,
      ...filters
    };
    const result = await service.list(query);
    setData(result.content);
  };
  
  return (
    <MUIDataTable
      data={data}
      columns={model.columns.map(col => ({
        name: col.attributeName,
        label: col.label,
        options: {
          sort: col.sortable,
          filter: col.filterable
        }
      }))}
      options={{
        page,
        rowsPerPage: pageSize,
        onChangePage: setPage,
        onChangeRowsPerPage: setPageSize,
        onColumnSortChange: (changedColumn, direction) => {
          setSortBy({ column: changedColumn, direction });
        },
        selectableRows: model.enableSelection ? 'multiple' : 'none',
        onRowSelectionChange: (current, all) => {
          setSelected(all.map(i => data[i.dataIndex].id));
        }
      }}
    />
  );
}
```

---

**Status:** ✅ Complete  
**Created:** 2025-11-28

