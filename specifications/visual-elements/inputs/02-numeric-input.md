# NumericInput Specification

**Domain:** Visual Elements / Inputs  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `runtime-model/04-visual-element-model.md`, `metamodel/11-input-base.md`  
**Blocks:** Numeric input components  

## Overview

`NumericInput` handles integer and decimal number input with optional formatting, min/max constraints, and step controls. Used for INTEGER, LONG, DECIMAL, and DOUBLE attribute types.

## Component Interface

```typescript
interface NumericInputProps {
  model: InputModel;
  value: number | null;
  onChange: (value: number | null) => void;
  onBlur?: () => void;
  disabled?: boolean;
  error?: string;
  helperText?: string;
}

function NumericInput({ model, value, onChange, onBlur, disabled, error }: NumericInputProps) {
  const isInteger = ['integer', 'long'].includes(model.attributeType);
  const decimalPlaces = isInteger ? 0 : 2;
  
  return (
    <TextField
      name={model.name}
      label={model.label}
      type="number"
      value={value ?? ''}
      onChange={(e) => {
        const num = e.target.value === '' ? null : parseFloat(e.target.value);
        onChange(num);
      }}
      onBlur={onBlur}
      disabled={disabled || model.readOnly}
      required={model.required}
      error={!!error}
      helperText={error}
      inputProps={{
        min: model.validation.minValue,
        max: model.validation.maxValue,
        step: isInteger ? 1 : 0.01
      }}
      InputProps={{
        endAdornment: model.annotations?.unit && (
          <InputAdornment position="end">
            {model.annotations.unit}
          </InputAdornment>
        )
      }}
      fullWidth
    />
  );
}
```

## Examples

### Example 1: Integer Input

```typescript
const model: InputModel = {
  id: 've-age',
  name: 'age',
  type: 'numericInput',
  attributeName: 'age',
  attributeType: 'integer',
  label: 'Age',
  col: 6,
  required: false,
  readOnly: false,
  validation: {
    minValue: 0,
    maxValue: 150
  }
};

<NumericInput
  model={model}
  value={data.age}
  onChange={(value) => handleChange('age', value)}
  error={errors.age}
/>
```

### Example 2: Decimal Input (Money)

```typescript
const model: InputModel = {
  id: 've-price',
  name: 'price',
  type: 'numericInput',
  attributeName: 'price',
  attributeType: 'decimal',
  label: 'Price',
  col: 6,
  required: true,
  readOnly: false,
  validation: {
    required: true,
    minValue: 0,
    maxValue: 999999.99
  },
  annotations: {
    unit: 'USD',
    decimalPlaces: '2'
  }
};

<NumericInput
  model={model}
  value={data.price}
  onChange={(value) => handleChange('price', value)}
  error={errors.price}
/>
```

### Example 3: With Unit Display

```typescript
const model: InputModel = {
  id: 've-weight',
  name: 'weight',
  type: 'numericInput',
  attributeName: 'weight',
  attributeType: 'decimal',
  label: 'Weight',
  col: 6,
  required: false,
  readOnly: false,
  validation: {
    minValue: 0
  },
  annotations: {
    unit: 'kg'
  }
};

<TextField
  type="number"
  InputProps={{
    endAdornment: <InputAdornment position="end">kg</InputAdornment>
  }}
/>
```

### Example 4: Percentage Input

```typescript
const model: InputModel = {
  id: 've-discount',
  name: 'discount',
  type: 'numericInput',
  attributeName: 'discount',
  attributeType: 'decimal',
  label: 'Discount',
  col: 6,
  required: false,
  readOnly: false,
  validation: {
    minValue: 0,
    maxValue: 100
  },
  annotations: {
    unit: '%'
  }
};
```

### Example 5: Dynamic Min/Max

