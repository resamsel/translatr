import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslocoTestingModule } from '@ngneat/transloco';

import { ActivityMemberLinkComponent } from './activity-member-link.component';

describe('ActivityMemberLinkComponent', () => {
  let component: ActivityMemberLinkComponent;
  let fixture: ComponentFixture<ActivityMemberLinkComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        declarations: [ActivityMemberLinkComponent],
        imports: [RouterTestingModule, TranslocoTestingModule]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(ActivityMemberLinkComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
