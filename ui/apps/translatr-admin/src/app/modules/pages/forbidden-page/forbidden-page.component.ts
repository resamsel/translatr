import { Component, ChangeDetectionStrategy } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { ActivatedRoute } from '@angular/router';
import { ErrorPageComponent, ErrorPageHeaderComponent, ErrorPageMessageComponent } from '@dev/translatr-components';
import { AppFacade } from '../../../+state/app.facade';
import { environment } from '../../../../environments/environment';

@Component({
  standalone: true,
  selector: 'dev-forbidden-page',
  templateUrl: './forbidden-page.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./forbidden-page.component.scss'],
  imports: [ErrorPageComponent, ErrorPageHeaderComponent, ErrorPageMessageComponent, MatButtonModule]
})
export class ForbiddenPageComponent {
  me$ = this.facade.me$;
  uiUrl = environment.uiUrl;

  constructor(private readonly facade: AppFacade, readonly route: ActivatedRoute) {
    facade.loadMe();
  }
}
