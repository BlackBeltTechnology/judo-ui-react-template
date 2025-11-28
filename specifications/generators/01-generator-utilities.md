# Generator Overview and Utilities Specification

**Domain:** Generators  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** All runtime model specifications  
**Blocks:** All generator implementations  

## Overview

This specification defines the generator infrastructure, utilities, and common patterns used across all model generators. These Java utilities extract metamodel elements and generate TypeScript runtime models.

## Generator Architecture

```java
package hu.blackbelt.judo.ui.generator.react.runtime;

/**
 * Base class for all runtime model generators
 */
public abstract class RuntimeModelGenerator {
    protected final UiModelContext context;
    protected final TypeScriptWriter writer;
    protected final NamingUtil namingUtil;
    
    public RuntimeModelGenerator(UiModelContext context) {
        this.context = context;
        this.writer = new TypeScriptWriter();
        this.namingUtil = new NamingUtil(context);
    }
    
    /**
     * Generate runtime model files
     */
    public abstract void generate() throws Exception;
    
    /**
     * Get output directory for generated files
     */
    protected abstract Path getOutputDirectory();
}
```

## Core Utilities

### UiModelContext

```java
/**
 * Context holding metamodel and configuration
 */
public class UiModelContext {
    private final UiModel uiModel;
    private final ApplicationModel applicationModel;
    private final Map<String, Object> configuration;
    
    public UiModelContext(UiModel uiModel, ApplicationModel applicationModel) {
        this.uiModel = uiModel;
        this.applicationModel = applicationModel;
        this.configuration = new HashMap<>();
    }
    
    public UiModel getUiModel() {
        return uiModel;
    }
    
    public ApplicationModel getApplicationModel() {
        return applicationModel;
    }
    
    // Navigation helpers
    public List<PageDefinition> getAllPages() {
        return uiModel.getElements().stream()
            .filter(e -> e instanceof PageDefinition)
            .map(e -> (PageDefinition) e)
            .collect(Collectors.toList());
    }
    
    public List<ClassType> getAllClasses() {
        return applicationModel.getPackages().stream()
            .flatMap(p -> p.getClasses().stream())
            .collect(Collectors.toList());
    }
    
    public List<EnumerationType> getAllEnums() {
        return applicationModel.getPackages().stream()
            .flatMap(p -> p.getEnums().stream())
            .collect(Collectors.toList());
    }
}
```

### NamingUtil

```java
/**
 * Utilities for naming conventions
 */
public class NamingUtil {
    private final UiModelContext context;
    
    public NamingUtil(UiModelContext context) {
        this.context = context;
    }
    
    /**
     * Convert PageDefinition name to model name
     * Example: "UserListPage" -> "UserListPageModel"
     */
    public String getPageModelName(PageDefinition page) {
        return page.getName() + "Model";
    }
    
    /**
     * Get fully qualified name for element
     */
    public String getFqn(NamedElement element) {
        List<String> parts = new ArrayList<>();
        NamedElement current = element;
        
        while (current != null) {
            parts.add(0, current.getName());
            current = current.eContainer() instanceof NamedElement 
                ? (NamedElement) current.eContainer() 
                : null;
        }
        
        return String.join(".", parts);
    }
    
    /**
     * Convert to camelCase
     */
    public String toCamelCase(String name) {
        if (name == null || name.isEmpty()) return name;
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }
    
    /**
     * Convert to PascalCase
     */
    public String toPascalCase(String name) {
        if (name == null || name.isEmpty()) return name;
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
    
    /**
     * Get i18n key prefix for page
     */
    public String getI18nKeyPrefix(PageDefinition page) {
        return "judo.pages." + page.getName();
    }
}
```

### TypeScriptWriter

```java
/**
 * Writer for TypeScript code generation
 */
public class TypeScriptWriter {
    private final StringBuilder buffer = new StringBuilder();
    private int indentLevel = 0;
    private static final String INDENT = "  ";
    
    public void writeLine(String line) {
        if (line.isEmpty()) {
            buffer.append("\n");
        } else {
            buffer.append(INDENT.repeat(indentLevel))
                  .append(line)
                  .append("\n");
        }
    }
    
    public void writeImport(String module, String... items) {
        if (items.length == 0) {
            writeLine("import '" + module + "';");
        } else {
            writeLine("import { " + String.join(", ", items) + " } from '" + module + "';");
        }
    }
    
    public void writeExport(String name, String type) {
        writeLine("export const " + name + ": " + type + " = {");
        indent();
    }
    
    public void writeProperty(String name, Object value) {
        String valueStr = formatValue(value);
        writeLine(name + ": " + valueStr + ",");
    }
    
    public void indent() {
        indentLevel++;
    }
    
    public void dedent() {
        indentLevel = Math.max(0, indentLevel - 1);
    }
    
    public void closeObject() {
        dedent();
        writeLine("};");
    }
    
    public String getContent() {
        return buffer.toString();
    }
    
    private String formatValue(Object value) {
        if (value == null) {
            return "null";
        } else if (value instanceof String) {
            return "'" + escapeString((String) value) + "'";
        } else if (value instanceof Boolean || value instanceof Number) {
            return value.toString();
        } else if (value instanceof List) {
            return formatArray((List<?>) value);
        } else if (value instanceof Map) {
            return formatObject((Map<?, ?>) value);
        } else {
            return value.toString();
        }
    }
    
    private String escapeString(String str) {
        return str.replace("\\", "\\\\")
                  .replace("'", "\\'")
                  .replace("\n", "\\n");
    }
    
    private String formatArray(List<?> list) {
        if (list.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(formatValue(list.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }
    
    private String formatObject(Map<?, ?> map) {
        StringBuilder sb = new StringBuilder("{ ");
        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) sb.append(", ");
            sb.append(entry.getKey()).append(": ").append(formatValue(entry.getValue()));
            first = false;
        }
        sb.append(" }");
        return sb.toString();
    }
}
```

