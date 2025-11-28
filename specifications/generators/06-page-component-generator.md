# Page Component Generator Specification
**Created:** 2025-11-28
**Status:** ✅ Complete  

---

```
}
    assertTrue(content.contains("UserViewPageModel"));
    assertTrue(content.contains("ModelDrivenPage"));
    assertTrue(content.contains("export function UserView"));
    String content = Files.readString(outputPath);
    
    assertTrue(Files.exists(outputPath));
    Path outputPath = Paths.get("target/generated/src/pages/UserView.tsx");
    
    generator.generate();
    PageComponentGenerator generator = new PageComponentGenerator(context);
public void testPageComponentGeneration() throws Exception {
@Test
```java

## Testing

```
}
  );
    />
      entityId={id}
      serviceImpl={UserService}
      model={UserViewPageModel}
    <ModelDrivenPage
  return (
  
  const { id } = useParams<{ id: string }>();
export function UserView() {

import { UserService } from '~/services/UserService';
import { UserViewPageModel } from '~/models/pages/UserView.model';
import { ModelDrivenPage } from '@judo/runtime';
import { useParams } from 'react-router-dom';
import React from 'react';
// Generated: ~/src/pages/UserView.tsx
```typescript

## Generated Output Example

```
}
    }
        return Paths.get("target/generated/src");
    protected Path getOutputDirectory() {
    @Override
    
    }
        return "DefaultService";
        }
            return classType.getName() + "Service";
            ClassType classType = (ClassType) page.getDataElement();
        if (page.getDataElement() instanceof ClassType) {
    private String getServiceName(PageDefinition page) {
    
    }
            || page.getContainer() instanceof FormPageContainer;
        return page.getContainer() instanceof ViewPageContainer 
    private boolean requiresEntityId(PageDefinition page) {
    
    }
        generateModelFile("pages/" + page.getName() + ".tsx", null);
        
        writer.writeLine("}");
        writer.dedent();
        
        writer.writeLine(");");
        writer.dedent();
        writer.writeLine("/>");
        writer.dedent();
        
        }
            writer.writeLine("entityId={id}");
        if (requiresEntityId(page)) {
        
        writer.writeLine("serviceImpl={" + getServiceName(page) + "}");
        writer.writeLine("model={" + page.getName() + "PageModel}");
        writer.indent();
        writer.writeLine("<ModelDrivenPage");
        writer.indent();
        writer.writeLine("return (");
        // Return
        
        }
            writer.writeLine("");
            writer.writeLine("const { id } = useParams<{ id: string }>();");
        if (requiresEntityId(page)) {
        // Get ID from params if needed
        
        writer.indent();
        writer.writeLine("export function " + page.getName() + "() {");
        // Component
        
        writer.writeLine("");
            getServiceName(page));
        writer.writeImport("~/services/" + getServiceName(page), 
            page.getName() + "PageModel");
        writer.writeImport("~/models/pages/" + page.getName() + ".model", 
        writer.writeImport("@judo/runtime", "ModelDrivenPage");
        writer.writeImport("react-router-dom", "useParams");
        writer.writeImport("react", "React");
        // Imports
        
        writer = new TypeScriptWriter();
    private void generatePageComponent(PageDefinition page) throws IOException {
    
    }
        }
            generatePageComponent(page);
        for (PageDefinition page : pages) {
        
        List<PageDefinition> pages = context.getAllPages();
    public void generate() throws Exception {
    @Override
    
public class PageComponentGenerator extends RuntimeModelGenerator {
```java

## Generator Implementation

Page Component Generator generates minimal React page component files that import and use the generated page models with ModelDrivenPage component.

## Overview

**Dependencies:** `01-generator-utilities.md`, `02-page-model-generator.md`  
**Assigned:** AI Agent  
**Status:** Complete  
**Domain:** Generators  


