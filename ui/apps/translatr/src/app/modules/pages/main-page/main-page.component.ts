import { Component } from '@angular/core';
import { Aggregate, Feature, PagedList, User, UserRole } from '@dev/translatr-model';
import { ActivityService, StatisticService } from '@dev/translatr-sdk';
import { pluck, startWith } from 'rxjs/operators';
import { AppFacade } from '../../../+state/app.facade';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-main-page',
  templateUrl: './main-page.component.html',
  styleUrls: ['./main-page.component.scss']
})
export class MainPageComponent {
  me$ = this.facade.me$;
  statistics$ = this.statisticsService
    .find()
    .pipe(startWith({ projectCount: 0, userCount: 0, activityCount: 0 }));

  readonly adminUrl = environment.adminUrl;
  readonly endpointUrl = environment.endpointUrl;
  readonly aggregatedActivity$ = this.activityService
    .aggregated({})
    .pipe(startWith({ list: [] }), pluck<PagedList<Aggregate>, Aggregate[]>('list'));

  readonly Feature = Feature;

  constructor(
    private readonly facade: AppFacade,
    private readonly activityService: ActivityService,
    private readonly statisticsService: StatisticService
  ) {}

  isAdmin(me: User | undefined): boolean {
    return !!me && me.role === UserRole.Admin;
  }
}
