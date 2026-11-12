/**
 * How the inline file preview shows a file (change harden-native-file-preview). Pure.
 *
 * An allow-list: the stored content type wins, the extension is used only when the type is empty or
 * `application/octet-stream`. HTML, XML and every unknown type are download-only. `type` is the Blob type
 * the preview forces, never the uploader's claim. Every display kind is safe for any bytes, so a wrong
 * classification can only make a preview fail, never run code.
 */
export type PreviewKind = 'pdf' | 'image' | 'svg' | 'video' | 'audio' | 'text' | 'download';

export interface PreviewPlan {
  kind: PreviewKind;
  /** The Blob type to force for pdf / image / svg / video / audio; empty otherwise. */
  type: string;
}

const TEXT_TYPES = new Set(['text/plain', 'text/markdown', 'text/x-markdown', 'text/csv', 'application/json']);
const RASTER_IMAGE = /^image\/(png|jpe?g|gif|webp|bmp|avif)$/;

/** Extension → type, used only when the stored type is empty or generic. `Map`: no prototype keys. */
const TYPE_BY_EXTENSION = new Map<string, string>(
  Object.entries({
    pdf: 'application/pdf',
    png: 'image/png',
    jpg: 'image/jpeg',
    jpeg: 'image/jpeg',
    gif: 'image/gif',
    webp: 'image/webp',
    bmp: 'image/bmp',
    avif: 'image/avif',
    svg: 'image/svg+xml',
    mp4: 'video/mp4',
    webm: 'video/webm',
    mov: 'video/quicktime',
    mp3: 'audio/mpeg',
    wav: 'audio/wav',
    m4a: 'audio/mp4',
    ogg: 'audio/ogg',
    txt: 'text/plain',
    md: 'text/plain',
    hbs: 'text/plain',
    csv: 'text/plain',
    json: 'text/plain',
    log: 'text/plain',
  }),
);

/**
 * Accepted media types (aliases included) → the literal Blob type we force. Never the stored string
 * itself: `video/mp4,text/html` or a bare `video/` would make an "open in new tab" render HTML.
 */
const MEDIA_TYPE = new Map<string, string>(
  Object.entries({
    'video/mp4': 'video/mp4',
    'video/webm': 'video/webm',
    'video/quicktime': 'video/quicktime',
    'video/ogg': 'video/ogg',
    'audio/mpeg': 'audio/mpeg',
    'audio/mp3': 'audio/mpeg',
    'audio/wav': 'audio/wav',
    'audio/wave': 'audio/wav',
    'audio/x-wav': 'audio/wav',
    'audio/mp4': 'audio/mp4',
    'audio/x-m4a': 'audio/mp4',
    'audio/ogg': 'audio/ogg',
    'audio/webm': 'audio/webm',
  }),
);

const DOWNLOAD: PreviewPlan = { kind: 'download', type: '' };

export const previewKind = (fileName: string, fileType: string): PreviewPlan => {
  const stored = (fileType ?? '').split(';')[0].trim().toLowerCase();
  const extension = (fileName ?? '').toLowerCase().split('.').pop() ?? '';
  const type = stored && stored !== 'application/octet-stream' ? stored : (TYPE_BY_EXTENSION.get(extension) ?? '');

  if (type === 'application/pdf') return { kind: 'pdf', type };
  if (type === 'image/svg+xml') return { kind: 'svg', type };
  if (RASTER_IMAGE.test(type)) return { kind: 'image', type: type === 'image/jpg' ? 'image/jpeg' : type };
  const media = MEDIA_TYPE.get(type);
  if (media) return { kind: media.startsWith('video/') ? 'video' : 'audio', type: media };
  if (TEXT_TYPES.has(type)) return { kind: 'text', type: '' };
  return DOWNLOAD;
};

/** A generically typed copy: the browser can only download it, never render it as a page. */
export const downloadBlob = (data: Blob | Uint8Array<ArrayBuffer>): Blob => new Blob([data], { type: 'application/octet-stream' });

/** `data:` URL for bytes (SVG): a script in an SVG shown as `<img src=data:…>` never runs. */
export const toDataUrl = (type: string, bytes: Uint8Array): string => {
  let binary = '';
  const chunk = 0x8000;
  for (let i = 0; i < bytes.length; i += chunk) binary += String.fromCharCode(...bytes.subarray(i, i + chunk));
  return `data:${type};base64,${btoa(binary)}`;
};

/**
 * Saves data as a file via a short-lived, generically typed object URL. A click handler, never an `href`:
 * there is no link a user could open in a new tab and render. Revoke is delayed because WebKit can fail a
 * download whose URL is revoked right after the click.
 */
export function saveAsDownload(data: Blob | Uint8Array<ArrayBuffer>, filename: string) {
  const url = URL.createObjectURL(downloadBlob(data));
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  setTimeout(() => URL.revokeObjectURL(url), 30_000);
}
