# Page Model Generator Specification

**Domain:** Generators  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `01-generator-utilities.md`, `runtime-model/02-page-model.md`  
**Blocks:** Page model generation  

## Overview

Page Model Generator extracts PageDefinition elements from the metamodel and generates TypeScript PageModel files with complete configuration including containers, actions, and i18n.

## Generator Implementation

```java
package hu.blackbelt.judo.ui.generator.react.runtime;

public class PageModelGenerator extends RuntimeModelGenerator {
    
    private final TypeMapper typeMapper;
    private final XmiIdResolver xmiIdResolver;
    
    public PageModelGenerator(UiModelContext context) {
        super(context);
        this.typeMapper = new TypeMapper();
        this.xmiIdResolver = new XmiIdResolver();
    }
    
    @Override
    public void generate() throws Exception {
        List<PageDefinition> pages = context.getAllPages();
        
        for (PageDefinition page : pages) {
            generatePageModel(page);
        }
        
        log.info("Generated {} page models", pages.size());
    }
    
    private void generatePageModel(PageDefinition page) throws IOException {
        writer = new TypeScriptWriter();
        
        // Imports
        writeImports();
        
        // Model export
        String modelName = namingUtil.getPageModelName(page);
        writer.writeLine("");
        writer.writeExport(modelName, "PageModel");
        
        // Basic properties
        writeBasicProperties(page);
        
        // Type & classification
        writeTypeProperties(page);
        
        // Display properties
        writeDisplayProperties(page);
        
        // Data binding
        writeDataBinding(page);
        
        // Dialog configuration
        writeDialogConfig(page);
        
        // Container
        writeContainer(page);
        
        // Actions
        writeActions(page);
        
        // Navigation
        writeNavigation(page);
        
        // I18n
        writeI18n(page);
        
        writer.closeObject();
        
        // Write file
        generateModelFile("pages/" + page.getName() + ".model.ts", null);
    }
    
    private void writeImports() {
        writer.writeImport("../types", 
            "PageModel", 
            "PageContainerType", 
            "DialogSize",
            "IconModel"
        );
        writer.writeImport("../containers/" + page.getName() + "Container.model",
            page.getName() + "ContainerModel"
        );
    }
    
    private void writeBasicProperties(PageDefinition page) {
        writer.writeProperty("id", "page-" + namingUtil.toCamelCase(page.getName()));
        writer.writeProperty("name", page.getName());
        writer.writeProperty("fqn", namingUtil.getFqn(page));
        
        String xmiId = xmiIdResolver.getXmiId(page);
        writer.writeProperty("sourceId", xmiId);
    }
    
    private void writeTypeProperties(PageDefinition page) {
        PageContainer container = page.getContainer();
        
        // Determine type from container
        String type;
        if (container instanceof FormPageContainer) {
            type = "form";
        } else if (container instanceof ViewPageContainer) {
            type = "view";
        } else if (container instanceof TablePageContainer) {
            type = "table";
        } else {
            type = "view";
        }
        
        writer.writeProperty("type", type);
        writer.writeProperty("isSelector", page.isSelector());
        writer.writeProperty("isRelationSelector", page.isRelationSelector());
        writer.writeProperty("isDashboard", page.isDashboard());
    }
    
    private void writeDisplayProperties(PageDefinition page) {
        if (page.getLabel() != null) {
            writer.writeProperty("label", page.getLabel().getValue());
        }
        
        if (page.getIcon() != null) {
            Map<String, Object> icon = new HashMap<>();
            icon.put("name", page.getIcon().getName());
            if (page.getIcon().getColor() != null) {
                icon.put("color", page.getIcon().getColor());
            }
            writer.writeProperty("icon", icon);
        }
    }
    
    private void writeDataBinding(PageDefinition page) {
        if (page.getDataElement() != null) {
            NamedElement dataElement = page.getDataElement();
            writer.writeProperty("dataElement", namingUtil.getFqn(dataElement));
            
            if (dataElement instanceof ClassType) {
                writer.writeProperty("dataElementType", "class");
            } else if (dataElement instanceof RelationType) {
                writer.writeProperty("dataElementType", "relation");
            }
        }
    }
    
    private void writeDialogConfig(PageDefinition page) {
        writer.writeProperty("openInDialog", page.isOpenInDialog());
        
        if (page.isOpenInDialog() && page.getDialogSize() != null) {
            writer.writeProperty("dialogSize", page.getDialogSize().toString().toLowerCase());
        }
    }
    
    private void writeContainer(PageDefinition page) {
        writer.writeLine("container: " + page.getName() + "ContainerModel,");
    }
    
    private void writeActions(PageDefinition page) {
        List<Action> actions = extractActions(page);
        
        writer.writeLine("actions: [");
        writer.indent();
        
        for (Action action : actions) {
            writeAction(action);
        }
        
        writer.dedent();
        writer.writeLine("],");
    }
    
    private void writeAction(Action action) {
        writer.writeLine("{");
        writer.indent();
        
        writer.writeProperty("id", "action-" + action.getName());
        writer.writeProperty("name", action.getName());
        writer.writeProperty("type", getActionType(action));
        
        // Action-specific properties
        if (action.getTargetType() != null) {
            writer.writeProperty("targetType", action.getTargetType().getName());
        }
        
        if (action instanceof RelationAction) {
            RelationAction ra = (RelationAction) action;
            writer.writeProperty("relationName", ra.getRelation().getName());
            writer.writeProperty("relationFqn", namingUtil.getFqn(ra.getRelation()));
        }
        
        writer.writeProperty("isBulk", action.isBulk());
        writer.writeProperty("isTransient", action.isTransient());
        
        writer.dedent();
        writer.writeLine("},");
    }
    
    private String getActionType(Action action) {
        if (action instanceof RefreshAction) return "refresh";
        if (action instanceof CreateAction) return "create";
        if (action instanceof UpdateAction) return "update";
        if (action instanceof DeleteAction) return "delete";
        if (action instanceof AddToRelationAction) return "add";
        if (action instanceof RemoveFromRelationAction) return "remove";
        if (action instanceof SetRelationAction) return "set";
        if (action instanceof UnsetRelationAction) return "unset";
        if (action instanceof CallOperationAction) return "callOperation";
        if (action instanceof OpenPageAction) return "openPage";
        return "custom";
    }
    
    private void writeNavigation(PageDefinition page) {
        String routePath = generateRoutePath(page);
        writer.writeProperty("routePath", routePath);
        
        if (page.getParentPage() != null) {
            writer.writeProperty("parentPage", page.getParentPage().getName());
        }
    }
    
    private String generateRoutePath(PageDefinition page) {
        if (page.isSelector()) {
            return "/selectors/" + page.getName();
        }
        
        if (page.getDataElement() instanceof RelationType) {
            RelationType relation = (RelationType) page.getDataElement();
            ClassType owner = relation.getOwner();
            return "/pages/" + owner.getName() + "/:ownerId/" + relation.getName();
        }
        
        if (page.getDataElement() instanceof ClassType) {
            if (page.getContainer() instanceof FormPageContainer) {
                return "/pages/" + page.getName();
            } else {
                return "/pages/" + page.getName() + "/:id";
            }
        }
        
        return "/pages/" + page.getName();
    }
    
    private void writeI18n(PageDefinition page) {
        writer.writeLine("i18n: {");
        writer.indent();
        
        String keyPrefix = namingUtil.getI18nKeyPrefix(page);
        writer.writeProperty("keyPrefix", keyPrefix);
        writer.writeProperty("titleKey", keyPrefix + ".title");
        
        writer.writeLine("keys: {");
        writer.indent();
        
        // Collect i18n keys from labels
        if (page.getLabel() != null) {
            writer.writeProperty("title", page.getLabel().getValue());
        }
        
        writer.dedent();
        writer.writeLine("}");
        
        writer.dedent();
        writer.writeLine("}");
    }
    
    @Override
    protected Path getOutputDirectory() {
        return Paths.get("target/generated/models");
    }
}
```

