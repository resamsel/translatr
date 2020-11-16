import { BreakpointObserver } from '@angular/cdk/layout';
import { Component, Input, OnChanges, OnDestroy, SimpleChanges } from '@angular/core';
import { Aggregate } from '@dev/translatr-model';
import { TranslocoService } from '@ngneat/transloco';
import * as d3 from 'd3';
import moment from 'moment';
import { Subject } from 'rxjs';
import { distinctUntilChanged, takeUntil } from 'rxjs/operators';
import { shortenNumber } from '../pipes/short-number';

const dayOfWeek = (d: Date) => (d.getDay() + 6) % 7;
const numberOfColors = 4;
const color = d3
  .scaleQuantize()
  .domain([0, 1])
  .range(d3.range(numberOfColors));

interface DirectionConfig {
  top?: number;
  right?: number;
  bottom?: number;
  left?: number;
}

const defaultDirectionConfig: DirectionConfig = {
  top: 0,
  right: 0,
  bottom: 0,
  left: 0
};

const cellPosition = (
  date: Date,
  width: number,
  cellSize: number,
  offset: DirectionConfig
): [number, number] => {
  offset = { ...defaultDirectionConfig, ...offset };
  const weekDiff = d3.timeMonday.count(date, new Date());

  return [
    width - cellSize * (weekDiff + 1) - offset.right,
    dayOfWeek(date) * cellSize + offset.top
  ];
};

const locales = {
  en: {
    dateTime: '%x, %X',
    date: '%-m/%-d/%Y',
    time: '%-I:%M:%S %p',
    periods: ['AM', 'PM'],
    days: ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'],
    shortDays: ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'],
    months: [
      'January',
      'February',
      'March',
      'April',
      'May',
      'June',
      'July',
      'August',
      'September',
      'October',
      'November',
      'December'
    ],
    shortMonths: [
      'Jan',
      'Feb',
      'Mar',
      'Apr',
      'May',
      'Jun',
      'Jul',
      'Aug',
      'Sep',
      'Oct',
      'Nov',
      'Dec'
    ]
  },
  de: {
    dateTime: '%A, der %e. %B %Y, %X',
    date: '%d.%m.%Y',
    time: '%H:%M:%S',
    periods: ['AM', 'PM'],
    days: ['Sonntag', 'Montag', 'Dienstag', 'Mittwoch', 'Donnerstag', 'Freitag', 'Samstag'],
    shortDays: ['So', 'Mo', 'Di', 'Mi', 'Do', 'Fr', 'Sa'],
    months: [
      'Januar',
      'Februar',
      'März',
      'April',
      'Mai',
      'Juni',
      'Juli',
      'August',
      'September',
      'Oktober',
      'November',
      'Dezember'
    ],
    shortMonths: [
      'Jan',
      'Feb',
      'Mrz',
      'Apr',
      'Mai',
      'Jun',
      'Jul',
      'Aug',
      'Sep',
      'Okt',
      'Nov',
      'Dez'
    ]
  }
};

const breakpoint = '(min-width: 600px)';

interface Id {
  id: string;
}

interface SvgText extends Id {
  x: number;
  y: number;
  text: string;
}

interface SvgRect extends Id {
  x: number;
  y: number;
  width: number;
  height: number;
  title: string;
  clazz?: string;
}

interface DataRect extends SvgRect {
  value: number;
}

@Component({
  selector: 'dev-activity-graph',
  templateUrl: './activity-graph.component.html',
  styleUrls: ['./activity-graph.component.scss']
})
export class ActivityGraphComponent implements OnChanges, OnDestroy {
  @Input() data: Aggregate[];
  @Input() cellInnerSize = 16;
  @Input() cellPadding = 1;
  @Input() offsetTop = 20;
  @Input() offsetRight = 16;
  @Input() offsetBottom = 20;
  @Input() offsetLeft = 50;
  @Input() weekdays = [2, 4, 6];

  readonly today: Date = d3.timeDay.ceil(new Date());
  start: Date = d3.timeDay.offset(d3.timeDay.floor(this.today), -Math.floor(365 / 2));
  weeks: number = d3.timeWeek.count(this.start, this.today);

  cellSize: number = this.cellInnerSize + this.cellPadding * 2;
  height: number = this.cellSize * 7 + this.offsetTop + this.offsetBottom;
  width: number = this.offsetLeft + (this.weeks + 2) * this.cellSize + this.offsetRight;
  viewBox: string = [0, 0, this.width, this.height].join(' ');
  offset: { top: number; left: number; bottom: number; right: number };
  filteredData: Aggregate[];
  maxValue = 1;

