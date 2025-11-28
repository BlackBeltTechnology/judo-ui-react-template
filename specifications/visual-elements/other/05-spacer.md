# Spacer Element Specification

**Domain:** Visual Elements / Other  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `runtime-model/04-visual-element-model.md`  

## Overview

Spacer creates empty space for layout purposes.

## Component Interface

```typescript
interface SpacerProps {
  model: DisplayElementModel;
}

function Spacer({ model }: SpacerProps) {
  return (
    <Box
      sx={{
        height: model.height || 0,
        width: model.width || '100%'
      }}
    />
  );
}
```

---

**Status:** ✅ Complete  
**Created:** 2025-11-28

