# Complete User CRUD Example
**Created:** 2025-11-28
**Status:** ✅ Complete  

---

The generated code is **15-20 lines** compared to **1000+ lines** in current generator!

## Usage

```
}
  );
    />
      serviceImpl={UserService}
      model={UserListPageModel}
    <ModelDrivenPage
  return (
export function UserList() {

import { UserService } from '~/services/UserService';
import { UserListPageModel } from '~/models/pages/UserList.model';
import { ModelDrivenPage } from '@judo/runtime';
import React from 'react';
```typescript

## Generated Component

```
};
  }
    titleKey: 'judo.pages.UserList.title'
    keyPrefix: 'judo.pages.UserList',
  i18n: {
  routePath: '/pages/UserList',
  ],
    { id: 'action-create', name: 'create', type: 'create', targetType: 'User' }
    { id: 'action-refresh', name: 'refresh', type: 'refresh' },
  actions: [
  container: UserListContainerModel,
  dataElement: 'User',
  type: 'table',
  name: 'UserList',
  id: 'page-userList',
export const UserListPageModel: PageModel = {
```typescript

## Generated Page Model

```
</PageDefinition>
  <actions xsi:type="ui:CreateAction" name="create" targetType="User" targetPageName="UserForm" />
  <actions xsi:type="ui:RefreshAction" name="refresh" />
  </container>
    </actionButtonGroups>
      <buttons actionDefinition="//@actions.1" />  <!-- create -->
      <buttons actionDefinition="//@actions.0" />  <!-- refresh -->
    <actionButtonGroups>
    </table>
      </columns>
        <column name="status" label="Status" sortable="true" />
        <column name="email" label="Email" sortable="true" />
        <column name="lastName" label="Last Name" sortable="true" />
        <column name="firstName" label="First Name" sortable="true" />
      <columns>
    <table name="users">
  <container xsi:type="ui:TablePageContainer">
<PageDefinition name="UserList" openInDialog="false">
```xml

## Metamodel (ui.ecore)

Complete end-to-end example showing User entity with full CRUD operations, including list, view, create, edit, and delete.

## Overview

**Dependencies:** All specifications  
**Assigned:** AI Agent  
**Status:** Complete  
**Domain:** Examples  


