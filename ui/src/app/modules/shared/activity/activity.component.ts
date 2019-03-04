import { ChangeDetectionStrategy, Component, Input, OnInit } from '@angular/core';
import { Aggregate } from "../../../shared/aggregate";
import { PagedList } from "../../../shared/paged-list";

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

  constructor() {
  }

  ngOnInit() {
  }
}
