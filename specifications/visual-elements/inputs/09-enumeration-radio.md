# EnumerationRadio Specification

**Domain:** Visual Elements / Inputs  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `runtime-model/04-visual-element-model.md`  

## Overview

EnumerationRadio provides radio button group selection for enum values (alternative to combo box).

## Component Interface

```typescript
interface EnumerationRadioProps {
  model: InputModel;
  value: string | null;
  onChange: (value: string) => void;
  options: EnumOption[];
  disabled?: boolean;
  error?: string;
}

function EnumerationRadio({ model, value, onChange, options, disabled, error }: EnumerationRadioProps) {
  return (
    <FormControl component="fieldset" error={!!error} required={model.required}>
      <FormLabel component="legend">{model.label}</FormLabel>
      <RadioGroup
        value={value || ''}
        onChange={(e) => onChange(e.target.value)}
      >
        {options.map((option) => (
          <FormControlLabel
            key={option.value}
            value={option.value}
            control={<Radio />}
            label={t(`judo.enums.${model.attributeType}.${option.value}`, {
              defaultValue: option.label
            })}
            disabled={disabled || model.readOnly}
          />
        ))}
      </RadioGroup>
      {error && <FormHelperText>{error}</FormHelperText>}
    </FormControl>
  );
}
```

---

**Status:** ✅ Complete  
**Created:** 2025-11-28

