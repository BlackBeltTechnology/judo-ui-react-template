# Visual Element Registry Specification
**Created:** 2025-11-28
**Status:** ✅ Complete  

---

- [x] Error boundaries work
- [x] Conditional rendering works
- [x] Custom elements render
- [x] All built-in elements render
### Integration Tests

- [x] Renderer handles unknown types gracefully
- [x] Returns undefined for unknown types
- [x] Can unregister elements
- [x] Can register custom elements
- [x] Registry initializes with built-in elements
### Unit Tests

## Testing Criteria

```
}
  );
    </Box>
      {children}
    >
      }}
        height: element.size?.height
        width: element.size?.width,
        gridColumn: `span ${element.col}`,
      sx={{
    <Box
  return (
function ElementWrapper({ element, children }: ElementWrapperProps) {

}
  children: React.ReactNode;
  element: VisualElementModel;
interface ElementWrapperProps {
```typescript

## Element Wrapper

```
}
  return type === 'table';
function isTableElement(type: VisualElementType): boolean {

}
  return ['flex', 'tabController', 'card'].includes(type);
function isContainerElement(type: VisualElementType): boolean {

}
  ].includes(type);
    'binaryTypeInput'
    'enumerationRadio',
    'enumerationCombo',
    'checkbox',
    'textArea',
    'timeInput',
    'dateTimeInput',
    'dateInput',
    'numericInput',
    'textInput',
  return [
function isInputElement(type: VisualElementType): boolean {
```typescript

## Type Guards for Element Types

```
registerCustomElement('customChart', CustomChartComponent);

};
  );
    />
      data={data[model.name]}
      type={model.annotations?.chartType || 'bar'}
    <Chart
  return (
const CustomChartComponent: ElementRenderer = ({ model, data }) => {
// Example: Custom chart component

}
  elementRegistry.register(type as VisualElementType, component);
) {
  component: ElementRenderer
  type: string,
function registerCustomElement(
// Register custom element
```typescript

## Custom Element Registration

```
}
  );
    />
      disabled={disabled}
      error={error}
      onChange={onChange ? (value) => onChange(element.attributeName, value) : undefined}
      data={data}
      model={element}
    <Component
  return (
  
  const disabled = !enabled || element.readOnly;
  const enabled = !element.enabledBy || data[element.enabledBy];
  // Determine if element is enabled
  
  if (hidden) return null;
  const hidden = element.hiddenBy && data[element.hiddenBy];
  // Handle conditional rendering
  
  }
    );
      </Alert>
        Unknown element type: {element.type}
      <Alert severity="error">
    return (
    console.error(`Unknown element type: ${element.type}`);
  if (!Component) {
  
  const Component = elementRegistry.get(element.type);
}: VisualElementRendererProps) {
  error 
  onChange, 
  data, 
  element, 
export function VisualElementRenderer({ 

}
  error?: string;
  onChange?: (fieldName: string, value: any) => void;
  data: any;
  element: VisualElementModel;
interface VisualElementRendererProps {
```typescript

## Visual Element Renderer Component

```
export const elementRegistry = new VisualElementRegistry();
// Singleton instance

}
  }
    return new Map(this.registry);
  getAll(): Map<VisualElementType, ElementRenderer> {
  
  }
    return this.registry.has(type);
  has(type: VisualElementType): boolean {
  
  }
    return this.registry.get(type);
  get(type: VisualElementType): ElementRenderer | undefined {
  
  }
    this.registry.delete(type);
  unregister(type: VisualElementType) {
  
  }
    this.registry.set(type, component);
  register(type: VisualElementType, component: ElementRenderer) {
  
  }
    this.register('alert', AlertComponent);
    this.register('spacer', SpacerComponent);
    this.register('divider', DividerComponent);
    this.register('text', TextDisplayComponent);
    // Display
    
    this.register('link', LinkComponent);
    // Navigation
    
    this.register('table', DataTableComponent);
    // Tables
    
    this.register('card', CardComponent);
    this.register('tabController', TabControllerComponent);
    this.register('flex', FlexContainerComponent);
    // Containers
    
    this.register('binaryTypeInput', BinaryTypeInputComponent);
    this.register('enumerationRadio', EnumerationRadioComponent);
    this.register('enumerationCombo', EnumerationComboComponent);
    this.register('checkbox', CheckboxComponent);
    this.register('textArea', TextAreaComponent);
    this.register('timeInput', TimeInputComponent);
    this.register('dateTimeInput', DateTimeInputComponent);
    this.register('dateInput', DateInputComponent);
    this.register('numericInput', NumericInputComponent);
    this.register('textInput', TextInputComponent);
    // Inputs
  private registerBuiltInElements() {
  
  }
    this.registerBuiltInElements();
  constructor() {
  
  private registry: Map<VisualElementType, ElementRenderer> = new Map();
class VisualElementRegistry {

}>;
  error?: string;
  onChange?: (value: any) => void;
  data: any;
  model: VisualElementModel;
type ElementRenderer = React.ComponentType<{
```typescript

## Registry Implementation

Visual Element Registry maps element types to React components, enabling dynamic rendering of any visual element from its model.

## Overview

**Blocks:** Dynamic element rendering  
**Dependencies:** All visual element specifications  
**Assigned:** AI Agent  
**Status:** Complete  
**Domain:** Components  


