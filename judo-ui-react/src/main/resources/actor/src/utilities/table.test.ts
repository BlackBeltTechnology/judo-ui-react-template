import { expect, describe, it } from 'vitest';
import type { JudoStored } from '~/services/data-api/common';
import { isRowSelectable, pinColumns } from './table';

interface TestType extends JudoStored<TestType>{
    name: string
}

interface ResponseTestType extends TestType {
    __selected?: boolean;
};

type BaseTestType = Omit<TestType, 'name'>;

describe('isRowSelectable', () => {
    const baseType: BaseTestType = {
        __signedIdentifier: 'test-sid'
    };
    const rangeResultRow: ResponseTestType = {
        ...baseType,
        name: 'test1'
    };

    describe('aggregation', () => {
        describe('table', () => {
            it('selectable, not present in memory', () => {
                const inMemoryItems: TestType[] = [];
                expect(isRowSelectable(rangeResultRow, false, inMemoryItems)).toBe(true);
            });

            it('not selectable, present in memory', () => {
                const inMemoryItems: TestType[] = [
                    rangeResultRow,
                ];
                expect(isRowSelectable(rangeResultRow, false, inMemoryItems)).toBe(false);
            });

            it('not selectable, present in memory, even if __selected is false', () => {
                const rangeResultRowNotSelected: ResponseTestType = {
                    ...rangeResultRow,
                    __selected: false
                };
                const inMemoryItems: TestType[] = [
                    {
                        ...rangeResultRowNotSelected,
                    },
                ];
                expect(isRowSelectable(rangeResultRowNotSelected, false, inMemoryItems)).toBe(false);
            });
        });

        describe('single', () => {
            it('selectable, not present in memory', () => {
                const inMemoryItems: TestType[] = [];
                expect(isRowSelectable(rangeResultRow, true, inMemoryItems)).toBe(true);
            });

            it('selectable, no value present', () => {
                expect(isRowSelectable(rangeResultRow, true)).toBe(true);
            });

            it('not selectable, respects __selected flag', () => {
                const rangeResultRowSelected: ResponseTestType = {
                    ...rangeResultRow,
                    __selected: true,
                };
                expect(isRowSelectable(rangeResultRowSelected, true)).toBe(false);
            });

            it('not selectable, present in memory', () => {
                const inMemoryItems: TestType[] = [
                    rangeResultRow,
                ];
                expect(isRowSelectable(rangeResultRow, true, inMemoryItems)).toBe(false);
            });
        });
    });

    describe('association', () => {
        describe('table', () => {
            it('selectable, __selected missing', () => {
                expect(isRowSelectable(rangeResultRow, false)).toBe(true);
            });

            it('selectable, __selected false', () => {
                const rangeResultRowNotSelected: ResponseTestType = {
                    ...rangeResultRow,
                    __selected: false,
                };
                expect(isRowSelectable(rangeResultRowNotSelected, false)).toBe(true);
            });

            it('not selectable, __selected true', () => {
                const rangeResultRowSelected: ResponseTestType = {
                    ...rangeResultRow,
                    __selected: true,
                };
                expect(isRowSelectable(rangeResultRowSelected, false)).toBe(false);
            });
        });
    });
});

describe('pinColumns', () => {
    const cols: Parameters<typeof pinColumns>[0] = [
        { field: 'note', flex: 1, minWidth: 150 },
        { field: 'total', width: 120 },
        { field: 'name', width: 200 },
        { field: 'code', width: 100 },
        { field: 'actions', type: 'actions', minWidth: 80 },
    ];

    it('moves pins to their edge, offsets them by the pinned widths and keeps actions outermost right', () => {
        const { sx, columns } = pinColumns(cols, { left: ['name', 'code'], right: ['total'] });

        expect(columns.map((c) => c.field)).toEqual(['name', 'code', 'note', 'total', 'actions']);
        expect(sx).toEqual({
            '& [data-field="name"]': { left: 0 },
            '& [data-field="code"]': { left: 200, boxShadow: '4px 0 6px -3px rgba(0, 0, 0, 0.25)' },
            '& [data-field="actions"]': { right: 0 },
            '& [data-field="total"]': { right: 80, boxShadow: '-4px 0 6px -3px rgba(0, 0, 0, 0.25)' },
        });
    });

    it('gives pinned columns a fixed width and MUI pinned classes, leaves the rest untouched', () => {
        const { columns } = pinColumns(cols, { left: ['name'] });

        expect(columns[0]).toMatchObject({
            field: 'name',
            width: 200,
            cellClassName: 'MuiDataGrid-cell--pinnedLeft',
            headerClassName: 'MuiDataGrid-columnHeader--pinnedLeft',
        });
        expect(columns.find((c) => c.field === 'actions')).toMatchObject({ width: 80, cellClassName: 'MuiDataGrid-cell--pinnedRight' });
        expect(columns.find((c) => c.field === 'note')).toBe(cols[0]);
    });

    it('drops flex from a pinned column', () => {
        const { columns } = pinColumns([{ field: 'note', flex: 1, minWidth: 150 }], { left: ['note'] });

        expect(columns[0].flex).toBeUndefined();
        expect(columns[0].width).toBe(150);
    });

    it('keeps existing class names', () => {
        const { columns } = pinColumns([{ field: 'name', width: 10, headerClassName: 'data-grid-column-header', cellClassName: () => 'x' }], { left: ['name'] });

        expect(columns[0].headerClassName).toBe('data-grid-column-header MuiDataGrid-columnHeader--pinnedLeft');
        expect((columns[0].cellClassName as (p: unknown) => string)({})).toBe('x MuiDataGrid-cell--pinnedLeft');
    });

    it('pins only the actions column when nothing is modelled', () => {
        const { sx, columns } = pinColumns([{ field: 'name' }, { field: 'actions', type: 'actions', width: 90 }], {});

        expect(sx).toEqual({ '& [data-field="actions"]': { right: 0, boxShadow: '-4px 0 6px -3px rgba(0, 0, 0, 0.25)' } });
        expect(columns.map((c) => c.field)).toEqual(['name', 'actions']);
    });
});
