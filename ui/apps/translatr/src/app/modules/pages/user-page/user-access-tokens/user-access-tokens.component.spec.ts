import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { UserAccessTokensComponent } from './user-access-tokens.component';

describe('UserAccessTokensComponent', () => {
  let component: UserAccessTokensComponent;
  let fixture: ComponentFixture<UserAccessTokensComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [UserAccessTokensComponent]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UserAccessTokensComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
