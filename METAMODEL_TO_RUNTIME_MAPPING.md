# Metamodel to Runtime Model Mapping Reference

## Quick Reference Guide

This document provides a mapping between the judo-meta-ui metamodel (Ecore) and the proposed TypeScript runtime model structures.

---

## Core Element Mappings

### PageDefinition → PageModel

| Metamodel Property | Runtime Model Property | Notes |
|-------------------|----------------------|-------|
| `name` | `name` | Direct mapping |
| `container` | `container` | Reference to ContainerModel |
| `actions` | `actions` | Array of ActionModel |
| `openInDialog` | `openInDialog` | Boolean |
| `dialogSize` | `dialogSize` | 'xs' \| 'sm' \| 'md' \| 'lg' \| 'xl' |
| `dashboard` | `isDashboard` | Boolean |
| `isSelector` | `isSelector` | Boolean |
| `dataElement` (RelationType/ClassType) | `dataElement` | String reference |
| `label` | `i18n.keys.title` | Stored in i18n section |

### PageContainer → ContainerModel

| Metamodel Property | Runtime Model Property | Notes |
|-------------------|----------------------|-------|
| `type` (TABLE/FORM/VIEW) | `type` | 'table' \| 'form' \| 'view' |
| `dataElement` | `dataElement` | String reference to ClassType |
| `children` (VisualElement[]) | `visualElements` | Flattened array |
| `actionButtonGroups` | `actionButtonGroups` | Array of ButtonGroupModel |
| `direction` | `layout.direction` | 'horizontal' \| 'vertical' |
| `mainAxisAlignment` | `layout.mainAxisAlignment` | Layout enum |
| `crossAxisAlignment` | `layout.crossAxisAlignment` | Layout enum |
| `titleAttribute` | `titleAttribute` | String reference |
| `additionalMaskAttributes` | `additionalMaskAttributes` | String[] |

### VisualElement → VisualElementModel

| Metamodel Property | Runtime Model Property | Notes |
|-------------------|----------------------|-------|
| `name` | `name` | Unique identifier |
| `label` | `label` | Display text |
| `icon` | `icon` | Icon path |
| `col` | `col` | Grid column span |
| `row` | `row` | Grid row |
| `disabled` | Computed via `enabledBy` | Dynamic |
| `isReadOnly` | `isReadOnly` | Boolean |
| `hiddenBy` (AttributeType) | `hiddenBy` | String reference |
| `enabledBy` (AttributeType) | `enabledBy` | String reference |
| `requiredBy` (AttributeType) | `requiredBy` | String reference |
| `stretch` | `stretch` | 'none' \| 'horizontal' \| 'vertical' \| 'both' |
| `fit` | `fit` | 'none' \| 'loose' \| 'tight' |
| `size` | `size` | { width?, height? } |
| `customImplementation` | `customImplementation` | Boolean |
| `subTheme` | `subTheme` | String |
| `onBlur` | `onBlur` | Boolean |
| `order` | Implicit via array order | Sorted in generator |

### Input Types → Config

#### TextInput → VisualElementModel

| Metamodel Property | Runtime Model Property | Location |
|-------------------|----------------------|----------|
| `attributeType` | `attributeType` | Root |
| `required` | `isRequired` | Root |
| `tooltipText` | `tooltipText` | Root |
| `mask` | `config.mask` | Config object |
| `regexp` | `config.pattern` | Config object |
| `maxLength` | `config.maxLength` | Config object |
| `isTypeAheadField` | `config.typeAhead` | Config object |

#### NumericInput → VisualElementModel

| Metamodel Property | Runtime Model Property | Location |
|-------------------|----------------------|----------|
| `attributeType` | `attributeType` | Root |
| `scale` | `config.scale` | Config object |
| `precision` | `config.precision` | Config object |
| `formatValue` | `config.formatValue` | Config object |
| `minValueBy` (via InputValueConstraint) | `config.minValueBy` | Config object |
| `maxValueBy` (via InputValueConstraint) | `config.maxValueBy` | Config object |

#### DateInput / DateTimeInput → VisualElementModel

