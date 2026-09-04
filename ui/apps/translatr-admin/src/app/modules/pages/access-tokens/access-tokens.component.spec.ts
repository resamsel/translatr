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
        updateAccessToken: jest.fn()
      };
      dialog = { open: jest.fn() };

      TestBed.configureTestingModule({
        declarations: [AccessTokensComponent],
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
});
