import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
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
import { TimeAgoModule } from '@dev/translatr-components';
import { AppFacade } from '../../../+state/app.facade';

import { UsersComponent } from './users.component';

describe('UsersComponent', () => {
  let component: UsersComponent;
  let fixture: ComponentFixture<UsersComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        declarations: [UsersComponent],
        imports: [
          FeatureFlagTestingModule,
          EntityTableTestingModule,
          ButtonTestingModule,
          EllipsisModule,

          RouterTestingModule,
          TimeAgoModule,

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
              unloadUsers$: mockObservable(),
              unloadUsers: jest.fn()
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
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(UsersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
