import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { ActivatedRoute, Params } from '@angular/router';
import { Feature } from '@dev/translatr-model';
import { map, switchMap } from 'rxjs/operators';
import { AppFacade } from '../../../+state/app.facade';

@Component({
  standalone: false,
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'dev-user',
  templateUrl: './user.component.html',
  styleUrls: ['./user.component.scss']
})
export class UserComponent implements OnInit {
  userId$ = this.route.params.pipe(map((params: Params) => params.id));
  user$ = this.userId$.pipe(switchMap((id: string) => this.facade.user$(id)));
  projects$ = this.facade.projects$;
  activities$ = this.facade.activities$;

  readonly Feature = Feature;

  constructor(private readonly route: ActivatedRoute, private readonly facade: AppFacade) {}

  ngOnInit() {
    this.userId$.subscribe((userId: string) => {
      this.facade.loadUser(userId);
      this.facade.loadProjects({ fetch: 'count', ownerId: userId });
      this.facade.loadActivities({ fetch: 'count', userId });
    });
  }
}
