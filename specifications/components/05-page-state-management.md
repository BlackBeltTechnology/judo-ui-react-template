# Page State Management Specification
**Created:** 2025-11-28
**Status:** ✅ Complete  

---

- [x] Optimistic updates work
- [x] Concurrent updates detected
- [x] Auto-save triggers
- [x] Unsaved changes warning works
### Integration Tests

- [x] Reload clears dirty flag
- [x] Validation errors tracked
- [x] Field updates mark page dirty
- [x] Data loads on mount
- [x] Page state initializes correctly
### Unit Tests

## Testing Criteria

```
}
  return <Editor data={data} onChange={updateField} />;
  
  );
    30000
    async (d) => await DocumentService.update(d.id, d),
    isDirty,
    data,
  useAutoSave(
  
  const { data, isDirty, updateField } = usePageState(DocumentPageModel);
function DocumentEditor() {
```typescript

### Example 2: With Auto-Save

```
}
  );
    />
      errors={validationErrors}
      onChange={updateField}
      data={data}
      model={UserFormPageModel}
    <ModelDrivenPage
  return (
  
  if (error) return <Error message={error} />;
  if (loading) return <Loading />;
  
  };
    }
      // Handle error
    } catch (err) {
      await reload();
      await UserService.update(id, data);
    try {
    
    if (!isValid) return;
  const handleSave = async () => {
  
  useUnsavedChangesWarning(isDirty);
  
  } = usePageState(UserFormPageModel, id);
    reload
    setValidationErrors,
    touchField,
    updateField,
    validationErrors,
    isValid,
    isDirty,
    error,
    loading,
    data,
  const {
  const { id } = useParams();
function UserFormPage() {
```typescript

### Example 1: Complete Page State

## Examples

```
}
  return { checkForUpdates };
  
  }, [entityId, lastModified]);
    return { hasConflict: false };
    
    }
      };
        message: 'This entity has been modified by someone else'
        hasConflict: true,
      return {
    if (current.lastModified > lastModified) {
    
    const current = await service.getById(entityId);
  const checkForUpdates = useCallback(async () => {
function useConcurrentUpdateDetection(entityId: string, lastModified: Date) {
```typescript

## Concurrent Update Detection

```
}
  return { value: optimisticValue, isPending, update };
  
  }, [currentValue, updateFn]);
    }
      setIsPending(false);
    } finally {
      throw err;
      setOptimisticValue(currentValue);
      // Revert on error
    } catch (err) {
      setOptimisticValue(result);
      const result = await updateFn(newValue);
      // Update on server
    try {
    
    setIsPending(true);
    setOptimisticValue(newValue);
    // Immediately show new value
  const update = useCallback(async (newValue: T) => {
  
  const [isPending, setIsPending] = useState(false);
  const [optimisticValue, setOptimisticValue] = useState(currentValue);
) {
  updateFn: (value: T) => Promise<T>
  currentValue: T,
function useOptimisticUpdate<T>(
```typescript

## Optimistic Updates

```
}
  }, [data, isDirty, onSave, intervalMs]);
    return () => clearInterval(timer);
    
    }, intervalMs);
      }
        }
          console.error('Auto-save failed:', err);
        } catch (err) {
          await onSave(data);
        try {
      if (isDirty) {
    const timer = setInterval(async () => {
    
    if (!isDirty) return;
  useEffect(() => {
) {
  intervalMs: number = 30000
  onSave: (data: any) => Promise<void>,
  isDirty: boolean,
  data: any,
function useAutoSave(
```typescript

## Auto-Save

```
}
  );
    isDirty
    'You have unsaved changes. Are you sure you want to leave?',
  usePrompt(
  // Also block navigation
  
  }, [isDirty]);
    return () => window.removeEventListener('beforeunload', handleBeforeUnload);
    window.addEventListener('beforeunload', handleBeforeUnload);
    
    };
      }
        e.returnValue = '';
        e.preventDefault();
      if (isDirty) {
    const handleBeforeUnload = (e: BeforeUnloadEvent) => {
  useEffect(() => {
function useUnsavedChangesWarning(isDirty: boolean) {
```typescript

## Unsaved Changes Warning

```
}
  };
    reload
    reset,
    setValidationErrors,
    touchField,
    updateField,
    ...state,
  return {
  
  }, [entityId]);
    setState(prev => ({ ...prev, isDirty: false, touchedFields: new Set() }));
    await loadData();
  const reload = useCallback(async () => {
  
  }, []);
    });
      touchedFields: new Set()
      validationErrors: {},
      isValid: true,
      isDirty: false,
      error: null,
      loading: false,
      data: {},
    setState({
  const reset = useCallback(() => {
  
  }, []);
    }));
      isValid: Object.keys(errors).length === 0
      validationErrors: errors,
      ...prev,
    setState(prev => ({
  const setValidationErrors = useCallback((errors: ValidationErrors) => {
  
  }, []);
    }));
      touchedFields: new Set(prev.touchedFields).add(fieldName)
      ...prev,
    setState(prev => ({
  const touchField = useCallback((fieldName: string) => {
  
  }, []);
    }));
      isDirty: true
      data: { ...prev.data, [fieldName]: value },
      ...prev,
    setState(prev => ({
  const updateField = useCallback((fieldName: string, value: any) => {
  
  };
    }
      setState(prev => ({ ...prev, error: err.message, loading: false }));
    } catch (err) {
      setState(prev => ({ ...prev, data, loading: false }));
      const data = await service.getById(entityId);
      setState(prev => ({ ...prev, loading: true, error: null }));
    try {
  const loadData = async () => {
  
  }, [entityId]);
    }
      setState(prev => ({ ...prev, loading: false, data: getDefaults(model) }));
    } else {
      loadData();
    if (entityId) {
  useEffect(() => {
  // Load data
  
  const service = useService(model.dataElement);
  
  });
    touchedFields: new Set()
    validationErrors: {},
    isValid: true,
    isDirty: false,
    error: null,
    loading: true,
    data: {},
  const [state, setState] = useState<PageState>({
function usePageState(model: PageModel, entityId?: string) {

}
  touchedFields: Set<string>;
  validationErrors: ValidationErrors;
  isValid: boolean;
  isDirty: boolean;
  error: string | null;
  loading: boolean;
  data: any;
interface PageState {
```typescript

## Page State Hook

Page state management coordinates all state for a model-driven page including data, loading, errors, validation, and dirty tracking.

## Overview

**Blocks:** Complete page state handling  
**Dependencies:** `components/01-model-driven-page.md`, `components/04-field-state-management.md`  
**Assigned:** AI Agent  
**Status:** Complete  
**Domain:** Components  


