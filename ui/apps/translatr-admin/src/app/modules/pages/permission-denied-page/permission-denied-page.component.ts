import { Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AppFacade } from '../../../+state/app.facade';

@Component({
  selector: 'dev-not-allowed-page',
  templateUrl: './permission-denied-page.component.html',
  styleUrls: ['./permission-denied-page.component.scss']
})
export class PermissionDeniedPageComponent {
  me$ = this.facade.me$;

  constructor(
    private readonly facade: AppFacade,
    readonly route: ActivatedRoute
  ) {
    facade.loadMe();
  }
}