```typescript
const minQuantityModel: InputModel = {
  id: 've-minQuantity',
  name: 'minQuantity',
  type: 'numericInput',
  attributeName: 'minQuantity',
  attributeType: 'integer',
  label: 'Minimum Quantity',
  col: 6,
  validation: {
    minValue: 0
  }
};

const maxQuantityModel: InputModel = {
  id: 've-maxQuantity',
  name: 'maxQuantity',
  type: 'numericInput',
  attributeName: 'maxQuantity',
  attributeType: 'integer',
  label: 'Maximum Quantity',
  col: 6,
  validation: {
    minValueBy: 'minQuantity'
  }
};
```

## Validation

```typescript
function validateNumericInput(
  value: number | null,
  validation: ValidationRules,
  data: any
): string | null {
  if (validation.required && (value === null || value === undefined)) {
    return 'This field is required';
  }
  
  if (value !== null) {
    if (validation.minValue !== undefined && value < validation.minValue) {
      return `Minimum value is ${validation.minValue}`;
    }
    
    if (validation.maxValue !== undefined && value > validation.maxValue) {
      return `Maximum value is ${validation.maxValue}`;
    }
    
    if (validation.minValueBy) {
      const minValue = data[validation.minValueBy];
      if (minValue !== undefined && value < minValue) {
        return `Must be >= ${validation.minValueBy}`;
      }
    }
    
    if (validation.maxValueBy) {
      const maxValue = data[validation.maxValueBy];
      if (maxValue !== undefined && value > maxValue) {
        return `Must be <= ${validation.maxValueBy}`;
      }
    }
  }
  
  return null;
}
```

## Number Formatting

```typescript
function formatNumber(value: number, options: {
  decimalPlaces?: number;
  locale?: string;
  style?: 'decimal' | 'currency' | 'percent';
  currency?: string;
}): string {
  return new Intl.NumberFormat(options.locale || 'en-US', {
    style: options.style || 'decimal',
    minimumFractionDigits: options.decimalPlaces || 0,
    maximumFractionDigits: options.decimalPlaces || 2,
    currency: options.currency
  }).format(value);
}

// Usage
formatNumber(1234.56, { decimalPlaces: 2 })  // "1,234.56"
formatNumber(50, { style: 'percent' })        // "50%"
formatNumber(99.99, { style: 'currency', currency: 'USD' })  // "$99.99"
```

## Common Patterns

### Increment/Decrement Buttons

```typescript
<TextField
  type="number"
  InputProps={{
    endAdornment: (
      <InputAdornment position="end">
        <IconButton size="small" onClick={() => onChange((value || 0) + 1)}>
          <ArrowUpIcon />
        </IconButton>
        <IconButton size="small" onClick={() => onChange((value || 0) - 1)}>
          <ArrowDownIcon />
        </IconButton>
      </InputAdornment>
    )
  }}
/>
```

### Slider Combination

```typescript
<Box>
  <NumericInput model={model} value={value} onChange={setValue} />
  <Slider
    value={value || 0}
    onChange={(e, newValue) => setValue(newValue as number)}
    min={model.validation.minValue || 0}
    max={model.validation.maxValue || 100}
  />
</Box>
```

## Testing Criteria

### Unit Tests
- [x] Accepts numeric input
- [x] Rejects non-numeric input
- [x] Respects min/max constraints
- [x] Handles null/empty values
- [x] Shows unit if provided
- [x] Integer step works

### Integration Tests
- [x] Validation triggers correctly
- [x] Dynamic min/max works
- [x] Formatting displays correctly
- [x] Decimal places respected

### Edge Cases
- [x] Zero value
- [x] Negative numbers
- [x] Very large numbers
- [x] Scientific notation
- [x] Leading zeros

## Related Specifications

**Metamodel:**
- `metamodel/11-input-base.md` - Input base

**Runtime Model:**
- `runtime-model/04-visual-element-model.md` - InputModel

**Data Model:**
- `data-model/06-data-types.md` - Numeric types

---

**Status:** ✅ Complete  
**Created:** 2025-11-28  
**Last Updated:** 2025-11-28  
**Reviewed:** Not yet reviewed

