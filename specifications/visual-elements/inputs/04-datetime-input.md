# DateTimeInput Specification
**Created:** 2025-11-28
**Status:** ✅ Complete  

---

```
}
  );
    </LocalizationProvider>
      />
        }}
          }
            fullWidth: true
            helperText: error,
            error: !!error,
            required: model.required,
          textField: {
        slotProps={{
        disabled={disabled || model.readOnly}
        onChange={onChange}
        value={value}
        label={model.label}
      <DateTimePicker
    <LocalizationProvider dateAdapter={AdapterDateFns}>
  return (
function DateTimeInput({ model, value, onChange, disabled, error }: DateTimeInputProps) {

}
  error?: string;
  disabled?: boolean;
  onChange: (value: Date | null) => void;
  value: Date | null;
  model: InputModel;
interface DateTimeInputProps {
```typescript

## Component Interface

DateTimeInput provides combined date and time selection for DATE_TIME and TIMESTAMP types.

## Overview

**Dependencies:** `runtime-model/04-visual-element-model.md`  
**Assigned:** AI Agent  
**Status:** Complete  
**Domain:** Visual Elements / Inputs  


