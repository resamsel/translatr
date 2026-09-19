import { LayoutModule } from '@angular/cdk/layout';
import { CommonModule } from '@angular/common';
import { Component, ChangeDetectionStrategy } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material/tooltip';
import {
  ActivityGraphComponent,
  AuthBarLanguageSwitcherComponent,
  FeatureFlagDirective, FeatureFlagClassDirective,
  FooterComponent,
  LanguageSwicher,
  MetricComponent,
  NavbarComponent,
  ShortNumberPipe
} from '@dev/translatr-components';
import { Aggregate, Feature, PagedList, User, UserRole } from '@dev/translatr-model';
import { ActivityService, StatisticService } from '@dev/translatr-sdk';
import { SvgIconComponent } from '@ngneat/svg-icon';
import { TranslocoModule } from '@jsverse/transloco';
import { shareReplay, map, startWith } from 'rxjs/operators';
import { AppFacade } from '../../../+state/app.facade';
import { environment } from '../../../../environments/environment';

@Component({
  standalone: true,
  selector: 'app-main-page',
  templateUrl: './main-page.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./main-page.component.scss'],
  imports: [
    CommonModule,
    NavbarComponent,
    AuthBarLanguageSwitcherComponent,
    MatGridListModule,
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    MatMenuModule,
    LayoutModule,
    MatDividerModule,
    MatTooltipModule,
    FooterComponent,
    ActivityGraphComponent,
    TranslocoModule,
    FeatureFlagDirective, FeatureFlagClassDirective,
    MetricComponent,
    ShortNumberPipe,
    SvgIconComponent
  ],
  providers: [{ provide: LanguageSwicher, useClass: AppFacade }]
})
export class MainPageComponent {
  me$ = this.facade.me$;
  statistics$ = this.statisticsService
    .find()
    .pipe(startWith({ projectCount: 0, userCount: 0, activityCount: 0 }), shareReplay(1));

  readonly adminUrl = environment.adminUrl;
  readonly endpointUrl = environment.endpointUrl;
  readonly aggregatedActivity$ = this.activityService
    .aggregated({})
    .pipe(
      startWith({ list: [] }),
      map((x: PagedList<Aggregate>) => x.list),
      shareReplay(1)
    );

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
