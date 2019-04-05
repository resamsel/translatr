import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Project } from "../../../../../shared/project";
import { Locale } from "../../../../../shared/locale";
import { PagedList } from "../../../../../shared/paged-list";

@Component({
  selector: 'app-locale-list',
  templateUrl: './locale-list.component.html',
  styleUrls: ['./locale-list.component.scss']
})
export class LocaleListComponent {
  @Input() project: Project;
  @Input() locales: PagedList<Locale>;
  @Output() more = new EventEmitter<number>();
}
