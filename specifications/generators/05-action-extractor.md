# Action Extractor Specification

**Domain:** Generators  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `01-generator-utilities.md`, `runtime-model/05-action-model.md`  

## Overview

Action Extractor extracts action definitions from PageDefinition and Button elements, generating ActionModel structures.

## Extractor Implementation

```java
public class ActionExtractor {
    
    private final NamingUtil namingUtil;
    
    public List<Map<String, Object>> extractActions(PageDefinition page) {
        List<Map<String, Object>> actions = new ArrayList<>();
        
        // Extract from button groups
        if (page.getContainer() instanceof PageContainer) {
            PageContainer container = (PageContainer) page.getContainer();
            for (ButtonGroup group : container.getActionButtonGroups()) {
                for (Button button : group.getButtons()) {
                    if (button.getActionDefinition() != null) {
                        actions.add(extractAction(button.getActionDefinition()));
                    }
                }
            }
        }
        
        return actions;
    }
    
    public Map<String, Object> extractAction(Action action) {
        Map<String, Object> model = new HashMap<>();
        
        model.put("id", "action-" + action.getName());
        model.put("name", action.getName());
        model.put("type", determineActionType(action));
        
        // Target
        if (action.getTargetType() != null) {
            model.put("targetType", action.getTargetType().getName());
        }
        
        // Relation actions
        if (action instanceof RelationAction) {
            RelationAction ra = (RelationAction) action;
            model.put("relationName", ra.getRelation().getName());
            model.put("relationFqn", namingUtil.getFqn(ra.getRelation()));
            model.put("relationCardinality", ra.getRelation().getCardinality().toString().toLowerCase());
        }
        
        // Operation actions
        if (action instanceof CallOperationAction) {
            CallOperationAction ca = (CallOperationAction) action;
            model.put("operationName", ca.getOperation().getName());
            model.put("operationKind", ca.getOperation().getKind().toString().toLowerCase());
        }
        
        model.put("isBulk", action.isBulk());
        model.put("isTransient", action.isTransient());
        
        // Confirmation
        if (action.getConfirmation() != null) {
            model.put("confirmation", extractConfirmation(action.getConfirmation()));
        }
        
        return model;
    }
    
    private String determineActionType(Action action) {
        if (action instanceof RefreshAction) return "refresh";
        if (action instanceof CreateAction) return "create";
        if (action instanceof UpdateAction) return "update";
        if (action instanceof DeleteAction) return "delete";
        if (action instanceof AddToRelationAction) return "add";
        if (action instanceof RemoveFromRelationAction) return "remove";
        if (action instanceof SetRelationAction) return "set";
        if (action instanceof UnsetRelationAction) return "unset";
        if (action instanceof CallOperationAction) return "callOperation";
        return "custom";
    }
}
```

---

**Status:** ✅ Complete  
**Created:** 2025-11-28

