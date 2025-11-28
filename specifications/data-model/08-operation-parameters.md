# Operation Parameters Specification

**Domain:** Data Model  
**Ecore Class:** `data::OperationParameter`  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `04-operation-type.md`, `06-data-types.md`  
**Blocks:** Operation input forms, call operation actions  

## Overview

`OperationParameter` defines input parameters for OperationTypes. Parameters specify name, type, and constraints for values passed to operations. When an operation has parameters, the UI generates an input form before calling the operation.

## Metamodel Definition

### Class Hierarchy

```
NamedElement
└─ OperationParameter
```

### Properties

| Name | Type | Multiplicity | Default | Description |
|------|------|--------------|---------|-------------|
| `name` | EString | 1 | - | Parameter name (e.g., "newStatus", "reason") |
| `sourceId` | EString | 0..1 | - | XMIID from source |
| `annotations` | Annotation | 0..* | - | Custom metadata |
| `type` | DataType | 1 | - | Parameter data type |
| `required` | EBoolean | 0..1 | false | Whether parameter is mandatory |
| `isCollection` | EBoolean | 0..1 | false | Whether parameter accepts multiple values |
| `defaultValue` | EString | 0..1 | - | Default parameter value |

### Constraints

| Name | Type | Description |
|------|------|-------------|
| `minValue` | EString | Minimum numeric value |
| `maxValue` | EString | Maximum numeric value |
| `minLength` | EInt | Minimum string length |
| `maxLength` | EInt | Maximum string length |
| `pattern` | EString | Regex validation pattern |

### Relationships

**Contains:**
- Inherited from NamedElement

**Referenced By:**
- `OperationType.parameters` - Operation inputs

## Key Concepts

### Parameter Types

**Primitive Types:**
```xml
<parameters name="quantity" type="INTEGER" required="true" minValue="1"/>
<parameters name="reason" type="STRING" maxLength="500"/>
<parameters name="effectiveDate" type="DATE" required="true"/>
```

**Enum Types:**
```xml
<parameters name="newStatus" type="OrderStatus" required="true"/>
```

**Entity References:**
```xml
<parameters name="assignTo" type="User" required="false"/>
```

### Collection Parameters

When `isCollection=true`, parameter accepts array of values:
```xml
<parameters name="userIds" 
            type="STRING" 
            isCollection="true"
            required="true"/>
```

**Generated TypeScript:**
```typescript
interface OperationParams {
  userIds: string[];  // Array type
}
```

### Required vs Optional

**Required Parameter:**
- Must be provided
- Form validation enforces
- No default value needed

```xml
<parameters name="email" type="STRING" required="true"/>
```

**Optional Parameter:**
- Can be omitted
- May have default value
- Form shows as optional

```xml
<parameters name="comments" type="STRING" required="false"/>
```

### Default Values

```xml
<parameters name="notifyUser" 
            type="BOOLEAN" 
            required="false"
            defaultValue="true"/>
```

Form pre-fills with default value.

## Examples from Sample Model

### Example 1: Simple String Parameter

```xml
<operations name="updateStatus" operationKind="INSTANCE">
  <parameters name="newStatus" 
              type="UserStatus" 
              required="true"/>
  <parameters name="reason" 
              type="STRING" 
              required="false"
              maxLength="500"/>
</operations>
```

**Generated Form:**
```typescript
interface UpdateStatusParams {
  newStatus: UserStatus;    // Required enum
  reason?: string;          // Optional, max 500 chars
}

<Form onSubmit={handleSubmit}>
  <EnumerationCombo
    name="newStatus"
    label="New Status"
    required={true}
    options={UserStatusOptions}
  />
  <TextArea
    name="reason"
    label="Reason"
    maxLength={500}
  />
</Form>
```

### Example 2: Numeric Parameters with Constraints

```xml
<operations name="adjustQuantity" operationKind="INSTANCE">
  <parameters name="adjustment" 
              type="INTEGER" 
              required="true"
              minValue="-100"
              maxValue="100"/>
</operations>
```

**Generated Form:**
```typescript
<NumericInput
  name="adjustment"
  label="Adjustment"
  required={true}
  min={-100}
  max={100}
  integer={true}
/>
```

### Example 3: Date Range Parameters

```xml
<operations name="generateReport" operationKind="STATIC">
  <parameters name="startDate" type="DATE" required="true"/>
  <parameters name="endDate" type="DATE" required="true"/>
</operations>
```

**Generated Form:**
```typescript
<DateInput name="startDate" required={true} />
<DateInput name="endDate" required={true} min={formData.startDate} />
```

### Example 4: Collection Parameter

```xml
<operations name="assignToUsers" operationKind="INSTANCE">
  <parameters name="userIds" 
              type="STRING" 
              isCollection="true"
              required="true"/>
</operations>
```

**Generated Form:**
```typescript
interface Params {
  userIds: string[];
}

<MultiSelect
  name="userIds"
  label="Assign To"
  options={users}
  required={true}
  multiple={true}
/>
```

### Example 5: Entity Reference Parameter

```xml
<operations name="transfer" operationKind="INSTANCE">
  <parameters name="targetUser" 
              type="User" 
              required="true"/>
  <parameters name="transferAll" 
              type="BOOLEAN" 
              defaultValue="false"/>
</operations>
```

**Generated Form:**
```typescript
<AutocompleteSelect
  name="targetUser"
  label="Transfer To"
  service={UserService}
  required={true}
/>
<Checkbox
  name="transferAll"
  label="Transfer All Items"
  defaultChecked={false}
/>
```

### Example 6: Password Change

