# Visibility Manager Specification

**Domain:** Components  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `components/01-model-driven-page.md`  
**Blocks:** Conditional rendering  

## Overview

Visibility Manager handles conditional rendering of elements based on hiddenBy, enabledBy, and requiredBy attributes.

## Core Hook

```typescript
function useVisibility(element: VisualElementModel, data: any) {
  const isHidden = useMemo(() => {
    if (!element.hiddenBy) return false;
    return Boolean(data[element.hiddenBy]);
  }, [element.hiddenBy, data]);
  
  const isEnabled = useMemo(() => {
    if (!element.enabledBy) return true;
    return Boolean(data[element.enabledBy]);
  }, [element.enabledBy, data]);
  
  const isRequired = useMemo(() => {
    if (element.requiredBy) {
      return Boolean(data[element.requiredBy]);
    }
    return element.required || false;
  }, [element.requiredBy, element.required, data]);
  
  return {
    isHidden,
    isEnabled,
    isRequired,
    isVisible: !isHidden
  };
}
```

## Visibility Provider

```typescript
interface VisibilityContextValue {
  getVisibility: (elementId: string) => {
    isHidden: boolean;
    isEnabled: boolean;
    isRequired: boolean;
  };
}

const VisibilityContext = createContext<VisibilityContextValue | null>(null);

export function VisibilityProvider({ 
  elements, 
  data, 
  children 
}: {
  elements: VisualElementModel[];
  data: any;
  children: React.ReactNode;
}) {
  const visibilityMap = useMemo(() => {
    const map = new Map<string, ReturnType<typeof useVisibility>>();
    
    elements.forEach(element => {
      map.set(element.id, {
        isHidden: element.hiddenBy ? Boolean(data[element.hiddenBy]) : false,
        isEnabled: element.enabledBy ? Boolean(data[element.enabledBy]) : true,
        isRequired: element.requiredBy ? Boolean(data[element.requiredBy]) : element.required || false,
        isVisible: element.hiddenBy ? !Boolean(data[element.hiddenBy]) : true
      });
    });
    
    return map;
  }, [elements, data]);
  
  const getVisibility = useCallback((elementId: string) => {
    return visibilityMap.get(elementId) || {
      isHidden: false,
      isEnabled: true,
      isRequired: false
    };
  }, [visibilityMap]);
  
  return (
    <VisibilityContext.Provider value={{ getVisibility }}>
      {children}
    </VisibilityContext.Provider>
  );
}
```

## Conditional Wrapper

```typescript
interface ConditionalWrapperProps {
  element: VisualElementModel;
  data: any;
  children: React.ReactNode;
}

function ConditionalWrapper({ element, data, children }: ConditionalWrapperProps) {
  const { isHidden } = useVisibility(element, data);
  
  if (isHidden) return null;
  
  return <>{children}</>;
}
```

## Visibility Rules Engine

```typescript
function evaluateVisibilityRules(
  element: VisualElementModel,
  data: any,
  customRules?: Record<string, (data: any) => boolean>
): boolean {
  // Built-in rules
  if (element.hiddenBy && data[element.hiddenBy]) {
    return false;
  }
  
  // Custom rules from annotations
  if (element.annotations?.visibilityRule) {
    const rule = customRules?.[element.annotations.visibilityRule];
    if (rule && !rule(data)) {
      return false;
    }
  }
  
  return true;
}
```

## Examples

### Example 1: Conditional Field

```typescript
const ssnField: InputModel = {
  id: 've-ssn',
  name: 'ssn',
  type: 'textInput',
  attributeName: 'ssn',
  hiddenBy: 'isForeign',  // Hidden when user is foreign
  requiredBy: 'needsSSN'   // Required when needsSSN is true
};

function SSNField({ data }: Props) {
  const { isHidden, isRequired } = useVisibility(ssnField, data);
  
  if (isHidden) return null;
  
  return (
    <TextField
      label="SSN"
      required={isRequired}
      value={data.ssn}
    />
  );
}
```

### Example 2: Conditional Section

```typescript
function ConditionalSection({ section, data }: Props) {
  const visible = evaluateVisibilityRules(section, data);
  
  if (!visible) return null;
  
  return (
    <Box>
      <Typography variant="h6">{section.label}</Typography>
      {section.children.map(child => (
        <ConditionalWrapper key={child.id} element={child} data={data}>
          <VisualElementRenderer element={child} data={data} />
        </ConditionalWrapper>
      ))}
    </Box>
  );
}
```

### Example 3: Dynamic Required

```typescript
const endDateField: InputModel = {
  id: 've-endDate',
  name: 'endDate',
  type: 'dateInput',
  attributeName: 'endDate',
  requiredBy: 'hasEndDate'  // Required only if hasEndDate checkbox is checked
};

const { isRequired } = useVisibility(endDateField, {
  hasEndDate: true
});
// isRequired = true
```

## Testing Criteria

### Unit Tests
- [x] Visibility calculated correctly
- [x] Hidden elements not rendered
- [x] Enabled state works
- [x] Required state dynamic

### Integration Tests
- [x] Conditional rendering works
- [x] Data changes update visibility
- [x] Provider wraps children
- [x] Custom rules work

---

**Status:** ✅ Complete  
**Created:** 2025-11-28

