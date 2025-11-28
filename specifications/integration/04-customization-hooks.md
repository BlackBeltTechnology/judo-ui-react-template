# Customization Hooks Specification

**Domain:** Integration  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `components/12-customization-system.md`  

## Overview

Customization Hooks define extension points where developers can inject custom logic without modifying generated code.

## Application-Level Customization

```typescript
// ~/src/customizations/index.ts
import { customizationRegistry } from '@judo/runtime';
import { CustomUserList } from './CustomUserList';
import { customEmailValidator } from './validators';

export function registerCustomizations() {
  // Custom page component
  customizationRegistry.registerPage('UserList', CustomUserList);
  
  // Custom element
  customizationRegistry.registerElement('emailInput', CustomEmailInput);
  
  // Custom validator
  registerValidator('customEmail', customEmailValidator);
}
```

## Page-Level Hooks

```typescript
const userFormHooks: PageHooks = {
  onLoad: (data) => {
    console.log('User loaded:', data);
  },
  beforeAction: (action) => {
    if (action.type === 'delete') {
      return confirm('Really delete?');
    }
    return true;
  }
};

<ModelDrivenPage model={UserFormPageModel} hooks={userFormHooks} />
```

---

**Status:** ✅ Complete  
**Created:** 2025-11-28

