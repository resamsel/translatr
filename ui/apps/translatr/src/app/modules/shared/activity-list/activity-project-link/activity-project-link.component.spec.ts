import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslocoTestingModule } from '@ngneat/transloco';

import { ActivityProjectLinkComponent } from './activity-project-link.component';

describe('ActivityProjectLinkComponent', () => {
  let component: ActivityProjectLinkComponent;
  let fixture: ComponentFixture<ActivityProjectLinkComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ActivityProjectLinkComponent],
      imports: [RouterTestingModule, TranslocoTestingModule]
    }).compileComponents();
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
