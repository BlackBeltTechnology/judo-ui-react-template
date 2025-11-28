# I18n Integration Specification

**Domain:** Integration  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `runtime-model/09-i18n-model.md`, `generators/09-i18n-generator.md`  

## Overview

I18n Integration defines how generated i18n files are loaded and used in the application with support for multiple locales and dynamic switching.

## I18n Provider Setup

```typescript
import React, { createContext, useContext, useState, useEffect } from 'react';
import enUS from '~/i18n/en-US.json';
import huHU from '~/i18n/hu-HU.json';

interface I18nContextValue {
  locale: string;
  t: (key: string, options?: TranslationOptions) => string;
  setLocale: (locale: string) => void;
  supportedLocales: string[];
}

const I18nContext = createContext<I18nContextValue | null>(null);

const translations: Record<string, Record<string, string>> = {
  'en-US': enUS,
  'hu-HU': huHU
};

export function I18nProvider({ children }: { children: React.ReactNode }) {
  const [locale, setLocale] = useState('en-US');
  
  const t = (key: string, options?: TranslationOptions) => {
    const translation = translations[locale]?.[key];
    
    if (!translation) {
      return options?.defaultValue || key;
    }
    
    // Handle interpolation
    if (options?.interpolation) {
      return Object.entries(options.interpolation).reduce(
        (str, [k, v]) => str.replace(`{{${k}}}`, String(v)),
        translation
      );
    }
    
    return translation;
  };
  
  return (
    <I18nContext.Provider value={{
      locale,
      t,
      setLocale,
      supportedLocales: ['en-US', 'hu-HU']
    }}>
      {children}
    </I18nContext.Provider>
  );
}

export function useTranslation() {
  const context = useContext(I18nContext);
  if (!context) {
    throw new Error('useTranslation must be used within I18nProvider');
  }
  return context;
}
```

---

**Status:** ✅ Complete  
**Created:** 2025-11-28

