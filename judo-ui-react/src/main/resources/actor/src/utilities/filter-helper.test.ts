import { assert, describe, it } from 'vitest';
import { _NumericOperation, _StringOperation, _BooleanOperation, _EnumerationOperation } from '@judo/data-api-common';
import { filterByStringOperation, filterByNumericOperation, filterByDateOperation, filterByBooleanOperation } from '~/utilities/filter-helper';
import type { Filter, Operation, FilterOption } from '~/components-api';
import { FilterType } from '~/components-api';

interface TestType {
  name: string;
  age: number;
  isOkay?: boolean;
  registered: string;
  left: string;
}

const data: TestType[] = [
  { name: 'Jake', age: 31, isOkay: true, registered: '2023-06-19', left: '2023-06-20T13:20:00.000Z' },
  { name: 'Andrea', age: 14, isOkay: true, registered: '2023-02-19', left: '2023-02-20T13:20:00.000Z' },
  { name: 'Julia', age: 55, registered: '2023-04-19', left: '2023-04-20T13:18:00.000Z' },
  { name: 'Henry', age: 41, isOkay: false, registered: '2023-04-18', left: '2023-04-20T13:20:00.000Z' },
];

describe('filterByStringOperation', () => {
  it('equal', () => {
    const filter = createStringFilter('name', _StringOperation.equal, 'Jake');
    const result = filterByStringOperation(filter, data);

    assert.equal(1, result.length);
    assert.equal('Jake', result[0].name);
  });

  it('notEqual', () => {
    const filter = createStringFilter('name', _StringOperation.notEqual, 'Jake');
    const result = filterByStringOperation(filter, data);

    assert.equal(3, result.length);
    assert.equal('Andrea', result[0].name);
    assert.equal('Julia', result[1].name);
    assert.equal('Henry', result[2].name);
  });

  it('like', () => {
    const filter = createStringFilter('name', _StringOperation.like, 'he');
    const result = filterByStringOperation(filter, data);

    assert.equal(1, result.length);
    assert.equal('Henry', result[0].name);
  });

  it('greaterOrEqual', () => {
    const filter = createStringFilter('name', _StringOperation.greaterOrEqual, 'Jake');
    const result = filterByStringOperation(filter, data);

    assert.equal(2, result.length);
    assert.equal('Jake', result[0].name);
    assert.equal('Julia', result[1].name);
  });

  it('greaterThan', () => {
    const filter = createStringFilter('name', _StringOperation.greaterThan, 'Jake');
    const result = filterByStringOperation(filter, data);

    assert.equal(1, result.length);
    assert.equal('Julia', result[0].name);
  });

  it('lessOrEqual', () => {
    const filter = createStringFilter('name', _StringOperation.lessOrEqual, 'Jake');
    const result = filterByStringOperation(filter, data);

    assert.equal(3, result.length);
    assert.equal('Jake', result[0].name);
    assert.equal('Andrea', result[1].name);
    assert.equal('Henry', result[2].name);
  });

  it('lessThan', () => {
    const filter = createStringFilter('name', _StringOperation.lessThan, 'Jake');
    const result = filterByStringOperation(filter, data);

    assert.equal(2, result.length);
    assert.equal('Andrea', result[0].name);
    assert.equal('Henry', result[1].name);
  });
});

describe('filterByNumericOperation', () => {
  it('equal', () => {
    const filter = createNumericFilter('age', _NumericOperation.equal, 31);
    const result = filterByNumericOperation(filter, data);

    assert.equal(1, result.length);
    assert.equal('Jake', result[0].name);
  });

  it('notEqual', () => {
    const filter = createNumericFilter('age', _NumericOperation.notEqual, 31);
    const result = filterByNumericOperation(filter, data);

    assert.equal(3, result.length);
    assert.equal('Andrea', result[0].name);
    assert.equal('Julia', result[1].name);
    assert.equal('Henry', result[2].name);
  });

  it('lessThan', () => {
    const filter = createNumericFilter('age', _NumericOperation.lessThan, 31);
    const result = filterByNumericOperation(filter, data);

    assert.equal(1, result.length);
    assert.equal('Andrea', result[0].name);
  });

  it('lessOrEqual', () => {
    const filter = createNumericFilter('age', _NumericOperation.lessOrEqual, 31);
    const result = filterByNumericOperation(filter, data);

    assert.equal(2, result.length);
    assert.equal('Jake', result[0].name);
    assert.equal('Andrea', result[1].name);
  });

  it('greaterThan', () => {
    const filter = createNumericFilter('age', _NumericOperation.greaterThan, 31);
    const result = filterByNumericOperation(filter, data);

    assert.equal(2, result.length);
    assert.equal('Julia', result[0].name);
    assert.equal('Henry', result[1].name);
  });

  it('greaterOrEqual', () => {
    const filter = createNumericFilter('age', _NumericOperation.greaterOrEqual, 31);
    const result = filterByNumericOperation(filter, data);

    assert.equal(3, result.length);
    assert.equal('Jake', result[0].name);
    assert.equal('Julia', result[1].name);
    assert.equal('Henry', result[2].name);
  });
});

