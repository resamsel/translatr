import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslocoTestingModule } from '@ngneat/transloco';

import { ActivityMessageLinkComponent } from './activity-message-link.component';

describe('ActivityMessageLinkComponent', () => {
  let component: ActivityMessageLinkComponent;
  let fixture: ComponentFixture<ActivityMessageLinkComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        declarations: [ActivityMessageLinkComponent],
        imports: [RouterTestingModule, TranslocoTestingModule]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(ActivityMessageLinkComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
