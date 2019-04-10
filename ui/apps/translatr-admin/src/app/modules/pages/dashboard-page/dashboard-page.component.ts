import {Component, OnInit} from '@angular/core';
import {AppFacade} from "../../../+state/app.facade";

@Component({
  selector: 'dev-dashboard-page',
  templateUrl: './dashboard-page.component.html',
  styleUrls: ['./dashboard-page.component.css']
})
export class DashboardPageComponent implements OnInit {
  me$ = this.facade.me$;

  constructor(private readonly facade: AppFacade) {
  }

  ngOnInit() {
  }

}
