import {ChangeDetectionStrategy, Component, Input, OnInit} from '@angular/core';
import {Aggregate, PagedList} from '@dev/translatr-model';
import {HeatmapData} from '../frappe-chart/frappe-chart.component';

interface DataPoint {
  name: string;
  date: string;
  value: number;
}

interface Data {
  date: string;
  total: number;
  details: Array<DataPoint>;
}

const minusOneYear = (date: Date): Date => {
  const d = new Date(date.getTime());
  d.setFullYear(date.getFullYear() - 1);
  return d;
};

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-activity',
  templateUrl: './activity.component.html',
  styleUrls: ['./activity.component.scss']
})
export class ActivityComponent implements OnInit {

  @Input() set activity(activity: PagedList<Aggregate> | undefined) {
    if (!activity) {
      this.data = undefined;
      return;
    }

    this.data = {
      dataPoints: activity.list.reduce(
        (data: { [key: string]: number }, value: Aggregate) => {
          data[value.millis / 1000] = value.value;
          return data;
        }, {}),
      start: minusOneYear(new Date()),
      end: new Date()
    };
  }

  data: HeatmapData;

  constructor() {
  }

  ngOnInit() {
  }
}
