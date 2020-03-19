import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MetricComponent } from './metric.component';
import { MatCardModule, MatIconModule, MatTooltipModule } from '@angular/material';
import { RouterTestingModule } from '@angular/router/testing';

describe('TileComponent', () => {
  let component: MetricComponent;
  let fixture: ComponentFixture<MetricComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [MetricComponent],
      imports: [
        RouterTestingModule,

        MatCardModule,
        MatIconModule,
        MatTooltipModule
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MetricComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
