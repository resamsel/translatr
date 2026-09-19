import { CommonModule } from '@angular/common';
import { Component, ChangeDetectionStrategy } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { ErrorPageComponent, ErrorPageHeaderComponent, ErrorPageMessageComponent } from '@dev/translatr-components';
import { AppFacade } from '../../../+state/app.facade';

@Component({
  standalone: true,
  selector: 'app-forbidden-page',
  templateUrl: './forbidden-page.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./forbidden-page.component.scss'],
  imports: [CommonModule, MatButtonModule, RouterModule, ErrorPageComponent, ErrorPageHeaderComponent, ErrorPageMessageComponent]
})
export class ForbiddenPageComponent {
  me$ = this.facade.me$;

  constructor(private readonly facade: AppFacade, readonly route: ActivatedRoute) {
    facade.loadMe();
  }
}