```xml
<operations name="changePassword" operationKind="INSTANCE">
  <parameters name="oldPassword" 
              type="STRING" 
              required="true"
              pattern="^.{8,}$">
    <annotations key="inputType" value="password"/>
  </parameters>
  <parameters name="newPassword" 
              type="STRING" 
              required="true"
              pattern="^.{8,}$">
    <annotations key="inputType" value="password"/>
  </parameters>
</operations>
```

### Example 7: File Upload Parameter

```xml
<operations name="uploadDocument" operationKind="INSTANCE">
  <parameters name="file" 
              type="BINARY" 
              required="true">
    <annotations key="maxSize" value="10485760"/><!-- 10MB -->
    <annotations key="allowedTypes" value="application/pdf,image/*"/>
  </parameters>
  <parameters name="description" type="STRING"/>
</operations>
```

## Validation Rules

### Required Properties
- `name` - Must be unique within operation
- `type` - Must specify valid DataType or ClassType

### Constraints
- Required parameters should not have default values
- Default value must match parameter type
- Collection parameters should have clear semantics
- MinValue < MaxValue for numeric types
- Pattern must be valid regex
- Entity reference types must exist

### Naming Conventions
- Use camelCase (e.g., `newStatus`, `targetUser`)
- Descriptive names (not `param1`, `param2`)
- Boolean parameters: prefix with `is`, `has`, `should`
- Collection parameters: use plural (e.g., `userIds`, `tags`)

## Runtime Model Mapping

```typescript
interface OperationParameterModel {
  name: string;                    // name
  type: DataType | string;         // type (primitive or ClassType name)
  required: boolean;               // required
  isCollection: boolean;           // isCollection
  defaultValue?: any;              // defaultValue
  
  // Constraints
  minValue?: number | string;      // minValue
  maxValue?: number | string;      // maxValue
  minLength?: number;              // minLength
  maxLength?: number;              // maxLength
  pattern?: string;                // pattern
  
  // UI hints
  inputType?: string;              // From type
  label?: string;                  // Generated from name
  
  // Metadata
  sourceId?: string;
  annotations?: Record<string, string>;
}

// Form input generation
interface ParameterInput {
  name: string;
  component: string;               // Input component type
  props: Record<string, any>;      // Component props
  validation: ValidationRules;     // Validation rules
}
```

## Generator Implementation

See `generators/05-action-extractor.md`.

**Key Methods:**
```java
public class OperationParameterGenerator {
    public static OperationParameterModel extractParameter(OperationParameter param);
    public static String getInputComponent(OperationParameter param);
    public static String getTypeScriptType(OperationParameter param);
    public static ValidationRules extractValidationRules(OperationParameter param);
    public static boolean isEntityReference(OperationParameter param);
}
```

## Common Patterns

### Parameter Form Generation

```typescript
function generateParameterForm(operation: OperationTypeModel) {
  if (!operation.hasParameters) return null;
  
  return (
    <Dialog open={showForm} onClose={handleClose}>
      <DialogTitle>{t(`operations.${operation.name}`)}</DialogTitle>
      <DialogContent>
        <Form onSubmit={handleSubmit}>
          {operation.parameters.map(param => (
            <ParameterInput key={param.name} param={param} />
          ))}
        </Form>
      </DialogContent>
      <DialogActions>
        <Button onClick={handleClose}>Cancel</Button>
        <Button type="submit">Execute</Button>
      </DialogActions>
    </Dialog>
  );
}
```

### Parameter Input Rendering

```typescript
function ParameterInput({ param }: { param: OperationParameterModel }) {
  const Component = getInputComponent(param.type);
  
  return (
    <Component
      name={param.name}
      label={t(`params.${param.name}`)}
      required={param.required}
      defaultValue={param.defaultValue}
      {...getValidationProps(param)}
    />
  );
}
```

### Operation Invocation

```typescript
async function callOperation(
  operation: OperationTypeModel,
  entityId: string | null,
  params: Record<string, any>
) {
  // Validate parameters
  const validated = validateParameters(params, operation.parameters);
  
  // Call service method
  if (operation.kind === 'instance') {
    return await service[operation.name](entityId!, validated);
  } else {
    return await service[operation.name](validated);
  }
}
```

## Testing Criteria

### Unit Tests
- [x] Parameter extraction from metamodel
- [x] Type mapping to TypeScript
- [x] Input component determination
- [x] Validation rule generation
- [x] Collection parameter handling
- [x] Default value processing

### Integration Tests
- [x] Parameter form renders correctly
- [x] Required parameters enforced
- [x] Optional parameters work
- [x] Default values pre-filled
- [x] Validation applied correctly
- [x] Collection inputs work

### Edge Cases
- [x] Operation with no parameters
- [x] Operation with many parameters (>10)
- [x] Complex nested entity parameters
- [x] Invalid default value type
- [x] Collection of entity references
- [x] Circular parameter dependencies

## Related Specifications

**Data Model:**
- `04-operation-type.md` - Contains parameters
- `06-data-types.md` - Parameter types

**Runtime Model:**
- `runtime-model/01-core-types.md` - OperationParameterModel
- `runtime-model/05-action-model.md` - Operation actions

**Actions:**
- `actions/15-call-operation-action.md` - Uses parameters

**Visual Elements:**
- All input specifications - Parameter inputs

**Examples:**
- `examples/04-operation-with-input.md` - Complete example

---

**Status:** ✅ Complete  
**Created:** 2025-11-28  
**Last Updated:** 2025-11-28  
**Reviewed:** Not yet reviewed

