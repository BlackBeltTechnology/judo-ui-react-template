# Table Specification

**Domain:** Metamodel  
**Ecore Class:** `ui::Table`  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `02-visual-element.md`, `02-relation-type.md`  
**Blocks:** Table runtime models, table component specifications  

## Overview

`Table` is a visual element that displays collections of entities in a tabular format with columns, filtering, sorting, pagination, and row actions. Tables are the primary mechanism for displaying lists of data and enabling batch operations.

## Metamodel Definition

### Class Hierarchy

```
NamedElement (abstract)
└─ VisualElement (abstract)
   └─ Table
```

### Properties

| Name | Type | Multiplicity | Default | Description |
|------|------|--------------|---------|-------------|
| `name` | EString | 1 | - | Table identifier |
| `sourceId` | EString | 0..1 | - | XMIID from source |
| `dataElement` | DataElement | 1 | - | ClassType or RelationType for table data |
| `columns` | Column | 1..* | - | Table columns |
| `rowActions` | Action | 0..* | - | Actions available per row |
| `filters` | Filter | 0..* | - | Available filters |
| `enableFiltering` | EBoolean | 0..1 | true | Enable filter UI |
| `enableSorting` | EBoolean | 0..1 | true | Enable column sorting |
| `enablePagination` | EBoolean | 0..1 | true | Enable pagination |
| `enableSelection` | EBoolean | 0..1 | false | Enable row selection |
| `defaultPageSize` | EInt | 0..1 | 10 | Default rows per page |
| `pageSizeOptions` | EInt | 0..* | [10,25,50,100] | Page size choices |

### Column Properties

| Name | Type | Multiplicity | Default | Description |
|------|------|--------------|---------|-------------|
| `name` | EString | 1 | - | Column identifier |
| `label` | EString | 0..1 | - | Column header text |
| `attributeType` | AttributeType | 1 | - | Attribute to display |
| `sortable` | EBoolean | 0..1 | true | Can sort by this column |
| `filterable` | EBoolean | 0..1 | true | Can filter by this column |
| `width` | EInt | 0..1 | - | Fixed column width (px) |
| `align` | Alignment | 0..1 | LEFT | Column alignment |

### Filter Properties

| Name | Type | Multiplicity | Default | Description |
|------|------|--------------|---------|-------------|
| `name` | EString | 1 | - | Filter identifier |
| `label` | EString | 0..1 | - | Filter label |
| `attributeType` | AttributeType | 1 | - | Attribute to filter |
| `filterType` | FilterType | 1 | - | Filter UI type |
| `defaultValue` | EString | 0..1 | - | Default filter value |

### Enumerations

#### Alignment
```
LEFT   = 0  // Left align (default)
CENTER = 1  // Center align
RIGHT  = 2  // Right align (typically for numbers)
```

#### FilterType
```
TEXT       = 0  // Text input filter
SELECT     = 1  // Dropdown select
DATE_RANGE = 2  // Date range picker
NUMERIC_RANGE = 3  // Numeric range
BOOLEAN    = 4  // Checkbox/toggle
```

### Relationships

**Contains:**
- `columns: Column[]` - Table columns
- `filters: Filter[]` - Available filters

**Referenced By:**
- Container children
- PageContainer (in TABLE pages)

**References:**
- `dataElement: ClassType | RelationType` - Data source
- `columns[].attributeType: AttributeType` - Column data
- `filters[].attributeType: AttributeType` - Filter targets
- `rowActions: Action[]` - Per-row actions

## Key Concepts

### Data Binding

Tables bind to ClassType or RelationType:

**ClassType (Entity List):**
```xml
<children xsi:type="ui:Table" 
          name="userTable" 
          dataElement="User">
```
Lists all User entities.

**RelationType (Related Entities):**
```xml
<children xsi:type="ui:Table" 
          name="postsTable" 
          dataElement="User#posts">
```
Lists posts related to current user.

### Columns

Each column displays an attribute:
```xml
<columns name="firstName" 
         label="First Name"
         attributeType="User#firstName"
         sortable="true"
         filterable="true"
         width="150"
         align="LEFT"/>
```

