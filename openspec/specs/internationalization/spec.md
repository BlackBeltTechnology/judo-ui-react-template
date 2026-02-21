# Internationalization

Generates i18n translation files and translation key usage throughout the generated React application. The generator produces JSON translation files with keys for every labeled element, enumeration value, menu item, and system message. Applications use `react-i18next` for runtime translation resolution.

## Requirements

### Requirement: Application Translation File Generation

The generator SHALL produce two application translation JSON files for each application:

`application_default.json` SHALL contain all translation keys with default values from model labels. `application_configured.json` SHALL be initially identical, intended for customization. Keys SHALL be hierarchical, derived from the element's fully qualified name. Key format SHALL follow `{ClassType}.{ContainerName}.{ElementName}` with `defaultValue` as the label text.

#### Scenario: Application translations generated

- **WHEN** an `Application` model exists with labeled elements
- **THEN** the generator produces `public/i18n/{lang}/application_default.json` with all translation keys
- **AND** `public/i18n/{lang}/application_configured.json` with identical content for customization

**Key Helpers**: `UiI18NHelper.getTranslationKeyForVisualElement()`

**Templates**: `public/i18n/application_default.json.hbs`, `public/i18n/application_configured.json.hbs`

---

### Requirement: System Translation File Generation

The generator SHALL produce system-level translation files for framework UI elements.

System translations SHALL include: button labels (Save, Cancel, Delete, Back, etc.), dialog titles and messages, table actions (Filter, Export, Add, Remove, etc.), error messages, confirmation dialog text, pagination labels, and authentication messages.

The generator SHALL produce translations for multiple languages out of the box: `system_default.json` (English defaults), `system_en-US.json` (US English), `system_hu-HU.json` (Hungarian).

#### Scenario: System translations generated

- **WHEN** an application is generated
- **THEN** system translation files are produced for default, en-US, and hu-HU languages

**Key Helpers**: `UiI18NHelper.getSystemTranslationForVisualElement()`

**Templates**: `public/i18n/system_default.json.hbs`, `public/i18n/system_en-US.json.hbs`, `public/i18n/system_hu-HU.json.hbs`

---

### Requirement: Translation Key Usage in Components

The generator SHALL produce `useTranslation()` hook calls and `t()` function invocations in all components that render labeled elements.

Translation calls SHALL use the format `t('TranslationKey', { defaultValue: 'Fallback' })`. The default value SHALL be the label from the model (English). At runtime, `i18next` SHALL resolve the key against loaded translation files.

#### Scenario: Labeled input with translation

- **WHEN** a component renders a labeled input element
- **THEN** the label is rendered via `t('EntityType.ViewName.fieldName', { defaultValue: 'Field Name' })`

---

### Requirement: Enumeration Translation

The generator SHALL produce translation keys for each `EnumerationMember`.

Key format SHALL follow `enumerations.{EnumTypeName}.{MemberName}`. Enum translation keys SHALL be used in combo boxes, radio groups, toggle bars, and table column displays.

#### Scenario: Enum member translations

- **WHEN** an `EnumerationType` has members
- **THEN** each member gets a translation key in the application translation files
- **AND** enum display values in widgets resolve via these keys

**Key Helpers**: `UiI18NHelper.i18nEnumerationTypes()`

---

### Requirement: Menu/Navigation Translation

The generator SHALL produce translation keys for each `NavigationItem` label.

Key format SHALL follow `menuTree.{ItemName}`. Keys SHALL be applied to sidebar menu items and header navigation.

#### Scenario: Menu item translations

- **WHEN** navigation items exist with labels
- **THEN** each label gets a translation key in the format `menuTree.{ItemName}`

**Key Helpers**: `UiI18NHelper.i18nMenuTreeLabels()`

---

### Requirement: MUI Locale Integration

The generator SHALL map the `defaultLanguage` to MUI locale imports for component localization.

The language code SHALL be converted from standard format to MUI format (e.g., `en-US` → `enUS`). MUI components (DataGrid, DatePicker, etc.) SHALL use the configured locale.

#### Scenario: MUI locale mapping

- **WHEN** `defaultLanguage = en-US`
- **THEN** MUI locale imports use `enUS` locale
- **AND** DataGrid, DatePicker, and other MUI components display in English

**Key Helpers**: `UiI18NHelper.shortLocale()`, `UiI18NHelper.muiTranslationToken()`, `UiI18NHelper.getDefaultLanguage()`

---

### Requirement: Multi-Language Support Structure

The generator SHALL organize translation files by language code under `public/i18n/`.

The `defaultLanguage` template parameter SHALL determine the primary language. Additional languages SHALL be addable by creating new translation file directories. Language switching SHALL be supported at runtime via i18next. `src/config/general.ts` SHALL configure the i18n initialization.

#### Scenario: Default language structure

- **WHEN** `defaultLanguage = en-US`
- **THEN** translation files are generated under `public/i18n/en-US/`
- **AND** `src/config/general.ts` initializes i18n with `en-US` as the default

**Template**: `src/config/general.ts.hbs`

---

## Integration Test Coverage

- **All tests**: Every generated application includes i18n files with all translation keys
- **All tests**: `defaultLanguage: en-US` configured across all integration tests
