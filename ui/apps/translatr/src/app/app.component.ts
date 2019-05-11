import { Component } from "@angular/core";
import { AppFacade } from "./+state/app.facade";

@Component({
  selector: 'app-root',
  template: '<router-outlet></router-outlet>'
})
export class AppComponent {
  constructor(private readonly facade: AppFacade) {
    facade.loadMe();
  }
}
