import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { LanguageProvider } from '@dev/translatr-sdk';
import { TranslocoTestingModule } from '@jsverse/transloco';
import { RegistrationPageRoutingModule } from './registration-page-routing.module';

import { RegistrationPageComponent } from './registration-page.component';

describe('RegistrationPageComponent', () => {
  let component: RegistrationPageComponent;
  let fixture: ComponentFixture<RegistrationPageComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        imports: [
          RegistrationPageComponent,
          HttpClientTestingModule,
          RouterTestingModule,
          NoopAnimationsModule,

          RegistrationPageRoutingModule,
          TranslocoTestingModule.forRoot({ langs: {}, translocoConfig: { availableLangs: ['en'] } })
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
