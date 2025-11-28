# EnumerationType Specification

**Domain:** Data Model  
**Ecore Class:** `data::EnumerationType`  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** None (independent from ClassType)  
**Blocks:** Enum inputs, enum attributes, runtime models  

## Overview

`EnumerationType` defines enumerated types with a fixed set of named values. Enums are used for attributes with predefined choices (like status, type, category) and are rendered as combo boxes or radio button groups in the UI.

## Metamodel Definition

### Class Hierarchy

```
NamedElement
└─ DataElement (abstract)
   └─ EnumerationType
```

### Properties

| Name | Type | Multiplicity | Default | Description |
|------|------|--------------|---------|-------------|
| `name` | EString | 1 | - | Enum name (e.g., "UserStatus", "OrderType") |
| `sourceId` | EString | 0..1 | - | XMIID from source |
| `annotations` | Annotation | 0..* | - | Custom metadata |
| `members` | EnumerationMember | 1..* | - | Enum values/literals |

### EnumerationMember Properties

| Name | Type | Multiplicity | Default | Description |
|------|------|--------------|---------|-------------|
| `name` | EString | 1 | - | Member name (e.g., "ACTIVE", "PENDING") |
| `ordinal` | EInt | 1 | - | Position/order (0-based) |
| `value` | EString | 0..1 | - | Custom value (defaults to name) |

### Relationships

**Contains:**
- `members: EnumerationMember[]` (1..*) - Enum values

**Referenced By:**
- `AttributeType.type` - Attributes using this enum
- `OperationParameter.type` - Parameters using this enum
- Input components - Enum selectors

## Key Concepts

### Enum Members

Each member represents a valid value:
```xml
<enumerationTypes name="UserStatus">
  <members name="ACTIVE" ordinal="0"/>
  <members name="INACTIVE" ordinal="1"/>
  <members name="PENDING" ordinal="2"/>
  <members name="SUSPENDED" ordinal="3"/>
</enumerationTypes>
```

### Ordinal Values

Ordinal defines sort order and can be used for comparisons:
- Must be unique within enum
- Typically sequential (0, 1, 2, ...)
- Used for ordering in UI

### Custom Values

By default, value equals name, but can be customized:
```xml
<members name="HIGH" ordinal="0" value="high_priority"/>
<members name="MEDIUM" ordinal="1" value="medium_priority"/>
<members name="LOW" ordinal="2" value="low_priority"/>
```

### Internationalization

Each member gets an i18n key:
```
judo.enums.{EnumName}.{memberName}
```

Example:
```
judo.enums.UserStatus.ACTIVE → "Active"
judo.enums.UserStatus.INACTIVE → "Inactive"
```

## Examples from Sample Model

### Example 1: Simple Status Enum

```xml
<enumerationTypes name="UserStatus" sourceId="_enum_userstatus">
  <members name="ACTIVE" ordinal="0"/>
  <members name="INACTIVE" ordinal="1"/>
  <members name="PENDING" ordinal="2"/>
  <members name="SUSPENDED" ordinal="3"/>
</enumerationTypes>
```

**Generated TypeScript:**
```typescript
export enum UserStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  PENDING = 'PENDING',
  SUSPENDED = 'SUSPENDED'
}

export const UserStatusOptions = [
  { value: 'ACTIVE', label: t('judo.enums.UserStatus.ACTIVE'), ordinal: 0 },
  { value: 'INACTIVE', label: t('judo.enums.UserStatus.INACTIVE'), ordinal: 1 },
  { value: 'PENDING', label: t('judo.enums.UserStatus.PENDING'), ordinal: 2 },
  { value: 'SUSPENDED', label: t('judo.enums.UserStatus.SUSPENDED'), ordinal: 3 }
];
```

### Example 2: Priority Enum with Custom Values

```xml
<enumerationTypes name="Priority">
  <members name="HIGH" ordinal="0" value="3"/>
  <members name="MEDIUM" ordinal="1" value="2"/>
  <members name="LOW" ordinal="2" value="1"/>
</enumerationTypes>
```

**Generated TypeScript:**
```typescript
export enum Priority {
  HIGH = '3',
  MEDIUM = '2',
  LOW = '1'
}
```

### Example 3: Order Type Enum

```xml
<enumerationTypes name="OrderType">
  <members name="STANDARD" ordinal="0"/>
  <members name="EXPRESS" ordinal="1"/>
  <members name="OVERNIGHT" ordinal="2"/>
  <members name="INTERNATIONAL" ordinal="3"/>
</enumerationTypes>
```

### Example 4: Payment Method Enum

```xml
<enumerationTypes name="PaymentMethod">
  <members name="CREDIT_CARD" ordinal="0"/>
  <members name="DEBIT_CARD" ordinal="1"/>
  <members name="PAYPAL" ordinal="2"/>
  <members name="BANK_TRANSFER" ordinal="3"/>
  <members name="CASH" ordinal="4"/>
</enumerationTypes>
```

