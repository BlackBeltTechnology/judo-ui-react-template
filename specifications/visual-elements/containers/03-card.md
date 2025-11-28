# Card Element Specification

**Domain:** Visual Elements / Containers  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `runtime-model/03-container-model.md`  

## Overview

Card element provides elevated container with optional title and actions.

## Component Interface

```typescript
interface CardProps {
  model: ContainerModel;
  data: any;
  title?: string;
  actions?: React.ReactNode;
}

function CardElement({ model, data, title, actions }: CardProps) {
  return (
    <Card elevation={model.annotations?.elevation ? parseInt(model.annotations.elevation) : 1}>
      {title && (
        <CardHeader
          title={title}
          action={actions}
        />
      )}
      <CardContent>
        {model.children.map(child => (
          <VisualElementRenderer key={child.id} element={child} data={data} />
        ))}
      </CardContent>
    </Card>
  );
}
```

---

**Status:** ✅ Complete  
**Created:** 2025-11-28

