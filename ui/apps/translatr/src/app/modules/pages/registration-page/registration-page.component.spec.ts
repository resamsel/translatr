import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { FeatureFlagModule, UserEditFormModule } from '@dev/translatr-components';
import { LanguageProvider } from '@dev/translatr-sdk';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslocoModule } from '@ngneat/transloco';
import { SidenavModule } from '../../nav/sidenav/sidenav.module';
import { RegistrationPageRoutingModule } from './registration-page-routing.module';

import { RegistrationPageComponent } from './registration-page.component';

describe('RegistrationPageComponent', () => {
  let component: RegistrationPageComponent;
  let fixture: ComponentFixture<RegistrationPageComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        declarations: [RegistrationPageComponent],
        imports: [
          HttpClientTestingModule,
          RouterTestingModule,
          NoopAnimationsModule,

          RegistrationPageRoutingModule,
          SidenavModule,
          TranslocoModule,
          FeatureFlagModule,
          UserEditFormModule,
          MatCardModule,
          MatButtonModule,
          FontAwesomeModule
        ],
        providers: [LanguageProvider]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(RegistrationPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
