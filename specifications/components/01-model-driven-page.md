# Model-Driven Page Component Specification
**Reviewed:** Not yet reviewed
**Last Updated:** 2025-11-28  
**Created:** 2025-11-28  
**Status:** ✅ Complete  

---

- `07-action-executor-hook.md` - Action execution
- `05-page-state-management.md` - State management
- `02-model-driven-container.md` - Container rendering
**Components:**

- `runtime-model/02-page-model.md` - Page model structure
**Runtime Model:**

## Related Specifications

- [x] Navigation during save
- [x] Concurrent updates
- [x] Network error
- [x] Save failure
- [x] Load failure
- [x] Missing entity ID
### Edge Cases

- [x] Navigation works
- [x] Validation prevents submit
- [x] Actions refresh data
- [x] Dialog opens/closes
- [x] Create form creates entity
- [x] Form page saves data
- [x] View page displays data
### Integration Tests

- [x] Shows error state
- [x] Shows loading state
- [x] Validates on submit
- [x] Executes actions correctly
- [x] Handles field changes
- [x] Fetches data on mount
- [x] Renders with valid model
### Unit Tests

## Testing Criteria

```
}
  );
    </Box>
      <CircularProgress />
    <Box display="flex" justifyContent="center" p={4}>
  return (
function LoadingState() {

}
  );
    </Alert>
      {error}
      <AlertTitle>Error Loading Page</AlertTitle>
    <Alert severity="error">
  return (
function ErrorBoundary({ error }: { error: string }) {
```typescript

## Error Handling

```
}
  };
    hooks?.afterAction?.(action, result);
    const result = await executeAction(action);
    
    }
      return;
    if (hooks?.beforeAction?.(action) === false) {
  const handleAction = async (action: ActionModel) => {
  
  }, []);
    });
      hooks?.onLoad?.(data);
    fetchData().then(data => {
  useEffect(() => {
function ModelDrivenPage({ model, hooks }: Props) {

}
  afterAction?: (action: ActionModel, result: ActionResult) => void;
  beforeAction?: (action: ActionModel) => boolean;
  onClose?: () => void;
  onError?: (error: Error) => void;
  onSave?: (data: any) => Promise<void>;
  onLoad?: (data: any) => void;
interface PageLifecycleHooks {
```typescript

## Lifecycle Hooks

```
};
  );
    />
      serviceImpl={UserService}
      model={UserListPageModel}
    <ModelDrivenPage
  return (
const UserListPage = () => {
```typescript

### Example 3: Table Page

```
};
  ) : null;
    />
      onClose={onClose}
      serviceImpl={UserService}
      model={CreateUserFormModel}
    <ModelDrivenPage
  return open ? (
const CreateUserDialog = ({ open, onClose }: Props) => {
```typescript

### Example 2: Create Form Dialog

```
};
  );
    />
      entityId={id}
      serviceImpl={UserService}
      model={UserViewPageModel}
    <ModelDrivenPage
  return (
  
  const { id } = useParams();
const UserViewPage = () => {
```typescript

### Example 1: User View Page

## Examples

```
}
  return handlers;
  
  });
    };
      return await executeAction(action, context);
    handlers[action.name] = async (...args: any[]) => {
  model.actions.forEach(action => {
  
  const handlers: Record<string, Function> = {};
): Record<string, Function> {
  context: ActionContext
  model: PageModel,
function createActionHandlers(
```typescript

## Action Integration

```
}
  return defaults;
  
  });
    }
      defaults[input.attributeName] = input.defaultValue;
    if (input.defaultValue !== undefined) {
  getAllInputElements(model.container).forEach(input => {
  
  const defaults: any = {};
function getCreateFormDefaults(model: PageModel): any {
```typescript
### Create Form (New)

```
}
  return data;
  const data = await service.getForUpdate(entityId);
async function fetchFormData(entityId: string, service: any) {
```typescript
### Form Page (Edit)

```
}
  return data;
  const data = await service.getById(entityId);
async function fetchViewData(entityId: string, service: any) {
```typescript
### View Page (Read-Only)

## Data Fetching Patterns

