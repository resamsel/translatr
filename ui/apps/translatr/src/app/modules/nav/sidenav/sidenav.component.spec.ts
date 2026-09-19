import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterTestingModule } from '@angular/router/testing';
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

  beforeEach(() => {
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

        RouterTestingModule,
        TranslocoTestingModule.forRoot({ langs: {}, translocoConfig: { availableLangs: ['en'] } }),

        MatIconModule,
        MatMenuModule,
        MatDividerModule,
        MatTooltipModule
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(SidenavComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should compile', () => {
    expect(component).toBeTruthy();
  });
});
