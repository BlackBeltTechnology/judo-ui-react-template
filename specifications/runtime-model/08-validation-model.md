# ValidationModel Specification

**Domain:** Runtime Model  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `01-core-types.md`  
**Blocks:** Validation engine, form validation  

## Overview

`ValidationModel` represents validation rules and error states for form fields and entities. It provides declarative validation configuration that drives the runtime validation engine.

## TypeScript Interfaces

### ValidationRuleModel

```typescript
interface ValidationRuleModel {
  attributeName: string;
  rules: ValidationRule[];
}

interface ValidationRule {
  type: ValidationRuleType;
  value?: any;
  message?: string;
  severity?: SeverityType;               // 'error' | 'warning' | 'info'
  
  // Dynamic validation
  valueBy?: string;                      // Reference to another field
  condition?: string;                    // Expression for conditional validation
}

type ValidationRuleType =
  | 'required'
  | 'minValue'
  | 'maxValue'
  | 'minLength'
  | 'maxLength'
  | 'pattern'
  | 'minValueBy'
  | 'maxValueBy'
  | 'custom';
```

### ValidationErrorModel

```typescript
interface ValidationErrorModel {
  field: string;
  rule: ValidationRuleType;
  message: string;
  severity: SeverityType;
  value?: any;
}

type ValidationErrors = Record<string, ValidationErrorModel[]>;
```

### ValidationStateModel

```typescript
interface ValidationStateModel {
  isValid: boolean;
  errors: ValidationErrors;
  warnings: ValidationErrors;
  touched: Record<string, boolean>;
  validating: Record<string, boolean>;
}
```

## Examples

### Example 1: Required Field

```typescript
const requiredValidation: ValidationRuleModel = {
  attributeName: 'email',
  rules: [
    {
      type: 'required',
      message: 'Email is required',
      severity: 'error'
    }
  ]
};
```

### Example 2: String Length Constraints

```typescript
const usernameValidation: ValidationRuleModel = {
  attributeName: 'username',
  rules: [
    {
      type: 'required',
      message: 'Username is required'
    },
    {
      type: 'minLength',
      value: 3,
      message: 'Username must be at least 3 characters'
    },
    {
      type: 'maxLength',
      value: 20,
      message: 'Username cannot exceed 20 characters'
    }
  ]
};
```

### Example 3: Numeric Range

```typescript
const ageValidation: ValidationRuleModel = {
  attributeName: 'age',
  rules: [
    {
      type: 'required',
      message: 'Age is required'
    },
    {
      type: 'minValue',
      value: 0,
      message: 'Age must be positive'
    },
    {
      type: 'maxValue',
      value: 150,
      message: 'Age must be less than 150'
    }
  ]
};
```

### Example 4: Pattern (Email)

```typescript
const emailValidation: ValidationRuleModel = {
  attributeName: 'email',
  rules: [
    {
      type: 'required',
      message: 'Email is required'
    },
    {
      type: 'pattern',
      value: '^[^@]+@[^@]+\\.[^@]+$',
      message: 'Invalid email format'
    }
  ]
};
```

### Example 5: Dynamic Validation (Date Range)

```typescript
const startDateValidation: ValidationRuleModel = {
  attributeName: 'startDate',
  rules: [
    {
      type: 'required',
      message: 'Start date is required'
    }
  ]
};

const endDateValidation: ValidationRuleModel = {
  attributeName: 'endDate',
  rules: [
    {
      type: 'required',
      message: 'End date is required'
    },
    {
      type: 'minValueBy',
      valueBy: 'startDate',
      message: 'End date must be after start date'
    }
  ]
};
```

### Example 6: Conditional Validation

```typescript
const ssnValidation: ValidationRuleModel = {
  attributeName: 'ssn',
  rules: [
    {
      type: 'required',
      condition: '!isForeign',           // Only required if not foreign
      message: 'SSN is required for domestic users'
    },
    {
      type: 'pattern',
      value: '^\\d{3}-\\d{2}-\\d{4}$',
      condition: 'ssn != null',
      message: 'Invalid SSN format'
    }
  ]
};
```

### Example 7: Multiple Rules with Different Severities

```typescript
const passwordValidation: ValidationRuleModel = {
  attributeName: 'password',
  rules: [
    {
      type: 'required',
      message: 'Password is required',
      severity: 'error'
    },
    {
      type: 'minLength',
      value: 8,
      message: 'Password must be at least 8 characters',
      severity: 'error'
    },
    {
      type: 'pattern',
      value: '.*[A-Z].*',
      message: 'Password should contain uppercase letters',
      severity: 'warning'                // Warning, not error
    },
    {
      type: 'pattern',
      value: '.*[0-9].*',
      message: 'Password should contain numbers',
      severity: 'warning'
    }
  ]
};
```

### Example 8: Custom Validation

```typescript
const customValidation: ValidationRuleModel = {
  attributeName: 'confirmPassword',
  rules: [
    {
      type: 'custom',
      message: 'Passwords must match',
      condition: 'password === confirmPassword'
    }
  ]
};
```

## Validation Engine

### Validate Single Field

