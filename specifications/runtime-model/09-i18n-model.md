# I18nModel Specification

**Domain:** Runtime Model  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `01-core-types.md`  
**Blocks:** Internationalization, label translation  

## Overview

`I18nModel` provides internationalization configuration for pages, containers, and elements. It defines translation keys, default values, and locale-specific translations.

## TypeScript Interfaces

### I18nModel

```typescript
interface I18nModel {
  keyPrefix: string;                    // Base i18n key (e.g., 'judo.pages.UserForm')
  keys: Record<string, I18nEntry>;      // Key-value translations
  defaultLocale: string;                // Default locale (e.g., 'en-US')
  supportedLocales: string[];           // Supported locales
}

interface I18nEntry {
  key: string;                          // Full i18n key
  defaultValue: string;                 // English fallback
  description?: string;                 // Context/usage description
  translations?: Record<string, string>; // Locale-specific translations
}
```

### LocaleModel

```typescript
interface LocaleModel {
  code: string;                         // ISO code (e.g., 'en-US', 'hu-HU')
  label: string;                        // Display name
  direction: 'ltr' | 'rtl';            // Text direction
  translations: Record<string, string>; // All translations for this locale
}
```

### TranslationFunction

```typescript
type TranslationFunction = (
  key: string,
  options?: {
    defaultValue?: string;
    interpolation?: Record<string, any>;
    count?: number;                     // For pluralization
  }
) => string;

// Usage
const t: TranslationFunction = (key, options) => {
  // Implementation in runtime
};
```

## Key Patterns

### Key Naming Convention

```typescript
// Pattern: judo.{domain}.{context}.{element}.{property}

// Examples:
'judo.pages.UserForm.title'           // Page title
'judo.pages.UserForm.actions.save'    // Action label
'judo.pages.UserForm.fields.firstName.label'  // Field label
'judo.pages.UserForm.fields.firstName.helperText' // Field helper
'judo.pages.UserForm.validation.required'  // Validation message
'judo.pages.UserForm.confirmation.delete.title' // Confirmation title
'judo.enums.UserStatus.ACTIVE'        // Enum value
'judo.common.save'                    // Common/shared translation
'judo.common.cancel'
'judo.common.confirm'
```

### Domain Prefixes

- `judo.pages.*` - Page-specific translations
- `judo.actions.*` - Action labels
- `judo.fields.*` - Field labels
- `judo.validation.*` - Validation messages
- `judo.enums.*` - Enum values
- `judo.common.*` - Shared translations
- `judo.errors.*` - Error messages

## Examples

### Example 1: Page I18n Model

```typescript
const userFormI18n: I18nModel = {
  keyPrefix: 'judo.pages.UserForm',
  defaultLocale: 'en-US',
  supportedLocales: ['en-US', 'hu-HU'],
  keys: {
    title: {
      key: 'judo.pages.UserForm.title',
      defaultValue: 'Create User',
      description: 'Page title',
      translations: {
        'en-US': 'Create User',
        'hu-HU': 'Felhasználó létrehozása'
      }
    },
    'actions.save': {
      key: 'judo.pages.UserForm.actions.save',
      defaultValue: 'Save',
      translations: {
        'en-US': 'Save',
        'hu-HU': 'Mentés'
      }
    },
    'actions.cancel': {
      key: 'judo.pages.UserForm.actions.cancel',
      defaultValue: 'Cancel',
      translations: {
        'en-US': 'Cancel',
        'hu-HU': 'Mégse'
      }
    },
    'fields.firstName.label': {
      key: 'judo.pages.UserForm.fields.firstName.label',
      defaultValue: 'First Name',
      translations: {
        'en-US': 'First Name',
        'hu-HU': 'Keresztnév'
      }
    },
    'fields.email.label': {
      key: 'judo.pages.UserForm.fields.email.label',
      defaultValue: 'Email Address',
      translations: {
        'en-US': 'Email Address',
        'hu-HU': 'E-mail cím'
      }
    }
  }
};
```

### Example 2: Validation Messages

```typescript
const validationI18n: I18nModel = {
  keyPrefix: 'judo.validation',
  defaultLocale: 'en-US',
  supportedLocales: ['en-US', 'hu-HU'],
  keys: {
    required: {
      key: 'judo.validation.required',
      defaultValue: 'This field is required',
      translations: {
        'en-US': 'This field is required',
        'hu-HU': 'Ez a mező kötelező'
      }
    },
    minLength: {
      key: 'judo.validation.minLength',
      defaultValue: 'Minimum {{min}} characters required',
      translations: {
        'en-US': 'Minimum {{min}} characters required',
        'hu-HU': 'Minimum {{min}} karakter szükséges'
      }
    },
    maxLength: {
      key: 'judo.validation.maxLength',
      defaultValue: 'Maximum {{max}} characters allowed',
      translations: {
        'en-US': 'Maximum {{max}} characters allowed',
        'hu-HU': 'Maximum {{max}} karakter engedélyezett'
      }
    },
    invalidEmail: {
      key: 'judo.validation.invalidEmail',
      defaultValue: 'Invalid email format',
      translations: {
        'en-US': 'Invalid email format',
        'hu-HU': 'Érvénytelen email formátum'
      }
    }
  }
};
```

### Example 3: Enum I18n

