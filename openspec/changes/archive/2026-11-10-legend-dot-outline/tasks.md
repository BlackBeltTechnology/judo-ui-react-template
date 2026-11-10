## 1. Template

- [x] 1.1 Add `import type { Theme } from '@mui/material'` to `actor/src/components/table/RowHighlightLegend.tsx.hbs`
- [x] 1.2 Add `WebkitTextStroke: (theme: Theme) => \`1px ${theme.palette.grey[600]}\`` to the legend dot `sx`, keeping `color: s.backgroundColor` unchanged
- [x] 1.3 Keep the outline rationale out of the generated output: remove the inline comment block from the template and record the rationale in `proposal.md` under "Outline rationale (kept out of the generated code)"

## 2. Verify

- [x] 2.1 Confirm `MdiIcon` renders a webfont glyph (`<span class="mdi mdi-record">` inside a `Box`), so `-webkit-text-stroke` applies to it via the `Box` `sx`
- [x] 2.2 Confirm no committed itest snapshot contains `RowHighlightLegend.tsx`, so no snapshot refresh is needed

## 3. Spec Delta and Archive

- [x] 3.1 Add a `data-tables` delta: "Row Highlight Legend" requirement covering the swatch fill, theme-grey outline and 3:1 non-text contrast
- [x] 3.2 `openspec validate legend-dot-outline --strict` passes
- [x] 3.3 Archive the change and confirm the `data-tables` delta merges into `openspec/specs/data-tables/spec.md`
