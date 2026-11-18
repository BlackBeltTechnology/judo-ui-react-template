import { describe, expect, it, vi } from 'vitest';
import { composeFilePreviewProviders, type FilePreviewInput, safely } from './file-preview-hook';

const input = (fileName: string): FilePreviewInput => ({ fileName, fileType: '', blob: new Blob(['x']), height: 100 });
const xyz = ({ fileName }: FilePreviewInput) => (/\.xyz$/i.test(fileName) ? 'XYZ VIEWER' : undefined);

describe('file preview hook', () => {
  it('a provider for an invented format wins, with no framework change', () => {
    expect(safely(xyz, input('a.xyz'))).toBe('XYZ VIEWER');
  });
  it('a declining provider falls back (undefined means: default preview)', () => {
    expect(safely(xyz, input('a.pdf'))).toBeUndefined();
  });
  it('no provider registered: default preview', () => {
    expect(safely(undefined, input('a.eml'))).toBeUndefined();
  });
  it('a throwing provider is treated as declining', () => {
    const spy = vi.spyOn(console, 'error').mockImplementation(() => undefined);
    expect(safely(() => { throw new Error('boom'); }, input('a.xyz'))).toBeUndefined();
    spy.mockRestore();
  });
  it('compose: first viewer wins, others are skipped', () => {
    const abc = ({ fileName }: FilePreviewInput) => (/\.abc$/i.test(fileName) ? 'ABC' : undefined);
    const p = composeFilePreviewProviders(abc, xyz);
    expect(p(input('f.abc'))).toBe('ABC');
    expect(p(input('f.xyz'))).toBe('XYZ VIEWER');
    expect(p(input('f.pdf'))).toBeUndefined();
  });
});
