# Custom Component Integration Example

**Domain:** Examples  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `components/12-customization-system.md`  

## Overview

Example showing how developers customize generated components with custom logic while keeping generated code intact.

## Custom Page Component

```typescript
// ~/src/customizations/CustomUserList.tsx
import React from 'react';
import { Box, Typography } from '@mui/material';
import { ModelDrivenPage } from '@judo/runtime';
import { UserListPageModel } from '~/models/pages/UserList.model';
import { UserService } from '~/services/UserService';

export function CustomUserList() {
  return (
    <Box>
      {/* Custom header */}
      <Box sx={{ p: 2, bgcolor: 'primary.main', color: 'white' }}>
        <Typography variant="h4">Team Directory</Typography>
        <Typography variant="subtitle1">Manage your team members</Typography>
      </Box>
      
      {/* Standard generated page */}
      <ModelDrivenPage
        model={UserListPageModel}
        serviceImpl={UserService}
        hooks={{
          onLoad: (data) => {
            console.log('Loaded users:', data.length);
          },
          beforeAction: (action) => {
            if (action.type === 'delete') {
              return window.confirm('Remove team member?');
            }
            return true;
          }
        }}
      />
      
      {/* Custom footer */}
      <Box sx={{ p: 2, textAlign: 'center', color: 'text.secondary' }}>
        <Typography variant="caption">
          Total team members: {/* count */}
        </Typography>
      </Box>
    </Box>
  );
}
```

## Registration

```typescript
// ~/src/customizations/index.ts
import { customizationRegistry } from '@judo/runtime';
import { CustomUserList } from './CustomUserList';

export function registerCustomizations() {
  customizationRegistry.registerPage('UserList', CustomUserList);
}
```

## App Setup

```typescript
// ~/src/App.tsx
import { registerCustomizations } from './customizations';

function App() {
  useEffect(() => {
    registerCustomizations();
  }, []);
  
  return <AppRouter />;
}
```

## Result

- Generated code never modified
- Custom UI integrated seamlessly
- Lifecycle hooks for custom logic
- **50 lines** of custom code to customize entire page
- Generated code remains clean and regenerable

---

**Status:** ✅ Complete  
**Created:** 2025-11-28

