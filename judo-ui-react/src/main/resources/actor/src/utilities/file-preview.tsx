/**
 * Content of the inline file-preview dialog (change harden-native-file-preview).
 *
 * The server-typed object URL of the downloaded file is never put into the DOM: an unsandboxed
 * `<iframe src=blob:…>` ran an uploaded `.html` / `.svg` on the app origin. Every URL rendered here comes
 * from a Blob whose type this file chose, and is revoked on unmount.
 *  - an app-registered provider (FILE_PREVIEW_HOOK_INTERFACE_KEY) is asked first;
 *  - PDF → iframe; photo / SVG → `<img>`; video / audio → player; text → `<pre>`;
 *  - HTML, XML and everything else → "no preview" + a generic download.
 */
import Alert from '@mui/material/Alert';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import CircularProgress from '@mui/material/CircularProgress';
import { OBJECTCLASS } from '@pandino/pandino-api';
import { useTrackService } from '@pandino/react-hooks';
import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { FILE_PREVIEW_HOOK_INTERFACE_KEY, type FilePreviewProvider, safely } from '~/utilities/file-preview-hook';
import { type PreviewPlan, previewKind, saveAsDownload, toDataUrl } from '~/utilities/file-preview-model';

export interface FilePreviewProps {
  /** The downloaded file (`response.data` of the inline download). */
  blob: Blob;
  fileName: string;
  fileType: string;
}

export function FilePreview({ blob, fileName, fileType }: FilePreviewProps) {
  const height = document.body.scrollHeight * 0.6;
  const { service: provider } = useTrackService<FilePreviewProvider>(`(${OBJECTCLASS}=${FILE_PREVIEW_HOOK_INTERFACE_KEY})`);
  const custom = safely(provider, { fileName, blob, height });
  if (custom !== undefined && custom !== null) {
    return <>{custom}</>;
  }
  return <TypedPreview blob={blob} fileName={fileName} plan={previewKind(fileName, fileType)} height={height} />;
}

interface TypedPreviewProps {
  blob: Blob;
  fileName: string;
  plan: PreviewPlan;
  height: number;
}

/** `content`: an object URL (pdf / image / video / audio), a `data:` URL (svg) or the text (text). */
function TypedPreview({ blob, fileName, plan: { kind, type }, height }: TypedPreviewProps) {
  const { t } = useTranslation();
  const [content, setContent] = useState<string>();
  const [failed, setFailed] = useState(false);

  useEffect(() => {
    let alive = true;
    let objectUrl: string | undefined;
    setContent(undefined);
    setFailed(false);
    if (kind === 'pdf' || kind === 'image' || kind === 'video' || kind === 'audio') {
      objectUrl = URL.createObjectURL(new Blob([blob], { type }));
      setContent(objectUrl);
    } else if (kind === 'svg') {
      blob
        .arrayBuffer()
        .then((buffer) => alive && setContent(toDataUrl(type, new Uint8Array(buffer))))
        .catch(() => alive && setFailed(true));
    } else if (kind === 'text') {
      blob
        .text()
        .then((text) => alive && setContent(text))
        .catch(() => alive && setFailed(true));
    }
    return () => {
      alive = false;
      if (objectUrl) URL.revokeObjectURL(objectUrl);
    };
  }, [blob, kind, type]);

  if (kind === 'download' || failed) {
    return (
      <Alert
        severity="info"
        data-testid="file-no-preview"
        action={
          <Button size="small" onClick={() => saveAsDownload(blob, fileName)} data-testid="file-download">
            {t('judo.files.download', { defaultValue: 'Download' })}
          </Button>
        }
      >
        {t('judo.files.no-preview', { defaultValue: 'No preview is available for this file type.' })}
      </Alert>
    );
  }
  if (content === undefined) {
    return <CircularProgress size={24} />;
  }
  switch (kind) {
    case 'pdf':
      return <iframe src={content} title={fileName} width={'100%'} height={height} style={{ border: 0 }} />;
    case 'image':
    case 'svg':
      return (
        <Box
          component="img"
          src={content}
          alt={fileName}
          data-testid="file-preview-image"
          sx={{ display: 'block', maxWidth: '100%', maxHeight: height, mx: 'auto' }}
        />
      );
    case 'video':
      return <Box component="video" controls src={content} data-testid="file-preview-video" sx={{ maxWidth: '100%', maxHeight: height }} />;
    case 'audio':
      return <Box component="audio" controls src={content} data-testid="file-preview-audio" sx={{ width: '100%' }} />;
    default:
      return (
        <Box
          component="pre"
          data-testid="file-text"
          sx={{ m: 0, maxHeight: height, overflow: 'auto', whiteSpace: 'pre-wrap', overflowWrap: 'anywhere' }}
        >
          {content}
        </Box>
      );
  }
}
