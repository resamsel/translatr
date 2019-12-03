import { ActivatedRouteSnapshot, Router } from '@angular/router';

export const findParam = (next: ActivatedRouteSnapshot, param: string): string | undefined => {
  let current = next;
  while (!!current && !!current.params) {
    if (current.params[param]) {
      return current.params[param];
    }

    current = current.parent;
  }

  return undefined;
};

export const navigate = <T>(router: Router, criteria: Partial<T>): Promise<boolean> =>
  router.navigate([], {
    queryParamsHandling: 'merge',
    replaceUrl: true,
    queryParams: Object.keys(criteria).reduce((acc, curr) => ({ ...acc, [curr]: !!criteria[curr] ? criteria[curr] : null }), {})
  });

