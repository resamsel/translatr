import { Component } from '@angular/core';
import { AppFacade } from './+state/app.facade';

@Component({
  selector: 'dev-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'translatr-admin';

  constructor(readonly facade: AppFacade) {
    facade.loadLoggedInUser();
  }
}
