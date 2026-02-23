# Relation Management

Generates React components for managing relationships between entities. Relations are displayed as Link components (single relations) or Table components (collection relations), with CRUD operations determined by the relation's kind, cardinality, and behaviours. The generator produces selector dialogs, autocomplete inputs, and inline creation forms based on the relation configuration.

## Requirements

### Requirement: Link Component Generation

The generator SHALL produce a `<SingleRelationInput>` or `<AssociationButton>` component from `Link` elements bound to a single (non-collection) `RelationType`.

Display columns (`parts`) SHALL show attribute values from the related entity. Action buttons SHALL provide set/unset/open operations based on the relation's behaviours. Autocomplete support SHALL be enabled when `autocompleteSetActionDefinition` is present.

For each link, the generator SHALL produce:
- `src/containers/{ContainerPath}/components/{LinkName}/index.tsx` — link component
- `src/containers/{ContainerPath}/components/{LinkName}/types.ts` — TypeScript types

#### Scenario: Link with set/unset actions

- **WHEN** a `Link` element is bound to an association relation with SET and UNSET behaviours
- **THEN** the link displays the related entity's attributes
- **AND** action buttons allow setting and unsetting the relation

#### Scenario: Link with autocomplete

- **WHEN** a `Link` element has `autocompleteSetActionDefinition` set
- **THEN** the link supports type-ahead search instead of opening a selector dialog

**Key Helpers**: `UiWidgetHelper.linkComponentName()`, `UiWidgetHelper.isLinkAssociation()`, `UiWidgetHelper.getFirstAutocompleteColumnForLink()`, `UiWidgetHelper.getSortColumnForLink()`, `UiPageContainerHelper.getLinksForPageContainers()`

**Template**: `actor/src/containers/components/link/index.tsx.hbs`

---

### Requirement: Association Relation Operations

The generator SHALL produce set, unset, and open actions for `RelationType` elements with `relationKind = ASSOCIATION`.

For single associations: **Set** SHALL open a selector dialog to pick one entity and bind it. **Unset** SHALL remove the binding (set to null). **Open** SHALL navigate to the related entity's view page.

For collection associations: **Add** SHALL open a selector to pick entities to add. **Remove** SHALL remove entities from the collection. **Open** SHALL navigate from a table row to the entity detail view.

Autocomplete SHALL be available as an alternative to the selector dialog.

#### Scenario: Single association set via selector

- **WHEN** an association relation has SET behaviour and `isCollection = false`
- **THEN** a selector dialog allows picking one entity to bind

#### Scenario: Collection association add via selector

- **WHEN** an association relation has ADD behaviour and `isCollection = true`
- **THEN** a multi-select selector dialog allows picking entities to add

**Actions Generated**: `SetActionDefinition`, `UnsetActionDefinition`, `OpenPageActionDefinition`, `OpenSetSelectorActionDefinition`, `AddActionDefinition`, `RemoveActionDefinition`, `OpenAddSelectorActionDefinition`, `AutocompleteSetActionDefinition`

---

### Requirement: Composition Relation Operations

The generator SHALL produce create, delete, and open actions for `RelationType` elements with `relationKind = COMPOSITION`.

For single compositions: **Create** SHALL open a create form; the entity is owned by the parent. **Delete** SHALL delete the child entity (cascading ownership). **Open** SHALL navigate to the child detail view.

For collection compositions: **Create** SHALL open a create form or inline-create a new child. **Delete** SHALL remove and delete the child from the collection. **Open** SHALL navigate from a table row to the child detail view.

#### Scenario: Single composition create

- **WHEN** a composition relation has CREATE behaviour and `isCollection = false`
- **THEN** a create form opens to create the owned child entity

#### Scenario: Collection composition delete

- **WHEN** a composition relation has DELETE behaviour and `isCollection = true`
- **THEN** deleting a row removes and deletes the child entity

**Actions Generated**: `CreateActionDefinition`, `DeleteActionDefinition`, `OpenCreateFormActionDefinition`, `OpenPageActionDefinition`, `RowOpenPageActionDefinition`

---

### Requirement: Aggregation Relation Operations

The generator SHALL produce add, remove, create, delete, and open actions for `RelationType` elements with `relationKind = AGGREGATION` and `isCollection = true`.

**Add** SHALL open a selector dialog to pick entities to add to the collection. **Remove** SHALL remove an entity from the collection without deleting it. **Create** SHALL create new entities into the collection. **Delete** SHALL delete entities from the collection. **Bulk remove** SHALL remove multiple selected rows at once.

#### Scenario: Aggregation add and remove

