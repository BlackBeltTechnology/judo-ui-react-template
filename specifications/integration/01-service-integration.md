# Service Integration Specification
**Created:** 2025-11-28
**Status:** ✅ Complete  

---

```
}
  return params.toString();
  
  });
    }
      params.append(key, String(value));
    if (!key.startsWith('_')) {
  Object.entries(query).forEach(([key, value]) => {
  // Filters
  
  }
    });
      params.append('sort', `${sort.attribute},${direction}`);
      const direction = sort.descending ? 'desc' : 'asc';
    query._orderBy.forEach(sort => {
  if (query._orderBy) {
  // Sorting
  
  }
    params.append('limit', String(query._seek.limit || 10));
    params.append('page', String(query._seek.page || 0));
  if (query._seek) {
  // Pagination
  
  const params = new URLSearchParams();
  
  if (!query) return '';
function buildQueryParams(query?: QueryCustomizer): string {
```typescript

## Query Customizer

```
}
  }
    return response.json();
    });
      body: JSON.stringify(params)
      headers: { 'Content-Type': 'application/json' },
      method: 'POST',
    const response = await fetch(url, {
    
      : `${this.baseUrl}/${operationName}`;
      ? `${this.baseUrl}/${id}/${operationName}`
    const url = id 
  async callOperation(operationName: string, id?: string, params?: any): Promise<any> {
  
  }
    });
      method: 'POST'
    await fetch(`${this.baseUrl}/${ownerId}/${relationName}/${targetId}`, {
  async addToRelation(ownerId: string, relationName: string, targetId: string): Promise<void> {
  
  }
    });
      method: 'DELETE'
    await fetch(`${this.baseUrl}/${id}`, {
  async delete(id: string): Promise<void> {
  
  }
    return response.json();
    });
      body: JSON.stringify(data)
      headers: { 'Content-Type': 'application/json' },
      method: 'PUT',
    const response = await fetch(`${this.baseUrl}/${id}`, {
  async update(id: string, data: Partial<User>): Promise<User> {
  
  }
    return response.json();
    });
      body: JSON.stringify(data)
      headers: { 'Content-Type': 'application/json' },
      method: 'POST',
    const response = await fetch(this.baseUrl, {
  async create(data: Partial<User>): Promise<User> {
  
  }
    return response.json();
    const response = await fetch(`${this.baseUrl}/${id}`);
  async getById(id: string): Promise<User> {
  
  }
    return response.json();
    const response = await fetch(`${this.baseUrl}?${params}`);
    const params = buildQueryParams(query);
  async list(query?: QueryCustomizer): Promise<PagedResult<User>> {
  
  private readonly baseUrl = '/api/users';
export class UserService implements BaseService<User> {
```typescript

## Service Implementation Example

```
}
  callOperation(operationName: string, id?: string, params?: any): Promise<any>;
  // Operations
  
  unsetRelation(ownerId: string, relationName: string): Promise<void>;
  setRelation(ownerId: string, relationName: string, targetId: string): Promise<void>;
  removeFromRelation(ownerId: string, relationName: string, targetId: string): Promise<void>;
  addToRelation(ownerId: string, relationName: string, targetId: string): Promise<void>;
  // Relation operations
  
  getForUpdate(id: string): Promise<T>;
  // For update operations
  
  delete(id: string): Promise<void>;
  update(id: string, data: Partial<T>): Promise<T>;
  create(data: Partial<T>): Promise<T>;
  getById(id: string): Promise<T>;
  list(query?: QueryCustomizer): Promise<PagedResult<T>>;
  // CRUD operations
interface BaseService<T> {
```typescript

## Service Interface

Service Integration defines how the runtime connects to backend services, handles API calls, and manages data flow between the UI and REST endpoints.

## Overview

**Dependencies:** All component and generator specifications  
**Assigned:** AI Agent  
**Status:** Complete  
**Domain:** Integration  


