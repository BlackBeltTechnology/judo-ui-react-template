# Container Model Generator Specification
**Created:** 2025-11-28
**Status:** ✅ Complete  

---

```
}
    }
        return Paths.get("target/generated/models");
    protected Path getOutputDirectory() {
    @Override
    
    }
        writer.writeLine("},");
        writer.dedent();
        
        }
            writeInputProperties((InputBase) element);
        if (element instanceof InputBase) {
        
        writer.writeProperty("col", element.getCol());
        writer.writeProperty("type", getElementType(element));
        writer.writeProperty("name", element.getName());
        writer.writeProperty("id", "ve-" + element.getName());
        
        writer.indent();
        writer.writeLine("{");
    private void writeVisualElement(VisualElement element) {
    
    }
        writer.writeLine("],");
        writer.dedent();
        
        }
            writeVisualElement(element);
        for (VisualElement element : container.getChildren()) {
        
        writer.indent();
        writer.writeLine("visualElements: [");
    private void writeVisualElements(Container container) {
    
    }
        return "view";
        if (container instanceof TablePageContainer) return "table";
        if (container instanceof ViewPageContainer) return "view";
        if (container instanceof FormPageContainer) return "form";
    private String determineContainerType(Container container) {
    
    }
        generateModelFile("containers/" + pageName + "Container.model.ts", null);
        
        writer.closeObject();
        
        }
            writeActionButtonGroups((PageContainer) container);
        if (container instanceof PageContainer) {
        // Action button groups
        
        writeVisualElements(container);
        // Visual elements
        
        }
            writer.writeProperty("dataElement", namingUtil.getFqn(container.getDataElement()));
        if (container.getDataElement() != null) {
        
        writer.writeProperty("type", determineContainerType(container));
        writer.writeProperty("name", "container");
        writer.writeProperty("id", "container-" + namingUtil.toCamelCase(pageName));
        // Properties
        
        writer.writeExport(modelName, "PageContainerModel");
        String modelName = pageName + "ContainerModel";
        // Export
        
        writer.writeImport("../types", "ContainerModel", "PageContainerModel");
        // Imports
        
        writer = new TypeScriptWriter();
    private void generateContainerModel(Container container, String pageName) throws IOException {
    
    }
        }
            generateContainerModel(page.getContainer(), page.getName());
        for (PageDefinition page : pages) {
        
        List<PageDefinition> pages = context.getAllPages();
    public void generate() throws Exception {
    @Override
    
public class ContainerModelGenerator extends RuntimeModelGenerator {
```java

## Generator Implementation

Container Model Generator extracts PageContainer and nested containers from the metamodel and generates ContainerModel TypeScript files.

## Overview

**Dependencies:** `01-generator-utilities.md`, `runtime-model/03-container-model.md`  
**Assigned:** AI Agent  
**Status:** Complete  
**Domain:** Generators  


