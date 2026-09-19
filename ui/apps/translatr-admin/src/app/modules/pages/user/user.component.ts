import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ActivatedRoute, Params, RouterModule } from '@angular/router';
import {
  FeatureFlagClassDirective,
  MetricComponent,
  ShortNumberPipe,
  TimeAgoPipe,
  UserCardComponent
} from '@dev/translatr-components';
import { Feature } from '@dev/translatr-model';
import { map, switchMap } from 'rxjs/operators';
import { GravatarModule } from 'ngx-gravatar';
import { AppFacade } from '../../../+state/app.facade';
import { AdminPageComponent } from '../../admin-page/admin-page.component';

@Component({
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'dev-user',
  templateUrl: './user.component.html',
  styleUrls: ['./user.component.scss'],
  imports: [
    CommonModule,
    RouterModule,
    AdminPageComponent,
    MetricComponent,
    UserCardComponent,
    ShortNumberPipe,
    TimeAgoPipe,
    FeatureFlagClassDirective,
    GravatarModule,
    MatIconModule,
    MatTooltipModule
  ]
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
