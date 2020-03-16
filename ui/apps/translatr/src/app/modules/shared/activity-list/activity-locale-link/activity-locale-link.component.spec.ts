import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ActivityLocaleLinkComponent } from './activity-locale-link.component';
import { RouterTestingModule } from '@angular/router/testing';

describe('ActivityLocaleLinkComponent', () => {
  let component: ActivityLocaleLinkComponent;
  let fixture: ComponentFixture<ActivityLocaleLinkComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ActivityLocaleLinkComponent],
      imports: [RouterTestingModule]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ActivityLocaleLinkComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
