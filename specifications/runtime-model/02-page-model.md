# PageModel Specification

**Domain:** Runtime Model  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `01-core-types.md`, `metamodel/05-page-definition.md`  
**Blocks:** Page component implementation, page generation  

## Overview

`PageModel` is the runtime representation of a PageDefinition from the metamodel. It contains all configuration needed to render a complete page including container structure, actions, navigation settings, and i18n keys.

## TypeScript Interface

```typescript
interface PageModel extends BaseModel {
  // Identification
  id: string;                           // Page identifier
  name: string;                         // Page name
  fqn: string;                          // Fully qualified name
  sourceId?: string;                    // Original XMIID
  
  // Type & Classification
  type: PageContainerType;              // 'form' | 'view' | 'table'
  isSelector: boolean;                  // Whether this is a selector page
  isRelationSelector: boolean;          // Whether selecting related entities
  isDashboard: boolean;                 // Whether this is a dashboard
  
  // Display Properties
  label?: string;                       // Page title
  icon?: IconModel;                     // Page icon
  
  // Data Binding
  dataElement?: string;                 // ClassType or RelationType FQN
  dataElementType?: 'class' | 'relation'; // Type of data element
  
  // Dialog Configuration
  openInDialog: boolean;                // Whether page opens in dialog
  dialogSize?: DialogSize;              // Dialog size if applicable
  
  // Container
  container: ContainerModel;            // Page content structure
  
  // Actions
  actions: ActionModel[];               // Available actions
  
  // Navigation
  routePath?: string;                   // Generated route path
  parentPage?: string;                  // Parent page reference
  
  // Customization
  generateActionsHook: boolean;         // Whether to generate actions hook
  customComponent?: string;             // Custom component override
  
  // Internationalization
  i18n: {
    keyPrefix: string;                  // I18n key prefix
    titleKey: string;                   // Page title i18n key
    keys: Record<string, string>;       // Additional i18n keys
  };
  
  // Metadata
  annotations?: Record<string, string>;
}
```

## Properties Detail

### Type Classification

**Form Page:**
```typescript
{
  type: 'form',
  openInDialog: true,
  dialogSize: 'md',
  container: {
    type: 'form',
    visualElements: [/* input fields */]
  }
}
```

**View Page:**
```typescript
{
  type: 'view',
  openInDialog: false,
  container: {
    type: 'view',
    visualElements: [/* mixed content */]
  }
}
```

**Table Page:**
```typescript
{
  type: 'table',
  container: {
    type: 'table',
    visualElements: [/* single table */]
  }
}
```

### Selector Configuration

```typescript
{
  isSelector: true,
  isRelationSelector: true,
  dataElement: 'User#posts',
  dataElementType: 'relation'
}
```

### Route Path Generation

Paths are generated based on page type:

```typescript
// Regular page
routePath: '/pages/UserView/:id'

// Selector page
routePath: '/selectors/UserSelector'

// Relation page
routePath: '/pages/User/:ownerId/posts'

// Dashboard
routePath: '/dashboard'
```

## Examples

### Example 1: Simple Form Page

