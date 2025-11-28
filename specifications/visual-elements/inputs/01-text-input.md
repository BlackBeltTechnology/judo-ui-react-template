# TextInput Specification
**Reviewed:** Not yet reviewed
**Last Updated:** 2025-11-28  
**Created:** 2025-11-28  
**Status:** ✅ Complete  

---

- `validation/02-field-validators.md` - String validation
**Validation:**

- `data-model/03-attribute-type.md` - String attributes
**Data Model:**

- `runtime-model/04-visual-element-model.md` - InputModel
**Runtime Model:**

- `metamodel/11-input-base.md` - Input base
**Metamodel:**

## Related Specifications

- [x] Rapid typing
- [x] Paste behavior
- [x] Special characters
- [x] Very long text (>1000 chars)
- [x] Empty string vs null
### Edge Cases

- [x] Auto-focus works
- [x] Helper text shows
- [x] Icon renders if provided
- [x] Error messages display
- [x] Validation triggers correctly
### Integration Tests

- [x] Disabled when readOnly
- [x] Respects maxLength
- [x] Shows error state
- [x] Calls onChange when typing
- [x] Displays value correctly
- [x] Shows required indicator
- [x] Renders with correct label
### Unit Tests

## Testing Criteria

```
/>
  aria-describedby={error ? `${model.id}-error` : undefined}
  aria-invalid={!!error}
  aria-required={model.required}
  aria-label={model.label}
  label={model.label}
  name={model.name}
  id={model.id}
<TextField
```typescript

## Accessibility

```
};
  onChange(value.toLowerCase());
const handleUsernameChange = (value: string) => {
// Uppercase transformation

};
  onChange(formatted);
  const formatted = formatPhoneNumber(value);
const handlePhoneChange = (value: string) => {
// Phone number formatting
```typescript

### Auto-formatting

```
}
  );
    />
      error={error}
      onBlur={model.onBlur ? handleBlur : undefined}
      onChange={handleChange}
      value={value}
      model={model}
    <TextInput
  return (
  
  };
    setError(validationError);
    const validationError = validateTextInput(value, model.validation);
  const handleBlur = () => {
  
  };
    setError(null);
    setValue(newValue);
  const handleChange = (newValue: string) => {
  
  const [error, setError] = useState<string | null>(null);
  const [value, setValue] = useState('');
function FormField({ model }: Props) {
```typescript

### Controlled Input

## Common Patterns

```
}
  return null;
  
  }
    return 'Invalid format';
  if (validation.pattern && !new RegExp(validation.pattern).test(value)) {
  
  }
    return `Maximum ${validation.maxLength} characters allowed`;
  if (validation.maxLength && value.length > validation.maxLength) {
  
  }
    return `Minimum ${validation.minLength} characters required`;
  if (validation.minLength && value.length < validation.minLength) {
  
  }
    return 'This field is required';
  if (validation.required && !value?.trim()) {
function validateTextInput(value: string, validation: ValidationRules): string | null {
```typescript

- `pattern` - Regex validation
- `maxLength` - Maximum character count
- `minLength` - Minimum character count
- `required` - Field must have a value
TextInput supports these validation rules:

## Validation

```
/>
  // ...other props
  }}
    )
      </InputAdornment>
        <PersonIcon />
      <InputAdornment position="start">
    startAdornment: (
  InputProps={{
<TextField

};
  }
    pattern: '^[a-zA-Z0-9_]+$'
    maxLength: 20,
    minLength: 3,
    required: true,
  validation: {
  readOnly: false,
  required: true,
  col: 12,
  icon: { name: 'person' },
  label: 'Username',
  attributeType: 'string',
  attributeName: 'username',
  type: 'textInput',
  name: 'username',
  id: 've-username',
const model: InputModel = {
```typescript

### Example 4: With Icon

```
/>
  disabled={true}
  onChange={() => {}} // No-op for read-only
  value={data.id}
  model={model}
<TextInput

};
  validation: {}
  readOnly: true,
  required: false,
  col: 6,
  label: 'ID',
  attributeType: 'string',
  attributeName: 'id',
  type: 'textInput',
  name: 'id',
  id: 've-id',
const model: InputModel = {
```typescript

### Example 3: Read-Only Display

```
/>
  helperText="Enter a valid email address"
  error={errors.email}
  onChange={(value) => handleChange('email', value)}
  value={data.email}
  model={model}
<TextInput

};
  }
    maxLength: 100
    pattern: '^[^@]+@[^@]+\\.[^@]+$',
    required: true,
  validation: {
  readOnly: false,
  required: true,
  col: 12,
  icon: { name: 'email' },
  label: 'Email Address',
  attributeType: 'string',
  attributeName: 'email',
  type: 'textInput',
  name: 'email',
  id: 've-email',
const model: InputModel = {
```typescript

### Example 2: Email Input with Pattern

```
/>
  error={errors.firstName}
  onChange={(value) => handleChange('firstName', value)}
  value={data.firstName}
  model={model}
<TextInput

};
  }
    maxLength: 50
    required: true,
  validation: {
  readOnly: false,
  required: true,
  col: 6,
  label: 'First Name',
  attributeType: 'string',
  attributeName: 'firstName',
  type: 'textInput',
  name: 'firstName',
  id: 've-firstName',
const model: InputModel = {
```typescript

### Example 1: Basic Text Input

## Examples

```
}
  );
    />
      fullWidth
      autoFocus={model.autoFocus}
      }}
        minLength: model.validation.minLength
        maxLength: model.validation.maxLength,
      inputProps={{
      helperText={error || helperText || model.validation.pattern && 'Format: ...'}
      error={!!error}
      required={model.required}
      disabled={disabled || model.readOnly}
      onBlur={onBlur}
      onChange={(e) => onChange(e.target.value)}
      value={value || ''}
      label={model.label}
      name={model.name}
    <TextField
  return (
function TextInput({ model, value, onChange, onBlur, disabled, error, helperText }: TextInputProps) {

}
  helperText?: string;
  error?: string;
  disabled?: boolean;
  onBlur?: () => void;
  onChange: (value: string) => void;
  value: string;
  model: InputModel;
interface TextInputProps {
```typescript

## Component Interface

`TextInput` is a single-line text input field for string data. It's the most common input type, used for names, emails, short text values, and any string attribute.

## Overview

**Blocks:** Text-based input components  
**Dependencies:** `runtime-model/04-visual-element-model.md`, `metamodel/11-input-base.md`  
**Assigned:** AI Agent  
**Status:** Complete  
**Domain:** Visual Elements / Inputs  


