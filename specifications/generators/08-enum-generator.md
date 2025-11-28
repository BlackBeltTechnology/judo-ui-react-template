# Enum Generator Specification

**Domain:** Generators  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `01-generator-utilities.md`  

## Overview

Enum Generator extracts EnumerationType definitions from the application model and generates TypeScript enum models with i18n support.

## Generator Implementation

```java
public class EnumGenerator extends RuntimeModelGenerator {
    
    @Override
    public void generate() throws Exception {
        List<EnumerationType> enums = context.getAllEnums();
        
        for (EnumerationType enumType : enums) {
            generateEnumModel(enumType);
        }
    }
    
    private void generateEnumModel(EnumerationType enumType) throws IOException {
        writer = new TypeScriptWriter();
        
        // TypeScript enum
        writer.writeLine("export enum " + enumType.getName() + " {");
        writer.indent();
        
        for (EnumLiteral literal : enumType.getLiterals()) {
            writer.writeLine(literal.getName() + " = '" + literal.getName() + "',");
        }
        
        writer.dedent();
        writer.writeLine("}");
        writer.writeLine("");
        
        // Enum model with metadata
        writer.writeExport(enumType.getName() + "Model", "EnumModel");
        writer.writeProperty("name", enumType.getName());
        
        writer.writeLine("values: [");
        writer.indent();
        
        for (EnumLiteral literal : enumType.getLiterals()) {
            writer.writeLine("{");
            writer.indent();
            writer.writeProperty("value", literal.getName());
            writer.writeProperty("label", literal.getLabel() != null ? literal.getLabel().getValue() : literal.getName());
            writer.writeProperty("ordinal", literal.getOrdinal());
            writer.dedent();
            writer.writeLine("},");
        }
        
        writer.dedent();
        writer.writeLine("]");
        
        writer.closeObject();
        
        generateModelFile("enums/" + enumType.getName() + ".model.ts", null);
    }
    
    @Override
    protected Path getOutputDirectory() {
        return Paths.get("target/generated/models");
    }
}
```

---

**Status:** ✅ Complete  
**Created:** 2025-11-28

