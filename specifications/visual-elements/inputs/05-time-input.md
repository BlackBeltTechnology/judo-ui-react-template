# TimeInput Specification

**Domain:** Visual Elements / Inputs  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `runtime-model/04-visual-element-model.md`  

## Overview

TimeInput provides time selection for TIME type attributes.

## Component Interface

```typescript
interface TimeInputProps {
  model: InputModel;
  value: string | null;  // HH:mm:ss format
  onChange: (value: string | null) => void;
  disabled?: boolean;
  error?: string;
}

function TimeInput({ model, value, onChange, disabled, error }: TimeInputProps) {
  return (
    <LocalizationProvider dateAdapter={AdapterDateFns}>
      <TimePicker
        label={model.label}
        value={value ? parseTime(value) : null}
        onChange={(date) => onChange(date ? formatTime(date) : null)}
        disabled={disabled || model.readOnly}
        slotProps={{
          textField: {
            required: model.required,
            error: !!error,
            helperText: error,
            fullWidth: true
          }
        }}
      />
    </LocalizationProvider>
  );
}
```

---

**Status:** ✅ Complete  
**Created:** 2025-11-28

