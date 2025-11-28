# Validation Engine Component Specification

**Domain:** Components  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `validation/03-validation-engine.md`  
**Blocks:** Form validation integration  

## Overview

Validation Engine component integrates validation system with React components, providing hooks and utilities for form validation.

## Validation Hook

```typescript
function useValidation(validationRules: ValidationRuleModel[]) {
  const engine = useMemo(() => new ValidationEngine(), []);
  const [errors, setErrors] = useState<ValidationErrors>({});
  const [touched, setTouched] = useState<Record<string, boolean>>({});
  
  const validateField = useCallback(async (
    fieldName: string,
    value: any,
    formData: any
  ) => {
    const rules = validationRules.find(r => r.attributeName === fieldName);
    if (!rules) return;
    
    const error = await engine.validateField(fieldName, value, rules.rules, formData);
    
    setErrors(prev => ({
      ...prev,
      [fieldName]: error ? [error] : []
    }));
  }, [engine, validationRules]);
  
  const validateForm = useCallback(async (formData: any) => {
    const allErrors = await engine.validateForm(formData, validationRules);
    setErrors(allErrors);
    return Object.keys(allErrors).length === 0;
  }, [engine, validationRules]);
  
  const touch = useCallback((fieldName: string) => {
    setTouched(prev => ({ ...prev, [fieldName]: true }));
  }, []);
  
  const clearErrors = useCallback(() => {
    setErrors({});
  }, []);
  
  const isValid = useMemo(
    () => Object.keys(errors).length === 0,
    [errors]
  );
  
  return {
    errors,
    touched,
    isValid,
    validateField,
    validateForm,
    touch,
    clearErrors
  };
}
```

## Form Validation Provider

```typescript
interface ValidationContextValue {
  errors: ValidationErrors;
  touched: Record<string, boolean>;
  isValid: boolean;
  validateField: (fieldName: string, value: any, formData: any) => Promise<void>;
  validateForm: (formData: any) => Promise<boolean>;
  touch: (fieldName: string) => void;
}

const ValidationContext = createContext<ValidationContextValue | null>(null);

export function ValidationProvider({ 
  validationRules, 
  children 
}: { 
  validationRules: ValidationRuleModel[];
  children: React.ReactNode;
}) {
  const validation = useValidation(validationRules);
  
  return (
    <ValidationContext.Provider value={validation}>
      {children}
    </ValidationContext.Provider>
  );
}

export function useValidationContext() {
  const context = useContext(ValidationContext);
  if (!context) {
    throw new Error('useValidationContext must be used within ValidationProvider');
  }
  return context;
}
```

## Validated Field Component

```typescript
interface ValidatedFieldProps {
  fieldName: string;
  value: any;
  formData: any;
  children: (props: {
    error: string | null;
    touched: boolean;
    validate: () => Promise<void>;
  }) => React.ReactNode;
}

function ValidatedField({ fieldName, value, formData, children }: ValidatedFieldProps) {
  const { errors, touched, validateField, touch } = useValidationContext();
  
  const error = errors[fieldName]?.[0]?.message || null;
  const isTouched = touched[fieldName] || false;
  
  const validate = useCallback(async () => {
    touch(fieldName);
    await validateField(fieldName, value, formData);
  }, [fieldName, value, formData, validateField, touch]);
  
  return <>{children({ error, touched: isTouched, validate })}</>;
}
```

## Form Submit with Validation

```typescript
function useValidatedSubmit(
  validationRules: ValidationRuleModel[],
  onSubmit: (data: any) => Promise<void>
) {
  const { validateForm } = useValidation(validationRules);
  const [submitting, setSubmitting] = useState(false);
  
  const handleSubmit = useCallback(async (data: any) => {
    setSubmitting(true);
    
    try {
      const isValid = await validateForm(data);
      
      if (!isValid) {
        return { success: false, error: 'Please fix validation errors' };
      }
      
      await onSubmit(data);
      return { success: true };
      
    } catch (err) {
      return { success: false, error: err.message };
    } finally {
      setSubmitting(false);
    }
  }, [validateForm, onSubmit]);
  
  return { handleSubmit, submitting };
}
```

## Examples

### Example 1: Form with Validation

```typescript
function UserForm({ data, onChange }: Props) {
  const validationRules: ValidationRuleModel[] = [
    {
      attributeName: 'email',
      rules: [
        { type: 'required', message: 'Email is required' },
        { type: 'email', message: 'Invalid email' }
      ]
    }
  ];
  
  return (
    <ValidationProvider validationRules={validationRules}>
      <ValidatedField fieldName="email" value={data.email} formData={data}>
        {({ error, touched, validate }) => (
          <TextField
            value={data.email}
            onChange={(e) => onChange('email', e.target.value)}
            onBlur={validate}
            error={touched && !!error}
            helperText={touched && error}
          />
        )}
      </ValidatedField>
    </ValidationProvider>
  );
}
```

### Example 2: Submit with Validation

```typescript
function SubmitForm() {
  const [data, setData] = useState({});
  const { handleSubmit, submitting } = useValidatedSubmit(
    validationRules,
    async (d) => await service.create(d)
  );
  
  return (
    <form onSubmit={(e) => {
      e.preventDefault();
      handleSubmit(data);
    }}>
      {/* form fields */}
      <Button type="submit" disabled={submitting}>
        Submit
      </Button>
    </form>
  );
}
```

## Testing Criteria

### Unit Tests
- [x] Validation hook initializes
- [x] Field validation works
- [x] Form validation works
- [x] Errors tracked correctly
- [x] Touch tracking works

### Integration Tests
- [x] Provider wraps children
- [x] Context accessible in children
- [x] Validation prevents submit
- [x] Error messages display

---

**Status:** ✅ Complete  
**Created:** 2025-11-28

