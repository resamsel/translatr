import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { UserAccessTokenComponent } from './user-access-token.component';
import { AccessTokenEditFormTestingModule } from '../../../shared/access-token-edit-form/testing';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { RouterTestingModule } from '@angular/router/testing';
import { UserFacade } from '../+state/user.facade';
import { mockObservable } from '@translatr/utils/testing';

describe('UserAccessTokenComponent', () => {
  let component: UserAccessTokenComponent;
  let fixture: ComponentFixture<UserAccessTokenComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [UserAccessTokenComponent],
      imports: [
        AccessTokenEditFormTestingModule,

        RouterTestingModule,

        MatCardModule,
        MatButtonModule
      ],
      providers: [
        {
          provide: UserFacade,
          useFactory: () => ({
            accessToken$: mockObservable()
          })
        }
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UserAccessTokenComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
