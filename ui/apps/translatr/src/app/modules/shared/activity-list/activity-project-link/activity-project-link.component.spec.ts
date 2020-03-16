import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ActivityProjectLinkComponent } from './activity-project-link.component';
import { RouterTestingModule } from '@angular/router/testing';

describe('ActivityProjectLinkComponent', () => {
  let component: ActivityProjectLinkComponent;
  let fixture: ComponentFixture<ActivityProjectLinkComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ActivityProjectLinkComponent],
      imports: [RouterTestingModule]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ActivityProjectLinkComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
