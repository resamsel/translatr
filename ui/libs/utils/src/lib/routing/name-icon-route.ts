import { Route } from '@angular/router';

export interface NameIconRoute extends Route {
  data: {
    icon: string;
    name: string;
  };
}