### TypeMapper

```java
/**
 * Maps metamodel types to TypeScript types
 */
public class TypeMapper {
    
    public String mapDataType(AttributeType attribute) {
        String primitiveType = attribute.getType();
        
        switch (primitiveType) {
            case "string":
            case "text":
                return "string";
            case "integer":
            case "long":
            case "decimal":
            case "double":
                return "number";
            case "boolean":
                return "boolean";
            case "date":
            case "dateTime":
            case "timestamp":
                return "Date";
            case "time":
                return "string";
            case "binary":
                return "Blob";
            default:
                // Must be enum type
                return attribute.getType();
        }
    }
    
    public String mapToInputType(AttributeType attribute) {
        String primitiveType = attribute.getType();
        
        switch (primitiveType) {
            case "string":
                return "textInput";
            case "text":
                return "textArea";
            case "integer":
            case "long":
            case "decimal":
            case "double":
                return "numericInput";
            case "boolean":
                return "checkbox";
            case "date":
                return "dateInput";
            case "dateTime":
            case "timestamp":
                return "dateTimeInput";
            case "time":
                return "timeInput";
            case "binary":
                return "binaryTypeInput";
            default:
                // Enum
                return "enumerationCombo";
        }
    }
}
```

### XmiIdResolver

```java
/**
 * Resolves and tracks XMI IDs for traceability
 */
public class XmiIdResolver {
    private final Map<EObject, String> xmiIds = new HashMap<>();
    
    public String getXmiId(EObject element) {
        return xmiIds.computeIfAbsent(element, e -> generateXmiId(e));
    }
    
    private String generateXmiId(EObject element) {
        // Use XMI resource to get actual ID if available
        Resource resource = element.eResource();
        if (resource instanceof XMIResource) {
            String id = ((XMIResource) resource).getID(element);
            if (id != null) return id;
        }
        
        // Generate deterministic ID based on path
        return "_" + UUID.nameUUIDFromBytes(
            getElementPath(element).getBytes()
        ).toString().replace("-", "");
    }
    
    private String getElementPath(EObject element) {
        List<String> segments = new ArrayList<>();
        EObject current = element;
        
        while (current != null) {
            if (current instanceof NamedElement) {
                segments.add(0, ((NamedElement) current).getName());
            } else {
                segments.add(0, current.eClass().getName());
            }
            current = current.eContainer();
        }
        
        return String.join("/", segments);
    }
}
```

## File Generation Pattern

```java
/**
 * Standard pattern for generating model files
 */
public void generateModelFile(String fileName, Object model) throws IOException {
    Path outputPath = getOutputDirectory().resolve(fileName);
    Files.createDirectories(outputPath.getParent());
    
    String content = writer.getContent();
    Files.writeString(outputPath, content, StandardCharsets.UTF_8);
    
    log.info("Generated: {}", outputPath);
}
```

## Common Patterns

### Extract Visual Elements

```java
protected List<VisualElement> extractVisualElements(Container container) {
    return container.getChildren().stream()
        .filter(e -> e instanceof VisualElement)
        .map(e -> (VisualElement) e)
        .collect(Collectors.toList());
}
```

### Extract Actions

```java
protected List<Action> extractActions(PageDefinition page) {
    List<Action> actions = new ArrayList<>();
    
    // From button groups
    if (page.getContainer() instanceof PageContainer) {
        PageContainer pc = (PageContainer) page.getContainer();
        for (ButtonGroup group : pc.getActionButtonGroups()) {
            for (Button button : group.getButtons()) {
                if (button.getActionDefinition() != null) {
                    actions.add(button.getActionDefinition());
                }
            }
        }
    }
    
    return actions;
}
```

## Testing Utilities

```java
/**
 * Test helper for generator testing
 */
public class GeneratorTestHelper {
    
    public static UiModelContext createTestContext() {
        // Load test model
        UiModel uiModel = loadUiModel("test-model.ui");
        ApplicationModel appModel = loadAppModel("test-model.app");
        return new UiModelContext(uiModel, appModel);
    }
    
    public static void assertGeneratedFileExists(Path outputDir, String fileName) {
        Path filePath = outputDir.resolve(fileName);
        assertTrue(Files.exists(filePath), "Generated file should exist: " + fileName);
    }
    
    public static void assertValidTypeScript(String content) {
        // Basic TypeScript syntax validation
        assertFalse(content.contains("undefined"), "Should not contain 'undefined'");
        assertTrue(content.contains("export"), "Should have exports");
    }
}
```

## Examples

### Example 1: Basic Generator

```java
public class BasicModelGenerator extends RuntimeModelGenerator {
    
    public BasicModelGenerator(UiModelContext context) {
        super(context);
    }
    
    @Override
    public void generate() throws Exception {
        writer.writeImport("../types", "BaseModel");
        writer.writeLine("");
        
        writer.writeExport("ExampleModel", "BaseModel");
        writer.writeProperty("id", "example-1");
        writer.writeProperty("name", "Example");
        writer.closeObject();
        
        generateModelFile("Example.model.ts", null);
    }
    
    @Override
    protected Path getOutputDirectory() {
        return Paths.get("target/generated/models");
    }
}
```

## Related Specifications

**Runtime Model:**
- All runtime-model specifications define the output format

**Generators:**
- `02-page-model-generator.md` - Uses these utilities
- `03-container-generator.md` - Uses these utilities
- All other generators use this foundation

---

**Status:** ✅ Complete  
**Created:** 2025-11-28  
**Last Updated:** 2025-11-28  
**Reviewed:** Not yet reviewed

