import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSidenavModule } from '@angular/material/sidenav';

import { SidenavComponent } from './sidenav.component';
import { FeatureFlagTestingModule, FooterTestingModule, NavbarTestingModule } from '@translatr/components/testing';
import { TranslocoTestingModule } from '@ngneat/transloco';
import { MatTooltipModule } from '@angular/material';

describe('SidenavComponent', () => {
  let component: SidenavComponent;
  let fixture: ComponentFixture<SidenavComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [SidenavComponent],
      imports: [
        NavbarTestingModule,
        FooterTestingModule,
        FeatureFlagTestingModule,

        NoopAnimationsModule,
        TranslocoTestingModule,

        MatButtonModule,
        MatIconModule,
        MatSidenavModule,
        MatTooltipModule
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SidenavComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should compile', () => {
    expect(component).toBeTruthy();
  });
});
