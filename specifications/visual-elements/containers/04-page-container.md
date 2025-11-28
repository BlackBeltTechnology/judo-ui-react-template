# PageContainer Element Specification

**Domain:** Visual Elements / Containers  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `runtime-model/03-container-model.md`  

## Overview

PageContainer is the top-level container for page content, managing layout and action button groups.

## Component Interface

```typescript
interface PageContainerProps {
  model: PageContainerModel;
  data: any;
  actions: ActionMap;
}

function PageContainer({ model, data, actions }: PageContainerProps) {
  return (
    <Box>
      {/* Action Button Groups */}
      {model.actionButtonGroups?.map(group => (
        <ButtonGroupRenderer
          key={group.id}
          group={group}
          data={data}
          actions={actions}
        />
      ))}
      
      {/* Content */}
      <Grid container spacing={model.layout?.spacing || 2}>
        {model.visualElements.map(element => (
          <Grid item xs={12} md={element.col} key={element.id}>
            <VisualElementRenderer element={element} data={data} />
          </Grid>
        ))}
      </Grid>
    </Box>
  );
}
```

---

**Status:** ✅ Complete  
**Created:** 2025-11-28

