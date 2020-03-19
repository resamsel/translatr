import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DashboardFeatureFlagsComponent } from './dashboard-feature-flags.component';
import { ButtonTestingModule, EntityTableTestingModule, FeatureFlagTestingModule } from '@translatr/components/testing';
import { EllipsisModule } from '@dev/translatr-components';
import { RouterTestingModule } from '@angular/router/testing';
import { MomentModule } from 'ngx-moment';
import { MatButtonModule, MatIconModule, MatSnackBar, MatTableModule, MatTooltipModule } from '@angular/material';
import { AppFacade } from '../../../../+state/app.facade';
import { mockObservable } from '@translatr/utils/testing';

describe('DashboardFeatureFlagsComponent', () => {
  let component: DashboardFeatureFlagsComponent;
  let fixture: ComponentFixture<DashboardFeatureFlagsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [DashboardFeatureFlagsComponent],
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
            featureFlagDeleted$: mockObservable(),
            featureFlagsDeleted$: mockObservable()
          })
        },
        {
          provide: MatSnackBar,
          useFactory: () => ({})
        }
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DashboardFeatureFlagsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
