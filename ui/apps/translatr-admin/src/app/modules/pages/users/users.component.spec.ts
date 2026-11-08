import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterTestingModule } from '@angular/router/testing';
import { ConfirmButtonComponent, EntityTableComponent, SelectionActionsComponent } from '@dev/translatr-components';
import {
  MockConfirmButtonComponent,
  MockEntityTableComponent, MockSelectionActionsComponent
} from '@translatr/components/testing';
import { mockObservable } from '@translatr/utils/testing';
import { AppFacade } from '../../../+state/app.facade';
import { AdminPageComponent } from '../../admin-page/admin-page.component';
import { MockAdminPageComponent } from '../../admin-page/testing';

import { UsersComponent } from './users.component';

describe('UsersComponent', () => {
  let component: UsersComponent;
  let fixture: ComponentFixture<UsersComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.overrideComponent(UsersComponent, {
        remove: {
          imports: [AdminPageComponent, EntityTableComponent, ConfirmButtonComponent, SelectionActionsComponent]
        },
        add: {
          imports: [MockAdminPageComponent, MockEntityTableComponent, MockConfirmButtonComponent, MockSelectionActionsComponent]
        }
      }).configureTestingModule({
        imports: [
          UsersComponent,

          RouterTestingModule,

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

  describe('displayed columns', () => {
    it('does not include email or when_created, so the table fits without horizontal scroll', () => {
      expect(component.displayedColumns).toEqual(['name', 'username', 'role', 'actions']);
    });
  });
});
