import { Component, ElementRef, Input, OnChanges, OnDestroy, SimpleChanges } from '@angular/core';
import { Aggregate } from '@dev/translatr-model';
import { TranslocoService } from '@ngneat/transloco';
import * as d3 from 'd3';
import { BaseType, Selection } from 'd3';
import moment from 'moment';
import { Subject } from 'rxjs';
import { distinctUntilChanged, takeUntil } from 'rxjs/operators';

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

  private hostElement: any;
  private svg: Selection<BaseType, unknown, HTMLElement, any>;
  private g: Selection<SVGGElement, unknown, HTMLElement, undefined>;

  private monthFormat: (date: Date) => string;
  private dateFormat: (date: Date) => string;
  private weekdayFormat: (date: Date) => string;

  private readonly destroy$: Subject<void> = new Subject<void>();

  constructor(readonly elRef: ElementRef, private readonly translocoService: TranslocoService) {
    this.hostElement = elRef.nativeElement;
    translocoService.langChanges$
      .pipe(distinctUntilChanged(), takeUntil(this.destroy$))
      .subscribe((language) => this.updateLanguage(language));
  }

  private updateLanguage(language: string) {
    const timeFormat = d3.timeFormatDefaultLocale(locales[language]);
    this.dateFormat = timeFormat.format('%B %d, %Y');
    this.monthFormat = timeFormat.format('%b');
    this.weekdayFormat = timeFormat.format('%a');
    this.drawGraph();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.data && changes.data.currentValue) {
      this.drawGraph();
    }
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private drawGraph() {
    if (this.data === undefined || this.data === null) {
      return;
    }

    d3.select(this.hostElement)
      .select('svg')
      .remove();

    const today = d3.timeDay.ceil(new Date());
    const aYearAgo = d3.timeYear.offset(d3.timeDay.floor(today), -1);
    const weeks = d3.timeWeek.count(aYearAgo, today);
    const data = this.data.filter(
      (aggregate: Aggregate) => aggregate.date.getTime() > aYearAgo.getTime()
    );

    const cellSize = this.cellInnerSize + this.cellPadding * 2;
    const width = this.offsetLeft + (weeks + 2) * cellSize + this.offsetRight;
    const height = cellSize * 7 + this.offsetTop + this.offsetBottom;
    const offset = {
      top: this.offsetTop,
      right: this.offsetRight,
      bottom: this.offsetBottom,
      left: this.offsetLeft
    };

    this.svg = d3
      .select(this.hostElement)
      .append('svg')
      .attr('preserveAspectRatio', 'xMinYMin meet')
      .attr('viewBox', [0, 0, width, height].join(' '));

    this.g = this.svg.append('g').attr('transform', 'translate(0,0)');

    d3.timeMonths(aYearAgo, today).forEach((date: Date) => {
      const [x] = cellPosition(date, width, cellSize, offset);

      this.createText(x, 12, this.monthFormat(date), 'month');
    });

    this.weekdays.forEach((weekday: number) => {
      this.createText(
        this.offsetLeft - 8,
        this.offsetTop + weekday * cellSize - 4,
        this.weekdayFormat(
          moment()
            .weekday(weekday)
            .toDate()
        ),
        'weekday'
      ).attr('text-anchor', 'end');
    });

    d3.timeDays(aYearAgo, today).forEach((date: Date) => {
      const [x, y] = cellPosition(date, width, cellSize, offset);

      this.createRect(x, y, this.cellInnerSize, this.cellInnerSize, 'day')
        .append('title')
        .text(this.dateFormat(date));
    });

    const maxValue = data.reduce((max, curr) => Math.max(max, curr.value), 0);

    data.forEach((aggregate) => {
      const [x, y] = cellPosition(aggregate.date, width, cellSize, offset);

      this.createRect(
        x,
        y,
        this.cellInnerSize,
        this.cellInnerSize,
        `day q${color(aggregate.value / maxValue)}`
      )
        .append('title')
        .text(`${this.dateFormat(aggregate.date)}: ${aggregate.value}`);
    });

    Array(numberOfColors + 1)
      .fill(0)
      .forEach((_: number, i, arr) => {
        const x = width - (3 + arr.length) * cellSize + i * cellSize - offset.right;
        const y = height - cellSize;
        const clazz = i === 0 ? '' : `q${color((i - 1) / numberOfColors)}`;

        this.createRect(x, y, this.cellInnerSize, this.cellInnerSize, `day ${clazz}`);
      });

    this.createText(
      width - (3 + numberOfColors + 1) * cellSize - 8 - offset.right,
      height - 4,
      this.translocoService.translate('contributions.legend.less'),
      'weekday'
    ).attr('text-anchor', 'end');
    this.createText(
      width - 3 * cellSize + 4 - offset.right,
      height - 4,
      this.translocoService.translate('contributions.legend.more'),
      'weekday'
    );
  }

  private createRect(
    x: number,
    y: number,
    width: number,
    height: number,
    clazz: string
  ): Selection<BaseType, unknown, HTMLElement, undefined> {
    return this.g
      .append('rect')
      .attr('class', clazz)
      .attr('width', `${width}`)
      .attr('height', `${height}`)
      .attr('x', `${x}`)
      .attr('y', `${y}`);
  }

  private createText(
    x: number,
    y: number,
    text: string,
    clazz: string
  ): Selection<BaseType, unknown, HTMLElement, undefined> {
    return this.g
      .append('text')
      .attr('class', clazz)
      .attr('x', `${x}`)
      .attr('y', `${y}`)
      .text(text);
  }
}
