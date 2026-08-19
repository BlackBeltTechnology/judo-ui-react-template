import { afterEach, describe, expect, it, vi } from 'vitest';
import {
  clearNamespacedStorage,
  deriveStorageNamespace,
  getStorageKeyPrefix,
  getStorageNamespace,
  namespacedStorageKey,
} from './storage-namespace';

describe('deriveStorageNamespace', () => {
  it('maps an application base path to a dot-joined namespace', () => {
    expect(deriveStorageNamespace('/RelationTest/Actor/')).toBe('RelationTest.Actor');
  });

  it('keeps sibling engines on distinct namespaces', () => {
    expect(deriveStorageNamespace('/RelationTest_runtime/Actor/')).toBe('RelationTest_runtime.Actor');
    expect(deriveStorageNamespace('/RelationTest/Actor/')).not.toBe(deriveStorageNamespace('/RelationTest_runtime/Actor/'));
  });

  it('falls back to the root namespace for the origin root', () => {
    expect(deriveStorageNamespace('/')).toBe('root');
    expect(deriveStorageNamespace('')).toBe('root');
    expect(deriveStorageNamespace('///')).toBe('root');
  });

  it('collapses redundant slashes', () => {
    expect(deriveStorageNamespace('//RelationTest//Actor//')).toBe('RelationTest.Actor');
  });

  it('is total: every input yields a non-empty namespace', () => {
    for (const input of ['/', 'a', '/a/b/c/', '////', 'a//b']) {
      expect(deriveStorageNamespace(input).length).toBeGreaterThan(0);
    }
  });
});

describe('getStorageNamespace', () => {
  const originalDocument = globalThis.document;

  afterEach(() => {
    if (originalDocument === undefined) {
      // biome-ignore lint/performance/noDelete: test teardown of a global
      delete (globalThis as { document?: unknown }).document;
    } else {
      (globalThis as { document?: unknown }).document = originalDocument;
    }
  });

  it('returns the root namespace when document is undefined', () => {
    // biome-ignore lint/performance/noDelete: simulating a non-browser context
    delete (globalThis as { document?: unknown }).document;
    expect(getStorageNamespace()).toBe('root');
  });

  it('never throws on a malformed base URI', () => {
    (globalThis as { document?: unknown }).document = { baseURI: 'not a url' };
    expect(() => getStorageNamespace()).not.toThrow();
    expect(getStorageNamespace()).toBe('root');
  });

  it('derives the namespace from the document base URI', () => {
    (globalThis as { document?: unknown }).document = { baseURI: 'https://example.com/RelationTest/Actor/' };
    expect(getStorageNamespace()).toBe('RelationTest.Actor');
  });
});

describe('namespacedStorageKey', () => {
  it('prefixes a key with the judo namespace marker', () => {
    expect(namespacedStorageKey('oidc.user:realm:client', 'RelationTest.Actor')).toBe(
      'judo:RelationTest.Actor:oidc.user:realm:client',
    );
  });

  it('agrees with the exported prefix', () => {
    expect(namespacedStorageKey('k', 'ns')).toBe(`${getStorageKeyPrefix('ns')}k`);
  });
});

describe('clearNamespacedStorage', () => {
  it('removes only this application entries', () => {
    const entries = new Map<string, string>([
      [`${getStorageKeyPrefix()}mine`, '1'],
      ['judo:OtherApp.Actor:theirs', '2'],
      ['unprefixed', '3'],
    ]);
    const storage = {
      get length() {
        return entries.size;
      },
      key: (index: number) => [...entries.keys()][index] ?? null,
      removeItem: (key: string) => {
        entries.delete(key);
      },
    } as unknown as Storage;

    clearNamespacedStorage(storage);

    expect([...entries.keys()]).toEqual(['judo:OtherApp.Actor:theirs', 'unprefixed']);
  });
});