| Metamodel Property | Runtime Model Property | Location |
|-------------------|----------------------|----------|
| `attributeType` | `attributeType` | Root |
| `baseUnit` (DateTimeInput only) | `config.baseUnit` | Config object |
| `minValueBy` | `config.minValueBy` | Config object |
| `maxValueBy` | `config.maxValueBy` | Config object |

### Table → VisualElementModel

| Metamodel Property | Runtime Model Property | Location |
|-------------------|----------------------|----------|
| `columns` | `config.columns` | Config object |
| `filters` | `config.filters` | Config object |
| `rowsPerPage` | `config.rowsPerPage` | Config object |
| `isEager` | `config.isEager` | Config object |
| `allowSelectMultiple` | `config.allowSelectMultiple` | Config object |
| `showTotalCount` | `config.showTotalCount` | Config object |
| `isInlineEditable` | `config.isInlineEditable` | Config object |
| `representationComponent` | `config.representation` | 'table' \| 'tag' \| 'card' |
| `tableActionButtonGroup` | See ButtonGroup mapping | Separate model |
| `rowActionButtonGroup` | See ButtonGroup mapping | Separate model |

### Column → ColumnModel

| Metamodel Property | Runtime Model Property | Notes |
|-------------------|----------------------|-------|
| `name` | `name` | Column identifier |
| `label` | `label` | Display text |
| `attributeType` | `attributeType` | String reference |
| `format` | `format` | Format string |
| `sort` | `sort` | 'asc' \| 'desc' \| 'none' |
| `sortPrecedence` | `sortPrecedence` | Number |
| `width` | `width` | String (CSS value) |
| `formatValue` | `formatValue` | Boolean |
| `representsRelation` | `representsRelation` | String reference |

### Link → VisualElementModel

| Metamodel Property | Runtime Model Property | Location |
|-------------------|----------------------|----------|
| `parts` (Column[]) | `config.columns` | Config object |
| `relationName` | `config.relationName` | Config object |
| `dataElement` | `dataElement` | Root |
| `isEager` | `config.isEager` | Config object |
| `selectorRowsPerPage` | `config.selectorRowsPerPage` | Config object |
| `autoCompleteRows` | `config.autoCompleteRows` | Config object |
| `additionalMaskAttributes` | `config.additionalMaskAttributes` | Config object |

### ButtonGroup → ButtonGroupModel

| Metamodel Property | Runtime Model Property | Notes |
|-------------------|----------------------|-------|
| `buttons` | `buttons` | Array of ButtonModel |
| `featuredActions` | `featuredActions` | Number of featured buttons |

### Button → ButtonModel

| Metamodel Property | Runtime Model Property | Notes |
|-------------------|----------------------|-------|
| `name` | `name` | Identifier |
| `label` | `label` | Display text |
| `icon` | `icon` | Icon path |
| `actionDefinition` | `action` | Reference to ActionModel ID |
| `preFetchActionDefinition` | `preFetchAction` | Reference to ActionModel ID |
| `buttonStyle` | `buttonStyle` | String |
| `confirmation` | Stored in ActionModel | Moved to action |
| `tooltipText` | `tooltipText` | String |
| `hiddenBy` | `hiddenBy` | String reference |
| `enabledBy` | `enabledBy` | String reference |
| `relationName` | `relationName` | String |

---

## Action Mappings

### ActionDefinition → ActionModel

Base properties for all action types:

| Metamodel Property | Runtime Model Property | Notes |
|-------------------|----------------------|-------|
| `name` | `name` | Action identifier |
| `isTransient` | `isTransient` | Boolean |
| `targetType` (ClassType) | `targetType` | String reference |
| `isContainedRelationAction` | `isContainedRelation` | Boolean |
| `isBulk` | `isBulk` | Boolean |

### Action Type Detection → type Property

The metamodel uses multiple ActionDefinition subclasses. Map to string type:

| Metamodel Class | Runtime Model type Value |
|----------------|-------------------------|
| `RefreshActionDefinition` | `'refresh'` |
| `CreateActionDefinition` | `'create'` |
| `UpdateActionDefinition` | `'update'` |
| `DeleteActionDefinition` | `'delete'` |
| `AddActionDefinition` | `'add'` |
| `RemoveActionDefinition` | `'remove'` |
| `SetActionDefinition` | `'set'` |
| `UnsetActionDefinition` | `'unset'` |
| `CallOperationActionDefinition` | `'callOperation'` |
| `OpenPageActionDefinition` | `'openPage'` |
| `OpenFormActionDefinition` | `'openForm'` |
| `OpenSelectorActionDefinition` | `'openSelector'` |
| `FilterActionDefinition` | `'filter'` |
| `ClearActionDefinition` | `'clear'` |
| `BackActionDefinition` | `'back'` |
| `CancelActionDefinition` | `'cancel'` |
| `ExportActionDefinition` | `'export'` |
| `GetTemplateActionDefinition` | `'getTemplate'` |
| `BulkDeleteActionDefinition` | `'bulkDelete'` |
| `BulkRemoveActionDefinition` | `'bulkRemove'` |
| `RowDeleteActionDefinition` | `'rowDelete'` |
| `BulkCallOperationActionDefinition` | `'bulkCallOperation'` |
| `CustomActionDefinition` | `'custom'` |

### CallOperationActionDefinition → ActionModel

| Metamodel Property | Runtime Model Property | Notes |
|-------------------|----------------------|-------|
| `operation` (OperationType) | `operation` | String reference to operation name |

### OpenFormActionDefinition → ActionModel

| Metamodel Property | Runtime Model Property | Notes |
|-------------------|----------------------|-------|
| `formFor` (ActionDefinition) | `formFor` | String reference to action ID |

### OpenSelectorActionDefinition → ActionModel

| Metamodel Property | Runtime Model Property | Notes |
|-------------------|----------------------|-------|
| `selectorFor` (ActionDefinition) | `selectorFor` | String reference to action ID |

### CreateActionDefinition → ActionModel

| Metamodel Property | Runtime Model Property | Notes |
|-------------------|----------------------|-------|
| `autoOpenAfterCreate` | `autoOpenAfterCreate` | Boolean |

### UpdateActionDefinition → ActionModel

| Metamodel Property | Runtime Model Property | Notes |
|-------------------|----------------------|-------|
| `autoCloseOnSave` | `autoCloseOnSave` | Boolean |

### CustomActionDefinition → ActionModel

| Metamodel Property | Runtime Model Property | Notes |
|-------------------|----------------------|-------|
| `actionName` | `customActionName` | String |

### Confirmation → ConfirmationModel

| Metamodel Property | Runtime Model Property | Notes |
|-------------------|----------------------|-------|
| `confirmationType` | `type` | 'none' \| 'conditional' \| 'mandatory' |
| `confirmationMessage` | `message` | String |
| `confirmationCondition` (AttributeType) | `conditionAttribute` | String reference |

---

## Data Model Mappings

### ClassType → DataElementInfo

Store minimal class info in models:

| Metamodel Property | Usage in Runtime Model |
|-------------------|----------------------|
| `name` | String reference (e.g., "User") |
| `attributes` | Validation rules, field configs |
| `relations` | Link configs, navigation |
| `operations` | Action operation references |
| `behaviours` | Action availability |
| `representation` | Display mask attribute |

### RelationType → RelationInfo

| Metamodel Property | Usage in Runtime Model |
|-------------------|----------------------|
| `name` | String reference |
| `target` (ClassType) | Target class reference |
| `isCollection` | Determines table vs single |
| `relationKind` | Navigation behavior |
| `behaviours` | Available actions |
| `isReadOnly` | Action availability |

### AttributeType → AttributeInfo

| Metamodel Property | Usage in Runtime Model |
|-------------------|----------------------|
| `name` | Field reference |
| `dataType` | Type info for validation |
| `memberType` | Field behavior |
| `isReadOnly` | Field editability |
| `isRequired` | Validation rule |
| `isFilterable` | Filter availability |

### OperationType → OperationInfo

| Metamodel Property | Usage in Runtime Model |
|-------------------|----------------------|
| `name` | Operation reference |
| `input` (OperationParameterType) | Input form reference |
| `output` (OperationParameterType) | Result handling |
| `operationType` | 'mapped' \| 'static' |
| `faults` | Error handling |