```typescript
function validateField(
  attributeName: string,
  value: any,
  rule: ValidationRule,
  data: any
): ValidationErrorModel | null {
  // Check condition
  if (rule.condition && !evaluateCondition(rule.condition, data)) {
    return null;  // Rule not applicable
  }
  
  switch (rule.type) {
    case 'required':
      if (!value || (typeof value === 'string' && value.trim() === '')) {
        return {
          field: attributeName,
          rule: 'required',
          message: rule.message || 'This field is required',
          severity: rule.severity || 'error'
        };
      }
      break;
    
    case 'minLength':
      if (typeof value === 'string' && value.length < rule.value) {
        return {
          field: attributeName,
          rule: 'minLength',
          message: rule.message || `Minimum ${rule.value} characters required`,
          severity: rule.severity || 'error'
        };
      }
      break;
    
    case 'maxLength':
      if (typeof value === 'string' && value.length > rule.value) {
        return {
          field: attributeName,
          rule: 'maxLength',
          message: rule.message || `Maximum ${rule.value} characters allowed`,
          severity: rule.severity || 'error'
        };
      }
      break;
    
    case 'minValue':
      if (typeof value === 'number' && value < rule.value) {
        return {
          field: attributeName,
          rule: 'minValue',
          message: rule.message || `Minimum value is ${rule.value}`,
          severity: rule.severity || 'error'
        };
      }
      break;
    
    case 'maxValue':
      if (typeof value === 'number' && value > rule.value) {
        return {
          field: attributeName,
          rule: 'maxValue',
          message: rule.message || `Maximum value is ${rule.value}`,
          severity: rule.severity || 'error'
        };
      }
      break;
    
    case 'pattern':
      if (typeof value === 'string' && !new RegExp(rule.value).test(value)) {
        return {
          field: attributeName,
          rule: 'pattern',
          message: rule.message || 'Invalid format',
          severity: rule.severity || 'error'
        };
      }
      break;
    
    case 'minValueBy':
      const minValue = data[rule.valueBy!];
      if (value < minValue) {
        return {
          field: attributeName,
          rule: 'minValueBy',
          message: rule.message || `Must be >= ${rule.valueBy}`,
          severity: rule.severity || 'error',
          value: minValue
        };
      }
      break;
    
    case 'maxValueBy':
      const maxValue = data[rule.valueBy!];
      if (value > maxValue) {
        return {
          field: attributeName,
          rule: 'maxValueBy',
          message: rule.message || `Must be <= ${rule.valueBy}`,
          severity: rule.severity || 'error',
          value: maxValue
        };
      }
      break;
    
    case 'custom':
      if (rule.condition && !evaluateCondition(rule.condition, data)) {
        return {
          field: attributeName,
          rule: 'custom',
          message: rule.message || 'Validation failed',
          severity: rule.severity || 'error'
        };
      }
      break;
  }
  
  return null;
}
```

### Validate All Fields

```typescript
function validateAll(
  data: any,
  validationRules: ValidationRuleModel[]
): ValidationStateModel {
  const errors: ValidationErrors = {};
  const warnings: ValidationErrors = {};
  
  for (const ruleModel of validationRules) {
    const value = data[ruleModel.attributeName];
    
    for (const rule of ruleModel.rules) {
      const error = validateField(ruleModel.attributeName, value, rule, data);
      
      if (error) {
        if (error.severity === 'error') {
          if (!errors[ruleModel.attributeName]) {
            errors[ruleModel.attributeName] = [];
          }
          errors[ruleModel.attributeName].push(error);
        } else if (error.severity === 'warning') {
          if (!warnings[ruleModel.attributeName]) {
            warnings[ruleModel.attributeName] = [];
          }
          warnings[ruleModel.attributeName].push(error);
        }
      }
    }
  }
  
  return {
    isValid: Object.keys(errors).length === 0,
    errors,
    warnings,
    touched: {},
    validating: {}
  };
}
```

## React Hook Usage

```typescript
function useValidation(validationRules: ValidationRuleModel[]) {
  const [state, setState] = useState<ValidationStateModel>({
    isValid: true,
    errors: {},
    warnings: {},
    touched: {},
    validating: {}
  });
  
  const validate = (data: any) => {
    const result = validateAll(data, validationRules);
    setState(result);
    return result.isValid;
  };
  
  const validateField = (attributeName: string, data: any) => {
    // Validate single field
    // ...
  };
  
  const touch = (attributeName: string) => {
    setState(prev => ({
      ...prev,
      touched: { ...prev.touched, [attributeName]: true }
    }));
  };
  
  return { state, validate, validateField, touch };
}
```

## Validation Rules

- Rules are executed in order
- First error stops validation for that field (unless all errors needed)
- Warnings don't prevent form submission
- Dynamic rules (minValueBy, maxValueBy) re-evaluate when dependencies change
- Conditional rules only execute when condition is true

## Related Specifications

**Runtime Model:**
- `01-core-types.md` - Base types
- `04-visual-element-model.md` - Input validation

**Validation:**
- `validation/01-validation-types.md` - Validation types
- `validation/02-field-validators.md` - Field validators

**Components:**
- `components/06-validation-engine.md` - Validation engine

---

**Status:** ✅ Complete  
**Created:** 2025-11-28  
**Last Updated:** 2025-11-28  
**Reviewed:** Not yet reviewed

