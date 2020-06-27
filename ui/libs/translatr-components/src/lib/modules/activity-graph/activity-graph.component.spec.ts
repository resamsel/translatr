import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ActivityGraphComponent } from '@dev/translatr-components';
import { TranslocoTestingModule } from '@ngneat/transloco';

describe('ActivityGraphComponent', () => {
  let component: ActivityGraphComponent;
  let fixture: ComponentFixture<ActivityGraphComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ActivityGraphComponent],
      imports: [TranslocoTestingModule]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ActivityGraphComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
