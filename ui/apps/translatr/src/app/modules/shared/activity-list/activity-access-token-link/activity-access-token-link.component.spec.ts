import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslocoTestingModule } from '@ngneat/transloco';

import { ActivityAccessTokenLinkComponent } from './activity-access-token-link.component';

describe('ActivityAccessTokenLinkComponent', () => {
  let component: ActivityAccessTokenLinkComponent;
  let fixture: ComponentFixture<ActivityAccessTokenLinkComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ActivityAccessTokenLinkComponent],
      imports: [RouterTestingModule, TranslocoTestingModule]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ActivityAccessTokenLinkComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