Column rendering determined by attribute type:
- String → Text cell
- Number → Right-aligned number
- Date → Formatted date
- Boolean → Checkbox/icon
- Enum → Enum label

### Sorting

**Single Column Sort:**
```typescript
// User clicks column header
sortBy = { column: 'firstName', direction: 'asc' }
```

**Multi-Column Sort:**
```typescript
sortBy = [
  { column: 'lastName', direction: 'asc' },
  { column: 'firstName', direction: 'asc' }
]
```

### Filtering

Filters narrow down results:
```xml
<filters name="activeFilter" 
         label="Active Only"
         attributeType="User#active"
         filterType="BOOLEAN"
         defaultValue="true"/>

<filters name="nameFilter" 
         label="Name Contains"
         attributeType="User#firstName"
         filterType="TEXT"/>
```

### Pagination

Controls page size and navigation:
```xml
<table enablePagination="true"
       defaultPageSize="25"
       pageSizeOptions="10,25,50,100">
```

### Row Actions

Actions available for each row:
```xml
<rowActions action="ViewUserAction"/>
<rowActions action="EditUserAction"/>
<rowActions action="DeleteUserAction"/>
```

Rendered as:
- Icon buttons in actions column
- Context menu on row right-click
- Overflow menu

### Row Selection

Enable multi-select for batch operations:
```xml
<table enableSelection="true">
```

Enables:
- Checkbox column
- Select all
- Bulk actions on selected rows

## Examples from Sample Model

### Example 1: Simple User Table

```xml
<children xsi:type="ui:Table" 
          name="userTable" 
          dataElement="User"
          col="12">
  
  <columns name="firstName" 
           attributeType="User#firstName"
           sortable="true"/>
  <columns name="lastName" 
           attributeType="User#lastName"
           sortable="true"/>
  <columns name="email" 
           attributeType="User#email"
           sortable="true"
           filterable="true"/>
  <columns name="active" 
           attributeType="User#active"
           align="CENTER"/>
  
  <rowActions action="ViewUserAction"/>
  <rowActions action="EditUserAction"/>
  <rowActions action="DeleteUserAction"/>
</children>
```

### Example 2: Relation Table with Filters

```xml
<children xsi:type="ui:Table" 
          name="ordersTable" 
          dataElement="Customer#orders"
          enableFiltering="true"
          defaultPageSize="25"
          col="12">
  
  <columns name="orderNumber" 
           attributeType="Order#orderNumber"
           width="120"/>
  <columns name="orderDate" 
           attributeType="Order#orderDate"
           sortable="true"/>
  <columns name="status" 
           attributeType="Order#status"
           filterable="true"/>
  <columns name="total" 
           attributeType="Order#total"
           align="RIGHT"/>
  
  <filters name="statusFilter" 
           label="Status"
           attributeType="Order#status"
           filterType="SELECT"/>
  <filters name="dateFilter" 
           label="Order Date"
           attributeType="Order#orderDate"
           filterType="DATE_RANGE"/>
  
  <rowActions action="ViewOrderAction"/>
</children>
```

### Example 3: Selectable Table

```xml
<children xsi:type="ui:Table" 
          name="selectableUsers" 
          dataElement="User"
          enableSelection="true"
          col="12">
  
  <columns name="name" attributeType="User#name"/>
  <columns name="email" attributeType="User#email"/>
  
  <!-- Bulk actions applied to selected rows -->
</children>
```

### Example 4: Compact Table (No Pagination)

```xml
<children xsi:type="ui:Table" 
          name="recentActivity" 
          dataElement="Activity"
          enablePagination="false"
          enableSorting="false"
          enableFiltering="false"
          col="12">
  
  <columns name="timestamp" attributeType="Activity#timestamp"/>
  <columns name="action" attributeType="Activity#action"/>
  <columns name="user" attributeType="Activity#user"/>
</children>
```

### Example 5: Numeric Data Table

