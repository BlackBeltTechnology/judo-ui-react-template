import { describe, expect, it } from 'vitest';
import {
  ROW_ACTION_ROLE_OVERFLOW,
  buildCellTestId,
  buildFilterPanelColumnInputTestId,
  buildFilterPanelOperatorInputTestId,
  buildFilterPanelTestId,
  buildFilterPanelValueInputTestId,
  buildRowActionTestId,
  buildRowTestId,
  buildSelectionCellTestId,
  buildTableTestId,
} from './transfer-id';

const table = 'Galaxies';
const row = { __identifier: 'sid-1' };

describe('buildRowActionTestId', () => {
  it('emits the canonical row-scoped action grammar', () => {
    expect(buildRowActionTestId(table, row, 'delete')).toBe('table::Galaxies::row::sid-1::action::delete');
  });

  it('is prefixed by the row test id', () => {
    expect(buildRowActionTestId(table, row, 'view')).toBe(`${buildRowTestId(table, row)}::action::view`);
  });

  it('supports the reserved overflow role for the dropdown trigger', () => {
    expect(ROW_ACTION_ROLE_OVERFLOW).toBe('overflow');
    expect(buildRowActionTestId(table, row, ROW_ACTION_ROLE_OVERFLOW)).toBe(
      'table::Galaxies::row::sid-1::action::overflow',
    );
  });

  it('never emits the legacy ::button:: form', () => {
    expect(buildRowActionTestId(table, row, 'delete')).not.toContain('::button::');
  });

  it('falls back to the positional index when the row has no identity', () => {
    expect(buildRowActionTestId(table, {}, 'delete', 3)).toBe('table::Galaxies::row::idx-3::action::delete');
  });

  it('rejects an empty or unknown role segment', () => {
    expect(() => buildRowActionTestId(table, row, '')).toThrow();
    expect(() => buildRowActionTestId(table, row, 'unknown')).toThrow();
  });
});

describe('buildFilterPanelTestId', () => {
  it('is scoped under the table test id', () => {
    expect(buildFilterPanelTestId(table)).toBe('table::Galaxies::filter-panel');
    expect(buildFilterPanelTestId(table)).toBe(`${buildTableTestId(table)}::filter-panel`);
  });

  it('emits the column, operator and value input variants', () => {
    expect(buildFilterPanelColumnInputTestId(table)).toBe('table::Galaxies::filter-panel::column');
    expect(buildFilterPanelOperatorInputTestId(table)).toBe('table::Galaxies::filter-panel::operator');
    expect(buildFilterPanelValueInputTestId(table)).toBe('table::Galaxies::filter-panel::value');
  });

  it('falls back to unknown for a missing table id', () => {
    expect(buildFilterPanelTestId(undefined)).toBe('table::unknown::filter-panel');
  });
});

describe('buildSelectionCellTestId', () => {
  it('delegates to buildCellTestId with the MUI selection field name', () => {
    expect(buildSelectionCellTestId(table, row)).toBe(buildCellTestId(table, row, '__check__'));
    expect(buildSelectionCellTestId(table, row)).toBe('table::Galaxies::row::sid-1::cell::__check__');
  });

  it('threads the positional index through', () => {
    expect(buildSelectionCellTestId(table, {}, 7)).toBe(buildCellTestId(table, {}, '__check__', 7));
  });
});
