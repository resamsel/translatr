import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatTabsModule } from '@angular/material/tabs';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslocoTestingModule } from '@jsverse/transloco';
import { FeatureFlagsPageComponent } from './feature-flags-page.component';

describe('FeatureFlagsPageComponent', () => {
  let component: FeatureFlagsPageComponent;
  let fixture: ComponentFixture<FeatureFlagsPageComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        declarations: [FeatureFlagsPageComponent],
        imports: [
          NoopAnimationsModule,
          MatTabsModule,
          RouterTestingModule,
          TranslocoTestingModule.forRoot({
            langs: { en: { 'featureFlags.tab.user': 'User', 'featureFlags.tab.global': 'Global' } },
            translocoConfig: { availableLangs: ['en'], defaultLang: 'en' }
          })
        ],
        schemas: [NO_ERRORS_SCHEMA]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(FeatureFlagsPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('renders a User and a Global tab link in the header', () => {
    const labels = Array.from(
      fixture.nativeElement.querySelectorAll(
        'header nav[mat-tab-nav-bar] a.mat-mdc-tab-link .mdc-tab__text-label > span'
      )
    ) as HTMLElement[];
    expect(labels.map(el => el.textContent?.trim())).toEqual(['User', 'Global']);
  });
});
