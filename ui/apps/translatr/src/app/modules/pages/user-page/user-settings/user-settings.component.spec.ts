import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatCardModule } from '@angular/material/card';
import { MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { By } from '@angular/platform-browser';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { ThemeService } from '@dev/translatr-components';
import { FeatureFlagFacade } from '@dev/translatr-model';
import { TranslocoTestingModule } from '@jsverse/transloco';
import { mockObservable } from '@translatr/utils/testing';
import { of } from 'rxjs';
import { UserFacade } from '../+state/user.facade';

import { UserSettingsComponent } from './user-settings.component';

describe('UserSettingsComponent', () => {
  let component: UserSettingsComponent;
  let fixture: ComponentFixture<UserSettingsComponent>;

  const createComponent = async (flagEnabled = false, preference = 'system') => {
    TestBed.resetTestingModule();
    await TestBed.configureTestingModule({
      imports: [
        UserSettingsComponent,
        TranslocoTestingModule.forRoot({ langs: {}, translocoConfig: { availableLangs: ['en'] } }),

        FormsModule,
        ReactiveFormsModule,
        RouterTestingModule,
        NoopAnimationsModule,

        MatButtonToggleModule,
        MatDialogModule,
        MatCardModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule
      ],
      providers: [
        { provide: MatSnackBar, useFactory: () => ({}) },
        {
          provide: UserFacade,
          useFactory: () => ({
            user$: mockObservable(),
            error$: mockObservable()
          })
        },
        {
          provide: FeatureFlagFacade,
          useFactory: () => ({ hasFeatures$: () => of(flagEnabled) })
        },
        {
          provide: ThemeService,
          useFactory: () => ({
            preference$: of(preference),
            effectiveTheme$: of(preference === 'dark' ? 'dark' : 'light'),
            setPreference: jest.fn()
          })
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(UserSettingsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    fixture.detectChanges();
  };

  beforeEach(waitForAsync(async () => createComponent()));

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('theme control', () => {
    it('hides the theme control when the ThemeSwitcher feature flag is disabled', waitForAsync(async () => {
      await createComponent(false, 'system');

      expect(fixture.debugElement.query(By.css('.theme-control'))).toBeFalsy();
    }));

    it('shows a Light/Dark/System control reflecting the stored preference when the flag is enabled', waitForAsync(
      async () => {
        await createComponent(true, 'dark');

        const group = fixture.debugElement.query(
          By.css('.theme-control mat-button-toggle-group')
        );
        expect(group).toBeTruthy();
        const darkToggle = fixture.debugElement.query(
          By.css('.theme-control mat-button-toggle[value="dark"]')
        );
        expect(darkToggle.componentInstance.checked).toBe(true);
      }
    ));

    it('defaults to "system" when the user has no stored preference', waitForAsync(async () => {
      await createComponent(true, 'system');

      const systemToggle = fixture.debugElement.query(
        By.css('.theme-control mat-button-toggle[value="system"]')
      );
      expect(systemToggle.componentInstance.checked).toBe(true);
    }));

    it('applies the selection immediately via ThemeService when the user picks "Dark"', waitForAsync(
      async () => {
        await createComponent(true, 'light');
        const themeService = TestBed.inject(ThemeService);

        component.onThemeChange('dark' as never);

        expect(themeService.setPreference).toHaveBeenCalledWith('dark');
      }
    ));
  });
});
