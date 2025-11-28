# Pagination Specification

**Domain:** Visual Elements / Tables  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `runtime-model/07-table-model.md`  

## Overview

Pagination controls page navigation and page size selection for tables.

## Pagination State

```typescript
interface PaginationState {
  page: number;              // 0-based page number
  pageSize: number;          // Rows per page
  totalCount: number;        // Total rows
  totalPages: number;        // Calculated total pages
}
```

## Pagination Component

```typescript
function TablePagination({ state, onPageChange, onPageSizeChange, pageSizeOptions }: PaginationProps) {
  return (
    <MUITablePagination
      component="div"
      count={state.totalCount}
      page={state.page}
      onPageChange={(e, newPage) => onPageChange(newPage)}
      rowsPerPage={state.pageSize}
      onRowsPerPageChange={(e) => onPageSizeChange(parseInt(e.target.value, 10))}
      rowsPerPageOptions={pageSizeOptions}
      labelRowsPerPage="Rows per page:"
      labelDisplayedRows={({ from, to, count }) => `${from}-${to} of ${count}`}
    />
  );
}
```

## Server-Side Pagination

```typescript
async function fetchPage(page: number, pageSize: number) {
  const query: QueryCustomizer = {
    _seek: {
      page,
      limit: pageSize
    }
  };
  
  const result = await service.list(query);
  
  return {
    data: result.content,
    totalCount: result.totalCount,
    totalPages: Math.ceil(result.totalCount / pageSize)
  };
}
```

---

**Status:** ✅ Complete  
**Created:** 2025-11-28

