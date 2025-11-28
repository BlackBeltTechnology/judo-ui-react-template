# Link Element Specification

**Domain:** Visual Elements / Other  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `runtime-model/04-visual-element-model.md`  
**Blocks:** Link component  

## Overview

Link element provides navigation to related entities through relations.

## Component Interface

```typescript
interface LinkElementProps {
  model: LinkModel;
  data: any;
  navigate: (path: string) => void;
}

function LinkElement({ model, data, navigate }: LinkElementProps) {
  const hidden = model.hiddenBy && data[model.hiddenBy];
  if (hidden) return null;
  
  const displayText = model.displayAttributeName
    ? data[model.relationName]?.[model.displayAttributeName]
    : model.label;
  
  const count = model.cardinality === 'many' 
    ? data[model.relationName]?.length 
    : undefined;
  
  const handleClick = () => {
    const path = generatePath(model, data);
    navigate(path);
  };
  
  return (
    <MuiLink
      component="button"
      onClick={handleClick}
      startIcon={model.icon && <Icon name={model.icon.name} />}
    >
      {displayText} {count !== undefined && `(${count})`}
    </MuiLink>
  );
}

function generatePath(model: LinkModel, data: any): string {
  if (model.cardinality === 'one') {
    const relatedId = data[model.relationName]?.id;
    return `/entities/${model.targetType}/${relatedId}`;
  } else {
    return `/entities/${data.__type}/${data.id}/${model.relationName}`;
  }
}
```

---

**Status:** ✅ Complete  
**Created:** 2025-11-28