```
}
  return { state, updateData, resetState };
  
  };
    });
      errors: {}
      isDirty: false,
      error: null,
      loading: false,
      data: {},
    setState({
  const resetState = () => {
  
  };
    }));
      isDirty: true
      data: { ...prev.data, [fieldName]: value },
      ...prev,
    setState(prev => ({
  const updateData = (fieldName: string, value: any) => {
  
  });
    errors: {}
    isDirty: false,
    error: null,
    loading: true,
    data: {},
  const [state, setState] = useState<PageState>({
function usePageState(model: PageModel, entityId?: string) {

}
  errors: ValidationErrors;           // Validation errors
  isDirty: boolean;                   // Has unsaved changes
  error: string | null;               // Error message
  loading: boolean;                   // Loading state
  data: any;                          // Entity data
interface PageState {
```typescript

## State Management

```
export default ModelDrivenPage;

}
  );
    </Box>
      />
        errors={errors}
        actions={actions}
        onChange={handleFieldChange}
        data={data}
        model={model.container}
      <PageContainer
      
      </Typography>
        {t(model.i18n.titleKey, { defaultValue: model.label })}
        {model.icon && <Icon name={model.icon.name} />}
      <Typography variant="h4" gutterBottom>
    <Box>
  return (
  
  }
    );
      </Dialog>
        </DialogContent>
          />
            errors={errors}
            actions={actions}
            onChange={handleFieldChange}
            data={data}
            model={model.container}
          <PageContainer
        <DialogContent>
        </DialogTitle>
          {t(model.i18n.titleKey, { defaultValue: model.label })}
          {model.icon && <Icon name={model.icon.name} />}
        <DialogTitle>
      <Dialog open maxWidth={model.dialogSize} fullWidth>
    return (
  if (model.openInDialog) {
  // Dialog or full page
  
  }
    return <Alert severity="error">{error}</Alert>;
  if (error) {
  
  }
    return <CircularProgress />;
  if (loading) {
  // Render
  
  }, [model.actions, data, entityId]);
    return actionMap;
    
    });
      };
        return result;
        
        }
          onClose();
        if (result.success && result.shouldClose && onClose) {
        
        }
          await fetchData();
        if (result.success && result.shouldRefresh) {
        
        });
          navigate: useNavigate()
          service: serviceImpl,
          formData: data,
          entityData: data,
          entityId,
        const result = await executeAction(action, {
      actionMap[action.name] = async (...args: any[]) => {
    model.actions.forEach(action => {
    
    const actionMap: Record<string, Function> = {};
  const actions = useMemo(() => {
  // Action handlers
  
  }, [data, validateField]);
    }
      validateField(fieldName, { ...data, [fieldName]: value });
    if (model.container.validationRules) {
    setData(prev => ({ ...prev, [fieldName]: value }));
  const handleFieldChange = useCallback((fieldName: string, value: any) => {
  // Field change handler
  
  };
    }
      setLoading(false);
    } finally {
      setError(err.message);
    } catch (err) {
      setData(result);
      const result = await serviceImpl.getById(entityId);
      setLoading(true);
    try {
  const fetchData = async () => {
  
  }, [entityId]);
    }
      setLoading(false);
    } else {
      fetchData();
    if (entityId && !initialData) {
  useEffect(() => {
  // Fetch initial data
  
  const { t } = useTranslation();
  const { errors, validateForm, validateField } = useValidation(model.container.validationRules);
  const { execute: executeAction } = useActionExecutor();
  // Hooks
  
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [data, setData] = useState(initialData || {});
  // State management
}: ModelDrivenPageProps) {
  onClose 
  initialData,
  entityId, 
  serviceImpl, 
  model, 
function ModelDrivenPage({ 

}
  onClose?: () => void;
  initialData?: any;
  entityId?: string;
  serviceImpl: any;
  model: PageModel;
interface ModelDrivenPageProps {
```typescript

## Component Interface

ModelDrivenPage is the top-level component that renders complete pages from PageModel configuration, managing data fetching, state, actions, and validation.

## Overview

**Blocks:** Page rendering implementation  
**Dependencies:** `runtime-model/02-page-model.md`, all runtime models  
**Assigned:** AI Agent  
**Status:** Complete  
**Domain:** Components  


