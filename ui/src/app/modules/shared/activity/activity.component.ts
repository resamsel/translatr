import {ChangeDetectionStrategy, Component, Input, OnInit} from '@angular/core';
import {Aggregate} from "../../../shared/aggregate";
import {PagedList} from "../../../shared/paged-list";

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

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-activity',
  templateUrl: './activity.component.html',
  styleUrls: ['./activity.component.scss']
})
export class ActivityComponent implements OnInit {

  @Input() activity: PagedList<Aggregate>;

  data: [
    {
      title: "Some Data",
      color: "light-blue",
      values: [25, 40, 30, 35, 8, 52, 17, -4]
    },
    {
      title: "Another Set",
      color: "violet",
      values: [25, 50, -10, 15, 18, 32, 27, 14]
    }
    ];

  constructor() {
  }

  ngOnInit() {
  }
}
