# Link Specification

**Domain:** Metamodel  
**Ecore Class:** `ui::Link`  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `02-visual-element.md`, `04-reference-typed-visual-element.md`, `02-relation-type.md`  
**Blocks:** Navigation patterns, relation navigation  

## Overview

`Link` is a visual element that provides navigation to related entities. It represents a clickable element that navigates to a page showing related data, typically following a RelationType from the current entity. Links enable master-detail navigation and related entity exploration.

## Metamodel Definition

### Class Hierarchy

```
NamedElement (abstract)
├─ VisualElement (abstract)
├─ LabeledElement (abstract)
└─ ReferenceTypedVisualElement (abstract)
   └─ Link
```

### Properties

| Name | Type | Multiplicity | Default | Description |
|------|------|--------------|---------|-------------|
| `name` | EString | 1 | - | Link identifier (inherited) |
| `sourceId` | EString | 0..1 | - | XMIID from source (inherited) |
| `label` | EString | 0..1 | - | Link text (inherited) |
| `icon` | Icon | 0..1 | - | Link icon (inherited) |
| `dataElement` | RelationType | 1 | - | Relation to navigate (inherited) |
| `targetPage` | PageDefinition | 0..1 | - | Page to open (auto-determined if null) |
| `displayValue` | AttributeType | 0..1 | - | Attribute to show as link text |

### Inherited Properties

From VisualElement:
- `col`, `row` - Grid positioning
- `hiddenBy`, `enabledBy` - Conditional rendering
- `size`, `stretch`, `fit` - Layout behavior

### Relationships

**Contains:**
- Inherited from base classes

**Referenced By:**
- Container children
- View containers (master-detail patterns)

**References:**
- `dataElement: RelationType` - The relation to navigate
- `targetPage: PageDefinition` - Destination page (optional)
- `displayValue: AttributeType` - Attribute for link text

## Key Concepts

### Relation Navigation

Links follow RelationType references:
```xml
<children xsi:type="ui:Link" 
          name="posts" 
          label="View Posts"
          dataElement="User#posts"/>
```

**Navigation:**
- From: User entity
- To: Posts related to this user
- Opens: List of posts or detail page

### Target Page Determination

**Explicit Target:**
```xml
<children xsi:type="ui:Link" 
          name="profile" 
          dataElement="User#profile"
          targetPage="UserProfilePage"/>
```

**Auto-Determined:**
```xml
<children xsi:type="ui:Link" 
          name="posts" 
          dataElement="User#posts"/>
```
System finds appropriate page for target type.

### Display Value

**Default:** Relation name as label
```xml
<children xsi:type="ui:Link" name="posts" dataElement="User#posts"/>
```
Shows: "Posts"

**Custom Label:**
```xml
<children xsi:type="ui:Link" 
          name="posts" 
          label="View All Posts"
          dataElement="User#posts"/>
```
Shows: "View All Posts"

**Dynamic from Data:**
```xml
<children xsi:type="ui:Link" 
          name="author" 
          dataElement="Post#author"
          displayValue="User#fullName"/>
```
Shows author's full name as link text.

### Cardinality Handling

**ONE Relation (Single Entity):**
```xml
<children xsi:type="ui:Link" 
          name="author" 
          dataElement="Post#author"/>
```
Opens detail page for that author.

**MANY Relation (Collection):**
```xml
<children xsi:type="ui:Link" 
          name="posts" 
          dataElement="User#posts"/>
```
Opens table/list of posts.

### Conditional Links

```xml
<children xsi:type="ui:Link" 
          name="premium Features" 
          dataElement="User#premiumFeatures"
          hiddenBy="User#isPremium"
          col="12"/>
```

Hidden when user is not premium.

## Examples from Sample Model

### Example 1: Simple ONE Relation Link

```xml
<children xsi:type="ui:Link" 
          name="department" 
          label="View Department"
          dataElement="Employee#department"
          col="6">
  <icon name="business"/>
</children>
```

**Runtime:**
```typescript
<Link 
  href={`/departments/${data.department.id}`}
  startIcon={<BusinessIcon />}
>
  View Department
</Link>
```

### Example 2: MANY Relation Link

```xml
<children xsi:type="ui:Link" 
          name="orders" 
          label="View Orders"
          dataElement="Customer#orders"
          col="12">
  <icon name="shopping_cart"/>
</children>
```

**Runtime:**
```typescript
<Link href={`/customers/${data.id}/orders`}>
  <ShoppingCartIcon />
  View Orders ({data.orders?.length || 0})
</Link>
```

### Example 3: Link with Display Value

```xml
<children xsi:type="ui:Link" 
          name="createdBy" 
          label="Created By"
          dataElement="Document#createdBy"
          displayValue="User#fullName"
          col="6"/>
```

**Runtime:**
```typescript
<Link href={`/users/${data.createdBy.id}`}>
  {data.createdBy.fullName}  {/* Instead of "Created By" */}
</Link>
```

### Example 4: Link with Explicit Target Page

```xml
<children xsi:type="ui:Link" 
          name="editProfile" 
          label="Edit Profile"
          dataElement="User#profile"
          targetPage="UserProfileEditPage"
          col="12"/>
```

