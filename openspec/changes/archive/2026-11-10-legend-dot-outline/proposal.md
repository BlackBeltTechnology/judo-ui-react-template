# Outline the row highlight legend dots

## Why

Tables with configured row stylers (`RowStylerConfigured`) render a `RowHighlightLegend` below the
grid: one coloured dot per styler, followed by its translated label. Each dot is an MDI `record`
glyph whose colour is the styler's `backgroundColor`.

Row highlight backgrounds are usually light, so they stay readable behind the row text. White and
near-white styler colours are common. Drawn on the white table card, those dots disappear: the
legend shows a label next to empty space, and users cannot connect the legend entry to the
highlighted rows.

## What Changes

- `actor/src/components/table/RowHighlightLegend.tsx.hbs` — the legend dot keeps
  `color: s.backgroundColor` and gains a 1px outline:
  `WebkitTextStroke: (theme: Theme) => \`1px ${theme.palette.grey[600]}\``, with
  `import type { Theme } from '@mui/material'`.
- The template carries no comment explaining the outline. The rationale lives below, following the
  `code-generation` requirement "Generated code SHALL NOT carry design rationale as comments".

## Outline rationale (kept out of the generated code)

- **Why an outline, not a different fill.** The fill must stay exactly the styler's
  `backgroundColor`, because the dot is a swatch of the row colour. An outline around the glyph
  makes light colours visible and leaves the swatch unchanged.
- **Why `-webkit-text-stroke`.** `MdiIcon` renders an MDI webfont glyph (`<span class="mdi
  mdi-record">`) inside a `Box`, not an SVG, so the glyph is text. `-webkit-text-stroke` outlines
  text glyphs, is inherited from the `Box` `sx`, and is supported by all evergreen browsers
  (including Firefox) under the `-webkit-` prefix. SVG `stroke` would have no effect here.
- **Why `grey[600]`.** WCAG 2.2 SC 1.4.11 (Non-text Contrast) requires 3:1 for graphical objects
  needed to understand content. MUI `grey[600]` (`#757575`) gives about 4.6:1 against the white
  card, so the outline meets the floor even when the fill itself has almost no contrast. Darker
  greys would compete with saturated styler colours, and lighter ones (`grey[400]`/`grey[500]`)
  fall below 3:1.
- **Why via `theme.palette`.** Reading the grey from the theme keeps it a token reference rather
  than a hard-coded hex value. Consuming apps that override the palette get a matching outline.

## Capabilities

### Added Capabilities

- `data-tables` — **Row Highlight Legend**: the legend renders one dot per configured row styler,
  filled with the styler's background colour and outlined with a theme grey that meets non-text
  contrast, followed by its translated label.

## Impact

- **Handlebars template**: `actor/src/components/table/RowHighlightLegend.tsx.hbs` — one type
  import, one `sx` property. No helper or `ui-react.yaml` change.
- **Generated apps**: every generated `src/components/table/RowHighlightLegend.tsx` changes. Used
  by `LazyTable`, `EagerTable` and the containers table component, wherever row stylers are configured.
- **Snapshots**: none. `src/components/table/RowHighlightLegend.tsx` is in no itest snapshot set.
- **Risk**: minimal, a visual-only change. A 1px outline on saturated colours is barely visible.
