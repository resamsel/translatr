import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ActivityMessageLinkComponent } from './activity-message-link.component';
import { RouterTestingModule } from '@angular/router/testing';

describe('ActivityMessageLinkComponent', () => {
  let component: ActivityMessageLinkComponent;
  let fixture: ComponentFixture<ActivityMessageLinkComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ActivityMessageLinkComponent],
      imports: [RouterTestingModule]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ActivityMessageLinkComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
