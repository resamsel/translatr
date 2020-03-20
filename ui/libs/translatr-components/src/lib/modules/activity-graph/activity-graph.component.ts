import { Component, ElementRef, Input, OnInit } from '@angular/core';
import * as d3 from 'd3';
import { BaseType, Selection } from 'd3';
import { Aggregate } from '@dev/translatr-model';

const dayOfWeek = (d: Date) => (d.getDay() + 6) % 7;
const numberOfColors = 4;
const color = d3.scaleQuantize()
  .domain([0, 1])
  .range(d3.range(numberOfColors));
const dateFormat = d3.timeFormat('%B %d, %Y');
const monthFormat = d3.timeFormat('%b');

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

const cellPosition = (date: Date, width: number, cellSize: number, offset: DirectionConfig): [number, number] => {
  const offsetTop = { ...defaultDirectionConfig, ...offset }.top;
  const weekDiff = d3.timeMonday.count(date, new Date());

  return [
    width - (cellSize * (weekDiff + 1)),
    dayOfWeek(date) * cellSize + offsetTop
  ];
};

@Component({
  selector: 'dev-activity-graph',
  templateUrl: './activity-graph.component.html',
  styleUrls: ['./activity-graph.component.scss']
})
export class ActivityGraphComponent implements OnInit {
  @Input() weeks = 52;
  @Input() cellInnerSize = 16;
  @Input() cellPadding = 1;
  @Input() offsetTop = 20;
  @Input() offsetRight = 0;
  @Input() offsetBottom = 20;
  @Input() offsetLeft = 50;
  @Input() weekdays = [['Tue', 2], ['Thu', 4], ['Sat', 6]];
  private hostElement: any;
  private svg: Selection<BaseType, unknown, HTMLElement, any>;
  private g: Selection<SVGGElement, unknown, HTMLElement, undefined>;

  constructor(readonly elRef: ElementRef) {
    this.hostElement = elRef.nativeElement;
  }

  private _data: Aggregate[];

  get data(): Aggregate[] {
    return this._data;
  }

  @Input() set data(data: Aggregate[]) {
    this._data = data;

    if (data) {
      this.drawGraph();
    }
  }

  ngOnInit() {
  }

  private drawGraph() {
    d3.select(this.hostElement).select('svg').remove();

    const cellSize = this.cellInnerSize + this.cellPadding * 2;
    const width = this.offsetLeft + (this.weeks + 1) * cellSize + this.offsetRight;
    const height = cellSize * 7 + this.offsetTop + this.offsetBottom;

    this.svg = d3.select(this.hostElement)
      .append('svg')
      .attr('preserveAspectRatio', 'xMinYMin meet')
      .attr('viewBox', [0, 0, width, height].join(' '));

    this.g = this.svg.append('g')
      .attr('transform', 'translate(0,0)');

    const today = new Date();
    today.setHours(23, 59, 59, 999);
    const aYearAgo = new Date();
    aYearAgo.setFullYear(aYearAgo.getFullYear() - 1, aYearAgo.getMonth(), aYearAgo.getDate());
    aYearAgo.setHours(0, 0, 0, 0);

    const data = this.data.filter((aggregate: Aggregate) => aggregate.date.getTime() > aYearAgo.getTime());

    d3.timeMonths(aYearAgo, today).forEach((date: Date) => {
      const [x] = cellPosition(date, width, cellSize, { top: this.offsetTop });

      this.createText(x, 12, monthFormat(date), 'month');
    });

    this.weekdays.forEach(([weekday, offset]: [string, number]) => {
      this.createText(this.offsetLeft - 8, this.offsetTop + offset * cellSize - 4, weekday, 'weekday')
        .attr('text-anchor', 'end');
    });

    d3.timeDays(aYearAgo, today).forEach((date: Date) => {
      const [x, y] = cellPosition(date, width, cellSize, { top: this.offsetTop });

      this.createRect(x, y, this.cellInnerSize, this.cellInnerSize, 'day')
        .append('title')
        .text(dateFormat(date));
    });

    const maxValue = data.reduce((max, curr) => Math.max(max, curr.value), 0);

    data.forEach((aggregate) => {
      const [x, y] = cellPosition(aggregate.date, width, cellSize, { top: this.offsetTop });

      this.createRect(x, y, this.cellInnerSize, this.cellInnerSize, `day q${color(aggregate.value / maxValue)}`)
        .append('title')
        .text(`${dateFormat(aggregate.date)}: ${aggregate.value}`);
    });

    Array(numberOfColors + 1).fill(0).forEach((_: number, i, arr) => {
      const x = width - (3 + arr.length) * cellSize + i * cellSize;
      const y = height - cellSize;
      const clazz = i === 0 ? '' : `q${color((i - 1) / numberOfColors)}`;

      this.createRect(x, y, this.cellInnerSize, this.cellInnerSize, `day ${clazz}`);
    });

    this.createText(
      width - (3 + numberOfColors + 1) * cellSize - 8,
      height - 4,
      'Less',
      'weekday'
    )
      .attr('text-anchor', 'end');
    this.createText(
      width - 3 * cellSize + 4,
      height - 4,
      'More',
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
    return this.g.append('rect')
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
    return this.g.append('text')
      .attr('class', clazz)
      .attr('x', `${x}`)
      .attr('y', `${y}`)
      .text(text);
  }
}
