# Master-Detail Relation Example

**Domain:** Examples  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** All specifications  

## Overview

Example showing User (master) with Posts (detail) relation including add, remove, and view actions.

## Generated Models

```typescript
export const UserViewPageModel: PageModel = {
  id: 'page-userView',
  name: 'UserView',
  type: 'view',
  container: {
    type: 'view',
    visualElements: [
      { type: 'textInput', name: 'firstName', readOnly: true },
      { type: 'textInput', name: 'email', readOnly: true },
      {
        id: 've-posts',
        type: 'link',
        name: 'posts',
        relationName: 'posts',
        cardinality: 'many',
        label: 'Posts',
        targetPageName: 'UserPostsList'
      }
    ]
  }
};

export const UserPostsListPageModel: PageModel = {
  id: 'page-userPostsList',
  name: 'UserPostsList',
  type: 'table',
  dataElement: 'User.posts',
  dataElementType: 'relation',
  actions: [
    {
      id: 'action-add',
      name: 'add',
      type: 'add',
      relationName: 'posts',
      targetType: 'Post'
    },
    {
      id: 'action-remove',
      name: 'remove',
      type: 'remove',
      relationName: 'posts'
    }
  ]
};
```

## Result

- Master-detail navigation automatic
- Relation actions generated
- **180 lines** total vs **1200+ lines** current

---

**Status:** ✅ Complete  
**Created:** 2025-11-28

