import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatTooltipModule } from '@angular/material/tooltip';

import { ActivityGraphComponent, ShortNumberPipe } from '@dev/translatr-components';
import { TranslocoTestingModule } from '@jsverse/transloco';

describe('ActivityGraphComponent', () => {
  let component: ActivityGraphComponent;
  let fixture: ComponentFixture<ActivityGraphComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        imports: [
          ActivityGraphComponent,
          TranslocoTestingModule.forRoot({
            langs: {
              en: {
                'contributions.tooltip.none': 'No activity on {{date}}',
                'contributions.tooltip.one': '1 activity on {{date}}',
                'contributions.tooltip.some': '{{amount}} activities on {{date}}'
              }
            },
            translocoConfig: { availableLangs: ['en'], defaultLang: 'en' },
            preloadLangs: true
          }),
          ShortNumberPipe,
          MatTooltipModule
        ]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(ActivityGraphComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('labels Mon, Wed and Fri inside their own rows', () => {
    const texts = component.weekdayTexts;
    expect(texts.map(t => t.text)).toEqual(['Mon', 'Wed', 'Fri']);
    texts.forEach((t, i) => {
      const row = [0, 2, 4][i];
      const rowTop = component.offsetTop + row * component.cellSize;
      expect(t.y).toBeGreaterThan(rowTop);
      expect(t.y).toBeLessThanOrEqual(rowTop + component.cellSize);
    });
  });

  it('places month labels fully inside the svg', () => {
    component.monthTexts.forEach(m => {
      expect(m.y).toBeGreaterThanOrEqual(12);
      expect(m.y).toBeLessThan(component.offsetTop);
    });
  });

  it('describes an empty day in the tooltip', () => {
    const day = component.dayRects[component.dayRects.length - 1];
    expect(day.title).toMatch(/^No activity on \w{3}, \w{3} \d{1,2}, \d{4}$/);
  });

  it('describes activity counts in the tooltip', () => {
    const date = new Date(2026, 9, 6);
    jest.useFakeTimers().setSystemTime(new Date(2026, 9, 20));
    fixture = TestBed.createComponent(ActivityGraphComponent);
    component = fixture.componentInstance;
    component.data = [
      { date, value: 12 },
      { date: new Date(2026, 9, 7), value: 1 }
    ];
    component.ngOnChanges({ data: { currentValue: component.data } as never });
    jest.useRealTimers();
    expect(component.dataRects.map(r => r.title)).toEqual([
      '12 activities on Tue, Oct 6, 2026',
      '1 activity on Wed, Oct 7, 2026'
    ]);
  });
});
