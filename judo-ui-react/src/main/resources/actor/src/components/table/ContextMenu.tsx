import type { JudoStored } from '@judo/data-api-common';
import type { ForwardedRef, MouseEvent, RefAttributes } from 'react';
import type { Filter, Operation } from '~/components-api';
import { Menu, MenuItem } from '@mui/material';
import { useState, forwardRef, useImperativeHandle } from 'react';
import { FilterType, FilterOption } from '~/components-api';

export interface ContextMenuProps<T, P extends JudoStored<T>> {
  filters: Filter[];
  data: P[];
  onFiltersUpdated: (newFilters: Filter[]) => void;
  filterOptions: FilterOption[];
}

export interface ContextMenuApi {
  handleContextMenu: (event: MouseEvent<HTMLElement>) => void;
}

export const ContextMenu = forwardRef<ContextMenuApi, ContextMenuProps<any, any>>(
  <T, P extends JudoStored<T>>(props: ContextMenuProps<T, P>, ref: ForwardedRef<ContextMenuApi>) => {
    const { filters, data, onFiltersUpdated, filterOptions } = props;
    const [selectedRow, setSelectedRow] = useState<string | undefined>();
    const [selectedRowData, setSelectedRowData] = useState<P | undefined>();
    const [selectedValue, setSelectedValue] = useState<any>();
    const [contextMenu, setContextMenu] = useState<{
      mouseX: number;
      mouseY: number;
    } | null>(null);
    const [field, setField] = useState<keyof P | undefined>();
    const filterTypeMapping: Record<keyof T, FilterType> = filterOptions.reduce((prev, current) => {
      return {
        ...prev,
        [current.attributeName]: current.filterType,
      };
    }, {} as Record<keyof T, FilterType>);

    useImperativeHandle(ref, () => ({
      handleContextMenu,
    }));

    const handleContextMenu = (event: MouseEvent<HTMLElement>): void => {
      event.preventDefault();
      const row = event.currentTarget.parentElement!.getAttribute('data-id')!;
      const rowData = data.find((d) => d.__identifier === row);
      const field: keyof P = event.currentTarget.dataset.field! as keyof P;
      const value: any = rowData ? rowData[field] : undefined;
      setSelectedRow(row);
      setSelectedRowData(rowData);
      setField(field);
      setSelectedValue(value);
      setContextMenu(contextMenu === null ? { mouseX: event.clientX - 2, mouseY: event.clientY - 4 } : null);
    };
    const closeContextMenu = () => {
      setContextMenu(null);
    };

    const filterByCell = () => {
      if (field && selectedValue !== null && selectedValue !== undefined) {
        const filterOption = filterOptions.find(f => f.attributeName === field)!;
        const filterType: FilterType = filterOption.filterType;
        const sourceFilters = filters.filter((f) => f.filterOption.attributeName !== field);
        const newFilters: Filter[] = [
          ...sourceFilters,
          {
            valueId: `${field as string}-value-${sourceFilters.length + 1}`,
            operationId: `${field as string}-operation-${sourceFilters.length + 1}`,
            id: field as string,
            filterOption: {
              id: `${field as string}-option-${sourceFilters.length + 1}`,
              attributeName: field as string,
              filterType: filterType,
              ...(filterType === FilterType.enumeration ? {
                enumValues: filterOption.enumValues,
              } : {}),
            },
            filterBy: {
              value: selectedValue,
              operator: (filterType === FilterType.boolean || filterType === FilterType.enumeration
                ? 'equals'
                : 'equal') as Operation,
            },
          },
        ];

        onFiltersUpdated(newFilters);
      }
      closeContextMenu();
    };

    const excludeByCell = () => {
      if (field && selectedValue !== null && selectedValue !== undefined) {
        const filterOption = filterOptions.find(f => f.attributeName === field)!;
        const filterType: FilterType = filterOption.filterType;
        const sourceFilters = filters.filter((f) => f.filterOption.attributeName !== field);
        const newFilters: Filter[] = [
          ...sourceFilters,
          {
            valueId: `${field as string}-value-${sourceFilters.length + 1}`,
            operationId: `${field as string}-operation-${sourceFilters.length + 1}`,
            id: field as string,
            filterOption: {
              id: `${field as string}-option-${sourceFilters.length + 1}`,
              attributeName: field as string,
              filterType: filterType,
              ...(filterType === FilterType.enumeration ? {
                enumValues: filterOption.enumValues,
              } : {}),
            },
            filterBy: {
              value: filterType === FilterType.boolean ? !selectedValue : selectedValue, // invert bool value because we only have "equals" operator
              operator: (filterType === FilterType.boolean
                ? 'equals'
                : filterType === FilterType.enumeration
                  ? 'notEquals'
                  : 'notEqual') as Operation,
            },
          },
        ];

        onFiltersUpdated(newFilters);
      }
      closeContextMenu();
    };

    return (
      <Menu
        open={contextMenu !== null}
        onClose={closeContextMenu}
        anchorReference="anchorPosition"
        anchorPosition={contextMenu !== null ? { top: contextMenu.mouseY, left: contextMenu.mouseX } : undefined}
        slotProps={{
          root: {
            onContextMenu: (e) => {
              e.preventDefault();
              closeContextMenu();
            },
          },
        }}
      >
        <MenuItem onClick={filterByCell}>Filter By</MenuItem>
        <MenuItem onClick={excludeByCell}>Exclude By</MenuItem>
      </Menu>
    );
  },
);
