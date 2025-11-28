# Field Validation Specification
**Created:** 2025-11-28
**Status:** ✅ Complete  

---

```
};
  ]
    }
      message: 'End date must be after start date'
      valueBy: 'startDate',
      type: 'minValueBy', 
    { 
    { type: 'required' },
  rules: [
  attributeName: 'endDate',
const endDateRules: ValidationRuleModel = {
```typescript

### Example 2: Dynamic Validation

```
const errors = validateAll(formData, validationRules);

];
  }
    ]
      { type: 'maxValue', value: 150 }
      { type: 'minValue', value: 0 },
    rules: [
    attributeName: 'age',
  {
  },
    ]
      { type: 'pattern', value: '^[^@]+@[^@]+\\.[^@]+$', message: 'Invalid email' }
      { type: 'required', message: 'Email is required' },
    rules: [
    attributeName: 'email',
  {
const validationRules: ValidationRuleModel[] = [
```typescript

### Example 1: Form Validation

## Usage Examples

```
}
  return null;
  
  }
    }
      return rule.message || error;
    if (error) {
    const error = applyValidator(rule.type, value, rule.value, allData);
    // Apply validator
    
    }
      continue;
    if (rule.condition && !evaluateCondition(rule.condition, allData)) {
    // Check condition
  for (const rule of rules) {
): string | null {
  allData: any
  rules: ValidationRule[],
  value: any,
  fieldName: string,
function validateField(
```typescript

## Validation Engine

```
};
    value > max ? `Must be before ${max.toLocaleDateString()}` : null
  maxDate: (value: Date, max: Date) => 
    value < min ? `Must be after ${min.toLocaleDateString()}` : null,
  minDate: (value: Date, min: Date) => 
  required: (value: Date | null) => !value ? 'Required' : null,
const dateValidators = {
```typescript

### Date Validation

```
};
    !Number.isInteger(value) ? 'Must be integer' : null
  integer: (value: number) => 
    value > max ? `Maximum ${max}` : null,
  max: (value: number, max: number) => 
    value < min ? `Minimum ${min}` : null,
  min: (value: number, min: number) => 
  required: (value: number | null) => value === null ? 'Required' : null,
const numericValidators = {
```typescript

### Numeric Validation

```
};
    value && !/^[^@]+@[^@]+\.[^@]+$/.test(value) ? 'Invalid email' : null
  email: (value: string) => 
    value && !new RegExp(pattern).test(value) ? 'Invalid format' : null,
  pattern: (value: string, pattern: string) => 
    value && value.length > max ? `Maximum ${max} characters` : null,
  maxLength: (value: string, max: number) => 
    value && value.length < min ? `Minimum ${min} characters` : null,
  minLength: (value: string, min: number) => 
  required: (value: string) => !value?.trim() ? 'Required' : null,
const stringValidators = {
```typescript

### String Validation

## Validation Rules

Field validation provides declarative validation rules for form inputs with real-time and on-submit validation.

## Overview

**Blocks:** Validation engine implementation  
**Dependencies:** `runtime-model/08-validation-model.md`  
**Assigned:** AI Agent  
**Status:** Complete  
**Domain:** Validation  


