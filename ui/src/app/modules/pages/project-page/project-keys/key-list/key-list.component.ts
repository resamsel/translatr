import {Component, Input, OnInit} from '@angular/core';
import {Project} from "../../../../../shared/project";
import {Key} from "../../../../../shared/key";

@Component({
  selector: 'app-key-list',
  templateUrl: './key-list.component.html',
  styleUrls: ['./key-list.component.scss']
})
export class KeyListComponent implements OnInit {

  @Input() project: Project;
  @Input() keys: Array<Key>;

  constructor() { }

  ngOnInit() {
  }

}
