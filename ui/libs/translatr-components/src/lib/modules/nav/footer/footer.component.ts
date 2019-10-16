import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { BUILD_INFO } from '../../../../build-info';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-footer',
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.scss']
})
export class FooterComponent implements OnInit {
  buildInfo = BUILD_INFO;

  constructor() {}

  ngOnInit() {}
}
