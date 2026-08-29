import { Component, ChangeDetectionStrategy } from '@angular/core';
import { TranslocoService } from '@jsverse/transloco';
import { filter } from 'rxjs/operators';
import { AppFacade } from './+state/app.facade';

@Component({
  standalone: false,
  selector: 'dev-root',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: '<router-outlet></router-outlet>'
})
export class AppComponent {
  constructor(readonly facade: AppFacade, readonly translocoService: TranslocoService) {
    facade.me$
      .pipe(filter(x => !!x && !!x.preferredLanguage))
      .subscribe(me => translocoService.setActiveLang(me.preferredLanguage));
  }
}
