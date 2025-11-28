# Model File Generator Specification

**Domain:** Generators  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `01-generator-utilities.md`  

## Overview

Model File Generator is the orchestrator that runs all sub-generators and produces the complete set of runtime model files.

## Main Generator

```java
public class ModelFileGenerator {
    
    private final UiModelContext context;
    private final Path outputDirectory;
    
    public ModelFileGenerator(UiModelContext context, Path outputDirectory) {
        this.context = context;
        this.outputDirectory = outputDirectory;
    }
    
    public void generate() throws Exception {
        log.info("Starting runtime model generation...");
        
        // Clean output directory
        cleanOutputDirectory();
        
        // Run all generators
        runPageModelGenerator();
        runContainerModelGenerator();
        runEnumGenerator();
        runI18nGenerator();
        runPageComponentGenerator();
        
        // Generate index files
        generateIndexFiles();
        
        log.info("Runtime model generation complete!");
    }
    
    private void runPageModelGenerator() throws Exception {
        log.info("Generating page models...");
        PageModelGenerator generator = new PageModelGenerator(context);
        generator.generate();
    }
    
    private void runContainerModelGenerator() throws Exception {
        log.info("Generating container models...");
        ContainerModelGenerator generator = new ContainerModelGenerator(context);
        generator.generate();
    }
    
    private void runEnumGenerator() throws Exception {
        log.info("Generating enum models...");
        EnumGenerator generator = new EnumGenerator(context);
        generator.generate();
    }
    
    private void runI18nGenerator() throws Exception {
        log.info("Generating i18n files...");
        I18nGenerator generator = new I18nGenerator(context);
        generator.generate();
    }
    
    private void runPageComponentGenerator() throws Exception {
        log.info("Generating page components...");
        PageComponentGenerator generator = new PageComponentGenerator(context);
        generator.generate();
    }
    
    private void generateIndexFiles() throws IOException {
        generateModelsIndex();
        generatePagesIndex();
        generateEnumsIndex();
    }
    
    private void generateModelsIndex() throws IOException {
        TypeScriptWriter writer = new TypeScriptWriter();
        
        // Export all page models
        List<PageDefinition> pages = context.getAllPages();
        for (PageDefinition page : pages) {
            writer.writeLine("export { " + page.getName() + "PageModel } from './pages/" + 
                page.getName() + ".model';");
        }
        
        writer.writeLine("");
        
        // Export all enum models
        List<EnumerationType> enums = context.getAllEnums();
        for (EnumerationType enumType : enums) {
            writer.writeLine("export { " + enumType.getName() + ", " + 
                enumType.getName() + "Model } from './enums/" + 
                enumType.getName() + ".model';");
        }
        
        Path indexPath = outputDirectory.resolve("models/index.ts");
        Files.writeString(indexPath, writer.getContent());
    }
    
    private void generatePagesIndex() throws IOException {
        TypeScriptWriter writer = new TypeScriptWriter();
        
        List<PageDefinition> pages = context.getAllPages();
        for (PageDefinition page : pages) {
            writer.writeLine("export { " + page.getName() + " } from './" + 
                page.getName() + "';");
        }
        
        Path indexPath = outputDirectory.resolve("src/pages/index.ts");
        Files.writeString(indexPath, writer.getContent());
    }
    
    private void generateEnumsIndex() throws IOException {
        TypeScriptWriter writer = new TypeScriptWriter();
        
        List<EnumerationType> enums = context.getAllEnums();
        for (EnumerationType enumType : enums) {
            writer.writeLine("export { " + enumType.getName() + " } from './" + 
                enumType.getName() + ".model';");
        }
        
        Path indexPath = outputDirectory.resolve("models/enums/index.ts");
        Files.writeString(indexPath, writer.getContent());
    }
    
    private void cleanOutputDirectory() throws IOException {
        if (Files.exists(outputDirectory)) {
            Files.walk(outputDirectory)
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        log.warn("Failed to delete: " + path, e);
                    }
                });
        }
        Files.createDirectories(outputDirectory);
    }
}
```

## Maven Plugin Integration

```java
@Mojo(name = "generate-runtime-models", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class GenerateRuntimeModelsMojo extends AbstractMojo {
    
    @Parameter(required = true)
    private File uiModel;
    
    @Parameter(required = true)
    private File applicationModel;
    
    @Parameter(defaultValue = "${project.build.directory}/generated-sources/judo-ui-react")
    private File outputDirectory;
    
    @Override
    public void execute() throws MojoExecutionException {
        try {
            getLog().info("Loading models...");
            UiModel ui = loadUiModel(uiModel);
            ApplicationModel app = loadApplicationModel(applicationModel);
            
            UiModelContext context = new UiModelContext(ui, app);
            
            getLog().info("Generating runtime models...");
            ModelFileGenerator generator = new ModelFileGenerator(
                context, 
                outputDirectory.toPath()
            );
            generator.generate();
            
            getLog().info("Runtime model generation complete!");
            
        } catch (Exception e) {
            throw new MojoExecutionException("Generation failed", e);
        }
    }
}
```

## Usage Example

```xml
<plugin>
    <groupId>hu.blackbelt.judo</groupId>
    <artifactId>judo-ui-react-maven-plugin</artifactId>
    <version>1.0.0</version>
    <executions>
        <execution>
            <goals>
                <goal>generate-runtime-models</goal>
            </goals>
            <configuration>
                <uiModel>${project.basedir}/model/ui.model</uiModel>
                <applicationModel>${project.basedir}/model/app.model</applicationModel>
                <outputDirectory>${project.build.directory}/generated-sources/judo-ui-react</outputDirectory>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## Generated File Structure

```
target/generated-sources/judo-ui-react/
├── models/
│   ├── index.ts
│   ├── types.ts
│   ├── pages/
│   │   ├── UserView.model.ts
│   │   ├── UserForm.model.ts
│   │   └── UserList.model.ts
│   ├── containers/
│   │   ├── UserViewContainer.model.ts
│   │   └── UserFormContainer.model.ts
│   └── enums/
│       ├── index.ts
│       ├── UserStatus.model.ts
│       └── UserRole.model.ts
├── src/
│   └── pages/
│       ├── index.ts
│       ├── UserView.tsx
│       ├── UserForm.tsx
│       └── UserList.tsx
└── i18n/
    ├── en-US.json
    └── hu-HU.json
```

---

**Status:** ✅ Complete  
**Created:** 2025-11-28

