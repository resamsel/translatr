import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterTestingModule } from '@angular/router/testing';
import { EllipsisModule } from '@dev/translatr-components';
import {
  ButtonTestingModule,
  EntityTableTestingModule,
  FeatureFlagTestingModule
} from '@translatr/components/testing';
import { mockObservable } from '@translatr/utils/testing';
import { MomentModule } from 'ngx-moment';
import { AppFacade } from '../../../../+state/app.facade';

import { DashboardUsersComponent } from './dashboard-users.component';

describe('UsersComponent', () => {
  let component: DashboardUsersComponent;
  let fixture: ComponentFixture<DashboardUsersComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [DashboardUsersComponent],
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
            userDeleted$: mockObservable(),
            usersDeleted$: mockObservable(),
            unloadUsers$: mockObservable()
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
    fixture = TestBed.createComponent(DashboardUsersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
