import { Params, Router } from '@angular/router';
import { FilterFieldFilter } from './filter-field-filter';

export const handleFilterFieldSelection = (
  router: Router,
  filters: ReadonlyArray<FilterFieldFilter>,
  selected: ReadonlyArray<FilterFieldFilter>
): Promise<boolean> => {
  const params: Params = filters.map(f => f.key)
    .reduce(
      (agg, key) => {
        const selection = selected.find(s => s.key === key);
        return {
          ...agg,
          [key]: selection ? selection.value : null
        };
      },
      {}
    );

  console.log('params', params);
  return router.navigate([], {
    queryParamsHandling: 'merge',
    queryParams: params
  });
};
