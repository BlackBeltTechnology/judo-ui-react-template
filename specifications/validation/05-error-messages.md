# Error Messages Specification

**Domain:** Validation  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `validation/01-validation-types.md`, `runtime-model/09-i18n-model.md`  

## Overview

Defines error message generation, i18n support, and user-friendly error display.

## Error Message Templates

```typescript
const errorMessageTemplates: Record<ValidationRuleType, string> = {
  required: 'This field is required',
  minLength: 'Minimum {{min}} characters required',
  maxLength: 'Maximum {{max}} characters allowed',
  minValue: 'Minimum value is {{min}}',
  maxValue: 'Maximum value is {{max}}',
  pattern: 'Invalid format',
  email: 'Invalid email address',
  url: 'Invalid URL',
  minValueBy: 'Must be >= {{field}}',
  maxValueBy: 'Must be <= {{field}}',
  custom: 'Validation failed'
};
```

## Message Generation

```typescript
function generateErrorMessage(
  rule: ValidationRule,
  fieldName: string,
  t: TranslationFunction
): string {
  const key = `judo.validation.${rule.type}`;
  const template = rule.message || errorMessageTemplates[rule.type];
  
  return t(key, {
    defaultValue: template,
    interpolation: {
      field: fieldName,
      min: rule.value,
      max: rule.value,
      ...rule
    }
  });
}
```

## Error Display

```typescript
function FieldError({ error }: { error: ValidationError }) {
  return (
    <FormHelperText error={error.severity === 'error'}>
      {error.message}
    </FormHelperText>
  );
}

function FormErrors({ errors }: { errors: ValidationErrors }) {
  const errorList = Object.entries(errors).flatMap(([field, errs]) => errs);
  
  if (errorList.length === 0) return null;
  
  return (
    <Alert severity="error">
      <AlertTitle>Please fix the following errors:</AlertTitle>
      <ul>
        {errorList.map((err, i) => (
          <li key={i}>{err.message}</li>
        ))}
      </ul>
    </Alert>
  );
}
```

---

**Status:** ✅ Complete  
**Created:** 2025-11-28