### EnumerationType → EnumModel

| Metamodel Property | Runtime Model Property |
|-------------------|----------------------|
| `name` | `name` |
| `members` | `members` array |

### EnumerationMember → EnumMemberModel

| Metamodel Property | Runtime Model Property |
|-------------------|----------------------|
| `name` | `value` |
| `ordinal` | `ordinal` |
| `sourceId` (XMIID) | `id` |

---

## Layout Mappings

### Flex Layout → LayoutModel

| Metamodel Property | Runtime Model Property |
|-------------------|----------------------|
| `direction` (HORIZONTAL/VERTICAL) | `direction` ('horizontal'/'vertical') |
| `mainAxisAlignment` | `mainAxisAlignment` |
| `crossAxisAlignment` | `crossAxisAlignment` |
| `mainAxisSize` | `mainAxisSize` ('min'/'max') |

### Alignment Values

| Metamodel Enum | Runtime String |
|---------------|---------------|
| `MainAxisAlignment.CENTER` | `'center'` |
| `MainAxisAlignment.END` | `'end'` |
| `MainAxisAlignment.START` | `'start'` |
| `MainAxisAlignment.SPACEAROUND` | `'spaceAround'` |
| `MainAxisAlignment.SPACEBETWEEN` | `'spaceBetween'` |
| `MainAxisAlignment.SPACEEVENLY` | `'spaceEvenly'` |
| `CrossAxisAlignment.BASELINE` | `'baseline'` |
| `CrossAxisAlignment.CENTER` | `'center'` |
| `CrossAxisAlignment.END` | `'end'` |
| `CrossAxisAlignment.START` | `'start'` |
| `CrossAxisAlignment.STRETCH` | `'stretch'` |

---

## Validation Mapping

### From AttributeType

| Source | Validation Rule Type | Config |
|--------|---------------------|--------|
| `attributeType.isRequired` | `required` | `{ type: 'required' }` |
| `dataType.maxLength` (StringType) | `maxLength` | `{ type: 'maxLength', value: N }` |
| `dataType.regExp` (StringType) | `pattern` | `{ type: 'pattern', value: 'regex' }` |
| `dataType.precision` (NumericType) | Stored in config | Used for display/validation |
| `dataType.scale` (NumericType) | Stored in config | Used for display/validation |

### From InputValueConstraint

| Source | Validation Rule Type | Config |
|--------|---------------------|--------|
| `minValueBy` (AttributeType) | `minValue` | `{ type: 'minValue', minValueBy: 'attrName' }` |
| `maxValueBy` (AttributeType) | `maxValue` | `{ type: 'maxValue', maxValueBy: 'attrName' }` |

---

## I18n Mapping

### Key Generation Pattern

| Element | Key Pattern | Example |
|---------|------------|---------|
| Page title | `{keyPrefix}.title` | `pages.user.form.title` |
| Field label | `{keyPrefix}.{fieldName}.label` | `pages.user.form.firstName.label` |
| Field tooltip | `{keyPrefix}.{fieldName}.tooltip` | `pages.user.form.email.tooltip` |
| Button label | `{keyPrefix}.actions.{actionName}` | `pages.user.form.actions.save` |
| Validation message | `judo.error.validation-failed.{TYPE}` | `judo.error.validation-failed.REQUIRED` |
| Enum member | `enumerations.{EnumName}.{memberName}` | `enumerations.UserRole.ADMIN` |

---

## Generator Java Helper Signatures

