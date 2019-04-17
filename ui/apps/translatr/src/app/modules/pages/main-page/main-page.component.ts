import {Component} from '@angular/core';
import {AppFacade} from "../../../+state/app.facade";

@Component({
  selector: 'app-main-page',
  templateUrl: './main-page.component.html',
  styleUrls: ['./main-page.component.scss']
})
export class MainPageComponent {

  me$ = this.facade.me$;

  constructor(private readonly facade: AppFacade) {
  }
}
