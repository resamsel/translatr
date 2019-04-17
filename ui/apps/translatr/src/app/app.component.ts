import {Component, OnInit} from '@angular/core';
import {AppFacade} from "./+state/app.facade";

@Component({
  selector: 'app-root',
  template: '<router-outlet></router-outlet>'
})
export class AppComponent implements OnInit {
  constructor(private readonly facade: AppFacade) {
  }

  ngOnInit(): void {
    this.facade.loadMe();
  }
}