  private monthFormat: (date: Date) => string;
  private dateFormat: (date: Date) => string;
  private weekdayFormat: (date: Date) => string;

  private readonly destroy$: Subject<void> = new Subject<void>();

  readonly cellPosition = cellPosition;
  readonly color = color;
  readonly numberOfColors = numberOfColors;

  constructor(
    readonly translocoService: TranslocoService,
    private readonly breakpointObserver: BreakpointObserver
  ) {
    translocoService.langChanges$
      .pipe(distinctUntilChanged(), takeUntil(this.destroy$))
      .subscribe(language => this.updateLanguage(language));
    breakpointObserver
      .observe(breakpoint)
      .pipe(takeUntil(this.destroy$))
      .subscribe(({ matches }) => this.updateBounds(matches, false));
  }

  trackById(index: number, item: Id): string {
    return item.id;
  }

  private updateLanguage(language: string) {
    const timeFormat = d3.timeFormatDefaultLocale(locales[language]);
    this.dateFormat = timeFormat.format('%B %d, %Y');
    this.monthFormat = timeFormat.format('%b');
    this.weekdayFormat = timeFormat.format('%a');
  }

  private updateBounds(matchesBreakpoint: boolean, dataUpdated: boolean): void {
    const numberOfDaysBack = matchesBreakpoint ? 365 : 182;
    this.start = d3.timeDay.offset(d3.timeDay.floor(this.today), -numberOfDaysBack);
    this.weeks = d3.timeWeek.count(this.start, this.today);
    this.cellSize = this.cellInnerSize + this.cellPadding * 2;
    this.height = this.cellSize * 7 + this.offsetTop + this.offsetBottom;
    this.width = this.offsetLeft + (this.weeks + 2) * this.cellSize + this.offsetRight;
    this.viewBox = [0, 0, this.width, this.height].join(' ');
    this.offset = {
      top: this.offsetTop,
      right: this.offsetRight,
      bottom: this.offsetBottom,
      left: this.offsetLeft
    };

    if (dataUpdated) {
      this.filteredData = this.data.filter(
        (aggregate: Aggregate) => aggregate.date.getTime() > this.start.getTime()
      );
      this.maxValue = this.filteredData.reduce((max, curr) => Math.max(max, curr.value), 0);
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.updateBounds(
      this.breakpointObserver.isMatched(breakpoint),
      changes.data && changes.data.currentValue
    );
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  get monthTexts(): SvgText[] {
    return d3.timeMonths(this.start, this.today).map(month => ({
      id: month.getTime().toString(),
      x: cellPosition(month, this.width, this.cellSize, this.offset)[0],
      y: 12,
      text: this.monthFormat(month)
    }));
  }

  get weekdayTexts(): SvgText[] {
    return this.weekdays.map(weekday => ({
      id: weekday.toString(),
      x: this.offsetLeft - 8,
      y: this.offsetTop + weekday * this.cellSize - 4,
      text: this.weekdayFormat(
        moment()
          .weekday(weekday)
          .toDate()
      )
    }));
  }

  get dayRects(): SvgRect[] {
    return d3.timeDays(this.start, this.today).map(day => {
      const [x, y] = cellPosition(day, this.width, this.cellSize, this.offset);

      return {
        id: day.getTime().toString(),
        x,
        y,
        width: this.cellInnerSize,
        height: this.cellInnerSize,
        title: this.dateFormat(day)
      };
    });
  }

  get dataRects(): DataRect[] {
    return this.filteredData.map(day => {
      const [x, y] = cellPosition(day.date, this.width, this.cellSize, this.offset);

      return {
        id: day.date.getTime().toString(),
        x,
        y,
        width: this.cellInnerSize,
        height: this.cellInnerSize,
        title: this.dateFormat(day.date),
        value: day.value
      };
    });
  }

  get legendRects(): SvgRect[] {
    return Array(numberOfColors + 1)
      .fill(0)
      .map((_: number, i, arr) => {
        const x =
          this.width - (3 + arr.length) * this.cellSize + i * this.cellSize - this.offset.right;
        const y = this.height - this.cellSize;
        const clazz = i === 0 ? '' : `q${color((i - 1) / numberOfColors)}`;

        return {
          id: i.toString(),
          x,
          y,
          width: this.cellInnerSize,
          height: this.cellInnerSize,
          title: `≥ ${shortenNumber(((i - 1) * this.maxValue) / numberOfColors)}`,
          clazz
        };
      });
  }
}