```typescript
const CreateUserPageModel: PageModel = {
  id: 'page-create-user',
  name: 'CreateUser',
  fqn: 'app.pages.CreateUser',
  sourceId: '_page_001',
  
  type: 'form',
  isSelector: false,
  isRelationSelector: false,
  isDashboard: false,
  
  label: 'Create User',
  icon: {
    name: 'person_add',
    color: 'primary'
  },
  
  dataElement: 'User',
  dataElementType: 'class',
  
  openInDialog: true,
  dialogSize: 'md',
  
  container: {
    id: 'container-create-user',
    name: 'container',
    type: 'form',
    dataElement: 'User',
    visualElements: [
      {
        id: 've-firstName',
        type: 'textInput',
        name: 'firstName',
        attributeName: 'firstName',
        col: 6,
        required: true
      },
      {
        id: 've-lastName',
        type: 'textInput',
        name: 'lastName',
        attributeName: 'lastName',
        col: 6,
        required: true
      },
      {
        id: 've-email',
        type: 'textInput',
        name: 'email',
        attributeName: 'email',
        col: 12,
        required: true
      }
    ],
    actionButtonGroups: [
      {
        id: 'form-actions',
        name: 'formActions',
        buttons: [
          {
            id: 'btn-create',
            name: 'create',
            label: 'Create',
            actionName: 'create',
            buttonStyle: 'contained'
          },
          {
            id: 'btn-cancel',
            name: 'cancel',
            label: 'Cancel',
            actionName: 'cancel',
            buttonStyle: 'text'
          }
        ],
        featuredActions: 2
      }
    ]
  },
  
  actions: [
    {
      id: 'action-create',
      name: 'create',
      type: 'create',
      targetType: 'User'
    },
    {
      id: 'action-cancel',
      name: 'cancel',
      type: 'close'
    }
  ],
  
  routePath: '/pages/CreateUser',
  generateActionsHook: false,
  
  i18n: {
    keyPrefix: 'judo.pages.CreateUser',
    titleKey: 'judo.pages.CreateUser.title',
    keys: {
      title: 'Create User',
      create: 'Create',
      cancel: 'Cancel'
    }
  }
};
```

### Example 2: View Page (Master-Detail)

```typescript
const UserViewPageModel: PageModel = {
  id: 'page-user-view',
  name: 'UserView',
  fqn: 'app.pages.UserView',
  
  type: 'view',
  isSelector: false,
  isRelationSelector: false,
  isDashboard: false,
  
  label: 'User Details',
  icon: { name: 'person' },
  
  dataElement: 'User',
  dataElementType: 'class',
  
  openInDialog: false,
  
  container: {
    id: 'container-user-view',
    name: 'container',
    type: 'view',
    dataElement: 'User',
    visualElements: [
      // User info section
      {
        id: 've-info-section',
        type: 'flex',
        name: 'infoSection',
        direction: 'horizontal',
        col: 12,
        children: [
          {
            id: 've-firstName',
            type: 'textInput',
            name: 'firstName',
            col: 6
          },
          {
            id: 've-lastName',
            type: 'textInput',
            name: 'lastName',
            col: 6
          }
        ]
      },
      // Posts table
      {
        id: 've-posts-table',
        type: 'table',
        name: 'posts',
        dataElement: 'User#posts',
        col: 12,
        columns: [/* ... */]
      }
    ],
    actionButtonGroups: [
      {
        id: 'page-actions',
        name: 'pageActions',
        buttons: [
          { name: 'refresh', actionName: 'refresh' },
          { name: 'update', actionName: 'update' },
          { name: 'delete', actionName: 'delete' }
        ],
        featuredActions: 3
      }
    ]
  },
  
  actions: [
    { name: 'refresh', type: 'refresh' },
    { name: 'update', type: 'update' },
    { name: 'delete', type: 'delete' }
  ],
  
  routePath: '/pages/UserView/:id',
  generateActionsHook: true,
  
  i18n: {
    keyPrefix: 'judo.pages.UserView',
    titleKey: 'judo.pages.UserView.title',
    keys: {}
  }
};
```

### Example 3: Table Page

```typescript
const UserListPageModel: PageModel = {
  id: 'page-user-list',
  name: 'UserList',
  fqn: 'app.pages.UserList',
  
  type: 'table',
  isSelector: false,
  isDashboard: false,
  
  label: 'Users',
  dataElement: 'User',
  dataElementType: 'class',
  
  openInDialog: false,
  
  container: {
    id: 'container-user-list',
    type: 'table',
    dataElement: 'User',
    visualElements: [
      {
        id: 've-user-table',
        type: 'table',
        name: 'userTable',
        dataElement: 'User',
        col: 12,
        columns: [
          { name: 'firstName', attributeName: 'firstName' },
          { name: 'lastName', attributeName: 'lastName' },
          { name: 'email', attributeName: 'email' }
        ],
        rowActions: [
          { actionName: 'view' },
          { actionName: 'edit' },
          { actionName: 'delete' }
        ],
        enableFiltering: true,
        enableSorting: true,
        enablePagination: true
      }
    ],
    actionButtonGroups: [
      {
        id: 'table-actions',
        buttons: [
          { name: 'refresh', actionName: 'refresh' },
          { name: 'create', actionName: 'create' }
        ],
        featuredActions: 2
      }
    ]
  },
  
  actions: [
    { name: 'refresh', type: 'refresh' },
    { name: 'create', type: 'create' },
    { name: 'view', type: 'openPage' },
    { name: 'edit', type: 'openForm' },
    { name: 'delete', type: 'delete' }
  ],
  
  routePath: '/pages/UserList',
  
  i18n: {
    keyPrefix: 'judo.pages.UserList',
    titleKey: 'judo.pages.UserList.title',
    keys: {}
  }
};
```

