import {Component, Input, OnInit} from '@angular/core';
import {Project} from "../../../../../shared/project";
import {Locale} from "../../../../../shared/locale";

@Component({
  selector: 'app-locale-list',
  templateUrl: './locale-list.component.html',
  styleUrls: ['./locale-list.component.scss']
})
export class LocaleListComponent implements OnInit {

  @Input() project: Project;
  @Input() locales: Array<Locale>;

  constructor() {
  }

  ngOnInit() {
  }

}
