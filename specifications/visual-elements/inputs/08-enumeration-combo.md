# EnumerationCombo Specification

**Domain:** Visual Elements / Inputs  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `runtime-model/04-visual-element-model.md`, `data-model/05-enumeration-type.md`  
**Blocks:** Enum input components  

## Overview

`EnumerationCombo` provides dropdown selection for enum values with i18n support.

## Component Interface

```typescript
interface EnumerationComboProps {
  model: InputModel;
  value: string | null;
  onChange: (value: string | null) => void;
  options: EnumOption[];
  disabled?: boolean;
  error?: string;
}

interface EnumOption {
  value: string;
  label: string;
  ordinal: number;
}

function EnumerationCombo({ model, value, onChange, options, disabled, error }: EnumerationComboProps) {
  return (
    <TextField
      select
      label={model.label}
      value={value || ''}
      onChange={(e) => onChange(e.target.value || null)}
      disabled={disabled || model.readOnly}
      required={model.required}
      error={!!error}
      helperText={error}
      fullWidth
    >
      {options.map((option) => (
        <MenuItem key={option.value} value={option.value}>
          {t(`judo.enums.${model.attributeType}.${option.value}`, {
            defaultValue: option.label
          })}
        </MenuItem>
      ))}
    </TextField>
  );
}
```

## Examples

### Example 1: Status Selection

```typescript
const model: InputModel = {
  id: 've-status',
  name: 'status',
  type: 'enumerationCombo',
  attributeName: 'status',
  attributeType: 'UserStatus',
  label: 'Status',
  col: 6,
  required: true,
  validation: { required: true },
  defaultValue: 'ACTIVE'
};

const options: EnumOption[] = [
  { value: 'ACTIVE', label: 'Active', ordinal: 0 },
  { value: 'INACTIVE', label: 'Inactive', ordinal: 1 },
  { value: 'PENDING', label: 'Pending', ordinal: 2 }
];

<EnumerationCombo
  model={model}
  value={data.status}
  onChange={(value) => handleChange('status', value)}
  options={options}
/>
```

---

**Status:** ✅ Complete  
**Created:** 2025-11-28

