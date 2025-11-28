# Customization System Specification

**Domain:** Components  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** All component specifications  
**Blocks:** Component customization  

## Overview

Customization System allows developers to override default components, inject custom logic, and extend the runtime without modifying generated code.

## Custom Component Registry

```typescript
interface CustomComponent {
  type: 'page' | 'container' | 'element' | 'action';
  name: string;
  component: React.ComponentType<any>;
}

class CustomizationRegistry {
  private customs: Map<string, CustomComponent> = new Map();
  
  registerPage(pageName: string, component: React.ComponentType<any>) {
    this.customs.set(`page:${pageName}`, {
      type: 'page',
      name: pageName,
      component
    });
  }
  
  registerElement(elementType: string, component: React.ComponentType<any>) {
    this.customs.set(`element:${elementType}`, {
      type: 'element',
      name: elementType,
      component
    });
  }
  
  getCustomPage(pageName: string): React.ComponentType<any> | undefined {
    return this.customs.get(`page:${pageName}`)?.component;
  }
  
  getCustomElement(elementType: string): React.ComponentType<any> | undefined {
    return this.customs.get(`element:${elementType}`)?.component;
  }
}

export const customizationRegistry = new CustomizationRegistry();
```

## Lifecycle Hooks

```typescript
interface PageHooks {
  onLoad?: (data: any) => void;
  onSave?: (data: any) => Promise<void>;
  beforeAction?: (action: ActionModel) => boolean;
  afterAction?: (action: ActionModel, result: ActionResult) => void;
  onError?: (error: Error) => void;
}

function usePage Hooks(hooks?: PageHooks) {
  const handleLoad = useCallback((data: any) => {
    hooks?.onLoad?.(data);
  }, [hooks]);
  
  const handleSave = useCallback(async (data: any) => {
    if (hooks?.onSave) {
      await hooks.onSave(data);
    }
  }, [hooks]);
  
  const handleBeforeAction = useCallback((action: ActionModel) => {
    return hooks?.beforeAction?.(action) !== false;
  }, [hooks]);
  
  const handleAfterAction = useCallback((action: ActionModel, result: ActionResult) => {
    hooks?.afterAction?.(action, result);
  }, [hooks]);
  
  return {
    handleLoad,
    handleSave,
    handleBeforeAction,
    handleAfterAction
  };
}
```

## Field Interceptors

```typescript
interface FieldInterceptor {
  fieldName: string;
  onBeforeChange?: (value: any, currentData: any) => any;
  onAfterChange?: (value: any, newData: any) => void;
  validate?: (value: any, data: any) => string | null;
}

function useFieldInterceptors(interceptors: FieldInterceptor[]) {
  const interceptorMap = useMemo(() => {
    const map = new Map<string, FieldInterceptor>();
    interceptors.forEach(i => map.set(i.fieldName, i));
    return map;
  }, [interceptors]);
  
  const interceptChange = useCallback((
    fieldName: string,
    value: any,
    currentData: any
  ) => {
    const interceptor = interceptorMap.get(fieldName);
    if (interceptor?.onBeforeChange) {
      return interceptor.onBeforeChange(value, currentData);
    }
    return value;
  }, [interceptorMap]);
  
  return { interceptChange };
}
```

## Custom Renderers

```typescript
interface CustomRenderer {
  condition: (element: VisualElementModel) => boolean;
  render: (element: VisualElementModel, data: any) => React.ReactNode;
}

function useCustomRenderers(renderers: CustomRenderer[]) {
  const findRenderer = useCallback((element: VisualElementModel) => {
    return renderers.find(r => r.condition(element));
  }, [renderers]);
  
  return { findRenderer };
}
```

## Examples

### Example 1: Custom Page Component

```typescript
// Custom implementation
function CustomUserListPage(props: any) {
  return (
    <Box>
      <CustomHeader />
      <ModelDrivenPage {...props} />
      <CustomFooter />
    </Box>
  );
}

// Register
customizationRegistry.registerPage('UserListPage', CustomUserListPage);

// Usage (automatic)
<ModelDrivenPage model={UserListPageModel} />
// Will use CustomUserListPage if registered
```

### Example 2: Custom Element

```typescript
// Custom email input with validation indicator
function CustomEmailInput({ model, value, onChange }: Props) {
  const [isValid, setIsValid] = useState(false);
  
  useEffect(() => {
    setIsValid(validateEmail(value));
  }, [value]);
  
  return (
    <Box>
      <TextField
        value={value}
        onChange={(e) => onChange(e.target.value)}
        InputProps={{
          endAdornment: isValid && <CheckCircleIcon color="success" />
        }}
      />
    </Box>
  );
}

// Register for specific field
customizationRegistry.registerElement('emailInput', CustomEmailInput);
```

### Example 3: Page Hooks

```typescript
const userFormHooks: PageHooks = {
  onLoad: (data) => {
    console.log('User loaded:', data);
    trackPageView('UserForm', data.id);
  },
  
  beforeAction: (action) => {
    if (action.type === 'delete') {
      // Additional confirmation
      return window.confirm('Really delete?');
    }
    return true;
  },
  
  afterAction: (action, result) => {
    if (result.success) {
      trackAction(action.name);
    }
  }
};

<ModelDrivenPage model={model} hooks={userFormHooks} />
```

### Example 4: Field Interceptors

```typescript
const fieldInterceptors: FieldInterceptor[] = [
  {
    fieldName: 'email',
    onBeforeChange: (value) => value.toLowerCase(),
    validate: (value) => {
      return validateEmail(value) ? null : 'Invalid email';
    }
  },
  {
    fieldName: 'phone',
    onBeforeChange: (value) => formatPhoneNumber(value)
  }
];

<ModelDrivenPage 
  model={model} 
  fieldInterceptors={fieldInterceptors}
/>
```

### Example 5: Theme Customization

```typescript
const customTheme = createTheme({
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 8
        }
      }
    }
  }
});

function App() {
  return (
    <ThemeProvider theme={customTheme}>
      <ModelDrivenApp />
    </ThemeProvider>
  );
}
```

## Testing Criteria

### Unit Tests
- [x] Custom components register
- [x] Hooks execute correctly
- [x] Interceptors modify values
- [x] Renderers apply

### Integration Tests
- [x] Custom page renders
- [x] Custom element renders
- [x] Hooks integration
- [x] Theme overrides work

---

**Status:** ✅ Complete  
**Created:** 2025-11-28

