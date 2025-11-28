# ContainerModel Specification

**Domain:** Runtime Model  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `01-core-types.md`, `metamodel/06-page-container.md`, `metamodel/07-container.md`  
**Blocks:** Container component, visual element rendering  

## Overview

`ContainerModel` represents container elements that group and organize visual elements. It includes PageContainer (top-level page content) and nested containers (Flex, TabController).

## TypeScript Interfaces

### Base ContainerModel

```typescript
interface ContainerModel extends BaseModel {
  // Identification
  id: string;
  name: string;
  type: 'form' | 'view' | 'table' | 'flex' | 'tabController' | 'card';
  
  // Data Binding (for PageContainer)
  dataElement?: string;                 // ClassType or RelationType FQN
  
  // Layout
  layout?: LayoutModel;
  
  // Children
  visualElements: VisualElementModel[];
  
  // Actions (for PageContainer)
  actionButtonGroups?: ButtonGroupModel[];
  
  // Grid positioning (for nested containers)
  col?: number;
  row?: number;
  
  // Conditional rendering
  hiddenBy?: string;
  enabledBy?: string;
  
  // Sizing
  size?: SizeModel;
  stretch?: StretchType;
  fit?: FitType;
  
  // Styling
  subTheme?: string;
  
  // Metadata
  sourceId?: string;
  annotations?: Record<string, string>;
}
```

### LayoutModel

```typescript
interface LayoutModel {
  direction?: FlexDirection;            // 'horizontal' | 'vertical'
  mainAxisAlignment?: MainAxisAlignment;
  crossAxisAlignment?: CrossAxisAlignment;
  wrap?: 'nowrap' | 'wrap' | 'wrap-reverse';
  spacing?: number;                     // Gap between children
}
```

### PageContainerModel

```typescript
interface PageContainerModel extends ContainerModel {
  type: 'form' | 'view' | 'table';
  dataElement: string;                  // Required for PageContainer
  actionButtonGroups: ButtonGroupModel[];
  validationRules?: ValidationRuleModel[];
}
```

### FlexModel

```typescript
interface FlexModel extends ContainerModel {
  type: 'flex';
  direction: FlexDirection;
  mainAxisAlignment: MainAxisAlignment;
  crossAxisAlignment: CrossAxisAlignment;
  wrap: 'nowrap' | 'wrap' | 'wrap-reverse';
  spacing: number;
  children: VisualElementModel[];       // Alias for visualElements
}
```

### TabControllerModel

```typescript
interface TabControllerModel extends ContainerModel {
  type: 'tabController';
  tabs: TabModel[];
  defaultTab: number;
  tabPosition: 'top' | 'bottom' | 'left' | 'right';
}

interface TabModel {
  id: string;
  name: string;
  label: string;
  icon?: IconModel;
  children: VisualElementModel[];
  disabled: boolean;
  hiddenBy?: string;
}
```

## Examples

### Example 1: PageContainer (Form)

```typescript
const formContainer: PageContainerModel = {
  id: 'container-user-form',
  name: 'container',
  type: 'form',
  dataElement: 'User',
  
  layout: {
    direction: 'vertical',
    spacing: 3
  },
  
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
    }
  ],
  
  actionButtonGroups: [
    {
      id: 'form-actions',
      name: 'formActions',
      buttons: [
        {
          id: 'btn-save',
          name: 'save',
          label: 'Save',
          actionName: 'update',
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
  ],
  
  validationRules: [
    {
      attributeName: 'email',
      rules: [
        { type: 'required', message: 'Email is required' },
        { type: 'pattern', value: '^[^@]+@[^@]+\\.[^@]+$', message: 'Invalid email' }
      ]
    }
  ]
};
```

### Example 2: Flex Container (Horizontal Layout)

```typescript
const flexContainer: FlexModel = {
  id: 'flex-name-row',
  name: 'nameRow',
  type: 'flex',
  direction: 'horizontal',
  mainAxisAlignment: 'start',
  crossAxisAlignment: 'stretch',
  wrap: 'nowrap',
  spacing: 2,
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
};
```

### Example 3: Nested Flex Containers

