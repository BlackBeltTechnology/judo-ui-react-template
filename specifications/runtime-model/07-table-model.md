# TableModel Specification

**Domain:** Runtime Model  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `01-core-types.md`, `04-visual-element-model.md`  
**Blocks:** Table component, data grid implementation  

## Overview

`TableModel` represents data tables with columns, filtering, sorting, pagination, and row actions. Tables display collections of entities with interactive features.

## TypeScript Interfaces

### TableModel

```typescript
interface TableModel extends VisualElementModel {
  type: 'table';
  
  // Data
  dataElement: string;                  // ClassType or RelationType FQN
  
  // Columns
  columns: ColumnModel[];
  
  // Actions
  rowActions: ActionModel[];
  bulkActions?: ActionModel[];
  
  // Features
  enableFiltering: boolean;
  enableSorting: boolean;
  enablePagination: boolean;
  enableSelection: boolean;
  
  // Pagination
  defaultPageSize: number;
  pageSizeOptions: number[];
  
  // Filters
  filters: FilterModel[];
  
  // Display
  dense?: boolean;
  striped?: boolean;
  bordered?: boolean;
}
```

### ColumnModel

```typescript
interface ColumnModel extends BaseModel {
  // Identification
  id: string;
  name: string;
  
  // Display
  label: string;
  width?: number;                       // Fixed width in pixels
  align: ColumnAlignment;               // 'left' | 'center' | 'right'
  
  // Data
  attributeName: string;
  attributeType: DataType;
  attributeFqn: string;
  
  // Features
  sortable: boolean;
  filterable: boolean;
  
  // Formatting
  format?: string;                      // Format string for dates/numbers
  customRenderer?: string;              // Custom renderer component name
  
  // Metadata
  sourceId?: string;
}
```

### FilterModel

```typescript
interface FilterModel extends BaseModel {
  // Identification
  id: string;
  name: string;
  
  // Display
  label: string;
  
  // Data
  attributeName: string;
  attributeType: DataType;
  
  // UI
  filterType: FilterType;               // 'text' | 'select' | 'dateRange' | 'numericRange' | 'boolean'
  
  // Options (for select filter)
  options?: Array<{
    value: any;
    label: string;
  }>;
  
  // Default
  defaultValue?: any;
  
  // Metadata
  sourceId?: string;
}
```

## Examples

### Example 1: Basic User Table

```typescript
const userTable: TableModel = {
  id: 'table-users',
  name: 'userTable',
  type: 'table',
  
  dataElement: 'User',
  col: 12,
  
  columns: [
    {
      id: 'col-firstName',
      name: 'firstName',
      label: 'First Name',
      attributeName: 'firstName',
      attributeType: 'string',
      attributeFqn: 'User.firstName',
      align: 'left',
      sortable: true,
      filterable: true,
      width: 150
    },
    {
      id: 'col-lastName',
      name: 'lastName',
      label: 'Last Name',
      attributeName: 'lastName',
      attributeType: 'string',
      attributeFqn: 'User.lastName',
      align: 'left',
      sortable: true,
      filterable: true,
      width: 150
    },
    {
      id: 'col-email',
      name: 'email',
      label: 'Email',
      attributeName: 'email',
      attributeType: 'string',
      attributeFqn: 'User.email',
      align: 'left',
      sortable: true,
      filterable: true
    },
    {
      id: 'col-active',
      name: 'active',
      label: 'Active',
      attributeName: 'active',
      attributeType: 'boolean',
      attributeFqn: 'User.active',
      align: 'center',
      sortable: true,
      filterable: true,
      width: 100
    }
  ],
  
  rowActions: [
    {
      id: 'action-view',
      name: 'view',
      type: 'openPage',
      targetPageName: 'UserView'
    },
    {
      id: 'action-edit',
      name: 'edit',
      type: 'openForm',
      targetPageName: 'EditUserForm'
    },
    {
      id: 'action-delete',
      name: 'delete',
      type: 'delete',
      targetType: 'User'
    }
  ],
  
  enableFiltering: true,
  enableSorting: true,
  enablePagination: true,
  enableSelection: false,
  
  defaultPageSize: 10,
  pageSizeOptions: [10, 25, 50, 100],
  
  filters: [
    {
      id: 'filter-active',
      name: 'activeFilter',
      label: 'Status',
      attributeName: 'active',
      attributeType: 'boolean',
      filterType: 'boolean',
      defaultValue: true
    }
  ]
};
```

### Example 2: Table with Numeric Columns

```typescript
const salesTable: TableModel = {
  id: 'table-sales',
  name: 'salesTable',
  type: 'table',
  dataElement: 'Sale',
  col: 12,
  
  columns: [
    {
      id: 'col-date',
      name: 'date',
      label: 'Date',
      attributeName: 'date',
      attributeType: 'date',
      attributeFqn: 'Sale.date',
      align: 'left',
      sortable: true,
      format: 'yyyy-MM-dd'
    },
    {
      id: 'col-product',
      name: 'product',
      label: 'Product',
      attributeName: 'product',
      attributeType: 'string',
      attributeFqn: 'Sale.product',
      align: 'left',
      sortable: true
    },
    {
      id: 'col-quantity',
      name: 'quantity',
      label: 'Qty',
      attributeName: 'quantity',
      attributeType: 'integer',
      attributeFqn: 'Sale.quantity',
      align: 'right',
      sortable: true,
      width: 80
    },
    {
      id: 'col-price',
      name: 'price',
      label: 'Price',
      attributeName: 'price',
      attributeType: 'decimal',
      attributeFqn: 'Sale.price',
      align: 'right',
      sortable: true,
      format: '0.00',
      width: 100
    },
    {
      id: 'col-total',
      name: 'total',
      label: 'Total',
      attributeName: 'total',
      attributeType: 'decimal',
      attributeFqn: 'Sale.total',
      align: 'right',
      sortable: true,
      format: '0.00',
      width: 120
    }
  ],
  
  enableFiltering: true,
  enableSorting: true,
  enablePagination: true,
  enableSelection: false,
  
  defaultPageSize: 50,
  pageSizeOptions: [25, 50, 100, 200],
  
  filters: [
    {
      id: 'filter-date',
      name: 'dateFilter',
      label: 'Date Range',
      attributeName: 'date',
      attributeType: 'date',
      filterType: 'dateRange'
    }
  ]
};
```

