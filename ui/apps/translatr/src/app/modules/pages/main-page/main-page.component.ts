import { Component } from '@angular/core';
import { AppFacade } from '../../../+state/app.facade';
import { Aggregate, PagedList, User, UserRole } from '@dev/translatr-model';
import { environment } from '../../../../environments/environment';
import { ActivityService } from '@dev/translatr-sdk';
import { pluck } from 'rxjs/operators';

@Component({
  selector: 'app-main-page',
  templateUrl: './main-page.component.html',
  styleUrls: ['./main-page.component.scss']
})
export class MainPageComponent {
  me$ = this.facade.me$;

  readonly adminUrl = environment.adminUrl;
  readonly endpointUrl = environment.endpointUrl;
  readonly aggregatedActivity$ = this.activityService
    .aggregated({})
    .pipe(pluck<PagedList<Aggregate>, Aggregate[]>('list'));

  constructor(
    private readonly facade: AppFacade,
    private readonly activityService: ActivityService
  ) {
  }

  isAdmin(me: User | undefined): boolean {
    return !!me && me.role === UserRole.Admin;
  }
}