```typescript
const nestedContainer: FlexModel = {
  id: 'flex-form-layout',
  name: 'formLayout',
  type: 'flex',
  direction: 'vertical',
  spacing: 3,
  col: 12,
  
  children: [
    // Section 1: Personal Info
    {
      id: 'flex-personal',
      type: 'flex',
      name: 'personalInfo',
      direction: 'horizontal',
      spacing: 2,
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
    
    // Section 2: Contact Info
    {
      id: 'flex-contact',
      type: 'flex',
      name: 'contactInfo',
      direction: 'vertical',
      spacing: 2,
      col: 12,
      children: [
        {
          id: 've-email',
          type: 'textInput',
          name: 'email',
          col: 12
        },
        {
          id: 've-phone',
          type: 'textInput',
          name: 'phone',
          col: 12
        }
      ]
    }
  ]
};
```

### Example 4: TabController

```typescript
const tabContainer: TabControllerModel = {
  id: 'tabs-user-details',
  name: 'userTabs',
  type: 'tabController',
  defaultTab: 0,
  tabPosition: 'top',
  col: 12,
  
  tabs: [
    {
      id: 'tab-basic',
      name: 'basic',
      label: 'Basic Information',
      icon: { name: 'person' },
      disabled: false,
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
    {
      id: 'tab-contact',
      name: 'contact',
      label: 'Contact',
      icon: { name: 'email' },
      disabled: false,
      children: [
        {
          id: 've-email',
          type: 'textInput',
          name: 'email',
          col: 12
        }
      ]
    },
    {
      id: 'tab-premium',
      name: 'premium',
      label: 'Premium Features',
      icon: { name: 'star' },
      disabled: false,
      hiddenBy: 'isPremium',
      children: [
        // Premium features
      ]
    }
  ]
};
```

### Example 5: PageContainer (View with Table)

```typescript
const viewContainer: PageContainerModel = {
  id: 'container-user-view',
  name: 'container',
  type: 'view',
  dataElement: 'User',
  
  visualElements: [
    // User info section
    {
      id: 'flex-user-info',
      type: 'flex',
      name: 'userInfo',
      direction: 'horizontal',
      col: 12,
      children: [
        {
          id: 've-firstName',
          type: 'textInput',
          name: 'firstName',
          readOnly: true,
          col: 6
        },
        {
          id: 've-lastName',
          type: 'textInput',
          name: 'lastName',
          readOnly: true,
          col: 6
        }
      ]
    },
    
    // Posts table
    {
      id: 've-posts',
      type: 'table',
      name: 'posts',
      dataElement: 'User#posts',
      col: 12,
      columns: [
        { name: 'title', attributeName: 'title' },
        { name: 'createdAt', attributeName: 'createdAt' }
      ]
    }
  ],
  
  actionButtonGroups: [
    {
      id: 'view-actions',
      name: 'viewActions',
      buttons: [
        { name: 'refresh', actionName: 'refresh' },
        { name: 'update', actionName: 'update' },
        { name: 'delete', actionName: 'delete' }
      ],
      featuredActions: 3
    }
  ]
};
```

## Component Rendering

```typescript
function ModelDrivenContainer({ model, data, actions }: Props) {
  // Handle different container types
  switch (model.type) {
    case 'flex':
      return <FlexContainer model={model as FlexModel} data={data} />;
    
    case 'tabController':
      return <TabControllerComponent model={model as TabControllerModel} data={data} />;
    
    case 'form':
    case 'view':
    case 'table':
      return <PageContainerComponent model={model} data={data} actions={actions} />;
    
    default:
      return <GenericContainer model={model} data={data} />;
  }
}
```

## Validation Rules

- Container must have unique `id` and `name`
- PageContainer must have `dataElement`
- Flex must have valid `direction`
- TabController must have at least one tab
- Children must be valid VisualElementModels
- ActionButtonGroups only on PageContainer

## Related Specifications

**Metamodel:**
- `metamodel/06-page-container.md` - PageContainer source
- `metamodel/07-container.md` - Container base
- `metamodel/08-flex.md` - Flex specification

**Runtime Model:**
- `01-core-types.md` - Base types
- `04-visual-element-model.md` - Children elements
- `06-button-model.md` - Action buttons

**Components:**
- `components/02-model-driven-container.md` - Container rendering

---

**Status:** ✅ Complete  
**Created:** 2025-11-28  
**Last Updated:** 2025-11-28  
**Reviewed:** Not yet reviewed

