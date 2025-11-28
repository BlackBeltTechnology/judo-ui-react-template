# Flex Container Specification

**Domain:** Visual Elements / Containers  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `runtime-model/03-container-model.md`  
**Blocks:** Flex layout component  

## Overview

Flex container arranges children using flexbox layout with configurable direction, alignment, and spacing.

## Component Interface

```typescript
interface FlexContainerProps {
  model: FlexModel;
  data: any;
  children: React.ReactNode;
}

function FlexContainer({ model, data, children }: FlexContainerProps) {
  const hidden = model.hiddenBy && data[model.hiddenBy];
  
  if (hidden) return null;
  
  return (
    <Box
      display="flex"
      flexDirection={model.direction === 'horizontal' ? 'row' : 'column'}
      justifyContent={mapMainAxisAlignment(model.mainAxisAlignment)}
      alignItems={mapCrossAxisAlignment(model.crossAxisAlignment)}
      flexWrap={model.wrap}
      gap={model.spacing}
      sx={{
        width: model.size?.width,
        height: model.size?.height
      }}
    >
      {children}
    </Box>
  );
}

function mapMainAxisAlignment(alignment: MainAxisAlignment): string {
  const map = {
    start: 'flex-start',
    center: 'center',
    end: 'flex-end',
    'space-between': 'space-between',
    'space-around': 'space-around',
    'space-evenly': 'space-evenly'
  };
  return map[alignment] || 'flex-start';
}
```

## Examples

### Horizontal Layout

```typescript
const model: FlexModel = {
  id: 'flex-row',
  name: 'nameRow',
  type: 'flex',
  direction: 'horizontal',
  mainAxisAlignment: 'start',
  spacing: 2,
  col: 12,
  children: [
    { type: 'textInput', name: 'firstName', col: 6 },
    { type: 'textInput', name: 'lastName', col: 6 }
  ]
};
```

---

**Status:** ✅ Complete  
**Created:** 2025-11-28