## Generated Output Example

```typescript
// Generated: ~/models/pages/UserView.model.ts
import type { PageModel, PageContainerType, DialogSize, IconModel } from '../types';
import { UserViewContainerModel } from '../containers/UserViewContainer.model';

export const UserViewPageModel: PageModel = {
  id: 'page-userView',
  name: 'UserView',
  fqn: 'app.pages.UserView',
  sourceId: '_xmiid_abc123',
  
  type: 'view',
  isSelector: false,
  isRelationSelector: false,
  isDashboard: false,
  
  label: 'User Details',
  icon: {
    name: 'person',
    color: 'primary'
  },
  
  dataElement: 'User',
  dataElementType: 'class',
  
  openInDialog: false,
  
  container: UserViewContainerModel,
  
  actions: [
    {
      id: 'action-refresh',
      name: 'refresh',
      type: 'refresh',
      isBulk: false,
      isTransient: true
    },
    {
      id: 'action-update',
      name: 'update',
      type: 'update',
      targetType: 'User',
      isBulk: false,
      isTransient: false
    },
    {
      id: 'action-delete',
      name: 'delete',
      type: 'delete',
      targetType: 'User',
      isBulk: false,
      isTransient: false
    }
  ],
  
  routePath: '/pages/UserView/:id',
  
  i18n: {
    keyPrefix: 'judo.pages.UserView',
    titleKey: 'judo.pages.UserView.title',
    keys: {
      title: 'User Details'
    }
  }
};
```

## Testing

```java
@Test
public void testPageModelGeneration() throws Exception {
    UiModelContext context = GeneratorTestHelper.createTestContext();
    PageModelGenerator generator = new PageModelGenerator(context);
    
    generator.generate();
    
    // Verify file exists
    Path outputDir = Paths.get("target/generated/models/pages");
    assertTrue(Files.exists(outputDir.resolve("UserView.model.ts")));
    
    // Verify content
    String content = Files.readString(outputDir.resolve("UserView.model.ts"));
    assertTrue(content.contains("export const UserViewPageModel"));
    assertTrue(content.contains("type: 'view'"));
    assertTrue(content.contains("dataElement: 'User'"));
}
```

## Related Specifications

**Runtime Model:**
- `runtime-model/02-page-model.md` - Defines output structure

**Generators:**
- `01-generator-utilities.md` - Base utilities
- `03-container-generator.md` - Container generation

---

**Status:** ✅ Complete  
**Created:** 2025-11-28

