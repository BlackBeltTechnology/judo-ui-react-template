# OperationType Specification

**Domain:** Data Model  
**Ecore Class:** `data::OperationType`  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `01-class-type.md`, `08-operation-parameters.md`  
**Blocks:** Actions (call operation), operation input forms  

## Overview

`OperationType` defines business operations/methods on ClassTypes. Operations can be static (class-level) or instance-level, may take parameters, return values, and trigger custom business logic. They are exposed in the UI as action buttons and forms.

## Metamodel Definition

### Class Hierarchy

```
NamedElement
└─ DataElement (abstract)
   └─ OperationType
```

### Properties

| Name | Type | Multiplicity | Default | Description |
|------|------|--------------|---------|-------------|
| `name` | EString | 1 | - | Operation name (e.g., "activate", "calculateTotal") |
| `sourceId` | EString | 0..1 | - | XMIID from source |
| `annotations` | Annotation | 0..* | - | Custom metadata |
| `operationKind` | OperationKind | 1 | INSTANCE | STATIC or INSTANCE |
| `returnType` | ClassType | 0..1 | - | Return type (null for void) |
| `isBound` | EBoolean | 0..1 | true | Whether bound to instance |
| `parameters` | OperationParameter | 0..* | - | Input parameters |

### Relationships

**Contains:**
- `parameters: OperationParameter[]` - Operation inputs

**Referenced By:**
- `ClassType.operations` - Owning class
- `CallOperationAction` - UI actions calling this operation
- `ActionDefinition` - Buttons triggering operation

**References:**
- `returnType: ClassType` - What operation returns
- Owner class (implicit) - Class this operation belongs to

### Enumerations

#### OperationKind
```
STATIC   = 0  // Class-level operation (no instance needed)
INSTANCE = 1  // Instance-level operation (requires entity)
```

## Key Concepts

### Operation Kinds

**Instance Operation:**
```xml
<operations name="activate" 
            operationKind="INSTANCE"
            returnType="User"
            isBound="true"/>
```
- Called on specific entity: `user.activate()`
- Has access to instance data
- Typically modifies the instance

**Static Operation:**
```xml
<operations name="register" 
            operationKind="STATIC"
            returnType="User"
            isBound="false"/>
```
- Called on class: `User.register()`
- No instance context
- Often creates new entities

### Return Types

**Void Operation (no return):**
```xml
<operations name="sendEmail" operationKind="INSTANCE"/>
```

**Returns Entity:**
```xml
<operations name="activate" 
            operationKind="INSTANCE"
            returnType="User"/>
```

**Returns Different Type:**
```xml
<operations name="exportToCSV" 
            operationKind="STATIC"
            returnType="FileDownload"/>
```

### Parameters

Operations can have input parameters (see `08-operation-parameters.md`):
```xml
<operations name="updateStatus" operationKind="INSTANCE">
  <parameters name="newStatus" type="UserStatus" required="true"/>
  <parameters name="reason" type="STRING" required="false"/>
</operations>
```

## Examples from Sample Model

### Example 1: Simple Instance Operation

```xml
<classTypes name="User">
  <operations name="activate" 
              operationKind="INSTANCE"
              returnType="User">
    <annotations key="description" value="Activate user account"/>
  </operations>
</classTypes>
```

**Generated Service Method:**
```typescript
interface UserService {
  activate(userId: string): Promise<User>;
}

// Usage
const activatedUser = await userService.activate(userId);
```

### Example 2: Operation with Parameters

```xml
<operations name="changePassword" operationKind="INSTANCE">
  <parameters name="oldPassword" type="STRING" required="true"/>
  <parameters name="newPassword" type="STRING" required="true"/>
</operations>
```

**Generated Service Method:**
```typescript
interface UserService {
  changePassword(
    userId: string,
    params: { oldPassword: string; newPassword: string }
  ): Promise<void>;
}
```

### Example 3: Static Operation (Factory)

```xml
<operations name="register" 
            operationKind="STATIC"
            returnType="User">
  <parameters name="email" type="STRING" required="true"/>
  <parameters name="password" type="STRING" required="true"/>
  <parameters name="firstName" type="STRING" required="true"/>
  <parameters name="lastName" type="STRING" required="true"/>
</operations>
```

**Generated Service Method:**
```typescript
interface UserService {
  register(params: {
    email: string;
    password: string;
    firstName: string;
    lastName: string;
  }): Promise<User>;
}
```

### Example 4: Complex Return Type

```xml
<classTypes name="Report"/>

<classTypes name="Invoice">
  <operations name="generateReport" 
              operationKind="INSTANCE"
              returnType="Report"/>
</classTypes>
```

### Example 5: Bulk Operation

