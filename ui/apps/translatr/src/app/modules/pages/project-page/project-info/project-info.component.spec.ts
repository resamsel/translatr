import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ProjectInfoComponent } from './project-info.component';
import { ProjectFacade } from '../../../shared/project-state';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslocoTestingModule } from '@jsverse/transloco';
import { WINDOW } from '@translatr/utils';
import {
  EmptyViewComponent,
  EmptyViewHeaderComponent,
  EmptyViewActionsComponent,
  FeatureFlagDirective,
  MetricComponent,
  ProjectInfographicComponent
} from '@dev/translatr-components';
import {
  MockEmptyViewComponent, MockEmptyViewActionsComponent, MockEmptyViewContentComponent, MockEmptyViewHeaderComponent,
  MockFeatureFlagDirective, MockFeatureFlagClassDirective,
  MockMetricComponent,
  MockProjectInfographicComponent
} from '@translatr/components/testing';
import { NavListComponent } from '../../../shared/nav-list/nav-list.component';
import { MockNavListComponent } from '../../../shared/nav-list/testing';
import { ActivityListComponent } from '../../../shared/activity-list/activity-list.component';
import { MockActivityListComponent } from '../../../shared/activity-list/testing';
import { ShortNumberPipe } from '@dev/translatr-components';
import { mockObservable } from '@translatr/utils/testing';
import { AppFacade } from '../../../../+state/app.facade';

describe('ProjectInfoComponent', () => {
  let component: ProjectInfoComponent;
  let fixture: ComponentFixture<ProjectInfoComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.overrideComponent(ProjectInfoComponent, {
        remove: {
          imports: [
            MetricComponent,
            NavListComponent,
            ActivityListComponent,
            FeatureFlagDirective,
            EmptyViewComponent, EmptyViewHeaderComponent, EmptyViewActionsComponent,
            ProjectInfographicComponent
          ]
        },
        add: {
          imports: [
            MockMetricComponent,
            MockNavListComponent,
            MockActivityListComponent,
            MockFeatureFlagDirective, MockFeatureFlagClassDirective,
            MockEmptyViewComponent, MockEmptyViewActionsComponent, MockEmptyViewContentComponent, MockEmptyViewHeaderComponent,
            MockProjectInfographicComponent
          ]
        }
      }).configureTestingModule({
        imports: [
          ProjectInfoComponent,
          ShortNumberPipe,

          RouterTestingModule,
          TranslocoTestingModule.forRoot({ langs: {}, translocoConfig: { availableLangs: ['en'] } }),

          MatDialogModule,
          MatCardModule,
          MatListModule,
          MatIconModule,
          MatProgressBarModule,
          MatButtonModule,
          MatFormFieldModule,
          MatSelectModule,
          MatTooltipModule
        ],
        providers: [
          {
            provide: ProjectFacade,
            useFactory: () => ({
              project$: mockObservable(),
              locales$: mockObservable(),
              keys$: mockObservable(),
              messages$: mockObservable(),
              activities$: mockObservable(),
              accessTokens$: mockObservable()
            })
          },
          {
            provide: AppFacade,
            useFactory: () => ({
              me$: mockObservable()
            })
          },
          { provide: WINDOW, useValue: { location: {} } }
        ]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectInfoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
