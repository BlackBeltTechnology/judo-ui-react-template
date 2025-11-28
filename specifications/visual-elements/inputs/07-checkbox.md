# Checkbox Specification

**Domain:** Visual Elements / Inputs  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `runtime-model/04-visual-element-model.md`  
**Blocks:** Boolean input components  

## Overview

`Checkbox` provides boolean input for true/false values. Used for BOOLEAN attribute types.

## Component Interface

```typescript
interface CheckboxProps {
  model: InputModel;
  value: boolean;
  onChange: (value: boolean) => void;
  disabled?: boolean;
  error?: string;
}

function Checkbox({ model, value, onChange, disabled, error }: CheckboxProps) {
  return (
    <FormControlLabel
      control={
        <MuiCheckbox
          checked={value || false}
          onChange={(e) => onChange(e.target.checked)}
          disabled={disabled || model.readOnly}
          required={model.required}
        />
      }
      label={model.label}
    />
  );
}
```

## Examples

### Example 1: Simple Checkbox

```typescript
const model: InputModel = {
  id: 've-active',
  name: 'active',
  type: 'checkbox',
  attributeName: 'active',
  attributeType: 'boolean',
  label: 'Active',
  col: 12,
  required: false,
  readOnly: false,
  defaultValue: true
};

<Checkbox
  model={model}
  value={data.active}
  onChange={(value) => handleChange('active', value)}
/>
```

### Example 2: Switch Variant

```typescript
<FormControlLabel
  control={
    <Switch
      checked={value}
      onChange={(e) => onChange(e.target.checked)}
    />
  }
  label={model.label}
/>
```

---

**Status:** ✅ Complete  
**Created:** 2025-11-28

