# Metamodel Domain Specifications

**Purpose:** Document all elements from `ui.ecore` metamodel with complete property coverage.

**Status:** 0/15 Complete

**Processing:** Can be done in parallel - each file is independent

## Files in this Domain

### Core Elements (Process First)
- `01-named-element.md` - Base class for all named elements
- `02-visual-element.md` - Base class for all visual elements  
- `03-labeled-element.md` - Base class for elements with labels
- `04-reference-typed-visual-element.md` - Base for elements with data references

### Container Elements
- `05-page-definition.md` - Top-level page definition
- `06-page-container.md` - Container for page content (Form/View/Table)
- `07-container.md` - Base container class
- `08-flex.md` - Flex layout container

### Layout Elements
- `09-size-and-constraints.md` - Size, SizeConstraint, Align classes
- `10-tab-controller.md` - Tab controller and Tab classes

### Visual Components
- `11-input-base.md` - Base Input class
- `12-button-and-button-group.md` - Button and ButtonGroup classes
- `13-table.md` - Table, Column, Filter classes
- `14-link.md` - Link class
- `15-other-visual-elements.md` - Text, Label, Divider, Spacer, IconImage

## Dependencies

**External:**
- None - this domain documents the source metamodel

**Internal:**
- Files should be processed in order (base classes first)
- But can be split among multiple agents

## Assignment Recommendations

### Option 1: Single Agent
Process files 01-15 sequentially

### Option 2: Multiple Agents
- **Agent 1A:** Files 01-04 (Base classes)
- **Agent 1B:** Files 05-08 (Containers)  
- **Agent 1C:** Files 09-10 (Layout)
- **Agent 1D:** Files 11-15 (Visual components)

Wait for Agent 1A before starting others (base class dependencies)

## Completion Criteria

For each file:
- [ ] All Ecore properties documented with types
- [ ] All operations listed
- [ ] Inheritance hierarchy clear
- [ ] Relationships to other classes documented
- [ ] Enumerations fully defined (if applicable)
- [ ] Examples of actual instances from sample model

## Output Format

Each file should follow the template:

```markdown
# [Element Name] Specification

**Domain:** Metamodel
**Ecore Class:** `ui::[ClassName]`
**Status:** [Draft|Complete]

## Overview
[Brief description]

## Metamodel Definition

### Class Hierarchy
[Inheritance tree]

### Properties
| Name | Type | Multiplicity | Default | Description |
|------|------|--------------|---------|-------------|
| ... | ... | ... | ... | ... |

### Operations
| Name | Return Type | Parameters | Description |
|------|-------------|------------|-------------|
| ... | ... | ... | ... |

### Relationships
[What this connects to]

## Examples from Sample Model
[Real examples from ActionGroupTest-ui.model]

## Related Specifications
- [Links to related specs]
```

## Processing Time

- **Estimated time per file:** 30-45 minutes
- **Sequential:** 7.5-11.25 hours
- **Parallel (4 agents):** 2-3 hours

