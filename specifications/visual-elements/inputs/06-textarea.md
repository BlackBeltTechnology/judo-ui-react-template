# TextArea Specification

**Domain:** Visual Elements / Inputs  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `runtime-model/04-visual-element-model.md`  
**Blocks:** Multi-line text components  

## Overview

`TextArea` provides multi-line text input for TEXT type attributes. Supports rows configuration and character limits.

## Component Interface

```typescript
interface TextAreaProps {
  model: InputModel;
  value: string;
  onChange: (value: string) => void;
  onBlur?: () => void;
  disabled?: boolean;
  error?: string;
  rows?: number;
}

function TextArea({ model, value, onChange, onBlur, disabled, error, rows = 4 }: TextAreaProps) {
  return (
    <TextField
      multiline
      rows={rows}
      name={model.name}
      label={model.label}
      value={value || ''}
      onChange={(e) => onChange(e.target.value)}
      onBlur={onBlur}
      disabled={disabled || model.readOnly}
      required={model.required}
      error={!!error}
      helperText={error}
      inputProps={{
        maxLength: model.validation.maxLength
      }}
      fullWidth
    />
  );
}
```

## Examples

```typescript
const model: InputModel = {
  id: 've-description',
  name: 'description',
  type: 'textArea',
  attributeName: 'description',
  attributeType: 'text',
  label: 'Description',
  col: 12,
  required: false,
  validation: {
    maxLength: 2000
  }
};
```

---

**Status:** ✅ Complete  
**Created:** 2025-11-28

