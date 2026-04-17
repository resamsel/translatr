import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslocoTestingModule } from '@jsverse/transloco';

import { ActivityLocaleLinkComponent } from './activity-locale-link.component';

describe('ActivityLocaleLinkComponent', () => {
  let component: ActivityLocaleLinkComponent;
  let fixture: ComponentFixture<ActivityLocaleLinkComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        declarations: [ActivityLocaleLinkComponent],
        imports: [RouterTestingModule, TranslocoTestingModule.forRoot({ langs: {}, translocoConfig: { availableLangs: ['en'] } })]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(ActivityLocaleLinkComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
