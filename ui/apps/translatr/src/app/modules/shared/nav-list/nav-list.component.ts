import { Component, EventEmitter, Input, OnInit, Output, TemplateRef } from '@angular/core';
import { PagedList } from '@dev/translatr-model';

@Component({
  selector: 'app-nav-list',
  templateUrl: './nav-list.component.html',
  styleUrls: ['./nav-list.component.scss']
})
export class NavListComponent implements OnInit {
  @Input() pagedList: PagedList<{ id: string }> | undefined;
  @Input() loadingListLength = 5;
  @Input() showLoadingAvatar = true;
  @Input() template: TemplateRef<any>;

  @Output() more = new EventEmitter<number>();

  constructor() {}

  ngOnInit() {}

  trackByFn(index: number, item: { id: string }): string {
    return item.id;
  }

  get loadingList(): number[] {
    return Array(this.loadingListLength).map(
      (value: number, index: number) => index
    );
  }

  loadMore(): void {
    if (this.pagedList !== undefined) {
      this.more.emit(this.pagedList.limit * 2);
    }
  }
}
