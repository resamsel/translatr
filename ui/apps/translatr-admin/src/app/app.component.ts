import { Component, ChangeDetectionStrategy } from '@angular/core';
import { ThemeService } from '@dev/translatr-components';
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
  constructor(
    readonly facade: AppFacade,
    readonly translocoService: TranslocoService,
    themeService: ThemeService
  ) {
    // Injecting ThemeService here (providedIn: 'root') forces it to instantiate as early
    // as possible, applying the stored/system theme before any component needs it.
    void themeService;
    facade.me$
      .pipe(filter(x => !!x && !!x.preferredLanguage))
      .subscribe(me => translocoService.setActiveLang(me.preferredLanguage));
  }
}
