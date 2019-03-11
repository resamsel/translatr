import { Component, EventEmitter, Input, OnInit, Output, TemplateRef } from '@angular/core';
import { PagedList } from "../../../shared/paged-list";

@Component({
  selector: 'app-nav-list',
  templateUrl: './nav-list.component.html',
  styleUrls: ['./nav-list.component.scss']
})
export class NavListComponent implements OnInit {

  @Input() pagedList: PagedList<{ id: string }> | undefined;

  @Output() more = new EventEmitter<number>();

  @Input() template: TemplateRef<any>;

  constructor() {
  }

  ngOnInit() {
  }

  trackByFn(index: number, item: { id: string }): string {
    return item.id;
  }

  loadMore(): void {
    if (this.pagedList !== undefined) {
      this.more.emit(this.pagedList.limit * 2);
    }
  }
}
