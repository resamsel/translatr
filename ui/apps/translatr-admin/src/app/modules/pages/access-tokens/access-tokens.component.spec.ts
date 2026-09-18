import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { By } from '@angular/platform-browser';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslocoTestingModule } from '@jsverse/transloco';
import { EllipsisPipe, EntityTableComponent } from '@dev/translatr-components';
import {
  ButtonTestingModule,
  FeatureFlagTestingModule,
  FilterFieldTestingModule
} from '@translatr/components/testing';
import { AccessToken } from '@dev/translatr-model';
import { mockObservable } from '@translatr/utils/testing';
import { of } from 'rxjs';
import { TimeAgoModule } from '@dev/translatr-components';
import { AppFacade } from '../../../+state/app.facade';
import { AdminPageTestingModule } from '../../admin-page/testing';

import { AccessTokensComponent } from './access-tokens.component';

describe('AccessTokensComponent', () => {
  let component: AccessTokensComponent;
  let fixture: ComponentFixture<AccessTokensComponent>;
  let facade: any;
  let dialog: { open: jest.Mock };

  beforeEach(
    waitForAsync(() => {
      facade = {
        me$: mockObservable(),
        accessTokenDeleted$: mockObservable(),
        accessTokensDeleted$: mockObservable(),
        accessTokenUpdated$: mockObservable(),
        accessTokenUpdateError$: mockObservable(),
        unloadAccessTokens$: mockObservable(),
        unloadAccessTokens: jest.fn(),
        loadAccessTokens: jest.fn(),
        updateAccessToken: jest.fn()
      };
      dialog = { open: jest.fn() };

      TestBed.configureTestingModule({
        declarations: [AccessTokensComponent, EntityTableComponent],
        imports: [
          AdminPageTestingModule,
          FeatureFlagTestingModule,
          FilterFieldTestingModule,
          ButtonTestingModule,
          EllipsisPipe,

          RouterTestingModule,
          TimeAgoModule,
          TranslocoTestingModule.forRoot({
            langs: {},
            translocoConfig: { availableLangs: ['en'] }
          }),

          MatTableModule,
          MatButtonModule,
          MatTooltipModule,
          MatIconModule,
          MatCheckboxModule,
          MatPaginatorModule
        ],
        providers: [
          { provide: AppFacade, useValue: facade },
          { provide: MatDialog, useValue: dialog },
          { provide: MatSnackBar, useValue: {} }
        ]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(AccessTokensComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('opens the edit dialog wired to updateAccessToken', () => {
    const token = { id: 1, name: 'ci', scope: 'read:key' } as any;

    component.onEdit(token);

    expect(dialog.open).toHaveBeenCalledTimes(1);
    const config = dialog.open.mock.calls[0][1];
    expect(config.data.type).toBe('update');
    expect(config.data.accessToken).toBe(token);

    config.data.onSubmit(token);
    expect(facade.updateAccessToken).toHaveBeenCalledWith(token);
  });

  it('renders the owning user in the User column (regression: admin UI showed a blank User column, gh#<ISSUE_NUMBER>)', () => {
    const token: AccessToken = {
      id: 1,
      name: 'ci',
      scope: 'read:key',
      userId: 'u-42',
      userUsername: 'jdoe',
      whenCreated: new Date().toISOString()
    } as AccessToken;

    facade.accessTokens$ = of({
      list: [token],
      hasNext: false,
      hasPrev: false,
      limit: 20,
      offset: 0,
      total: 1
    });

    fixture = TestBed.createComponent(AccessTokensComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    const userLink = fixture.debugElement.query(By.css('a[href="/users/u-42"]'));

    expect(userLink).toBeTruthy();
    expect(userLink.nativeElement.textContent.trim()).toBe('jdoe');
  });
});
