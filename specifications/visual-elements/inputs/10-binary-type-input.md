# BinaryTypeInput Specification

**Domain:** Visual Elements / Inputs  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `runtime-model/04-visual-element-model.md`  

## Overview

BinaryTypeInput provides file upload for BINARY type attributes.

## Component Interface

```typescript
interface BinaryTypeInputProps {
  model: InputModel;
  value: File | Blob | null;
  onChange: (value: File | null) => void;
  disabled?: boolean;
  error?: string;
}

function BinaryTypeInput({ model, value, onChange, disabled, error }: BinaryTypeInputProps) {
  const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0] || null;
    onChange(file);
  };
  
  return (
    <Box>
      <input
        accept={model.annotations?.allowedTypes || '*'}
        style={{ display: 'none' }}
        id={model.id}
        type="file"
        onChange={handleChange}
        disabled={disabled || model.readOnly}
      />
      <label htmlFor={model.id}>
        <Button
          variant="outlined"
          component="span"
          disabled={disabled || model.readOnly}
          startIcon={<UploadIcon />}
        >
          {model.label || 'Choose File'}
        </Button>
      </label>
      {value && <Typography variant="body2">{(value as File).name}</Typography>}
      {error && <FormHelperText error>{error}</FormHelperText>}
    </Box>
  );
}
```

---

**Status:** ✅ Complete  
**Created:** 2025-11-28

