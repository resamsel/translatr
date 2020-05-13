import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ActivityKeyLinkComponent } from './activity-key-link.component';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslocoTestingModule } from '@ngneat/transloco';

describe('ActivityKeyLinkComponent', () => {
  let component: ActivityKeyLinkComponent;
  let fixture: ComponentFixture<ActivityKeyLinkComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ActivityKeyLinkComponent],
      imports: [RouterTestingModule, TranslocoTestingModule]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ActivityKeyLinkComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
