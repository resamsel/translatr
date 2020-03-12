import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DashboardPageComponent } from './dashboard-page.component';
import { ActivityListTestingModule, ProjectCardListTestingModule, SidenavTestingModule } from '../../testing';
import { ShortNumberModule } from '@dev/translatr-components';
import { FeatureFlagTestingModule, MetricTestingModule } from '@translatr/components/testing';
import { AppFacade } from '../../../+state/app.facade';
import { DashboardFacade } from './+state/dashboard.facade';
import { ProjectsFacade } from '../projects-page/+state/projects.facade';
import { MatDialog } from '@angular/material/dialog';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';

describe('DashboardPageComponent', () => {
  let component: DashboardPageComponent;
  let fixture: ComponentFixture<DashboardPageComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [DashboardPageComponent],
      imports: [
        FeatureFlagTestingModule,
        ShortNumberModule,
        RouterTestingModule,
        ActivityListTestingModule,
        ProjectCardListTestingModule,
        MetricTestingModule,
        SidenavTestingModule
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
