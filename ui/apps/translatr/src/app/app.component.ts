import { AfterViewInit, Component, ChangeDetectionStrategy } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ThemeService } from '@dev/translatr-components';
import { HotkeysHelpComponent, HotkeysService } from '@ngneat/hotkeys';
import { TranslocoService } from '@jsverse/transloco';
import { filter } from 'rxjs/operators';
import { AppFacade } from './+state/app.facade';

@Component({
  standalone: false,
  selector: 'app-root',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: '<router-outlet></router-outlet>'
})
export class AppComponent implements AfterViewInit {
  constructor(
    readonly facade: AppFacade,
    readonly translocoService: TranslocoService,
    private readonly hotkeysService: HotkeysService,
    private readonly dialog: MatDialog,
    themeService: ThemeService
  ) {
    // Injecting ThemeService here (providedIn: 'root') forces it to instantiate as early
    // as possible, applying the stored/system theme before any component needs it.
    void themeService;
    facade.loadMe();
    facade.me$
      .pipe(filter(x => !!x && !!x.preferredLanguage))
      .subscribe(me => translocoService.setActiveLang(me.preferredLanguage));
  }

  ngAfterViewInit(): void {
    this.hotkeysService.registerHelpModal(() => {
      this.dialog.open(HotkeysHelpComponent, { width: '500px' });
    });
  }
}