### Example 5: Conditional Link

```xml
<children xsi:type="ui:Link" 
          name="manager" 
          label="View Manager"
          dataElement="Employee#manager"
          hiddenBy="Employee#isTopLevel"
          col="6"/>
```

**Runtime:**
```typescript
{!data.isTopLevel && (
  <Link href={`/employees/${data.manager.id}`}>
    View Manager
  </Link>
)}
```

### Example 6: Link in Card Layout

```xml
<children xsi:type="ui:Flex" direction="VERTICAL" col="6">
  <children xsi:type="ui:Text" name="postsLabel" value="Posts"/>
  <children xsi:type="ui:Link" 
            name="viewPosts" 
            label="View all posts"
            dataElement="User#posts">
    <icon name="arrow_forward"/>
  </children>
</children>
```

## Validation Rules

### Required Properties
- `name` - Must be unique
- `dataElement` - Must reference valid RelationType

### Constraints
- DataElement must be RelationType (not ClassType)
- Target page must be compatible with relation target type
- DisplayValue must be attribute of target ClassType
- Cannot link to same entity (circular reference warning)

### Best Practices
- Use descriptive labels
- Include icon for visual recognition
- Show count for MANY relations
- Provide tooltip with details
- Consider conditional display

## Runtime Model Mapping

```typescript
interface LinkModel extends VisualElementModel {
  type: 'link';
  
  // Relation
  relationName: string;            // dataElement.name
  relationFqn: string;             // dataElement FQN
  targetType: string;              // Target ClassType name
  cardinality: 'one' | 'many';     // Relation cardinality
  
  // Navigation
  targetPageName?: string;         // targetPage.name
  targetPagePath?: string;         // Generated path
  
  // Display
  displayAttributeName?: string;   // displayValue.name
  
  // Link properties
  label?: string;
  icon?: IconModel;
  
  // Conditional
  hiddenBy?: string;
  enabledBy?: string;
  
  // Inherited from VisualElement
  col: number;
}

// Generated navigation
interface LinkNavigation {
  path: string;                    // e.g., "/users/123/posts"
  params: Record<string, any>;     // Route parameters
  state?: any;                     // Navigation state
}
```

## Generator Implementation

See `generators/04-visual-element-extractor.md`.

**Key Methods:**
```java
public class LinkGenerator {
    public static LinkModel extractLink(Link link);
    public static String getTargetPath(Link link);
    public static String getDisplayValue(Link link, Object data);
    public static PageDefinition resolveTargetPage(Link link);
    public static boolean isManyRelation(Link link);
}
```

## Common Patterns

### Link Rendering

```typescript
function RelationLink({ model, data, navigate }: Props) {
  const displayText = model.displayAttributeName 
    ? data[model.relationName]?.[model.displayAttributeName]
    : model.label || model.relationName;
  
  const count = model.cardinality === 'many' 
    ? data[model.relationName]?.length 
    : undefined;
  
  const handleClick = (e: React.MouseEvent) => {
    e.preventDefault();
    const path = generateLinkPath(model, data);
    navigate(path);
  };
  
  return (
    <MuiLink
      component="button"
      onClick={handleClick}
      startIcon={model.icon && <Icon name={model.icon.name} />}
    >
      {displayText} {count !== undefined && `(${count})`}
    </MuiLink>
  );
}
```

### Path Generation

```typescript
function generateLinkPath(link: LinkModel, ownerData: any): string {
  const ownerId = ownerData.id;
  
  if (link.cardinality === 'one') {
    // Navigate to related entity detail
    const relatedId = ownerData[link.relationName]?.id;
    return `/entities/${link.targetType}/${relatedId}`;
  } else {
    // Navigate to related entities list
    return `/entities/${ownerData.__type}/${ownerId}/${link.relationName}`;
  }
}
```

### Prefetching

```typescript
// Prefetch related data on hover
const handleMouseEnter = async () => {
  if (link.cardinality === 'one') {
    await service.prefetch(data[link.relationName].id);
  }
};
```

## Testing Criteria

### Unit Tests
- [x] Link extraction from metamodel
- [x] Target page resolution
- [x] Path generation
- [x] Display value determination
- [x] Cardinality detection

### Integration Tests
- [x] ONE relation link navigates correctly
- [x] MANY relation link navigates correctly
- [x] Display value shows correctly
- [x] Count displayed for collections
- [x] Conditional links show/hide
- [x] Navigation preserves context

### Edge Cases
- [x] Null relation value
- [x] Empty collection
- [x] Missing target page
- [x] Very long display values
- [x] Circular references

## Related Specifications

**Metamodel:**
- `04-reference-typed-visual-element.md` - Base class
- `02-visual-element.md` - Inherited properties

**Data Model:**
- `02-relation-type.md` - Relation definitions

**Runtime Model:**
- `runtime-model/04-visual-element-model.md` - LinkModel interface

**Visual Elements:**
- `visual-elements/other/01-link-element.md` - Detailed implementation

**Actions:**
- `actions/12-navigate-action.md` - Navigation system

---

**Status:** ✅ Complete  
**Created:** 2025-11-28  
**Last Updated:** 2025-11-28  
**Reviewed:** Not yet reviewed

