import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { UserAccessTokensComponent } from './user-access-tokens.component';
import { MockNavListComponent } from '../../../shared/nav-list/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TimeAgoPipe } from '@dev/translatr-components';
import { MockConfirmButtonComponent, MockEmptyViewComponent, MockEmptyViewActionsComponent, MockEmptyViewContentComponent, MockEmptyViewHeaderComponent } from '@translatr/components/testing';
import { UserFacade } from '../+state/user.facade';
import { mockObservable } from '@translatr/utils/testing';

describe('UserAccessTokensComponent', () => {
  let component: UserAccessTokensComponent;
  let fixture: ComponentFixture<UserAccessTokensComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        declarations: [UserAccessTokensComponent],
        imports: [
          MockNavListComponent,
          MockEmptyViewComponent, MockEmptyViewActionsComponent, MockEmptyViewContentComponent, MockEmptyViewHeaderComponent,
          MockConfirmButtonComponent,

          RouterTestingModule,
          TimeAgoPipe,

          MatListModule,
          MatIconModule,
          MatTooltipModule
        ],
        providers: [
          {
            provide: UserFacade,
            useFactory: () => ({
              criteria$: mockObservable(),
              user$: mockObservable(),
              destroy$: mockObservable()
            })
          },
          { provide: MatDialog, useValue: {} },
          { provide: MatSnackBar, useValue: {} }
        ]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(UserAccessTokensComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
