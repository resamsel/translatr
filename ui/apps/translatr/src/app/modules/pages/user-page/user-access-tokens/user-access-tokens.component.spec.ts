import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { UserAccessTokensComponent } from './user-access-tokens.component';
import {
  ConfirmButtonComponent,
  EmptyViewComponent,
  EmptyViewHeaderComponent,
  EmptyViewContentComponent,
  EmptyViewActionsComponent
} from '@dev/translatr-components';
import { NavListComponent } from '../../../shared/nav-list/nav-list.component';
import { MockNavListComponent } from '../../../shared/nav-list/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { TranslocoTestingModule } from '@jsverse/transloco';
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
      TestBed.overrideComponent(UserAccessTokensComponent, {
        remove: {
          imports: [NavListComponent, ConfirmButtonComponent, EmptyViewComponent, EmptyViewHeaderComponent, EmptyViewContentComponent, EmptyViewActionsComponent]
        },
        add: {
          imports: [MockNavListComponent, MockConfirmButtonComponent, MockEmptyViewComponent, MockEmptyViewActionsComponent, MockEmptyViewContentComponent, MockEmptyViewHeaderComponent]
        }
      }).configureTestingModule({
        imports: [
          UserAccessTokensComponent,

          RouterTestingModule,
          TimeAgoPipe,

          MatListModule,
          MatIconModule,
          MatTooltipModule,
          TranslocoTestingModule.forRoot({ langs: {}, translocoConfig: { availableLangs: ['en'] } })
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
