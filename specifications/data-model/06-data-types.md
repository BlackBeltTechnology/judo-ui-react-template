# Data Types Specification

**Domain:** Data Model  
**Ecore Class:** `data::DataType` (enumeration)  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** None (fundamental types)  
**Blocks:** `03-attribute-type.md`, all input specifications  

## Overview

This specification defines the primitive data types supported by the JUDO system. These types are used for attributes, operation parameters, and form inputs. Each type maps to specific TypeScript types and UI input components.

## Supported Data Types

### String Types

#### STRING
- **Description:** Variable-length text
- **TypeScript:** `string`
- **Input Component:** `TextInput`
- **Validation:** minLength, maxLength, pattern
- **Use Cases:** Names, emails, short text fields

#### TEXT
- **Description:** Long-form text content
- **TypeScript:** `string`
- **Input Component:** `TextArea`
- **Validation:** minLength, maxLength
- **Use Cases:** Descriptions, comments, notes, articles

### Numeric Types

#### INTEGER
- **Description:** Whole numbers
- **TypeScript:** `number`
- **Input Component:** `NumericInput`
- **Range:** -2,147,483,648 to 2,147,483,647 (32-bit)
- **Validation:** minValue, maxValue
- **Use Cases:** Counts, IDs, quantities

#### LONG
- **Description:** Large whole numbers
- **TypeScript:** `number`
- **Input Component:** `NumericInput`
- **Range:** -9,223,372,036,854,775,808 to 9,223,372,036,854,775,807 (64-bit)
- **Validation:** minValue, maxValue
- **Use Cases:** Timestamps, large IDs

#### DECIMAL
- **Description:** Fixed-point decimal numbers
- **TypeScript:** `number`
- **Input Component:** `NumericInput` (with decimal places)
- **Precision:** Configurable decimal places
- **Validation:** minValue, maxValue, decimalPlaces
- **Use Cases:** Money, prices, precise measurements

#### DOUBLE
- **Description:** Floating-point numbers
- **TypeScript:** `number`
- **Input Component:** `NumericInput`
- **Precision:** Double-precision (64-bit)
- **Validation:** minValue, maxValue
- **Use Cases:** Scientific calculations, percentages

### Temporal Types

#### DATE
- **Description:** Calendar date (no time)
- **TypeScript:** `Date`
- **Input Component:** `DateInput`
- **Format:** YYYY-MM-DD
- **Validation:** minValue, maxValue, minValueBy, maxValueBy
- **Use Cases:** Birth dates, due dates, event dates

#### TIME
- **Description:** Time of day (no date)
- **TypeScript:** `string` (HH:mm:ss format)
- **Input Component:** `TimeInput`
- **Format:** HH:mm:ss
- **Validation:** minValue, maxValue
- **Use Cases:** Business hours, schedules, durations

#### DATE_TIME
- **Description:** Date and time combined
- **TypeScript:** `Date`
- **Input Component:** `DateTimeInput`
- **Format:** YYYY-MM-DDTHH:mm:ss
- **Validation:** minValue, maxValue
- **Use Cases:** Appointments, events, logs

#### TIMESTAMP
- **Description:** Unix timestamp (milliseconds since epoch)
- **TypeScript:** `number`
- **Input Component:** `DateTimeInput`
- **Format:** Milliseconds
- **Validation:** minValue, maxValue
- **Use Cases:** System timestamps, audit trails

### Boolean Type

#### BOOLEAN
- **Description:** True/false value
- **TypeScript:** `boolean`
- **Input Component:** `Checkbox` or `Switch`
- **Values:** true, false
- **Validation:** None (always valid)
- **Use Cases:** Flags, toggles, yes/no questions

### Binary Type

#### BINARY
- **Description:** Binary data (files, images)
- **TypeScript:** `Blob` or `File`
- **Input Component:** `FileInput`
- **Validation:** maxSize, allowedTypes
- **Use Cases:** File uploads, images, documents

## Type Mapping Reference

### Metamodel to TypeScript

```typescript
const typeMapping = {
  STRING: 'string',
  TEXT: 'string',
  INTEGER: 'number',
  LONG: 'number',
  DECIMAL: 'number',
  DOUBLE: 'number',
  DATE: 'Date',
  TIME: 'string',
  DATE_TIME: 'Date',
  TIMESTAMP: 'number',
  BOOLEAN: 'boolean',
  BINARY: 'Blob',
  [EnumName]: EnumName  // Custom enum type
};
```

### Type to Input Component

```typescript
const inputComponentMapping = {
  STRING: 'TextInput',
  TEXT: 'TextArea',
  INTEGER: 'NumericInput',
  LONG: 'NumericInput',
  DECIMAL: 'NumericInput',
  DOUBLE: 'NumericInput',
  DATE: 'DateInput',
  TIME: 'TimeInput',
  DATE_TIME: 'DateTimeInput',
  TIMESTAMP: 'DateTimeInput',
  BOOLEAN: 'Checkbox',
  BINARY: 'FileInput',
  [EnumName]: 'EnumerationCombo'
};
```

## Examples from Sample Model

### Example 1: String Attribute

```xml
<attributes name="firstName" type="STRING" maxLength="50"/>
```

**Generated:**
```typescript
interface Entity {
  firstName: string;
}

<TextInput
  name="firstName"
  maxLength={50}
  value={data.firstName}
  onChange={handleChange}
/>
```

### Example 2: Decimal for Money

```xml
<attributes name="price" type="DECIMAL" minValue="0" maxValue="999999.99"/>
```

