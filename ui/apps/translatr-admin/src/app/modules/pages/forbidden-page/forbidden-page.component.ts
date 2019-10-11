import { Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AppFacade } from '../../../+state/app.facade';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'dev-forbidden-page',
  templateUrl: './forbidden-page.component.html',
  styleUrls: ['./forbidden-page.component.scss']
})
export class ForbiddenPageComponent {
  me$ = this.facade.me$;
  uiUrl = environment.uiUrl;

  constructor(
    private readonly facade: AppFacade,
    readonly route: ActivatedRoute
  ) {
    facade.loadMe();
  }
}