```xml
<children xsi:type="ui:Table" 
          name="salesData" 
          dataElement="Sale"
          defaultPageSize="50"
          col="12">
  
  <columns name="date" attributeType="Sale#date" width="120"/>
  <columns name="product" attributeType="Sale#product" width="200"/>
  <columns name="quantity" 
           attributeType="Sale#quantity"
           align="RIGHT"
           width="100"/>
  <columns name="price" 
           attributeType="Sale#price"
           align="RIGHT"
           width="120"/>
  <columns name="total" 
           attributeType="Sale#total"
           align="RIGHT"
           width="120"/>
</children>
```

## Validation Rules

### Required Properties
- `name` - Must be unique
- `dataElement` - Must reference valid ClassType/RelationType
- `columns` - At least 1 column required

### Constraints
- Column attributeTypes must exist in dataElement
- Filter attributeTypes must exist in dataElement
- Row actions must be compatible with data type
- DefaultPageSize must be in pageSizeOptions
- Column widths should sum appropriately

### Best Practices
- 3-8 columns optimal for readability
- Right-align numeric columns
- Enable sorting for most columns
- Provide meaningful filters
- Keep column labels concise

## Runtime Model Mapping

```typescript
interface TableModel extends VisualElementModel {
  type: 'table';
  dataElement: string;             // FQN of ClassType/RelationType
  
  // Columns
  columns: ColumnModel[];
  
  // Actions
  rowActions: ActionModel[];
  
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
}

interface ColumnModel {
  name: string;
  label: string;
  attributeName: string;
  attributeType: string;            // Data type
  sortable: boolean;
  filterable: boolean;
  width?: number;
  align: 'left' | 'center' | 'right';
}

interface FilterModel {
  name: string;
  label: string;
  attributeName: string;
  filterType: 'text' | 'select' | 'dateRange' | 'numericRange' | 'boolean';
  defaultValue?: any;
}
```

## Generator Implementation

See `generators/06-table-model-generator.md`.

**Key Methods:**
```java
public class TableGenerator {
    public static TableModel extractTable(Table table);
    public static List<ColumnModel> extractColumns(Table table);
    public static List<FilterModel> extractFilters(Table table);
    public static QueryCustomizer getDefaultQuery(Table table);
}
```

## Common Patterns

### Table Rendering

```typescript
function DataTable({ model, service }: Props) {
  const [data, setData] = useState<any[]>([]);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(model.defaultPageSize);
  const [sortBy, setSortBy] = useState<SortConfig | null>(null);
  const [filters, setFilters] = useState<FilterConfig>({});
  
  const fetchData = async () => {
    const query = {
      page,
      size: pageSize,
      sort: sortBy,
      filters
    };
    const result = await service.list(query);
    setData(result.content);
  };
  
  return (
    <MUIDataTable
      data={data}
      columns={model.columns}
      options={{
        page,
        pageSize,
        onPageChange: setPage,
        onSort: setSortBy,
        onFilter: setFilters,
        rowActions: model.rowActions
      }}
    />
  );
}
```

## Testing Criteria

### Unit Tests
- [x] Table extraction from metamodel
- [x] Column extraction
- [x] Filter extraction
- [x] Row action extraction

### Integration Tests
- [x] Table renders data correctly
- [x] Sorting works
- [x] Filtering works
- [x] Pagination works
- [x] Row actions execute
- [x] Selection works

### Edge Cases
- [x] Empty table
- [x] Single row
- [x] Very wide tables (many columns)
- [x] Very long column values
- [x] All rows selected

## Related Specifications

**Metamodel:**
- `02-visual-element.md` - Base class
- `02-relation-type.md` - Data source

**Runtime Model:**
- `runtime-model/07-table-model.md` - TableModel interface

**Visual Elements:**
- `visual-elements/tables/01-table-element.md` - Detailed spec
- `visual-elements/tables/02-column-definition.md` - Columns
- `visual-elements/tables/03-filter-definition.md` - Filters

**Components:**
- `components/08-data-table.md` - Table component

---

**Status:** ✅ Complete  
**Created:** 2025-11-28  
**Last Updated:** 2025-11-28  
**Reviewed:** Not yet reviewed

