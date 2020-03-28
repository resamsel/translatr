import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AuthBarLanguageSwitcherComponent } from './auth-bar-language-switcher.component';
import { MatIconModule, MatMenuModule } from '@angular/material';
import { TranslocoTestingModule } from '@ngneat/transloco';

describe('AuthBarLanguageSwitcherComponent', () => {
  let component: AuthBarLanguageSwitcherComponent;
  let fixture: ComponentFixture<AuthBarLanguageSwitcherComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [AuthBarLanguageSwitcherComponent],
      imports: [
        TranslocoTestingModule,

        MatMenuModule,
        MatIconModule
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AuthBarLanguageSwitcherComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
