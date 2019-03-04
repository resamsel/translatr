import { ChangeDetectionStrategy, Component, Input, OnInit } from '@angular/core';
import { Aggregate } from "../../../shared/aggregate";
import { PagedList } from "../../../shared/paged-list";

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-activity',
  templateUrl: './activity.component.html',
  styleUrls: ['./activity.component.scss']
})
export class ActivityComponent implements OnInit {

  @Input() activity: PagedList<Aggregate>;

  constructor() { }

  ngOnInit() {
  }

}
