# I18n Generator Specification

**Domain:** Generators  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `01-generator-utilities.md`, `runtime-model/09-i18n-model.md`  

## Overview

I18n Generator collects all translatable strings from the metamodel and generates i18n JSON files per locale.

## Generator Implementation

```java
public class I18nGenerator extends RuntimeModelGenerator {
    
    private final Map<String, Map<String, String>> translations = new HashMap<>();
    
    @Override
    public void generate() throws Exception {
        // Collect translations
        collectPageTranslations();
        collectEnumTranslations();
        collectValidationTranslations();
        collectCommonTranslations();
        
        // Generate i18n files per locale
        for (String locale : getSupportedLocales()) {
            generateI18nFile(locale);
        }
    }
    
    private void collectPageTranslations() {
        for (PageDefinition page : context.getAllPages()) {
            String keyPrefix = "judo.pages." + page.getName();
            
            if (page.getLabel() != null) {
                addTranslation(keyPrefix + ".title", page.getLabel().getValue());
            }
            
            // Collect from visual elements
            collectElementTranslations(page.getContainer(), keyPrefix);
        }
    }
    
    private void collectEnumTranslations() {
        for (EnumerationType enumType : context.getAllEnums()) {
            String keyPrefix = "judo.enums." + enumType.getName();
            
            for (EnumLiteral literal : enumType.getLiterals()) {
                String key = keyPrefix + "." + literal.getName();
                String value = literal.getLabel() != null 
                    ? literal.getLabel().getValue() 
                    : literal.getName();
                addTranslation(key, value);
            }
        }
    }
    
    private void addTranslation(String key, String value) {
        translations.computeIfAbsent("en-US", k -> new HashMap<>())
            .put(key, value);
    }
    
    private void generateI18nFile(String locale) throws IOException {
        Map<String, String> localeTranslations = translations.get(locale);
        if (localeTranslations == null) return;
        
        // Sort keys
        Map<String, String> sorted = new TreeMap<>(localeTranslations);
        
        // Generate JSON
        String json = new Gson().toJson(sorted);
        
        Path outputPath = getOutputDirectory().resolve("i18n/" + locale + ".json");
        Files.createDirectories(outputPath.getParent());
        Files.writeString(outputPath, json, StandardCharsets.UTF_8);
    }
    
    private List<String> getSupportedLocales() {
        return Arrays.asList("en-US", "hu-HU");
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

