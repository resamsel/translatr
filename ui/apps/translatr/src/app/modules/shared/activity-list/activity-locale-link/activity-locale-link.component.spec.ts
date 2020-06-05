import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslocoTestingModule } from '@ngneat/transloco';

import { ActivityLocaleLinkComponent } from './activity-locale-link.component';

describe('ActivityLocaleLinkComponent', () => {
  let component: ActivityLocaleLinkComponent;
  let fixture: ComponentFixture<ActivityLocaleLinkComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ActivityLocaleLinkComponent],
      imports: [RouterTestingModule, TranslocoTestingModule]
    }).compileComponents();
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
