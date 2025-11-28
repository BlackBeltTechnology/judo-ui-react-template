# Filter Definition Specification

**Domain:** Visual Elements / Tables  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `runtime-model/07-table-model.md`  

## Overview

Filter definition specifies how table filters are configured and applied.

## Filter Model

```typescript
interface FilterModel {
  id: string;
  name: string;
  label: string;
  attributeName: string;
  filterType: 'text' | 'select' | 'dateRange' | 'numericRange' | 'boolean';
  options?: Array<{ value: any; label: string }>;
  defaultValue?: any;
}
```

## Filter Components

### Text Filter
```typescript
function TextFilter({ filter, onChange }: FilterProps) {
  return (
    <TextField
      label={filter.label}
      onChange={(e) => onChange(filter.attributeName, e.target.value)}
      placeholder="Filter..."
    />
  );
}
```

### Select Filter
```typescript
function SelectFilter({ filter, onChange }: FilterProps) {
  return (
    <TextField
      select
      label={filter.label}
      onChange={(e) => onChange(filter.attributeName, e.target.value)}
    >
      {filter.options?.map(opt => (
        <MenuItem key={opt.value} value={opt.value}>
          {opt.label}
        </MenuItem>
      ))}
    </TextField>
  );
}
```

### Date Range Filter
```typescript
function DateRangeFilter({ filter, onChange }: FilterProps) {
  const [from, setFrom] = useState<Date | null>(null);
  const [to, setTo] = useState<Date | null>(null);
  
  useEffect(() => {
    if (from || to) {
      onChange(filter.attributeName, { from, to });
    }
  }, [from, to]);
  
  return (
    <Box>
      <DatePicker label="From" value={from} onChange={setFrom} />
      <DatePicker label="To" value={to} onChange={setTo} />
    </Box>
  );
}
```

---

**Status:** ✅ Complete  
**Created:** 2025-11-28

