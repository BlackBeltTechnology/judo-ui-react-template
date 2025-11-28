# Model Parser Specification

**Domain:** Components  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** All runtime model specifications  
**Blocks:** Model loading and parsing  

## Overview

Model Parser loads and parses runtime models from generated files, providing utilities for model access and caching.

## Model Loader

```typescript
class ModelLoader {
  private cache: Map<string, any> = new Map();
  
  async loadPage(pageName: string): Promise<PageModel> {
    const cacheKey = `page:${pageName}`;
    
    if (this.cache.has(cacheKey)) {
      return this.cache.get(cacheKey);
    }
    
    try {
      const module = await import(`~/models/pages/${pageName}.model`);
      const model = module[`${pageName}Model`];
      this.cache.set(cacheKey, model);
      return model;
    } catch (err) {
      throw new Error(`Failed to load page model: ${pageName}`);
    }
  }
  
  async loadEnum(enumName: string): Promise<EnumModel> {
    const cacheKey = `enum:${enumName}`;
    
    if (this.cache.has(cacheKey)) {
      return this.cache.get(cacheKey);
    }
    
    try {
      const module = await import(`~/models/enums/${enumName}.model`);
      const model = module[`${enumName}Model`];
      this.cache.set(cacheKey, model);
      return model;
    } catch (err) {
      throw new Error(`Failed to load enum model: ${enumName}`);
    }
  }
  
  clearCache() {
    this.cache.clear();
  }
}

export const modelLoader = new ModelLoader();
```

## Model Parser Hook

```typescript
function useModelLoader() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  
  const loadPage = useCallback(async (pageName: string) => {
    setLoading(true);
    setError(null);
    
    try {
      const model = await modelLoader.loadPage(pageName);
      return model;
    } catch (err) {
      setError(err.message);
      return null;
    } finally {
      setLoading(false);
    }
  }, []);
  
  return { loadPage, loading, error };
}
```

## Model Validator

```typescript
function validatePageModel(model: PageModel): ModelValidationResult {
  const errors: string[] = [];
  
  if (!model.name) errors.push('Page name is required');
  if (!model.type) errors.push('Page type is required');
  if (!model.container) errors.push('Page container is required');
  
  if (model.type !== model.container.type) {
    errors.push('Page type must match container type');
  }
  
  return {
    valid: errors.length === 0,
    errors
  };
}
```

## Model Registry

```typescript
class ModelRegistry {
  private pages: Map<string, PageModel> = new Map();
  private enums: Map<string, EnumModel> = new Map();
  
  registerPage(name: string, model: PageModel) {
    this.pages.set(name, model);
  }
  
  getPage(name: string): PageModel | undefined {
    return this.pages.get(name);
  }
  
  getAllPages(): PageModel[] {
    return Array.from(this.pages.values());
  }
  
  registerEnum(name: string, model: EnumModel) {
    this.enums.set(name, model);
  }
  
  getEnum(name: string): EnumModel | undefined {
    return this.enums.get(name);
  }
}

export const modelRegistry = new ModelRegistry();
```

## Examples

### Example 1: Load Page Dynamically

```typescript
function DynamicPage({ pageName }: Props) {
  const { loadPage, loading, error } = useModelLoader();
  const [model, setModel] = useState<PageModel | null>(null);
  
  useEffect(() => {
    loadPage(pageName).then(setModel);
  }, [pageName]);
  
  if (loading) return <Loading />;
  if (error) return <Error message={error} />;
  if (!model) return null;
  
  return <ModelDrivenPage model={model} />;
}
```

### Example 2: Pre-register Models

```typescript
// App initialization
import { UserListPageModel } from '~/models/pages/UserListPage.model';
import { UserFormPageModel } from '~/models/pages/UserFormPage.model';

modelRegistry.registerPage('UserListPage', UserListPageModel);
modelRegistry.registerPage('UserFormPage', UserFormPageModel);

// Later usage
const model = modelRegistry.getPage('UserListPage');
```

### Example 3: Validate Model

```typescript
const model = await modelLoader.loadPage('UserForm');
const validation = validatePageModel(model);

if (!validation.valid) {
  console.error('Model validation errors:', validation.errors);
}
```

## Testing Criteria

### Unit Tests
- [x] Model loads correctly
- [x] Cache works
- [x] Validation detects errors
- [x] Registry stores models

### Integration Tests
- [x] Dynamic model loading
- [x] Error handling
- [x] Model validation
- [x] Cache invalidation

---

**Status:** ✅ Complete  
**Created:** 2025-11-28

