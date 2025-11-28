# Validation Generator Specification

**Domain:** Generators  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `01-generator-utilities.md`, `runtime-model/08-validation-model.md`  

## Overview

Validation Generator extracts validation rules from InputBase elements and generates ValidationRuleModel structures.

## Generator Implementation

```java
public class ValidationGenerator {
    
    public List<Map<String, Object>> extractValidationRules(Container container) {
        List<Map<String, Object>> rules = new ArrayList<>();
        
        for (VisualElement element : container.getChildren()) {
            if (element instanceof InputBase) {
                InputBase input = (InputBase) element;
                Map<String, Object> rule = extractValidationRule(input);
                if (!rule.isEmpty()) {
                    rules.add(rule);
                }
            }
        }
        
        return rules;
    }
    
    private Map<String, Object> extractValidationRule(InputBase input) {
        Map<String, Object> ruleModel = new HashMap<>();
        List<Map<String, Object>> rules = new ArrayList<>();
        
        // Required
        if (input.isRequired()) {
            rules.add(createRule("required", null, "This field is required"));
        }
        
        // Min/Max Length
        if (input.getMinLength() != null) {
            rules.add(createRule("minLength", input.getMinLength(), 
                "Minimum " + input.getMinLength() + " characters required"));
        }
        if (input.getMaxLength() != null) {
            rules.add(createRule("maxLength", input.getMaxLength(),
                "Maximum " + input.getMaxLength() + " characters allowed"));
        }
        
        // Min/Max Value
        if (input instanceof NumericInput) {
            NumericInput ni = (NumericInput) input;
            if (ni.getMinValue() != null) {
                rules.add(createRule("minValue", ni.getMinValue(),
                    "Minimum value is " + ni.getMinValue()));
            }
            if (ni.getMaxValue() != null) {
                rules.add(createRule("maxValue", ni.getMaxValue(),
                    "Maximum value is " + ni.getMaxValue()));
            }
        }
        
        // Pattern
        if (input.getPattern() != null) {
            rules.add(createRule("pattern", input.getPattern(), "Invalid format"));
        }
        
        if (!rules.isEmpty()) {
            ruleModel.put("attributeName", input.getAttribute().getName());
            ruleModel.put("rules", rules);
        }
        
        return ruleModel;
    }
    
    private Map<String, Object> createRule(String type, Object value, String message) {
        Map<String, Object> rule = new HashMap<>();
        rule.put("type", type);
        if (value != null) {
            rule.put("value", value);
        }
        rule.put("message", message);
        return rule;
    }
}
```

---

**Status:** ✅ Complete  
**Created:** 2025-11-28

