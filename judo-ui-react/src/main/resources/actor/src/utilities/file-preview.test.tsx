import { renderToStaticMarkup } from 'react-dom/server';
import { describe, expect, it } from 'vitest';
import { previewKind } from '~/utilities/file-preview-model';
import { TypedPreview } from '~/utilities/file-preview';

// server-side render: no DOM needed, and the first render of every kind that has to be inert is synchronous
const html = (fileName: string, fileType: string) =>
  renderToStaticMarkup(
    <TypedPreview blob={new Blob(['<script>1</script>'])} fileName={fileName} plan={previewKind(fileName, fileType)} height={100} />,
  );

describe('TypedPreview', () => {
  it.each([
    ['x.html', 'text/html'],
    ['x.svg.html', 'text/html'],
    ['x.xml', 'text/xml'],
    ['x.xhtml', 'application/xhtml+xml'],
    ['x.mp4', 'video/mp4,text/html'],
    ['unknown.bin', 'application/octet-stream'],
  ])('%s as %s shows "no preview" and never an iframe or the file', (name, type) => {
    const out = html(name, type);
    expect(out).toContain('data-testid="file-no-preview"');
    expect(out).toContain('data-testid="file-download"');
    expect(out).not.toContain('<iframe');
    expect(out).not.toContain('<script');
  });
});