### Example 4: Selector Page

```typescript
const UserSelectorPageModel: PageModel = {
  id: 'page-user-selector',
  name: 'UserSelector',
  fqn: 'app.selectors.UserSelector',
  
  type: 'table',
  isSelector: true,
  isRelationSelector: false,
  isDashboard: false,
  
  label: 'Select User',
  dataElement: 'User',
  dataElementType: 'class',
  
  openInDialog: true,
  dialogSize: 'lg',
  
  container: {
    id: 'container-user-selector',
    type: 'table',
    dataElement: 'User',
    visualElements: [
      {
        id: 've-selector-table',
        type: 'table',
        name: 'userTable',
        enableSelection: true,
        columns: [/* ... */]
      }
    ]
  },
  
  actions: [
    { name: 'select', type: 'openSelector' }
  ],
  
  routePath: '/selectors/UserSelector',
  
  i18n: {
    keyPrefix: 'judo.selectors.UserSelector',
    titleKey: 'judo.selectors.UserSelector.title',
    keys: {}
  }
};
```

### Example 5: Dashboard Page

```typescript
const DashboardPageModel: PageModel = {
  id: 'page-dashboard',
  name: 'Dashboard',
  fqn: 'app.pages.Dashboard',
  
  type: 'view',
  isDashboard: true,
  isSelector: false,
  
  label: 'Dashboard',
  icon: { name: 'dashboard' },
  
  // No dataElement for dashboards
  
  openInDialog: false,
  
  container: {
    id: 'container-dashboard',
    type: 'view',
    visualElements: [
      // Custom dashboard widgets
      {
        id: 've-stats',
        type: 'card',
        name: 'stats',
        col: 12
      }
    ]
  },
  
  actions: [],
  
  routePath: '/dashboard',
  customComponent: 'CustomDashboard',
  
  i18n: {
    keyPrefix: 'judo.pages.Dashboard',
    titleKey: 'judo.pages.Dashboard.title',
    keys: {}
  }
};
```

## Generator Output

Pages are exported as constants from model files:

```typescript
// Generated: ~/models/pages/UserFormPage.model.ts
import type { PageModel } from '../types';

export const UserFormPageModel: PageModel = {
  // ... model definition
};
```

## Component Usage

```typescript
import { ModelDrivenPage } from '@judo/runtime';
import { UserFormPageModel } from '~/models/pages/UserFormPage.model';
import { UserService } from '~/services/UserService';

export function UserFormPage() {
  return (
    <ModelDrivenPage
      model={UserFormPageModel}
      serviceImpl={UserService}
    />
  );
}
```

## Validation Rules

- `name` must be unique within application
- `type` must match container.type
- If `openInDialog`, must have `dialogSize`
- If `isSelector`, container should be table type
- If `isDashboard`, dataElement should be null
- `routePath` must be valid route pattern
- `i18n.keyPrefix` must follow pattern: `judo.pages.{PageName}`

## Related Specifications

**Metamodel:**
- `metamodel/05-page-definition.md` - Source specification

**Runtime Model:**
- `01-core-types.md` - Base types
- `03-container-model.md` - Container structure
- `05-action-model.md` - Actions

**Components:**
- `components/01-model-driven-page.md` - Page component

**Generators:**
- `generators/02-page-model-generator.md` - Generation logic

---

**Status:** ✅ Complete  
**Created:** 2025-11-28  
**Last Updated:** 2025-11-28  
**Reviewed:** Not yet reviewed

