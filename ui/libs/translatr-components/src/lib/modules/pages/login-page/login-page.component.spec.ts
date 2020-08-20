import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { MatIconModule } from '@angular/material/icon';
import { NavbarTestingModule } from '@translatr/components/testing';
import { AuthClientService } from '@translatr/translatr-sdk/src/lib/services/auth-client.service';
import { ENDPOINT_URL } from '@translatr/utils';
import { mockObservable } from '@translatr/utils/testing';

import { LoginPageComponent } from './login-page.component';

describe('LoginPageComponent', () => {
  let component: LoginPageComponent;
  let fixture: ComponentFixture<LoginPageComponent>;
  let authProviderService: AuthClientService & { find: jest.Mock };

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [LoginPageComponent],
      imports: [NavbarTestingModule, MatIconModule],
      providers: [
        {
          provide: AuthClientService,
          useFactory: () => ({
            find: jest.fn()
          })
        },
        { provide: ENDPOINT_URL, useValue: '' }
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    authProviderService = TestBed.get(AuthClientService);
    authProviderService.find.mockReturnValue(mockObservable());

    fixture = TestBed.createComponent(LoginPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
