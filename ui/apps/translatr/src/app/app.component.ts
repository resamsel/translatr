import { Component } from '@angular/core';
import { AppFacade } from './+state/app.facade';
import { TranslocoService } from '@ngneat/transloco';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-root',
  template: '<router-outlet></router-outlet>'
})
export class AppComponent {
  constructor(readonly facade: AppFacade, readonly translocoService: TranslocoService) {
    facade.loadMe();
    facade.me$.pipe(filter(x => !!x && !!x.preferredLanguage))
      .subscribe(me => translocoService.setActiveLang(me.preferredLanguage));
  }
}
