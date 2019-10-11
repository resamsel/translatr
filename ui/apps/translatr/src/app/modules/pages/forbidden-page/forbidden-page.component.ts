import { Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AppFacade } from '../../../+state/app.facade';

@Component({
  selector: 'app-forbidden-page',
  templateUrl: './forbidden-page.component.html',
  styleUrls: ['./forbidden-page.component.scss']
})
export class ForbiddenPageComponent {
  me$ = this.facade.me$;

  constructor(
    private readonly facade: AppFacade,
    readonly route: ActivatedRoute
  ) {
    facade.loadMe();
  }
}