### Example 5: Enum Used in Attribute

```xml
<classTypes name="User">
  <attributes name="status" type="UserStatus" required="true" defaultValue="PENDING"/>
</classTypes>
```

**Generated Input:**
```typescript
<EnumerationCombo
  name="status"
  label={t('fields.status')}
  value={data.status}
  onChange={handleChange}
  options={UserStatusOptions}
  required={true}
/>
```

## Validation Rules

### Required Properties
- `name` - Must be unique globally
- `members` - At least one member required
- `members[].name` - Must be unique within enum
- `members[].ordinal` - Must be unique within enum

### Constraints
- Member names should be UPPER_SNAKE_CASE
- Ordinals should be sequential starting from 0
- Enum name should be PascalCase
- At least 1 member, typically 2-10 members
- Avoid too many members (>20 may need different UI)

### Naming Conventions
- Enum type: PascalCase, often ends with "Status", "Type", "Kind"
- Members: UPPER_SNAKE_CASE
- Descriptive names (not "Type1", "Type2")

## Runtime Model Mapping

```typescript
interface EnumerationTypeModel {
  name: string;                        // name
  fqn: string;                         // Same as name (global scope)
  members: EnumerationMemberModel[];   // members
  
  // Metadata
  sourceId?: string;
  annotations?: Record<string, string>;
}

interface EnumerationMemberModel {
  name: string;                        // name
  ordinal: number;                     // ordinal
  value: string;                       // value (or name if not set)
  i18nKey: string;                     // Generated i18n key
}

// Generated TypeScript enum
export enum {EnumName} {
  {MEMBER1} = '{value1}',
  {MEMBER2} = '{value2}',
  // ...
}

// Generated options for UI
export const {EnumName}Options: EnumOption[] = [
  { value: '{value1}', label: string, ordinal: 0 },
  { value: '{value2}', label: string, ordinal: 1 },
  // ...
];
```

## Generator Implementation

See `generators/08-enum-generator.md`.

**Key Methods:**
```java
public class EnumerationTypeGenerator {
    public static EnumerationTypeModel extractEnumModel(EnumerationType enumType);
    public static String generateTypeScriptEnum(EnumerationType enumType);
    public static String generateEnumOptions(EnumerationType enumType);
    public static List<EnumerationMember> getSortedMembers(EnumerationType enumType);
    public static String getI18nKey(EnumerationType enumType, EnumerationMember member);
}
```

## Common Patterns

### Enum Combo Box

```typescript
<Select
  value={data.status}
  onChange={handleChange}
>
  {UserStatusOptions.map(option => (
    <MenuItem key={option.value} value={option.value}>
      {t(option.i18nKey, { defaultValue: option.label })}
    </MenuItem>
  ))}
</Select>
```

### Radio Button Group

```typescript
<RadioGroup value={data.priority} onChange={handleChange}>
  {PriorityOptions.map(option => (
    <FormControlLabel
      key={option.value}
      value={option.value}
      control={<Radio />}
      label={t(option.i18nKey)}
    />
  ))}
</RadioGroup>
```

### Type Guard

```typescript
function isActive(user: User): boolean {
  return user.status === UserStatus.ACTIVE;
}
```

### Enum Comparison

```typescript
// Using ordinal for priority comparison
if (task1.priority.ordinal < task2.priority.ordinal) {
  // task1 has higher priority
}
```

### Default Value

```typescript
const defaultUser: Partial<User> = {
  status: UserStatus.PENDING,  // From defaultValue
  // ...
};
```

## Testing Criteria

### Unit Tests
- [x] Enum extraction from metamodel
- [x] TypeScript enum generation
- [x] Options array generation
- [x] I18n key generation
- [x] Ordinal sorting
- [x] Custom value handling

### Integration Tests
- [x] Enum combo renders correctly
- [x] Radio group renders correctly
- [x] Enum values selectable
- [x] I18n translations applied
- [x] Default value set correctly
- [x] Enum validation works

### Edge Cases
- [x] Single member enum (valid but unusual)
- [x] Very large enum (>50 members)
- [x] Duplicate member names (validation error)
- [x] Duplicate ordinals (validation error)
- [x] Non-sequential ordinals (valid)
- [x] Custom value conflicts

## Related Specifications

**Metamodel:**
- `11-input-base.md` - Enum input fields

**Data Model:**
- `03-attribute-type.md` - Enum attributes
- `06-data-types.md` - Enum as data type

**Runtime Model:**
- `runtime-model/01-core-types.md` - EnumerationTypeModel

**Visual Elements:**
- `visual-elements/inputs/08-enumeration-combo.md` - Combo box
- `visual-elements/inputs/09-enumeration-radio.md` - Radio buttons

**Generators:**
- `generators/08-enum-generator.md` - Enum generation

---

**Status:** ✅ Complete  
**Created:** 2025-11-28  
**Last Updated:** 2025-11-28  
**Reviewed:** Not yet reviewed

