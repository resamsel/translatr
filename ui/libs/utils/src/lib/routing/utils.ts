import { ActivatedRouteSnapshot } from '@angular/router';

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