**Generated:**
```typescript
interface Product {
  price: number;
}

<NumericInput
  name="price"
  decimalPlaces={2}
  min={0}
  max={999999.99}
  value={data.price}
  onChange={handleChange}
/>
```

### Example 3: Date Range

```xml
<attributes name="startDate" type="DATE"/>
<attributes name="endDate" type="DATE" minValueBy="startDate"/>
```

**Generated:**
```typescript
interface Event {
  startDate: Date;
  endDate: Date;
}

<DateInput name="startDate" />
<DateInput name="endDate" min={data.startDate} />
```

### Example 4: Boolean Flag

```xml
<attributes name="isActive" type="BOOLEAN" defaultValue="true"/>
```

**Generated:**
```typescript
interface User {
  isActive: boolean;
}

<Checkbox
  name="isActive"
  checked={data.isActive}
  onChange={handleChange}
/>
```

### Example 5: Long Text

```xml
<attributes name="description" type="TEXT" maxLength="5000"/>
```

**Generated:**
```typescript
<TextArea
  name="description"
  maxLength={5000}
  rows={5}
  value={data.description}
  onChange={handleChange}
/>
```

### Example 6: Timestamp

```xml
<attributes name="createdAt" type="TIMESTAMP" isReadOnly="true"/>
```

**Generated:**
```typescript
interface Entity {
  createdAt: number;
}

// Displayed as formatted date
{new Date(data.createdAt).toLocaleString()}
```

## Type-Specific Validation

### String Types
```typescript
{
  required: boolean;
  minLength?: number;
  maxLength?: number;
  pattern?: RegExp;
}
```

### Numeric Types
```typescript
{
  required: boolean;
  min?: number;
  max?: number;
  decimalPlaces?: number;  // DECIMAL only
  integer?: boolean;       // INTEGER, LONG
}
```

### Temporal Types
```typescript
{
  required: boolean;
  min?: Date | string;
  max?: Date | string;
  minBy?: string;  // Reference to other field
  maxBy?: string;
}
```

### Boolean Type
```typescript
{
  required: boolean;  // Typically always true
}
```

### Binary Type
```typescript
{
  required: boolean;
  maxSize?: number;        // In bytes
  allowedTypes?: string[]; // MIME types
}
```

## Runtime Model Mapping

```typescript
type DataType = 
  | 'string'
  | 'text'
  | 'integer'
  | 'long'
  | 'decimal'
  | 'double'
  | 'date'
  | 'time'
  | 'dateTime'
  | 'timestamp'
  | 'boolean'
  | 'binary'
  | string;  // Enum name

interface TypeInfo {
  primitiveType: DataType;
  tsType: string;
  inputComponent: string;
  isNumeric: boolean;
  isTemporal: boolean;
  isEnum: boolean;
}
```

## Generator Implementation

See `generators/04-visual-element-extractor.md`.

**Key Methods:**
```java
public class DataTypeHelper {
    public static String getTypeScriptType(String dataType);
    public static String getInputComponent(String dataType);
    public static boolean isNumericType(String dataType);
    public static boolean isTemporalType(String dataType);
    public static boolean isStringType(String dataType);
    public static ValidationConfig getDefaultValidation(String dataType);
}
```

## Common Patterns

### Type Guards

```typescript
function isNumericType(type: DataType): boolean {
  return ['integer', 'long', 'decimal', 'double'].includes(type);
}

function isTemporalType(type: DataType): boolean {
  return ['date', 'time', 'dateTime', 'timestamp'].includes(type);
}
```

### Input Rendering

```typescript
function renderInput(attr: AttributeTypeModel) {
  const Component = getInputComponent(attr.type);
  return <Component {...attr} />;
}
```

### Serialization

```typescript
function serializeValue(value: any, type: DataType): any {
  switch (type) {
    case 'date':
    case 'dateTime':
      return value instanceof Date ? value.toISOString() : value;
    case 'timestamp':
      return value instanceof Date ? value.getTime() : value;
    default:
      return value;
  }
}
```

## Testing Criteria

### Unit Tests
- [x] Type to TypeScript mapping correct
- [x] Type to input component mapping correct
- [x] Numeric type detection
- [x] Temporal type detection
- [x] Validation config generation

### Integration Tests
- [x] Each input type renders correctly
- [x] Type-specific validation applied
- [x] Serialization/deserialization works
- [x] Date/time formatting correct
- [x] Numeric precision maintained

### Edge Cases
- [x] Unknown type (should error)
- [x] Custom enum types
- [x] Null/undefined values
- [x] Type coercion edge cases
- [x] Very large numbers (LONG)
- [x] High-precision decimals

## Related Specifications

**Data Model:**
- `03-attribute-type.md` - Attributes use these types
- `05-enumeration-type.md` - Custom enum types
- `08-operation-parameters.md` - Parameters use these types

**Visual Elements:**
- `visual-elements/inputs/01-text-input.md` - STRING
- `visual-elements/inputs/02-numeric-input.md` - INTEGER, DECIMAL, etc.
- `visual-elements/inputs/03-date-input.md` - DATE
- `visual-elements/inputs/06-textarea.md` - TEXT
- `visual-elements/inputs/07-checkbox.md` - BOOLEAN

**Runtime Model:**
- `runtime-model/01-core-types.md` - Type definitions

---

**Status:** ✅ Complete  
**Created:** 2025-11-28  
**Last Updated:** 2025-11-28  
**Reviewed:** Not yet reviewed

