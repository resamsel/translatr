import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatTabsModule } from '@angular/material/tabs';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { TranslocoTestingModule } from '@jsverse/transloco';
import { DashboardFeatureFlagsPageComponent } from './dashboard-feature-flags-page.component';

describe('DashboardFeatureFlagsPageComponent', () => {
  let component: DashboardFeatureFlagsPageComponent;
  let fixture: ComponentFixture<DashboardFeatureFlagsPageComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        declarations: [DashboardFeatureFlagsPageComponent],
        imports: [
          NoopAnimationsModule,
          MatTabsModule,
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
    fixture = TestBed.createComponent(DashboardFeatureFlagsPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('renders a User and a Global tab', () => {
    const labels = Array.from(
      fixture.nativeElement.querySelectorAll('.mat-mdc-tab .mdc-tab__text-label')
    ).map((el: HTMLElement) => el.textContent?.trim());
    expect(labels).toEqual(['User', 'Global']);
  });
});
