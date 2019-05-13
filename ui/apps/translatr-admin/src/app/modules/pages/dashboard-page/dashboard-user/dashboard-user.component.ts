import { Component, OnInit } from '@angular/core';
import { map, switchMap } from 'rxjs/operators';
import { ActivatedRoute, Params } from '@angular/router';
import { AppFacade } from '../../../../+state/app.facade';

@Component({
  selector: 'dev-dashboard-user',
  templateUrl: './dashboard-user.component.html',
  styleUrls: ['./dashboard-user.component.css']
})
export class DashboardUserComponent implements OnInit {
  userId$ = this.route.params.pipe(map((params: Params) => params.id));
  user$ = this.userId$.pipe(switchMap((id: string) => this.facade.user$(id)));

  constructor(
    private readonly route: ActivatedRoute,
    private readonly facade: AppFacade
  ) {}

  ngOnInit() {
    this.userId$.subscribe((userId: string) => this.facade.loadUser(userId));
  }
}
