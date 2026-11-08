import { CommonModule } from '@angular/common';
import { Component, ChangeDetectionStrategy } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { ErrorPageComponent, ErrorPageHeaderComponent, ErrorPageMessageComponent } from '@dev/translatr-components';
import { TranslocoModule } from '@jsverse/transloco';
import { AppFacade } from '../../../+state/app.facade';

@Component({
  standalone: true,
  selector: 'app-not-found-page',
  templateUrl: './not-found-page.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./not-found-page.component.scss'],
  imports: [
    CommonModule,
    MatButtonModule,
    RouterModule,
    TranslocoModule,
    ErrorPageComponent,
    ErrorPageHeaderComponent,
    ErrorPageMessageComponent
  ]
})
export class NotFoundPageComponent {
  me$ = this.facade.me$;

  constructor(private readonly facade: AppFacade, readonly route: ActivatedRoute) {
    facade.loadMe();
  }
}
