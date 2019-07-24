import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { UserAccessTokenComponent } from './user-access-token.component';

describe('UserAccessTokenComponent', () => {
  let component: UserAccessTokenComponent;
  let fixture: ComponentFixture<UserAccessTokenComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ UserAccessTokenComponent ]
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