describe('filterByDateOperation', () => {
  it('equal', () => {
    const filter = createDateFilter('registered', _NumericOperation.equal, new Date('2023-06-19'));
    const result = filterByDateOperation(filter, data);

    assert.equal(1, result.length);
    assert.equal('Jake', result[0].name);
  });

  it('notEqual', () => {
    const filter = createDateFilter('registered', _NumericOperation.notEqual, new Date('2023-06-19'));
    const result = filterByDateOperation(filter, data);

    assert.equal(3, result.length);
    assert.equal('Andrea', result[0].name);
    assert.equal('Julia', result[1].name);
    assert.equal('Henry', result[2].name);
  });

  it('lessThan', () => {
    const filter = createDateFilter('registered', _NumericOperation.lessThan, new Date('2023-04-18'));
    const result = filterByDateOperation(filter, data);

    assert.equal(1, result.length);
    assert.equal('Andrea', result[0].name);
  });

  it('lessOrEqual', () => {
    const filter = createDateFilter('registered', _NumericOperation.lessOrEqual, new Date('2023-04-19'));
    const result = filterByDateOperation(filter, data);

    assert.equal(3, result.length);
    assert.equal('Andrea', result[0].name);
    assert.equal('Julia', result[1].name);
    assert.equal('Henry', result[2].name);
  });

  it('greaterThan', () => {
    const filter = createDateFilter('registered', _NumericOperation.greaterThan, new Date('2023-04-18'));
    const result = filterByDateOperation(filter, data);

    assert.equal(2, result.length);
    assert.equal('Jake', result[0].name);
    assert.equal('Julia', result[1].name);
  });

  it('greaterOrEqual', () => {
    const filter = createDateFilter('registered', _NumericOperation.greaterOrEqual, new Date('2023-04-18'));
    const result = filterByDateOperation(filter, data);

    assert.equal(3, result.length);
    assert.equal('Jake', result[0].name);
    assert.equal('Julia', result[1].name);
    assert.equal('Henry', result[2].name);
  });
});

describe('filterByDateTimeOperation', () => {
  it('equal', () => {
    const filter = createDateTimeFilter('left', _NumericOperation.equal, new Date('2023-06-20T13:20:00.000Z'));
    const result = filterByDateOperation(filter, data);

    assert.equal(1, result.length);
    assert.equal('Jake', result[0].name);
  });

  it('notEqual', () => {
    const filter = createDateTimeFilter('left', _NumericOperation.notEqual, new Date('2023-06-20T13:20:00.000Z'));
    const result = filterByDateOperation(filter, data);

    assert.equal(3, result.length);
    assert.equal('Andrea', result[0].name);
    assert.equal('Julia', result[1].name);
    assert.equal('Henry', result[2].name);
  });

  it('lessThan', () => {
    const filter = createDateTimeFilter('left', _NumericOperation.lessThan, new Date('2023-04-20T13:18:00.000Z'));
    const result = filterByDateOperation(filter, data);

    assert.equal(1, result.length);
    assert.equal('Andrea', result[0].name);
  });

  it('lessOrEqual', () => {
    const filter = createDateTimeFilter('left', _NumericOperation.lessOrEqual, new Date('2023-04-20T13:20:00.000Z'));
    const result = filterByDateOperation(filter, data);

    assert.equal(3, result.length);
    assert.equal('Andrea', result[0].name);
    assert.equal('Julia', result[1].name);
    assert.equal('Henry', result[2].name);
  });

  it('greaterThan', () => {
    const filter = createDateTimeFilter('left', _NumericOperation.greaterThan, new Date('2023-04-20T13:18:00.000Z'));
    const result = filterByDateOperation(filter, data);

    assert.equal(2, result.length);
    assert.equal('Jake', result[0].name);
    assert.equal('Henry', result[1].name);
  });

  it('greaterOrEqual', () => {
    const filter = createDateTimeFilter('left', _NumericOperation.greaterOrEqual, new Date('2023-04-20T13:18:00.000Z'));
    const result = filterByDateOperation(filter, data);

    assert.equal(3, result.length);
    assert.equal('Jake', result[0].name);
    assert.equal('Julia', result[1].name);
    assert.equal('Henry', result[2].name);
  });
});

