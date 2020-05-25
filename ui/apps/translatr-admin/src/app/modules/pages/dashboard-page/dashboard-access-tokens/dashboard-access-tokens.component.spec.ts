import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DashboardAccessTokensComponent } from './dashboard-access-tokens.component';
import { AppFacade } from '../../../../+state/app.facade';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ButtonTestingModule, EntityTableTestingModule, FeatureFlagTestingModule } from '@translatr/components/testing';
import { MomentModule } from 'ngx-moment';
import { RouterTestingModule } from '@angular/router/testing';
import { EllipsisModule } from '@dev/translatr-components';
import { mockObservable } from '@translatr/utils/testing';

describe('DashboardAccessTokensComponent', () => {
  let component: DashboardAccessTokensComponent;
  let fixture: ComponentFixture<DashboardAccessTokensComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [DashboardAccessTokensComponent],
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
            accessTokenDeleted$: mockObservable(),
            accessTokensDeleted$: mockObservable()
          })
        },
        {
          provide: MatSnackBar,
          useFactory: () => ({})
        }
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DashboardAccessTokensComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
