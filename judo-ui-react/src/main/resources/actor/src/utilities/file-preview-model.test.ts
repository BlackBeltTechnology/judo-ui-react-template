import { describe, expect, it } from 'vitest';
import vectors from './file-preview-vectors.json';
import { previewKind } from './file-preview-model';

describe('file preview classifier (shared contract vectors)', () => {
  for (const v of vectors.vectors) {
    it(`${v.fileName} | ${v.storedType || '(empty)'} -> ${v.kind}`, () => {
      const plan = previewKind(v.fileName, v.storedType);
      // the contract lists SVG as its own kind; the framework default has no `eml`/`audio` special cases beyond the table
      expect({ kind: plan.kind, type: plan.type }).toEqual({ kind: v.kind, type: v.forcedType });
    });
  }
});
