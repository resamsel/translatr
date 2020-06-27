import { Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AppFacade } from '../../../+state/app.facade';

@Component({
  selector: 'app-not-found-page',
  templateUrl: './not-found-page.component.html',
  styleUrls: ['./not-found-page.component.scss']
})
export class NotFoundPageComponent {
  me$ = this.facade.me$;

  constructor(private readonly facade: AppFacade, readonly route: ActivatedRoute) {
    facade.loadMe();
  }
}
