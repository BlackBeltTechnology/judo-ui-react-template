# Field State Management Specification

**Domain:** Components  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `runtime-model/04-visual-element-model.md`  
**Blocks:** Form field state handling  

## Overview

Field state management provides hooks and utilities for managing individual field state including values, errors, touched state, and validation.

## Field State Hook

```typescript
interface FieldState {
  value: any;
  error: string | null;
  touched: boolean;
  dirty: boolean;
  validating: boolean;
}

function useFieldState(
  fieldName: string,
  initialValue: any,
  validationRules?: ValidationRule[]
) {
  const [state, setState] = useState<FieldState>({
    value: initialValue,
    error: null,
    touched: false,
    dirty: false,
    validating: false
  });
  
  const setValue = useCallback((newValue: any) => {
    setState(prev => ({
      ...prev,
      value: newValue,
      dirty: true
    }));
  }, []);
  
  const setError = useCallback((error: string | null) => {
    setState(prev => ({ ...prev, error }));
  }, []);
  
  const touch = useCallback(() => {
    setState(prev => ({ ...prev, touched: true }));
  }, []);
  
  const validate = useCallback(async () => {
    if (!validationRules) return null;
    
    setState(prev => ({ ...prev, validating: true }));
    
    const error = await validateField(fieldName, state.value, validationRules);
    
    setState(prev => ({
      ...prev,
      error,
      validating: false
    }));
    
    return error;
  }, [fieldName, state.value, validationRules]);
  
  const reset = useCallback(() => {
    setState({
      value: initialValue,
      error: null,
      touched: false,
      dirty: false,
      validating: false
    });
  }, [initialValue]);
  
  return {
    ...state,
    setValue,
    setError,
    touch,
    validate,
    reset
  };
}
```

## Form Fields State Hook

```typescript
interface FormFieldsState {
  values: Record<string, any>;
  errors: Record<string, string | null>;
  touched: Record<string, boolean>;
  dirty: Record<string, boolean>;
}

function useFormFields(initialValues: Record<string, any> = {}) {
  const [state, setState] = useState<FormFieldsState>({
    values: initialValues,
    errors: {},
    touched: {},
    dirty: {}
  });
  
  const setValue = useCallback((fieldName: string, value: any) => {
    setState(prev => ({
      ...prev,
      values: { ...prev.values, [fieldName]: value },
      dirty: { ...prev.dirty, [fieldName]: true }
    }));
  }, []);
  
  const setValues = useCallback((values: Record<string, any>) => {
    setState(prev => ({
      ...prev,
      values: { ...prev.values, ...values }
    }));
  }, []);
  
  const setError = useCallback((fieldName: string, error: string | null) => {
    setState(prev => ({
      ...prev,
      errors: { ...prev.errors, [fieldName]: error }
    }));
  }, []);
  
  const setErrors = useCallback((errors: Record<string, string | null>) => {
    setState(prev => ({
      ...prev,
      errors: { ...prev.errors, ...errors }
    }));
  }, []);
  
  const touch = useCallback((fieldName: string) => {
    setState(prev => ({
      ...prev,
      touched: { ...prev.touched, [fieldName]: true }
    }));
  }, []);
  
  const reset = useCallback(() => {
    setState({
      values: initialValues,
      errors: {},
      touched: {},
      dirty: {}
    });
  }, [initialValues]);
  
  const isDirty = useMemo(
    () => Object.values(state.dirty).some(d => d),
    [state.dirty]
  );
  
  const hasErrors = useMemo(
    () => Object.values(state.errors).some(e => e !== null),
    [state.errors]
  );
  
  return {
    ...state,
    setValue,
    setValues,
    setError,
    setErrors,
    touch,
    reset,
    isDirty,
    hasErrors
  };
}
```

## Field Change Handler

```typescript
function useFieldChangeHandler(
  setValue: (fieldName: string, value: any) => void,
  onValidate?: (fieldName: string, value: any) => Promise<void>
) {
  return useCallback(
    async (fieldName: string, value: any) => {
      setValue(fieldName, value);
      
      if (onValidate) {
        await onValidate(fieldName, value);
      }
    },
    [setValue, onValidate]
  );
}
```

## Debounced Validation

```typescript
function useFieldValidation(
  fieldName: string,
  value: any,
  validationRules: ValidationRule[],
  debounceMs: number = 300
) {
  const [error, setError] = useState<string | null>(null);
  const [validating, setValidating] = useState(false);
  
  const debouncedValidate = useMemo(
    () => debounce(async (val: any) => {
      setValidating(true);
      const err = await validateField(fieldName, val, validationRules);
      setError(err);
      setValidating(false);
    }, debounceMs),
    [fieldName, validationRules, debounceMs]
  );
  
  useEffect(() => {
    debouncedValidate(value);
  }, [value, debouncedValidate]);
  
  return { error, validating };
}
```

## Field Dependencies

```typescript
function useFieldDependencies(
  fieldName: string,
  dependencies: string[],
  values: Record<string, any>,
  onDependencyChange: (fieldName: string, dependentValues: any[]) => void
) {
  const prevDepsRef = useRef<any[]>([]);
  
  useEffect(() => {
    const currentDeps = dependencies.map(dep => values[dep]);
    const prevDeps = prevDepsRef.current;
    
    if (!isEqual(currentDeps, prevDeps)) {
      onDependencyChange(fieldName, currentDeps);
      prevDepsRef.current = currentDeps;
    }
  }, [fieldName, dependencies, values, onDependencyChange]);
}
```

## Examples

### Example 1: Single Field with Validation

```typescript
function EmailField() {
  const {
    value,
    error,
    touched,
    setValue,
    touch,
    validate
  } = useFieldState('email', '', [
    { type: 'required', message: 'Email is required' },
    { type: 'email', message: 'Invalid email' }
  ]);
  
  const handleBlur = async () => {
    touch();
    await validate();
  };
  
  return (
    <TextField
      value={value}
      onChange={(e) => setValue(e.target.value)}
      onBlur={handleBlur}
      error={touched && !!error}
      helperText={touched && error}
    />
  );
}
```

### Example 2: Form with Multiple Fields

```typescript
function UserForm() {
  const {
    values,
    errors,
    touched,
    setValue,
    touch,
    isDirty,
    hasErrors
  } = useFormFields({
    firstName: '',
    lastName: '',
    email: ''
  });
  
  const handleFieldChange = useFieldChangeHandler(setValue);
  
  return (
    <form>
      <TextField
        value={values.firstName}
        onChange={(e) => handleFieldChange('firstName', e.target.value)}
        error={touched.firstName && !!errors.firstName}
      />
      {/* ... more fields */}
    </form>
  );
}
```

## Testing Criteria

### Unit Tests
- [x] Field state initializes correctly
- [x] setValue updates value and marks dirty
- [x] touch marks field as touched
- [x] validate runs validation rules
- [x] reset resets to initial state

### Integration Tests
- [x] Form fields update independently
- [x] Validation triggers correctly
- [x] Dependencies update related fields
- [x] Debounced validation works

---

**Status:** ✅ Complete  
**Created:** 2025-11-28

