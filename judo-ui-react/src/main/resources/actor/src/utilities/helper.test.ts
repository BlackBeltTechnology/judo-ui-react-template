import { describe, expect, it } from 'vitest';
import { getValue, setValue } from './helper';

describe('getValue function', () => {
  const input = { a: [{ b: { c: 3 } }] };

  it('basic functionality', () => {
    expect(getValue(input, 'a[0].b.c')).toBe(3);
    expect(getValue(input, 'a.b.c', 'default')).toBe('default');
  });

  it('nested properties', () => {
    expect(getValue(input, 'a[0].b')).toBeTruthy();
    expect(getValue(input, 'a[0].b.c')).toBe(3);
    expect(getValue(input, 'a[0].b.c.d', 'default')).toBe('default');
  });

  it('default values', () => {
    expect(getValue(input, 'a.b.c', 'not found')).toBe('not found');
    expect(getValue(input, 'a[1].b.c', 'default')).toBe('default');
    expect(getValue(input, 'a[0].b.x', 42)).toBe(42);
  });

  it('non-existent paths', () => {
    expect(getValue(input, 'x.y.z', 'default')).toBe('default');
    expect(getValue(input, 'a[0].x.y.z', 'default')).toBe('default');
  });

  it('undefined objects return defaults', () => {
    expect(getValue(undefined, 'a.b.c', 'default')).toBe('default');
  });

  it('null objects return nulls', () => {
    expect(getValue(null, 'a.b.c', 'default')).toBe(null);
  });

  it('handling empty path', () => {
    expect(getValue(input, '', 'default')).toBe(input);
    expect(getValue(input, '', null)).toBe(input);
  });

  it('handling missing path', () => {
    expect(getValue(input, undefined, 'default')).toBe('default');
    expect(getValue(input)).toBe(null);
  });

  it('arrays as values', () => {
    const inputWithArray = { a: { b: [1, 2, 3] } };
    expect(getValue(inputWithArray, 'a.b[0]')).toBe(1);
    expect(getValue(inputWithArray, 'a.b[2]')).toBe(3);
    expect(getValue(inputWithArray, 'a.b[3]', 'default')).toBe('default');
  });
});

describe('setValue function', () => {
  it('sets a simple property', () => {
    const obj = {};
    setValue(obj, 'a', 1);
    expect(obj).toEqual({ a: 1 });
  });

  it('sets a nested property', () => {
    const obj = {};
    setValue(obj, 'a.b', 2);
    expect(obj).toEqual({ a: { b: 2 } });
  });

  it('sets a property in an array', () => {
    const obj = {};
    setValue(obj, 'a[0]', 3);
    expect(obj).toEqual({ a: [3] });
  });

  it('sets a nested property in an array', () => {
    const obj = {};
    setValue(obj, 'a[0].b', 4);
    expect(obj).toEqual({ a: [{ b: 4 }] });
  });

  it('creates intermediate objects and arrays', () => {
    const obj = {};
    setValue(obj, 'a[0].b.c[1]', 5);
    expect(obj).toEqual({ a: [{ b: { c: [undefined, 5] } }] });
  });

  it('overwrites existing property', () => {
    const obj = { a: 1 };
    setValue(obj, 'a', 2);
    expect(obj).toEqual({ a: 2 });
  });

  it('sets property in existing nested object', () => {
    const obj = { a: { b: 1 } };
    setValue(obj, 'a.c', 2);
    expect(obj).toEqual({ a: { b: 1, c: 2 } });
  });

  it('handles path with array indices', () => {
    const obj = {};
    setValue(obj, 'a[1].b[2]', 6);
    expect(obj).toEqual({ a: [undefined, { b: [undefined, undefined, 6] }] });
  });

  it('handles mixed array and object path', () => {
    const obj = {};
    setValue(obj, 'a.b[0].c', 7);
    expect(obj).toEqual({ a: { b: [{ c: 7 }] } });
  });

  it('handles leading dot in path', () => {
    const obj = {};
    setValue(obj, '.a.b.c', 8);
    expect(obj).toEqual({ a: { b: { c: 8 } } });
  });

  it('handles trailing dot in path', () => {
    const obj = {};
    setValue(obj, 'a.b.c.', 9);
    expect(obj).toEqual({ a: { b: { c: 9 } } });
  });

  it('sets value on non-object target', () => {
    const obj = { a: null };
    setValue(obj, 'a.b', 10);
    expect(obj).toEqual({ a: { b: 10 } });
  });

  it('handles number as key', () => {
    const obj = {};
    setValue(obj, '1', 11);
    expect(obj).toEqual({ '1': 11 });
  });

  it('creates nested arrays', () => {
    const obj = {};
    setValue(obj, 'a[0][1]', 13);
    expect(obj).toEqual({ a: [[undefined, 13]] });
  });

  it('handles complex path with numbers and strings', () => {
    const obj = {};
    setValue(obj, 'a[1].b[2].c', 14);
    expect(obj).toEqual({ a: [undefined, { b: [undefined, undefined, { c: 14 }] }] });
  });

  it('throws error for consecutive dots in path', () => {
    const obj = {};
    expect(() => setValue(obj, 'a..b', 15)).toThrow();
  });

  it('throws error for path with empty brackets', () => {
    const obj = {};
    expect(() => setValue(obj, 'a[].b', 16)).toThrow();
  });

  it('handles path with brackets and dots', () => {
    const obj = {};
    setValue(obj, 'a[0].b.c[1]', 17);
    expect(obj).toEqual({ a: [{ b: { c: [undefined, 17] } }] });
  });

  it('handles setting a deep nested value', () => {
    const obj = {};
    setValue(obj, 'a.b.c.d.e.f.g', 18);
    expect(obj).toEqual({ a: { b: { c: { d: { e: { f: { g: 18 } } } } } } });
  });

  it('does not overwrite existing nested objects', () => {
    const obj = { a: { b: { c: 1 } } };
    setValue(obj, 'a.b.d', 19);
    expect(obj).toEqual({ a: { b: { c: 1, d: 19 } } });
  });
});