```typescript
const userStatusI18n: I18nModel = {
  keyPrefix: 'judo.enums.UserStatus',
  defaultLocale: 'en-US',
  supportedLocales: ['en-US', 'hu-HU'],
  keys: {
    ACTIVE: {
      key: 'judo.enums.UserStatus.ACTIVE',
      defaultValue: 'Active',
      translations: {
        'en-US': 'Active',
        'hu-HU': 'Aktív'
      }
    },
    INACTIVE: {
      key: 'judo.enums.UserStatus.INACTIVE',
      defaultValue: 'Inactive',
      translations: {
        'en-US': 'Inactive',
        'hu-HU': 'Inaktív'
      }
    },
    PENDING: {
      key: 'judo.enums.UserStatus.PENDING',
      defaultValue: 'Pending',
      translations: {
        'en-US': 'Pending',
        'hu-HU': 'Függőben'
      }
    }
  }
};
```

### Example 4: Common/Shared Translations

```typescript
const commonI18n: I18nModel = {
  keyPrefix: 'judo.common',
  defaultLocale: 'en-US',
  supportedLocales: ['en-US', 'hu-HU'],
  keys: {
    save: {
      key: 'judo.common.save',
      defaultValue: 'Save',
      translations: { 'en-US': 'Save', 'hu-HU': 'Mentés' }
    },
    cancel: {
      key: 'judo.common.cancel',
      defaultValue: 'Cancel',
      translations: { 'en-US': 'Cancel', 'hu-HU': 'Mégse' }
    },
    delete: {
      key: 'judo.common.delete',
      defaultValue: 'Delete',
      translations: { 'en-US': 'Delete', 'hu-HU': 'Törlés' }
    },
    confirm: {
      key: 'judo.common.confirm',
      defaultValue: 'Confirm',
      translations: { 'en-US': 'Confirm', 'hu-HU': 'Megerősít' }
    },
    yes: {
      key: 'judo.common.yes',
      defaultValue: 'Yes',
      translations: { 'en-US': 'Yes', 'hu-HU': 'Igen' }
    },
    no: {
      key: 'judo.common.no',
      defaultValue: 'No',
      translations: { 'en-US': 'No', 'hu-HU': 'Nem' }
    }
  }
};
```

## Translation Function Usage

```typescript
// Simple translation
t('judo.pages.UserForm.title')  
// Returns: "Create User"

// With default fallback
t('judo.pages.UserForm.title', { defaultValue: 'Create User' })

// With interpolation
t('judo.validation.minLength', { 
  defaultValue: 'Minimum {{min}} characters', 
  interpolation: { min: 8 } 
})
// Returns: "Minimum 8 characters"

// With count (pluralization)
t('judo.common.itemsSelected', {
  defaultValue: '{{count}} item selected',
  count: 5
})
// Returns: "5 items selected"

// Nested object access
t('judo.pages.UserForm.fields.firstName.label', {
  defaultValue: 'First Name'
})
```

## Runtime Translation

```typescript
// I18n context
interface I18nContext {
  currentLocale: string;
  translations: Record<string, Record<string, string>>;
  t: TranslationFunction;
  setLocale: (locale: string) => void;
}

// Provider
function I18nProvider({ children, defaultLocale, locales }: Props) {
  const [currentLocale, setCurrentLocale] = useState(defaultLocale);
  const [translations, setTranslations] = useState<Record<string, Record<string, string>>>({});
  
  const t: TranslationFunction = (key, options = {}) => {
    const localeTranslations = translations[currentLocale] || {};
    const translation = localeTranslations[key];
    
    if (!translation) {
      return options.defaultValue || key;
    }
    
    // Handle interpolation
    if (options.interpolation) {
      return Object.entries(options.interpolation).reduce(
        (str, [key, value]) => str.replace(`{{${key}}}`, String(value)),
        translation
      );
    }
    
    return translation;
  };
  
  const context: I18nContext = {
    currentLocale,
    translations,
    t,
    setLocale: setCurrentLocale
  };
  
  return (
    <I18nContext.Provider value={context}>
      {children}
    </I18nContext.Provider>
  );
}

// Hook
function useTranslation() {
  const context = useContext(I18nContext);
  return { t: context.t, locale: context.currentLocale };
}
```

## Generated Translation Files

```typescript
// Generated: ~/i18n/en-US.json
{
  "judo.pages.UserForm.title": "Create User",
  "judo.pages.UserForm.actions.save": "Save",
  "judo.pages.UserForm.actions.cancel": "Cancel",
  "judo.pages.UserForm.fields.firstName.label": "First Name",
  "judo.validation.required": "This field is required",
  "judo.enums.UserStatus.ACTIVE": "Active"
}

// Generated: ~/i18n/hu-HU.json
{
  "judo.pages.UserForm.title": "Felhasználó létrehozása",
  "judo.pages.UserForm.actions.save": "Mentés",
  "judo.pages.UserForm.actions.cancel": "Mégse",
  "judo.pages.UserForm.fields.firstName.label": "Keresztnév",
  "judo.validation.required": "Ez a mező kötelező",
  "judo.enums.UserStatus.ACTIVE": "Aktív"
}
```

## Validation Rules

- All keys should follow the naming convention
- Default values must be in English
- Key prefixes should be consistent
- Translations should maintain same interpolation placeholders
- Pluralization rules should be locale-aware

## Related Specifications

**Runtime Model:**
- `01-core-types.md` - Base types

**Generators:**
- `generators/09-i18n-generator.md` - I18n file generation

**Integration:**
- `integration/02-i18n-integration.md` - I18n system integration

---

**Status:** ✅ Complete  
**Created:** 2025-11-28  
**Last Updated:** 2025-11-28  
**Reviewed:** Not yet reviewed

