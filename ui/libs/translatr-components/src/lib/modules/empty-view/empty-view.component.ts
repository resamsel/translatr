import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'dev-empty-view',
  templateUrl: './empty-view.component.html',
  styleUrls: ['./empty-view.component.scss']
})
export class EmptyViewComponent implements OnInit {
  @Input() icon: string;

  constructor() {
  }

  ngOnInit() {
  }

}
