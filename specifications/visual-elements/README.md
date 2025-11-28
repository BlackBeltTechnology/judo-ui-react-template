# Visual Elements Domain Specifications
- **Parallel (4 agents):** 3.5-5 hours
- **Sequential:** 12.5-18.75 hours
- **Estimated time per file:** 30-45 minutes

## Processing Time

```
[Test scenarios]
## Testing

[2+ examples]
## Examples

[ARIA attributes, keyboard nav]
### Accessibility

[Validation rules]
### Validation

[What user can do]
### User Interactions

## Behavior

\`\`\`
}
  // implementation notes
function ModelDriven[Element](props: [Element]Props) {

}
  // ...
  disabled: boolean;
  onChange: (value: any) => void;
  value: any;
  model: [Element]Model;
interface [Element]Props {
\`\`\`typescript

## React Component

\`\`\`
}
  };
    // element-specific config
  config: {
  type: '[elementType]';
interface [Element]Model extends VisualElementModel {
\`\`\`typescript
## Runtime Model

[All properties from ui.ecore]
## Metamodel Properties

[Element purpose]
## Overview

**Dependencies:** [List]
**Status:** [Draft|Complete]
**Metamodel:** `ui::[ClassName]`
**Domain:** Visual Elements / [Subdomain]

# [Element Name] Specification
```markdown

## Output Format

- [ ] Examples (at least 2)
- [ ] Accessibility requirements
- [ ] Styling/theming notes
- [ ] Validation logic
- [ ] Event handlers
- [ ] Props definition
- [ ] React component interface
- [ ] TypeScript interface for model
- [ ] Runtime model mapping
- [ ] Metamodel property documentation
Each file must include:

## Completion Criteria

All can start simultaneously after metamodel and runtime-model domains complete.

- **Agent 7:** `other/` (5 files)
- **Agent 6:** `tables/` (5 files)
- **Agent 5:** `containers/` (5 files)
- **Agent 4:** `inputs/` (10 files)
### Parallel (4 agents)

## Assignment Recommendations

- Within subdirectory, files can be parallel
- Each subdirectory can be processed independently
**Internal:**

- `runtime-model/04-visual-element-model.md` - Runtime model types
- `metamodel/11-input-base.md` - Base Input class
- `metamodel/02-visual-element.md` - Base VisualElement class
**External:**

## Dependencies

5. `05-icon-image.md` - Icon and IconImage
4. `04-divider-spacer.md` - Divider and Spacer
3. `03-label-text.md` - Label and Text elements
2. `02-button-element.md` - Button element
1. `01-link-element.md` - Link element
### other/ (5 files)

5. `05-inline-editing.md` - Inline editing support
4. `04-table-actions.md` - Table action buttons
3. `03-filter-definition.md` - Filter specifications
2. `02-column-definition.md` - Column specifications
1. `01-table-element.md` - Table visual element
### tables/ (5 files)

5. `05-grid-sizing.md` - Grid col/row sizing
4. `04-card-layouts.md` - Card/Frame layouts
3. `03-button-group.md` - ButtonGroup container
2. `02-tab-controller.md` - TabController and Tab
1. `01-flex-container.md` - Flex container with layout
### containers/ (5 files)

10. `10-binary-type-input.md` - BinaryTypeInput element
9. `09-enumeration-radio.md` - EnumerationRadio element
8. `08-enumeration-combo.md` - EnumerationCombo element
7. `07-checkbox.md` - Checkbox element
6. `06-textarea.md` - TextArea element
5. `05-time-input.md` - TimeInput element
4. `04-datetime-input.md` - DateTimeInput element
3. `03-date-input.md` - DateInput element
2. `02-numeric-input.md` - NumericInput element
1. `01-text-input.md` - TextInput element
### inputs/ (10 files)

## Files

- `other/` - Miscellaneous visual elements (5 files)
- `tables/` - Table-related specifications (5 files)
- `containers/` - Container element specifications (5 files)
- `inputs/` - Input field specifications (10 files)

## Subdirectories

**Processing:** Highly parallel - most files independent

**Status:** 0/25 Complete

**Purpose:** Specify all visual element types and their runtime implementations.


