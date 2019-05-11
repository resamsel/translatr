import { Component, EventEmitter, Input, Output } from "@angular/core";
import { Project } from "../../../../../../../../../libs/translatr-model/src/lib/model/project";
import { Locale } from "../../../../../../../../../libs/translatr-model/src/lib/model/locale";
import { PagedList } from "../../../../../../../../../libs/translatr-model/src/lib/model/paged-list";

@Component({
  selector: 'app-locale-list',
  templateUrl: './locale-list.component.html',
  styleUrls: ['./locale-list.component.scss']
})
export class LocaleListComponent {
  @Input() project: Project;
  @Input() locales: PagedList<Locale>;
  @Output() more = new EventEmitter<number>();
  @Output() edit = new EventEmitter<Locale>();
  @Output() delete = new EventEmitter<Locale>();

  onEdit(locale: Locale, event: MouseEvent) {
    this.edit.emit(locale);
    event.stopPropagation();
    event.preventDefault();
    return false;
  }

  onDelete(locale: Locale, event: MouseEvent) {
    this.delete.emit(locale);
    event.stopPropagation();
    event.preventDefault();
    return false;
  }
}
