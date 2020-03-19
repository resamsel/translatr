import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DashboardProjectsComponent } from './dashboard-projects.component';
import { ButtonTestingModule, EntityTableTestingModule, FeatureFlagTestingModule } from '@translatr/components/testing';
import { EllipsisModule } from '@dev/translatr-components';
import { RouterTestingModule } from '@angular/router/testing';
import { MomentModule } from 'ngx-moment';
import { MatButtonModule, MatDialog, MatIconModule, MatSnackBar, MatTableModule, MatTooltipModule } from '@angular/material';
import { AppFacade } from '../../../../+state/app.facade';
import { mockObservable } from '@translatr/utils/testing';

describe('DashboardProjectsComponent', () => {
  let component: DashboardProjectsComponent;
  let fixture: ComponentFixture<DashboardProjectsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [DashboardProjectsComponent],
      imports: [
        FeatureFlagTestingModule,
        EntityTableTestingModule,
        ButtonTestingModule,
        EllipsisModule,

        RouterTestingModule,
        MomentModule,

        MatTableModule,
        MatButtonModule,
        MatTooltipModule,
        MatIconModule
      ],
      providers: [
        {
          provide: AppFacade,
          useFactory: () => ({
            me$: mockObservable(),
            projectDeleted$: mockObservable(),
            projectsDeleted$: mockObservable()
          })
        },
        {
          provide: MatSnackBar,
          useFactory: () => ({})
        },
        {
          provide: MatDialog,
          useFactory: () => ({})
        }
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DashboardProjectsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
