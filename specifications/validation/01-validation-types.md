# Validation Types Specification

**Domain:** Validation  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `runtime-model/08-validation-model.md`  

## Overview

Defines all validation rule types and their implementations.

## Validation Rule Types

```typescript
type ValidationRuleType =
  | 'required'
  | 'minLength'
  | 'maxLength'
  | 'minValue'
  | 'maxValue'
  | 'pattern'
  | 'minValueBy'
  | 'maxValueBy'
  | 'email'
  | 'url'
  | 'custom';

interface ValidationRule {
  type: ValidationRuleType;
  value?: any;
  valueBy?: string;
  message?: string;
  condition?: string;
  severity?: 'error' | 'warning' | 'info';
}
```

## Built-in Validators

### Required
```typescript
required: (value: any) => !value ? 'Required' : null
```

### Length
```typescript
minLength: (value: string, min: number) => 
  value.length < min ? `Min ${min} chars` : null
maxLength: (value: string, max: number) => 
  value.length > max ? `Max ${max} chars` : null
```

### Value Range
```typescript
minValue: (value: number, min: number) => 
  value < min ? `Min value ${min}` : null
maxValue: (value: number, max: number) => 
  value > max ? `Max value ${max}` : null
```

### Pattern
```typescript
pattern: (value: string, regex: string) => 
  !new RegExp(regex).test(value) ? 'Invalid format' : null
email: (value: string) => 
  !/^[^@]+@[^@]+\.[^@]+$/.test(value) ? 'Invalid email' : null
```

---

**Status:** ✅ Complete  
**Created:** 2025-11-28

