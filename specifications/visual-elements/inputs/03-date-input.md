# DateInput Specification
**Created:** 2025-11-28
**Status:** ✅ Complete  

---

```
}
  return null;
  
  }
    }
      }
        return `Date must be after ${validation.minValueBy}`;
      if (value < minDate) {
      const minDate = new Date(data[validation.minValueBy]);
    if (validation.minValueBy && data[validation.minValueBy]) {
    }
      return `Date must be before ${validation.maxValue}`;
    if (validation.maxValue && value > new Date(validation.maxValue)) {
    }
      return `Date must be after ${validation.minValue}`;
    if (validation.minValue && value < new Date(validation.minValue)) {
  if (value) {
  
  }
    return 'Date is required';
  if (validation.required && !value) {
function validateDate(value: Date | null, validation: ValidationRules, data: any): string | null {
```typescript

## Validation

```
};
  }
    minValueBy: 'startDate'
    required: true,
  validation: { 
  label: 'End Date',
  type: 'dateInput',
  name: 'endDate',
  id: 've-endDate',
const endDateModel: InputModel = {

};
  validation: { required: true }
  label: 'Start Date',
  type: 'dateInput',
  name: 'startDate',
  id: 've-startDate',
const startDateModel: InputModel = {
```typescript

### Example 2: Date Range

```
};
  }
    maxValue: 'TODAY'
    required: true,
  validation: {
  required: true,
  col: 6,
  label: 'Birth Date',
  attributeType: 'date',
  attributeName: 'birthDate',
  type: 'dateInput',
  name: 'birthDate',
  id: 've-birthDate',
const model: InputModel = {
```typescript

### Example 1: Birth Date

## Examples

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
        maxDate={model.validation.maxValue ? new Date(model.validation.maxValue) : undefined}
        minDate={model.validation.minValue ? new Date(model.validation.minValue) : undefined}
        disabled={disabled || model.readOnly}
        onChange={onChange}
        value={value}
        label={model.label}
      <DatePicker
    <LocalizationProvider dateAdapter={AdapterDateFns}>
  return (
function DateInput({ model, value, onChange, disabled, error }: DateInputProps) {

}
  error?: string;
  disabled?: boolean;
  onBlur?: () => void;
  onChange: (value: Date | null) => void;
  value: Date | null;
  model: InputModel;
interface DateInputProps {
```typescript

## Component Interface

`DateInput` provides date selection for DATE type attributes. Supports calendar picker, validation, and date range constraints.

## Overview

**Blocks:** Date input components  
**Dependencies:** `runtime-model/04-visual-element-model.md`  
**Assigned:** AI Agent  
**Status:** Complete  
**Domain:** Visual Elements / Inputs  


