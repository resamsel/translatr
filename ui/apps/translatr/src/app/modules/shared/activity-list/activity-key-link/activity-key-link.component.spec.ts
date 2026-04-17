import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslocoTestingModule } from '@jsverse/transloco';

import { ActivityKeyLinkComponent } from './activity-key-link.component';

describe('ActivityKeyLinkComponent', () => {
  let component: ActivityKeyLinkComponent;
  let fixture: ComponentFixture<ActivityKeyLinkComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        declarations: [ActivityKeyLinkComponent],
        imports: [RouterTestingModule, TranslocoTestingModule.forRoot({ langs: {}, translocoConfig: { availableLangs: ['en'] } })]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(ActivityKeyLinkComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
