import { expect, describe, it } from 'vitest';
import type { JudoStored } from '~/services/data-api/common';
import { ghostShown, isRowSelectable, pinColumns } from './table';

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

    it('keeps model order and adds a 1px stand-in per pin on its own side, except for the actions column', () => {
        const { columns } = pinColumns(cols, { left: ['name', 'code'], right: ['total'] });

        expect(columns.map((c) => [c.field, c.width])).toEqual([
            ['name__pin', 1],
            ['code__pin', 1],
            ['note', undefined],
            ['total', 120],
            ['name', 200],
            ['code', 100],
            ['actions', 80],
            ['total__pin', 1],
        ]);
        expect(columns[7].cellClassName).toBe('MuiDataGrid-cell--pinnedRight');
    });

    it('gives pins and stand-ins the same sticky offsets, shadows only the innermost stuck one', () => {
        const { sx } = pinColumns(cols, { left: ['name', 'code'], right: ['total'] });

        expect(sx).toMatchObject({
            '& [data-field="name"]': { left: 0 },
            '& [data-field="code"]': { left: 200 },
            '& [data-field="code__pin"]': {
                left: 200,
                width: 'calc(100px - var(--pin-hide-code__pin, 100px)) !important',
                marginRight: 'calc(var(--pin-hide-code__pin, 100px) - 100px)',
            },
            '& [data-field="actions"]': { right: 0 },
            '& [data-field="total"]': { right: 80 },
            '& [data-field="total__pin"]': { right: 80, marginLeft: 'calc(var(--pin-hide-total__pin, 120px) - 120px)' },
            '&[data-pin-edge-l~="code"] [data-field="code"]': { boxShadow: '3px 0 5px -3px rgba(0, 0, 0, 0.14)' },
            '&[data-pin-edge-r~="total__pin"] [data-field="total__pin"]': { boxShadow: '-3px 0 5px -3px rgba(0, 0, 0, 0.14)' },
            '&[data-pin-stuck~="name"] .MuiDataGrid-cell[data-field="name"]': { zIndex: 31 },
        });
        expect(sx).not.toHaveProperty(['& [data-field="actions__pin"]']);
    });

    it('gives pinned columns a fixed width and MUI pinned classes, leaves the rest untouched', () => {
        const { columns } = pinColumns(cols, { left: ['name'] });

        expect(columns.find((c) => c.field === 'name')).toMatchObject({
            width: 200,
            cellClassName: 'MuiDataGrid-cell--pinnedLeft',
            headerClassName: 'MuiDataGrid-columnHeader--pinnedLeft',
        });
        expect(columns.find((c) => c.field === 'actions')).toMatchObject({ width: 80, cellClassName: 'MuiDataGrid-cell--pinnedRight' });
        expect(columns.find((c) => c.field === 'note')).toBe(cols[0]);
    });

    it('drops flex from a pinned column', () => {
        const { columns } = pinColumns([{ field: 'note', flex: 1, minWidth: 150 }], { left: ['note'] });
        const note = columns.find((c) => c.field === 'note');

        expect(note?.flex).toBeUndefined();
        expect(note?.width).toBe(150);
    });

    it('keeps existing class names', () => {
        const { columns } = pinColumns([{ field: 'name', width: 10, headerClassName: 'data-grid-column-header', cellClassName: () => 'x' }], { left: ['name'] });
        const name = columns.find((c) => c.field === 'name');

        expect(name?.headerClassName).toBe('data-grid-column-header MuiDataGrid-columnHeader--pinnedLeft');
        expect((name?.cellClassName as (p: unknown) => string)({})).toBe('x MuiDataGrid-cell--pinnedLeft');
    });

    it('pins only the actions column, without a stand-in, when nothing is modelled', () => {
        const { sx, columns } = pinColumns([{ field: 'name' }, { field: 'actions', type: 'actions', width: 90 }], {});

        expect(sx['& [data-field="actions"]']).toEqual({ right: 0 });
        expect(Object.keys(sx).filter((k) => !k.includes('"actions"'))).toEqual([]);
        expect(columns.map((c) => c.field)).toEqual(['name', 'actions']);
    });
});

describe('ghostShown', () => {
    it("shows on the pin's own side exactly as much as the pin went under the opposite stack", () => {
        expect(ghostShown(0, 400, 200)).toBe(0);
        expect(ghostShown(80, 400, 200)).toBe(80);
        expect(ghostShown(250, 400, 200)).toBe(200);
    });

    it('speeds up when the scroll ends sooner, so the stand-in is whole at the scroll end', () => {
        expect(ghostShown(50, 100, 200)).toBe(100);
        expect(ghostShown(100, 100, 200)).toBe(200);
    });
});