```xml
<operations name="deactivateAll" 
            operationKind="STATIC">
  <parameters name="userIds" type="STRING" isCollection="true"/>
  <annotations key="bulk" value="true"/>
</operations>
```

### Example 6: Void Operation

```xml
<operations name="sendNotification" 
            operationKind="INSTANCE">
  <parameters name="message" type="STRING" required="true"/>
  <!-- No returnType = void -->
</operations>
```

## Validation Rules

### Required Properties
- `name` - Must be unique within ClassType
- `operationKind` - Must be STATIC or INSTANCE

### Constraints
- Instance operations must have `isBound=true`
- Static operations should have `isBound=false`
- Parameter names must be unique within operation
- Return type must reference valid ClassType
- Void operations have null returnType

### Naming Conventions
- Use camelCase verbs (e.g., `activate`, `sendEmail`)
- Action-oriented names (e.g., `calculate`, `generate`, `update`)
- Static factories: `create`, `register`, `build`
- Instance actions: `activate`, `deactivate`, `approve`, `reject`

## Runtime Model Mapping

```typescript
interface OperationTypeModel {
  name: string;                           // name
  fqn: string;                            // ClassName.operationName
  kind: 'static' | 'instance';            // operationKind
  
  // Return
  returnType?: string;                    // returnType.name
  returnTypeFqn?: string;                 // returnType FQN
  
  // Parameters
  parameters: OperationParameterModel[];  // parameters
  hasParameters: boolean;                 // Convenience flag
  
  // Characteristics
  isBound: boolean;                       // isBound
  isVoid: boolean;                        // returnType === null
  
  // Metadata
  sourceId?: string;
  annotations?: Record<string, string>;
}

// Generated service method signature
type ServiceMethod<T> = 
  | ((id: string, params?: Record<string, any>) => Promise<T | void>)  // Instance
  | ((params?: Record<string, any>) => Promise<T | void>);              // Static
```

## Generator Implementation

See `generators/05-action-extractor.md`.

**Key Methods:**
```java
public class OperationTypeGenerator {
    public static OperationTypeModel extractOperationModel(OperationType op);
    public static String generateServiceMethod(OperationType op);
    public static String generateMethodSignature(OperationType op);
    public static boolean isVoid(OperationType op);
    public static boolean hasParameters(OperationType op);
    public static String getReturnTypeTS(OperationType op);
}
```

## Common Patterns

### Calling Instance Operations

```typescript
// Without parameters
const result = await service.activate(entityId);

// With parameters
const result = await service.updateStatus(entityId, {
  newStatus: 'ACTIVE',
  reason: 'Approved by admin'
});
```

### Calling Static Operations

```typescript
// Factory method
const newUser = await UserService.register({
  email: 'user@example.com',
  password: 'secure123',
  firstName: 'John',
  lastName: 'Doe'
});
```

### Operation Button

```typescript
<Button
  onClick={async () => {
    if (operation.hasParameters) {
      // Open input form dialog
      const params = await openOperationForm(operation);
      if (params) {
        await service[operation.name](entityId, params);
      }
    } else {
      // Direct call
      await service[operation.name](entityId);
    }
  }}
>
  {t(`operations.${operation.name}`)}
</Button>
```

### Parameter Form Generation

```typescript
// For operation with parameters, generate input form
const form = operation.parameters.map(param => ({
  name: param.name,
  type: param.type,
  required: param.required,
  inputType: getInputType(param.type)
}));
```

## Testing Criteria

### Unit Tests
- [x] Operation extraction from metamodel
- [x] Method signature generation
- [x] Return type mapping
- [x] Parameter collection
- [x] Static vs instance detection
- [x] Void operation handling

### Integration Tests
- [x] Instance operation callable with ID
- [x] Static operation callable without ID
- [x] Parameters passed correctly
- [x] Return values received
- [x] Void operations complete successfully
- [x] Operation buttons generated

### Edge Cases
- [x] Operation with no parameters
- [x] Operation with collection parameters
- [x] Operation returning self type
- [x] Operation with optional parameters
- [x] Overloaded operation names (should warn)
- [x] Invalid return type reference

## Related Specifications

**Metamodel:**
- `12-button-and-button-group.md` - Operation buttons

**Data Model:**
- `01-class-type.md` - Contains operations
- `08-operation-parameters.md` - Operation inputs

**Runtime Model:**
- `runtime-model/01-core-types.md` - OperationTypeModel
- `runtime-model/05-action-model.md` - CallOperationAction

**Actions:**
- `actions/15-call-operation-action.md` - Executing operations

**Components:**
- `components/07-action-executor-hook.md` - Operation execution

**Examples:**
- `examples/04-operation-with-input.md` - Complete example

---

**Status:** ✅ Complete  
**Created:** 2025-11-28  
**Last Updated:** 2025-11-28  
**Reviewed:** Not yet reviewed