```java
// Core model generation
public static PageModelData extractPageModel(PageDefinition page);
public static ContainerModelData extractContainerModel(PageContainer container);
public static List<VisualElementModelData> extractVisualElements(PageContainer container);
public static List<ActionModelData> extractActions(PageDefinition page);
public static List<ButtonGroupModelData> extractButtonGroups(PageContainer container);

// Visual element extraction
public static VisualElementModelData extractVisualElement(VisualElement ve);
public static String getVisualElementType(VisualElement ve);
public static Map<String, Object> getVisualElementConfig(VisualElement ve);

// Action extraction
public static ActionModelData extractAction(Action action);
public static String getActionType(ActionDefinition actionDef);
public static ConfirmationModelData extractConfirmation(Confirmation confirmation);

// Validation extraction
public static List<ValidationRuleModelData> extractValidationRules(PageContainer container);
public static ValidationRuleModelData extractAttributeValidation(AttributeType attributeType, Input input);

// I18n extraction
public static I18nModelData extractI18nKeys(PageDefinition page);
public static String getTranslationKeyPrefix(PageDefinition page);

// Reference resolution
public static String getDataElementReference(DataElement element);
public static String getAttributeReference(AttributeType attribute);
public static String getRelationReference(RelationType relation);
```

---

## Model File Organization

### Directory Structure

```
src/
└── models/
    ├── types.ts                    # Shared model type definitions
    ├── enums.ts                    # All enum definitions
    ├── validation.ts               # Validation types
    ├── pages/
    │   ├── index.ts                # Page model exports
    │   ├── user-form.model.ts      # Individual page models
    │   ├── user-table.model.ts
    │   └── ...
    └── data/
        ├── class-types.ts          # ClassType metadata
        ├── relations.ts            # RelationType metadata
        └── operations.ts           # OperationType metadata
```

### Import Pattern

```typescript
// In generated page
import { UserFormPageModel } from '~/models/pages/user-form.model';
import type { PageModel } from '~/models/types';
import { UserClassType } from '~/models/data/class-types';
```

---

## Serialization Strategy

### Model Size Optimization

1. **Omit defaults:** Don't include properties that match default values
2. **Reference by ID:** Use string references instead of nested objects
3. **Compress common patterns:** Use inheritance/extends
4. **Tree-shakeable:** Each model is a separate module

### Example Optimized Model

```typescript
// Instead of including full nested objects
visualElements: [
  {
    type: 'textInput',
    // ... 20 properties, half are defaults
  }
]

// Use a more compact form
visualElements: [
  {
    type: 'textInput',
    name: 'firstName',
    col: 6,
    attributeType: 'firstName',
    // Only non-default properties
    // Defaults handled by runtime
  }
]
```

---

## Runtime Resolution

### Reference Resolution at Runtime

```typescript
// Model stores string references
{
  dataElement: 'User',
  attributeType: 'firstName',
  enabledBy: 'isEditable'
}

// Runtime resolves to actual objects/values
const resolvedElement = {
  dataElement: ClassTypeRegistry.get('User'),
  attributeValue: data.firstName,
  enabled: data.isEditable,
};
```

### Lazy Loading Strategy

```typescript
// Page models can be lazy loaded
const UserFormPageModel = lazy(() => import('~/models/pages/user-form.model'));

// Used with Suspense
<Suspense fallback={<Loading />}>
  <ModelDrivenPage model={UserFormPageModel} />
</Suspense>
```

---

## Implementation Checklist

### Phase 1: Basic Model Generation
- [ ] Define TypeScript model types
- [ ] Create Java model data classes
- [ ] Implement extraction helpers
- [ ] Generate simple form model
- [ ] Test with basic inputs

### Phase 2: Complete Visual Elements
- [ ] All input type extractors
- [ ] Table model extraction
- [ ] Link model extraction
- [ ] Layout model extraction
- [ ] Tab controller extraction

### Phase 3: Actions & Behaviors
- [ ] All action type extractors
- [ ] Confirmation extraction
- [ ] Button group extraction
- [ ] Operation reference resolution

### Phase 4: Validation & I18n
- [ ] Validation rule extraction
- [ ] I18n key generation
- [ ] Enum model generation
- [ ] Custom validation support

### Phase 5: Optimization
- [ ] Model size optimization
- [ ] Reference compression
- [ ] Default value omission
- [ ] Tree-shaking support

---

## References

- **Metamodel:** `/ui.ecore`
- **Runtime Proposal:** `RUNTIME_MODEL_GENERATION_PROPOSAL.md`
- **Optimization Suggestions:** `CODE_GENERATION_OPTIMIZATION_SUGGESTIONS.md`

