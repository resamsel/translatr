import {Component, OnInit} from '@angular/core';
import {BUILD_INFO} from '../../../../build-info';

@Component({
  selector: 'app-footer',
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.scss']
})
export class FooterComponent implements OnInit {

  buildInfo = BUILD_INFO;

  constructor() { }

  ngOnInit() {
  }

}
