# Column Definition Specification

**Domain:** Visual Elements / Tables  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `runtime-model/07-table-model.md`  

## Overview

Column definition specifies how table columns are configured, rendered, and formatted.

## Column Model

```typescript
interface ColumnModel {
  id: string;
  name: string;
  label: string;
  attributeName: string;
  attributeType: DataType;
  align: 'left' | 'center' | 'right';
  width?: number;
  sortable: boolean;
  filterable: boolean;
  format?: string;
  customRenderer?: string;
}
```

## Column Rendering

```typescript
function renderCell(column: ColumnModel, value: any, row: any) {
  // Custom renderer
  if (column.customRenderer) {
    const CustomRenderer = getCustomRenderer(column.customRenderer);
    return <CustomRenderer value={value} row={row} />;
  }
  
  // Type-based rendering
  switch (column.attributeType) {
    case 'boolean':
      return <Checkbox checked={value} disabled />;
    
    case 'date':
    case 'dateTime':
      return formatDate(value, column.format || 'yyyy-MM-dd');
    
    case 'decimal':
    case 'double':
      return formatNumber(value, { decimalPlaces: 2 });
    
    default:
      return String(value || '');
  }
}
```

## Column Formatting

```typescript
const formatters = {
  date: (value: Date, format: string) => {
    return formatDate(value, format);
  },
  number: (value: number, decimals: number) => {
    return new Intl.NumberFormat('en-US', {
      minimumFractionDigits: decimals,
      maximumFractionDigits: decimals
    }).format(value);
  },
  currency: (value: number, currency: string) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency
    }).format(value);
  }
};
```

---

**Status:** ✅ Complete  
**Created:** 2025-11-28

