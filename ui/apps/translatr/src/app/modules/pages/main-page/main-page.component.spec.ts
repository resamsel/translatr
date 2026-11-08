import { LayoutModule } from '@angular/cdk/layout';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { ShortNumberPipe } from '@dev/translatr-components';
import { SvgIconComponent, provideSvgIcons } from '@ngneat/svg-icon';

import { MainPageComponent } from './main-page.component';
import {
  ActivityGraphComponent,
  AuthBarLanguageSwitcherComponent,
  FeatureFlagDirective, FeatureFlagClassDirective,
  FooterComponent,
  MetricComponent,
  NavbarComponent
} from '@dev/translatr-components';
import {
  MockActivityGraphComponent,
  MockFeatureFlagDirective, MockFeatureFlagClassDirective,
  MockFooterComponent,
  MockMetricComponent,
  MockNavbarComponent, MockAuthBarLanguageSwitcherComponent
} from '@translatr/components/testing';
import { MatDividerModule } from '@angular/material/divider';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterTestingModule } from '@angular/router/testing';
import { AppFacade } from '../../../+state/app.facade';
import { mockObservable } from '@translatr/utils/testing';
import { ActivityService, StatisticService } from '@dev/translatr-sdk';
import { TranslocoTestingModule } from '@jsverse/transloco';

describe('MainPageComponent', () => {
  let component: MainPageComponent;
  let fixture: ComponentFixture<MainPageComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.overrideComponent(MainPageComponent, {
        remove: {
          imports: [
            NavbarComponent,
            AuthBarLanguageSwitcherComponent,
            FooterComponent,
            ActivityGraphComponent,
            FeatureFlagDirective, FeatureFlagClassDirective,
            MetricComponent
          ]
        },
        add: {
          imports: [
            MockNavbarComponent, MockAuthBarLanguageSwitcherComponent,
            MockFooterComponent,
            MockActivityGraphComponent,
            MockFeatureFlagDirective, MockFeatureFlagClassDirective,
            MockMetricComponent
          ]
        }
      }).configureTestingModule({
        imports: [
          MainPageComponent,
          ShortNumberPipe,

          NoopAnimationsModule,
          RouterTestingModule,
          LayoutModule,
          TranslocoTestingModule.forRoot({ langs: {}, translocoConfig: { availableLangs: ['en'] } }),
          SvgIconComponent,

          MatButtonModule,
          MatCardModule,
          MatGridListModule,
          MatIconModule,
          MatMenuModule,
          MatDividerModule,
          MatTooltipModule
        ],
        providers: [
          provideSvgIcons([]),
          {
            provide: AppFacade,
            useFactory: () => ({
              me$: mockObservable()
            })
          },
          {
            provide: ActivityService,
            useFactory: () => ({
              aggregated: () => mockObservable()
            })
          },
          {
            provide: StatisticService,
            useFactory: () => ({
              find: () => mockObservable()
            })
          }
        ]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(MainPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should compile', () => {
    expect(component).toBeTruthy();
  });
});
