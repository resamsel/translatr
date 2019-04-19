import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { UserCardLinkComponent } from './user-card-link.component';

describe('UserCardLinkComponent', () => {
  let component: UserCardLinkComponent;
  let fixture: ComponentFixture<UserCardLinkComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ UserCardLinkComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UserCardLinkComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
