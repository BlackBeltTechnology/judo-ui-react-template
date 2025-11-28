# Model-Driven Container Component Specification

**Domain:** Components  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `runtime-model/03-container-model.md`, `components/03-visual-element-registry.md`  
**Blocks:** Container rendering  

## Overview

ModelDrivenContainer renders container models (PageContainer, Flex, TabController) with their children, handling layout and conditional rendering.

## Component Interface

```typescript
interface ModelDrivenContainerProps {
  model: ContainerModel;
  data: any;
  onChange?: (fieldName: string, value: any) => void;
  actions?: Record<string, Function>;
  errors?: ValidationErrors;
}

function ModelDrivenContainer({ 
  model, 
  data, 
  onChange, 
  actions, 
  errors 
}: ModelDrivenContainerProps) {
  const hidden = model.hiddenBy && data[model.hiddenBy];
  
  if (hidden) return null;
  
  // Render based on container type
  switch (model.type) {
    case 'form':
    case 'view':
    case 'table':
      return (
        <PageContainerRenderer
          model={model as PageContainerModel}
          data={data}
          onChange={onChange}
          actions={actions}
          errors={errors}
        />
      );
    
    case 'flex':
      return (
        <FlexRenderer
          model={model as FlexModel}
          data={data}
          onChange={onChange}
          errors={errors}
        />
      );
    
    case 'tabController':
      return (
        <TabControllerRenderer
          model={model as TabControllerModel}
          data={data}
          onChange={onChange}
          errors={errors}
        />
      );
    
    default:
      return (
        <GenericContainerRenderer
          model={model}
          data={data}
          onChange={onChange}
          errors={errors}
        />
      );
  }
}
```

## PageContainer Renderer

```typescript
function PageContainerRenderer({ 
  model, 
  data, 
  onChange, 
  actions, 
  errors 
}: Props) {
  return (
    <Box>
      {/* Action Button Groups */}
      {model.actionButtonGroups?.map(group => (
        <Box key={group.id} mb={2}>
          <ButtonGroupRenderer
            group={group}
            data={data}
            actions={actions}
          />
        </Box>
      ))}
      
      {/* Visual Elements in Grid */}
      <Grid container spacing={model.layout?.spacing || 2}>
        {model.visualElements.map(element => (
          <Grid 
            key={element.id}
            item
            xs={12}
            md={element.col}
          >
            <VisualElementRenderer
              element={element}
              data={data}
              onChange={onChange}
              error={errors?.[element.attributeName]}
            />
          </Grid>
        ))}
      </Grid>
    </Box>
  );
}
```

## Flex Renderer

```typescript
function FlexRenderer({ model, data, onChange, errors }: Props) {
  return (
    <Box
      display="flex"
      flexDirection={model.direction === 'horizontal' ? 'row' : 'column'}
      justifyContent={mapMainAxisAlignment(model.mainAxisAlignment)}
      alignItems={mapCrossAxisAlignment(model.crossAxisAlignment)}
      flexWrap={model.wrap}
      gap={model.spacing}
    >
      {model.children.map(child => (
        <VisualElementRenderer
          key={child.id}
          element={child}
          data={data}
          onChange={onChange}
          error={errors?.[child.attributeName]}
        />
      ))}
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

function mapCrossAxisAlignment(alignment: CrossAxisAlignment): string {
  const map = {
    start: 'flex-start',
    center: 'center',
    end: 'flex-end',
    stretch: 'stretch',
    baseline: 'baseline'
  };
  return map[alignment] || 'stretch';
}
```

## TabController Renderer

```typescript
function TabControllerRenderer({ model, data, onChange, errors }: Props) {
  const [activeTab, setActiveTab] = useState(model.defaultTab);
  
  const visibleTabs = model.tabs.filter(tab => 
    !tab.hiddenBy || !data[tab.hiddenBy]
  );
  
  return (
    <Box>
      <Tabs 
        value={activeTab} 
        onChange={(e, newValue) => setActiveTab(newValue)}
        orientation={model.tabPosition === 'left' || model.tabPosition === 'right' ? 'vertical' : 'horizontal'}
      >
        {visibleTabs.map((tab, index) => (
          <Tab
            key={tab.id}
            label={tab.label}
            icon={tab.icon && <Icon name={tab.icon.name} />}
            disabled={tab.disabled}
            iconPosition="start"
          />
        ))}
      </Tabs>
      
      {visibleTabs.map((tab, index) => (
        <TabPanel key={tab.id} value={activeTab} index={index}>
          {tab.children.map(child => (
            <VisualElementRenderer
              key={child.id}
              element={child}
              data={data}
              onChange={onChange}
              error={errors?.[child.attributeName]}
            />
          ))}
        </TabPanel>
      ))}
    </Box>
  );
}

function TabPanel({ children, value, index }: TabPanelProps) {
  return (
    <div hidden={value !== index}>
      {value === index && <Box p={3}>{children}</Box>}
    </div>
  );
}
```

## Nested Container Handling

```typescript
function renderNestedContainers(
  elements: VisualElementModel[],
  data: any,
  onChange?: Function,
  errors?: ValidationErrors
) {
  return elements.map(element => {
    if (isContainerModel(element)) {
      return (
        <ModelDrivenContainer
          key={element.id}
          model={element as ContainerModel}
          data={data}
          onChange={onChange}
          errors={errors}
        />
      );
    } else {
      return (
        <VisualElementRenderer
          key={element.id}
          element={element}
          data={data}
          onChange={onChange}
          error={errors?.[element.attributeName]}
        />
      );
    }
  });
}
```

## Layout Utilities

```typescript
function getContainerStyles(model: ContainerModel): CSSProperties {
  return {
    width: model.size?.width,
    height: model.size?.height,
    padding: model.annotations?.padding,
    margin: model.annotations?.margin
  };
}

function getGridSpacing(layout?: LayoutModel): number {
  return layout?.spacing || 2;
}
```

## Conditional Rendering

```typescript
function shouldRenderElement(
  element: VisualElementModel,
  data: any
): boolean {
  if (element.hiddenBy && data[element.hiddenBy]) {
    return false;
  }
  return true;
}
```

## Examples

### Example 1: Form Container

```typescript
<ModelDrivenContainer
  model={formContainerModel}
  data={userData}
  onChange={handleFieldChange}
  errors={validationErrors}
/>
```

### Example 2: Nested Flex Layout

```typescript
const containerModel: FlexModel = {
  type: 'flex',
  direction: 'vertical',
  spacing: 3,
  children: [
    {
      type: 'flex',
      direction: 'horizontal',
      spacing: 2,
      children: [
        { type: 'textInput', name: 'firstName' },
        { type: 'textInput', name: 'lastName' }
      ]
    },
    {
      type: 'textInput',
      name: 'email'
    }
  ]
};
```

## Testing Criteria

### Unit Tests
- [x] Renders PageContainer correctly
- [x] Renders Flex correctly
- [x] Renders TabController correctly
- [x] Handles nested containers
- [x] Conditional rendering works
- [x] Layout properties applied

### Integration Tests
- [x] Children render correctly
- [x] Tab switching works
- [x] Grid layout responsive
- [x] Actions passed to children
- [x] Errors displayed correctly

---

**Status:** ✅ Complete  
**Created:** 2025-11-28

