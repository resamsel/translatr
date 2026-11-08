import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslocoTestingModule } from '@jsverse/transloco';
import { FeatureFlagClassDirective, MetricComponent } from '@dev/translatr-components';
import { MockFeatureFlagClassDirective, MockMetricComponent } from '@translatr/components/testing';
import { of } from 'rxjs';
import { AppFacade } from '../../../+state/app.facade';
import { SidenavComponent } from '../../nav/sidenav/sidenav.component';
import { ActivityListComponent } from '../../shared/activity-list/activity-list.component';
import { ProjectCardListComponent } from '../../shared/project-card-list/project-card-list.component';
import {
  MockActivityListComponent,
  MockProjectCardListComponent,
  MockSidenavComponent
} from '../../testing';
import { ProjectsFacade } from '../projects-page/+state/projects.facade';
import { DashboardFacade } from './+state/dashboard.facade';
import { DashboardPageComponent } from './dashboard-page.component';

describe('DashboardPageComponent', () => {
  let component: DashboardPageComponent;
  let fixture: ComponentFixture<DashboardPageComponent>;

  beforeEach(() => {
    TestBed.overrideComponent(DashboardPageComponent, {
      remove: {
        imports: [
          SidenavComponent,
          MetricComponent,
          ActivityListComponent,
          ProjectCardListComponent,
          FeatureFlagClassDirective
        ]
      },
      add: {
        imports: [
          MockSidenavComponent,
          MockMetricComponent,
          MockActivityListComponent,
          MockProjectCardListComponent,
          MockFeatureFlagClassDirective
        ]
      }
    }).configureTestingModule({
      imports: [
        DashboardPageComponent,

        RouterTestingModule,
        TranslocoTestingModule.forRoot({ langs: {}, translocoConfig: { availableLangs: ['en'] } })
      ],
      providers: [
        {
          provide: AppFacade,
          useFactory: () => ({
            me$: of(undefined),
            loadUsers: jest.fn()
          })
        },
        { provide: DashboardFacade, useFactory: () => ({}) },
        { provide: ProjectsFacade, useFactory: () => ({}) },
        { provide: MatDialog, useFactory: () => ({}) }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
