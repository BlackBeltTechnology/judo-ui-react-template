# Validation Engine Specification

**Domain:** Validation  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `validation/01-validation-types.md`, `validation/02-field-validators.md`  

## Overview

Validation engine orchestrates field and form validation with support for async validation, conditional rules, and error aggregation.

## Core Engine

```typescript
class ValidationEngine {
  private validators: Map<ValidationRuleType, ValidatorFunction>;
  
  constructor() {
    this.registerBuiltInValidators();
  }
  
  async validateField(
    fieldName: string,
    value: any,
    rules: ValidationRule[],
    formData: any
  ): Promise<ValidationError | null> {
    for (const rule of rules) {
      // Check condition
      if (rule.condition && !evaluateCondition(rule.condition, formData)) {
        continue;
      }
      
      // Get validator
      const validator = this.validators.get(rule.type);
      if (!validator) {
        console.warn(`Unknown validator: ${rule.type}`);
        continue;
      }
      
      // Validate
      const error = await validator(value, rule, formData);
      if (error) {
        return {
          field: fieldName,
          rule: rule.type,
          message: rule.message || error,
          severity: rule.severity || 'error'
        };
      }
    }
    
    return null;
  }
  
  async validateForm(
    formData: any,
    validationRules: ValidationRuleModel[]
  ): Promise<ValidationErrors> {
    const errors: ValidationErrors = {};
    
    await Promise.all(
      validationRules.map(async (ruleModel) => {
        const value = formData[ruleModel.attributeName];
        const error = await this.validateField(
          ruleModel.attributeName,
          value,
          ruleModel.rules,
          formData
        );
        
        if (error && error.severity === 'error') {
          if (!errors[ruleModel.attributeName]) {
            errors[ruleModel.attributeName] = [];
          }
          errors[ruleModel.attributeName].push(error);
        }
      })
    );
    
    return errors;
  }
}
```

## React Hook

```typescript
function useValidation(validationRules: ValidationRuleModel[]) {
  const [errors, setErrors] = useState<ValidationErrors>({});
  const [touched, setTouched] = useState<Record<string, boolean>>({});
  const engine = useMemo(() => new ValidationEngine(), []);
  
  const validateField = useCallback(async (fieldName: string, formData: any) => {
    const rules = validationRules.find(r => r.attributeName === fieldName);
    if (!rules) return;
    
    const error = await engine.validateField(
      fieldName,
      formData[fieldName],
      rules.rules,
      formData
    );
    
    setErrors(prev => ({
      ...prev,
      [fieldName]: error ? [error] : []
    }));
  }, [validationRules, engine]);
  
  const validateForm = useCallback(async (formData: any) => {
    const allErrors = await engine.validateForm(formData, validationRules);
    setErrors(allErrors);
    return Object.keys(allErrors).length === 0;
  }, [validationRules, engine]);
  
  const touch = useCallback((fieldName: string) => {
    setTouched(prev => ({ ...prev, [fieldName]: true }));
  }, []);
  
  return {
    errors,
    touched,
    validateField,
    validateForm,
    touch,
    isValid: Object.keys(errors).length === 0
  };
}
```

---

**Status:** ✅ Complete  
**Created:** 2025-11-28

