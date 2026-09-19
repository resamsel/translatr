import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { ActivityGraphComponent, MetricComponent, ShortNumberPipe } from '@dev/translatr-components';
import { Aggregate, Feature, PagedList, User } from '@dev/translatr-model';
import { AppFacade } from '../../../+state/app.facade';
import { map, shareReplay, startWith } from 'rxjs/operators';
import { ActivityService } from '@dev/translatr-sdk';
import { AdminPageComponent } from '../../admin-page/admin-page.component';

@Component({
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'dev-info',
  templateUrl: './info.component.html',
  styleUrls: ['./info.component.scss'],
  imports: [CommonModule, RouterModule, AdminPageComponent, ActivityGraphComponent, MetricComponent, ShortNumberPipe]
})
export class InfoComponent {
  users$ = this.facade.users$;
  projects$ = this.facade.projects$;
  accessTokens$ = this.facade.accessTokens$;
  activities$ = this.facade.activities$;

  readonly aggregatedActivity$ = this.activityService.aggregated({}).pipe(
    startWith({ list: [] }),
    map((x: PagedList<Aggregate>) => x.list),
    shareReplay(1),
  );

  readonly Feature = Feature;

  constructor(
    private readonly facade: AppFacade,
    private readonly activityService: ActivityService,
  ) {
    facade.loadUsers({ limit: 1, fetch: 'count', order: 'whenCreated desc' });
    facade.loadProjects({ limit: 1, fetch: 'count', order: 'whenCreated desc' });
    facade.loadAccessTokens({ limit: 1, fetch: 'count', order: 'whenCreated desc' });
    facade.loadActivities({ limit: 1, fetch: 'count', order: 'whenCreated desc' });
  }

  userLink(user: User | undefined | null): string[] | undefined {
    if (user === undefined || user === null) {
      return undefined;
    }

    return ['users', user.id];
  }
}
