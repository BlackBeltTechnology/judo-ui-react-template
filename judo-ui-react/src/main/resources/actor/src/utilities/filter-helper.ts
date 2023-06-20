import { _NumericOperation, _StringOperation, _BooleanOperation, _EnumerationOperation } from '@judo/data-api-common';
import { isEqual, compareAsc } from 'date-fns';
import type { Filter, Operation } from '../components-api';
import { FilterType } from '../components-api';
import { dateToJudoDateString } from './helper';
import { serviceDateToUiDate } from './form-utils';

type FilterBy = {
  value: any;
  operator: Operation;
};

export const mapFiltersToQueryCustomizerProperty = (filters: Filter[], property: string): FilterBy[] | undefined => {
  if (!filters.some((filter) => filter.filterOption.attributeName === property)) return undefined;

  const convertFilterValue = (filter: Filter): any => {
    if (filter.filterOption.filterType === FilterType.dateTime && filter.filterBy.value instanceof Date) {
      return filter.filterBy.value.toISOString();
    } else if (filter.filterOption.filterType === FilterType.date && filter.filterBy.value instanceof Date) {
      return dateToJudoDateString(filter.filterBy.value);
    }
    return filter.filterBy.value;
  };

  return filters
    .filter((filter) => filter.filterOption.attributeName === property && filter.filterBy.value)
    .map((filter) => {
      return {
        value: convertFilterValue(filter),
        operator: filter.filterBy.operator,
      };
    });
};

export interface MultiFilter {
  [key: string]: FilterBy[] | undefined;
}

export const mapAllFiltersToQueryCustomizerProperties = (filters: Filter[], ...properties: string[]): MultiFilter => {
  let output: MultiFilter = {};

  for (const property of properties) {
    output[property] = mapFiltersToQueryCustomizerProperty(filters, property);
  }

  return output;
};

export function applyInMemoryFilters<T>(filters: Filter[], data: T[]): T[] {
  let newData: T[] = [...data];

  for (const filter of filters) {
    switch (filter.filterOption.filterType) {
      case FilterType.string:
        newData = filterByStringOperation(filter, data);
        break;
      case FilterType.numeric:
        newData = filterByNumericOperation(filter, data);
        break;
      case FilterType.date:
      case FilterType.dateTime:
        newData = filterByDateOperation(filter, data);
        break;
      case FilterType.boolean:
      case FilterType.trinaryLogic:
        newData = filterByBooleanOperation(filter, data);
        break;
      case FilterType.enumeration:
        newData = filterByEnumerationOperation(filter, data);
        break;
      default:
        console.warn(`Unsupported filter type: ${JSON.stringify(filter, null, 4)}`);
        newData = [...data];
    }
  }

  return newData;
}

export function filterByStringOperation<T>(filter: Filter, data: T[]): T[] {
  const attributeName = filter.filterOption.attributeName as keyof T;
  switch (filter.filterBy.operator) {
    case _StringOperation.equal:
      return data.filter((d) => d[attributeName] === filter.filterBy.value);
    case _StringOperation.notEqual:
      return data.filter((d) => d[attributeName] !== filter.filterBy.value);
    case _StringOperation.like:
      return data.filter((d) =>
        (d[attributeName] as string).toLowerCase().includes(filter.filterBy.value.toLowerCase()),
      );
    case _StringOperation.greaterOrEqual:
      return data.filter((d) => (d[attributeName] as string).localeCompare(filter.filterBy.value) >= 0);
    case _StringOperation.greaterThan:
      return data.filter((d) => (d[attributeName] as string).localeCompare(filter.filterBy.value) > 0);
    case _StringOperation.lessOrEqual:
      return data.filter((d) => (d[attributeName] as string).localeCompare(filter.filterBy.value) <= 0);
    case _StringOperation.lessThan:
      return data.filter((d) => (d[attributeName] as string).localeCompare(filter.filterBy.value) < 0);
    default:
      return [...data];
  }
}

export function filterByNumericOperation<T>(filter: Filter, data: T[]): T[] {
  const attributeName = filter.filterOption.attributeName as keyof T;
  switch (filter.filterBy.operator) {
    case _NumericOperation.equal:
      return data.filter((d) => (d[attributeName] as number) === filter.filterBy.value);
    case _NumericOperation.notEqual:
      return data.filter((d) => (d[attributeName] as number) !== filter.filterBy.value);
    case _NumericOperation.lessThan:
      return data.filter((d) => (d[attributeName] as number) < filter.filterBy.value);
    case _NumericOperation.lessOrEqual:
      return data.filter((d) => (d[attributeName] as number) <= filter.filterBy.value);
    case _NumericOperation.greaterThan:
      return data.filter((d) => (d[attributeName] as number) > filter.filterBy.value);
    case _NumericOperation.greaterOrEqual:
      return data.filter((d) => (d[attributeName] as number) >= filter.filterBy.value);
    default:
      return [...data];
  }
}

export function filterByDateOperation<T>(filter: Filter, data: T[]): T[] {
  const attributeName = filter.filterOption.attributeName as keyof T;
  switch (filter.filterBy.operator) {
    case _NumericOperation.equal:
      return data.filter((d) => isEqual(serviceDateToUiDate(d[attributeName]), filter.filterBy.value));
    case _NumericOperation.notEqual:
      return data.filter((d) => !isEqual(serviceDateToUiDate(d[attributeName]), filter.filterBy.value));
    case _NumericOperation.lessThan:
      return data.filter((d) => compareAsc(serviceDateToUiDate(d[attributeName]), filter.filterBy.value) < 0);
    case _NumericOperation.lessOrEqual:
      return data.filter((d) => compareAsc(serviceDateToUiDate(d[attributeName]), filter.filterBy.value) <= 0);
    case _NumericOperation.greaterThan:
      return data.filter((d) => compareAsc(serviceDateToUiDate(d[attributeName]), filter.filterBy.value) > 0);
    case _NumericOperation.greaterOrEqual:
      return data.filter((d) => compareAsc(serviceDateToUiDate(d[attributeName]), filter.filterBy.value) >= 0);
    default:
      return [...data];
  }
}

export function filterByBooleanOperation<T>(filter: Filter, data: T[]): T[] {
  const attributeName = filter.filterOption.attributeName as keyof T;
  switch (filter.filterBy.operator) {
    case _BooleanOperation.equals:
      return data.filter(d => {
        if (d[attributeName] === undefined || d[attributeName] === null) {
          return filter.filterBy.value === undefined || filter.filterBy.value === null;
        }
        return (d[attributeName] as boolean) === filter.filterBy.value
      });
    default:
      return [...data];
  }
}

export function filterByEnumerationOperation<T>(filter: Filter, data: T[]): T[] {
  const attributeName = filter.filterOption.attributeName as keyof T;
  switch (filter.filterBy.operator) {
    case _EnumerationOperation.equals:
      return data.filter((d) => d[attributeName] === filter.filterBy.value);
    case _EnumerationOperation.notEquals:
      return data.filter((d) => d[attributeName] !== filter.filterBy.value);
    default:
      return [...data];
  }
}