- **WHEN** an aggregation relation has ADD and REMOVE behaviours
- **THEN** entities can be added via a selector and removed from the collection
- **AND** removal does not delete the entity

**Actions Generated**: `AddActionDefinition`, `RemoveActionDefinition`, `OpenAddSelectorActionDefinition`, `AutocompleteAddActionDefinition`, `BulkRemoveActionDefinition`

---

### Requirement: Derived Relation Display

The generator SHALL produce read-only table display for `RelationType` elements with `memberType = DERIVED`.

No CRUD operations SHALL be available. The data SHALL be computed/derived server-side. Filtering, sorting, and navigation MAY still be supported.

#### Scenario: Derived collection table

- **WHEN** a derived relation is a collection
- **THEN** it is displayed as a read-only table with no create/update/delete actions

---

### Requirement: Transient Relation

The generator SHALL produce components for `RelationType` elements with `memberType = TRANSIENT`.

Relation data SHALL exist only during the current session and SHALL NOT be persisted to the backend. Full CRUD MAY be available client-side.

#### Scenario: Transient relation

- **WHEN** a transient relation exists
- **THEN** the relation data is managed client-side without persistence

---

### Requirement: Selector Dialog Generation

The generator SHALL produce selector dialog pages from `OpenAddSelectorActionDefinition` and `OpenSetSelectorActionDefinition` elements.

The selector SHALL present a table with filtering and sorting for entity selection. **Add Selector** SHALL support multi-select and return selected entities. **Set Selector** SHALL support single-select and return one entity. Pagination SHALL be controlled by `selectorRowsPerPage`.

#### Scenario: Add selector (multi-select)

- **WHEN** a selector page is opened for an Add action
- **THEN** the table allows multi-row selection
- **AND** the selected entities are returned on confirmation

#### Scenario: Set selector (single-select)

- **WHEN** a selector page is opened for a Set action
- **THEN** the table allows single-row selection
- **AND** the selected entity is returned on confirmation

**Key Helpers**: `UiActionsHelper.getRangeActionDefinitionForTable()`

---

### Requirement: Autocomplete for Relations

The generator SHALL produce type-ahead autocomplete components when link or table elements have autocomplete action definitions.

A text input SHALL provide suggestions from the relation range as the user types. `autoCompleteRows` SHALL limit the number of suggestions displayed. For links, `autocompleteSetActionDefinition` SHALL enable set-by-typing. For tables, `autocompleteRangeActionDefinition` and `autocompleteAddActionDefinition` SHALL enable add-by-typing.

#### Scenario: Autocomplete set on link

- **WHEN** a link has `autocompleteSetActionDefinition` set
- **THEN** typing in the field shows suggestions from the relation range
- **AND** selecting a suggestion sets the relation

**Key Helpers**: `UiWidgetHelper.isAutocompleteAvailable()`, `UiWidgetHelper.calculateLinkAutocompleteRows()`, `UiWidgetHelper.calculateTableAutocompleteRows()`

---

### Requirement: Inline Creatable Relations

When a `RelationType` has `isInlineCreatable = true`, the generator SHALL produce inline row creation in the relation table.

An `InlineCreateRowActionDefinition` SHALL add an empty row. The user SHALL fill values inline and save on blur or explicit action.

#### Scenario: Inline creatable relation

- **WHEN** a relation has `isInlineCreatable = true`
- **THEN** new rows can be added directly in the table without opening a dialog

---

### Requirement: Relation Data Masking

The generator SHALL compute data masks for relation components.

Link mask SHALL include parts' attribute types plus `additionalMaskAttributes`. `additionalMaskRelations` SHALL enable nested relation prefetching. The mask SHALL determine what data the API returns for the relation.

#### Scenario: Link with additional mask

- **WHEN** a link has `additionalMaskAttributes` and `additionalMaskRelations` set
- **THEN** the mask includes both display attributes and additional prefetch attributes

**Key Helpers**: `UiPageContainerHelper.getMaskForLink()`, `UiPageContainerHelper.serializeMaskForLink()`

---

## Integration Test Coverage

- **RelationTest**: Comprehensive coverage of all relation combinations:
  - ManyAggregationComposition, ManyAggregationAssociation
  - ManyAssociationAssociation, ManyAssociationComposition
  - ManyDerivedAggregation, ManyDerivedAssociation
  - ManyTransient
  - Tag, Card, and Table representations
  - Inline editing within relations
- **ActionGroupTest**: Relation navigation (Galaxy → Stars, Matter)
- **CRUDActionsTest**: Relation CRUD across different dashboard types
