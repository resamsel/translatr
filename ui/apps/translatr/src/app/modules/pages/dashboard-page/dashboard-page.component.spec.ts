import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { RouterTestingModule } from '@angular/router/testing';
import { ShortNumberModule } from '@dev/translatr-components';
import { FeatureFlagTestingModule, MetricTestingModule } from '@translatr/components/testing';
import { of } from 'rxjs';
import { AppFacade } from '../../../+state/app.facade';
import { ActivityListTestingModule, ProjectCardListTestingModule, SidenavTestingModule } from '../../testing';
import { ProjectsFacade } from '../projects-page/+state/projects.facade';
import { DashboardFacade } from './+state/dashboard.facade';
import { DashboardPageComponent } from './dashboard-page.component';

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
