import type { ReactNode } from 'react';

/**
 * Extension point of the inline file preview (change harden-native-file-preview).
 *
 * Register ONE service under this key to open formats the framework does not know (`.eml`, `.dwg`, a
 * proprietary type …):
 *
 *   context.registerService(FILE_PREVIEW_HOOK_INTERFACE_KEY, myProvider);
 *
 * The framework knows no application-specific format. A provider decides by file name and returns a
 * viewer, or `undefined` to leave the file to the default allow-list preview. The default can not be
 * replaced, so a provider only ever adds handling. A provider that throws is treated as declining.
 * A provider that renders HTML itself must sandbox it.
 */
export const FILE_PREVIEW_HOOK_INTERFACE_KEY = 'file-preview-hook-interface-key';

export interface FilePreviewInput {
  fileName: string;
  /** The downloaded bytes. Use these, not a URL: the uploader's content type is a claim, not a fact. */
  blob: Blob;
  /** Height in pixels the preview should fit. */
  height: number;
}

/** Returns a viewer for the file, or `undefined` when this provider does not handle it. */
export type FilePreviewProvider = (input: FilePreviewInput) => ReactNode | undefined;

/** Chains providers: the first one that returns a viewer wins. Use when an app has several formats. */
export const composeFilePreviewProviders =
  (...providers: FilePreviewProvider[]): FilePreviewProvider =>
  (input) => {
    for (const provider of providers) {
      const result = safely(provider, input);
      if (result !== undefined && result !== null) return result;
    }
    return undefined;
  };

export const safely = (provider: FilePreviewProvider | undefined, input: FilePreviewInput): ReactNode | undefined => {
  if (!provider) return undefined;
  try {
    return provider(input);
  } catch (error) {
    console.error('File preview provider failed, using the default preview', error);
    return undefined;
  }
};
