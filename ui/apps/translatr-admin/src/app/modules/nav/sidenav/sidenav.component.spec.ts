import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatTooltipModule } from '@angular/material/tooltip';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { TranslocoTestingModule } from '@jsverse/transloco';
import {
  AuthBarItemComponent,
  AuthBarLanguageSwitcherComponent,
  FeatureFlagDirective,
  FooterComponent,
  NavbarComponent
} from '@dev/translatr-components';
import {
  MockFeatureFlagDirective,
  MockFooterComponent,
  MockNavbarComponent, MockAuthBarItemComponent, MockAuthBarLanguageSwitcherComponent
} from '@translatr/components/testing';

import { SidenavComponent } from './sidenav.component';

describe('SidenavComponent', () => {
  let component: SidenavComponent;
  let fixture: ComponentFixture<SidenavComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.overrideComponent(SidenavComponent, {
        remove: {
          imports: [
            NavbarComponent,
            AuthBarItemComponent,
            AuthBarLanguageSwitcherComponent,
            FooterComponent,
            FeatureFlagDirective
          ]
        },
        add: {
          imports: [
            MockNavbarComponent,
            MockAuthBarItemComponent,
            MockAuthBarLanguageSwitcherComponent,
            MockFooterComponent,
            MockFeatureFlagDirective
          ]
        }
      }).configureTestingModule({
        imports: [
          SidenavComponent,

          NoopAnimationsModule,
          TranslocoTestingModule.forRoot({ langs: {}, translocoConfig: { availableLangs: ['en'] } }),

          MatButtonModule,
          MatIconModule,
          MatSidenavModule,
          MatTooltipModule
        ]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(SidenavComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should compile', () => {
    expect(component).toBeTruthy();
  });
});