### Example 3: Selectable Table

```typescript
const selectableTable: TableModel = {
  id: 'table-selectable',
  name: 'selectableTable',
  type: 'table',
  dataElement: 'User',
  col: 12,
  
  columns: [
    { name: 'firstName', label: 'First Name', attributeName: 'firstName', attributeType: 'string', align: 'left', sortable: true },
    { name: 'lastName', label: 'Last Name', attributeName: 'lastName', attributeType: 'string', align: 'left', sortable: true },
    { name: 'email', label: 'Email', attributeName: 'email', attributeType: 'string', align: 'left', sortable: true }
  ],
  
  rowActions: [],
  
  bulkActions: [
    {
      id: 'action-bulk-delete',
      name: 'bulkDelete',
      type: 'delete',
      targetType: 'User',
      isBulk: true
    },
    {
      id: 'action-bulk-export',
      name: 'bulkExport',
      type: 'export',
      isBulk: true
    }
  ],
  
  enableFiltering: true,
  enableSorting: true,
  enablePagination: true,
  enableSelection: true,                // Enable multi-select
  
  defaultPageSize: 25,
  pageSizeOptions: [10, 25, 50, 100],
  
  filters: []
};
```

### Example 4: Relation Table

```typescript
const postsTable: TableModel = {
  id: 'table-posts',
  name: 'postsTable',
  type: 'table',
  dataElement: 'User#posts',            // Relation
  col: 12,
  
  columns: [
    {
      id: 'col-title',
      name: 'title',
      label: 'Title',
      attributeName: 'title',
      attributeType: 'string',
      attributeFqn: 'Post.title',
      align: 'left',
      sortable: true
    },
    {
      id: 'col-createdAt',
      name: 'createdAt',
      label: 'Created',
      attributeName: 'createdAt',
      attributeType: 'dateTime',
      attributeFqn: 'Post.createdAt',
      align: 'left',
      sortable: true,
      format: 'yyyy-MM-dd HH:mm'
    }
  ],
  
  rowActions: [
    {
      id: 'action-view',
      name: 'view',
      type: 'openPage',
      targetPageName: 'PostView'
    },
    {
      id: 'action-remove',
      name: 'remove',
      type: 'remove',
      relationName: 'posts'
    }
  ],
  
  enableFiltering: false,
  enableSorting: true,
  enablePagination: true,
  enableSelection: false,
  
  defaultPageSize: 10,
  pageSizeOptions: [10, 25, 50],
  
  filters: []
};
```

## Table Rendering

```typescript
function DataTable({ model, service }: Props) {
  const [data, setData] = useState<any[]>([]);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(model.defaultPageSize);
  const [sortBy, setSortBy] = useState<SortConfig | null>(null);
  const [filters, setFilters] = useState<FilterConfig>({});
  const [selected, setSelected] = useState<string[]>([]);
  
  const fetchData = async () => {
    const query: QueryCustomizer = {
      _seek: { page, limit: pageSize },
      _orderBy: sortBy ? [sortBy] : undefined,
      ...filters
    };
    const result = await service.list(query);
    setData(result.content);
  };
  
  useEffect(() => {
    fetchData();
  }, [page, pageSize, sortBy, filters]);
  
  return (
    <MUIDataTable
      data={data}
      columns={model.columns}
      options={{
        page,
        rowsPerPage: pageSize,
        rowsPerPageOptions: model.pageSizeOptions,
        sortOrder: sortBy,
        onChangePage: setPage,
        onChangeRowsPerPage: setPageSize,
        onColumnSortChange: setSortBy,
        filter: model.enableFiltering,
        sort: model.enableSorting,
        pagination: model.enablePagination,
        selectableRows: model.enableSelection ? 'multiple' : 'none',
        onRowSelectionChange: setSelected
      }}
    />
  );
}
```

## Validation Rules

- Table must have at least one column
- Column names must be unique
- Column attributeNames must exist in dataElement
- Filter attributeNames must exist in dataElement
- DefaultPageSize must be in pageSizeOptions
- Row actions should be appropriate for dataElement

## Related Specifications

**Metamodel:**
- `metamodel/13-table.md` - Source specification

**Runtime Model:**
- `01-core-types.md` - Base types
- `04-visual-element-model.md` - Base class

**Visual Elements:**
- `visual-elements/tables/01-table-element.md` - Detailed implementation

---

**Status:** ✅ Complete  
**Created:** 2025-11-28  
**Last Updated:** 2025-11-28  
**Reviewed:** Not yet reviewed

