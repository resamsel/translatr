import { AfterViewInit, Component } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { HotkeysHelpComponent, HotkeysService } from '@ngneat/hotkeys';
import { TranslocoService } from '@ngneat/transloco';
import { filter } from 'rxjs/operators';
import { AppFacade } from './+state/app.facade';

@Component({
  selector: 'app-root',
  template: '<router-outlet></router-outlet>'
})
export class AppComponent implements AfterViewInit {
  constructor(
    readonly facade: AppFacade,
    readonly translocoService: TranslocoService,
    private readonly hotkeysService: HotkeysService,
    private readonly dialog: MatDialog
  ) {
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