describe('filterByBooleanOperation', () => {
  it('equals - true', () => {
    const filter = createBooleanFilter('isOkay', _BooleanOperation.equals, true);
    const result = filterByBooleanOperation(filter, data);

    assert.equal(2, result.length);
    assert.equal('Jake', result[0].name);
    assert.equal('Andrea', result[1].name);
  });

  it('equals - false', () => {
    const filter = createBooleanFilter('isOkay', _BooleanOperation.equals, false);
    const result = filterByBooleanOperation(filter, data);

    assert.equal(1, result.length);
    assert.equal('Henry', result[0].name);
  });
});

describe('filterByTrinaryLogicOperation', () => {
  it('equals - true', () => {
    const filter = createTrinaryLogicFilter('isOkay', _BooleanOperation.equals, true);
    const result = filterByBooleanOperation(filter, data);

    assert.equal(2, result.length);
    assert.equal('Jake', result[0].name);
    assert.equal('Andrea', result[1].name);
  });

  it('equals - false', () => {
    const filter = createTrinaryLogicFilter('isOkay', _BooleanOperation.equals, false);
    const result = filterByBooleanOperation(filter, data);

    assert.equal(1, result.length);
    assert.equal('Henry', result[0].name);
  });

  it('equals - undefined', () => {
    const filter = createTrinaryLogicFilter('isOkay', _BooleanOperation.equals, undefined);
    const result = filterByBooleanOperation(filter, data);

    assert.equal(1, result.length);
    assert.equal('Julia', result[0].name);
  });
});

function createStringFilter(attributeName: keyof TestType, operator: _StringOperation, value: string): Filter {
  return {
    id: 'filterByStringOperation',
    operationId: 'filterByStringOperation',
    valueId: attributeName,
    filterOption: {
      id: 'filterByStringOperation-' + attributeName,
      filterType: FilterType.string,
      attributeName,
    },
    filterBy: {
      operator,
      value,
    },
  };
}

function createNumericFilter(attributeName: keyof TestType, operator: _NumericOperation, value: number): Filter {
  return {
    id: 'filterByNumericOperation',
    operationId: 'filterByNumericOperation',
    valueId: attributeName,
    filterOption: {
      id: 'filterByNumericOperation-' + attributeName,
      filterType: FilterType.numeric,
      attributeName,
    },
    filterBy: {
      operator,
      value,
    },
  };
}

function createDateFilter(attributeName: keyof TestType, operator: _NumericOperation, value: Date): Filter {
  return {
    id: 'filterByNumericOperation',
    operationId: 'filterByNumericOperation',
    valueId: attributeName,
    filterOption: {
      id: 'filterByNumericOperation-' + attributeName,
      filterType: FilterType.date,
      attributeName,
    },
    filterBy: {
      operator,
      value,
    },
  };
}

function createDateTimeFilter(attributeName: keyof TestType, operator: _NumericOperation, value: Date): Filter {
  return {
    id: 'filterByNumericOperation',
    operationId: 'filterByNumericOperation',
    valueId: attributeName,
    filterOption: {
      id: 'filterByNumericOperation-' + attributeName,
      filterType: FilterType.dateTime,
      attributeName,
    },
    filterBy: {
      operator,
      value,
    },
  };
}

function createBooleanFilter(attributeName: keyof TestType, operator: _BooleanOperation, value: boolean): Filter {
  return {
    id: 'filterByBooleanOperation',
    operationId: 'filterByBooleanOperation',
    valueId: attributeName,
    filterOption: {
      id: 'filterByBooleanOperation-' + attributeName,
      filterType: FilterType.boolean,
      attributeName,
    },
    filterBy: {
      operator,
      value,
    },
  };
}

function createTrinaryLogicFilter(attributeName: keyof TestType, operator: _BooleanOperation, value?: boolean): Filter {
  return {
    id: 'filterByBooleanOperation',
    operationId: 'filterByBooleanOperation',
    valueId: attributeName,
    filterOption: {
      id: 'filterByBooleanOperation-' + attributeName,
      filterType: FilterType.trinaryLogic,
      attributeName,
    },
    filterBy: {
      operator,
      value,
    },
  };
}
