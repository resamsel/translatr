import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ActivityMemberLinkComponent } from './activity-member-link.component';
import { RouterTestingModule } from '@angular/router/testing';

describe('ActivityMemberLinkComponent', () => {
  let component: ActivityMemberLinkComponent;
  let fixture: ComponentFixture<ActivityMemberLinkComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ActivityMemberLinkComponent],
      imports: [RouterTestingModule]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ActivityMemberLinkComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
