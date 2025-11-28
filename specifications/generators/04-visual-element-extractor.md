# Visual Element Extractor Specification

**Domain:** Generators  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `01-generator-utilities.md`, `runtime-model/04-visual-element-model.md`  

## Overview

Visual Element Extractor extracts visual element configurations from Container children and generates VisualElementModel structures.

## Extractor Implementation

```java
public class VisualElementExtractor {
    
    private final TypeMapper typeMapper;
    private final NamingUtil namingUtil;
    
    public Map<String, Object> extractElement(VisualElement element) {
        Map<String, Object> model = new HashMap<>();
        
        // Basic properties
        model.put("id", "ve-" + element.getName());
        model.put("name", element.getName());
        model.put("type", determineElementType(element));
        
        // Layout
        model.put("col", element.getCol());
        if (element.getRow() != null) {
            model.put("row", element.getRow());
        }
        
        // Sizing
        if (element.getSize() != null) {
            model.put("size", extractSize(element.getSize()));
        }
        
        // Conditional
        if (element.getHiddenBy() != null) {
            model.put("hiddenBy", element.getHiddenBy().getName());
        }
        if (element.getEnabledBy() != null) {
            model.put("enabledBy", element.getEnabledBy().getName());
        }
        
        // Type-specific extraction
        if (element instanceof InputBase) {
            extractInputProperties((InputBase) element, model);
        } else if (element instanceof Link) {
            extractLinkProperties((Link) element, model);
        } else if (element instanceof Table) {
            extractTableProperties((Table) element, model);
        }
        
        return model;
    }
    
    private String determineElementType(VisualElement element) {
        if (element instanceof TextInput) return "textInput";
        if (element instanceof NumericInput) return "numericInput";
        if (element instanceof DateInput) return "dateInput";
        if (element instanceof Checkbox) return "checkbox";
        if (element instanceof Link) return "link";
        if (element instanceof Table) return "table";
        if (element instanceof Flex) return "flex";
        // ... more types
        return "text";
    }
    
    private void extractInputProperties(InputBase input, Map<String, Object> model) {
        if (input.getAttribute() != null) {
            model.put("attributeName", input.getAttribute().getName());
            model.put("attributeType", input.getAttribute().getType());
            model.put("attributeFqn", namingUtil.getFqn(input.getAttribute()));
        }
        
        model.put("readOnly", input.isReadOnly());
        model.put("required", input.isRequired());
        
        // Validation
        Map<String, Object> validation = new HashMap<>();
        if (input.isRequired()) {
            validation.put("required", true);
        }
        if (input.getMinLength() != null) {
            validation.put("minLength", input.getMinLength());
        }
        if (input.getMaxLength() != null) {
            validation.put("maxLength", input.getMaxLength());
        }
        model.put("validation", validation);
    }
}
```

---

**Status:** ✅ Complete  
**Created:** 2025-11-28

