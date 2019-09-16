import {
  AfterContentInit,
  ChangeDetectionStrategy,
  Component,
  ContentChildren,
  EventEmitter,
  Input,
  OnInit,
  Output,
  QueryList,
  ViewChild
} from '@angular/core';
import { SelectionModel } from '@angular/cdk/collections';
import { PageEvent } from '@angular/material/paginator';
import { MatColumnDef, MatTable } from '@angular/material/table';
import { PagedList, RequestCriteria } from '@dev/translatr-model';
import { merge, Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, map, scan, shareReplay, take } from 'rxjs/operators';
import { ActivatedRoute, Params, Router } from '@angular/router';

export interface Entity {
  id: string | number;
}

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'entity-table',
  templateUrl: './entity-table.component.html',
  styleUrls: ['./entity-table.component.scss']
})
export class EntityTableComponent implements OnInit, AfterContentInit {
  @Input() dataSource: PagedList<Entity>;
  private _displayedColumns: string[];

  @Input() set displayedColumns(displayedColumns: string[]) {
    this._displayedColumns =
      displayedColumns.indexOf('selection') >= 0
        ? displayedColumns
        : ['selection', ...displayedColumns];
  }

  get displayedColumns(): string[] {
    return this._displayedColumns;
  }

  @Input() set load(criteria: RequestCriteria) {
    this.init$.next(criteria);
    this.selection.clear();
  }

  @Output() readonly criteria = new EventEmitter<RequestCriteria>();
  @Output() readonly selected = new EventEmitter<Entity[]>();

  @ViewChild(MatTable, { static: true }) private table: MatTable<Entity>;

  @ContentChildren(MatColumnDef) protected columns: QueryList<MatColumnDef>;

  init$ = new Subject<RequestCriteria>();
  search$ = new Subject<string>();
  limit$ = new Subject<number>();
  offset$ = new Subject<number>();
  commands$ = merge(
    this.init$.asObservable(),
    this.search$.asObservable().pipe(
      distinctUntilChanged(),
      debounceTime(200),
      map((search: string) => ({ search }))
    ),
    this.limit$.asObservable().pipe(
      distinctUntilChanged(),
      map((limit: number) => ({ limit: `${limit}` }))
    ),
    this.offset$.asObservable().pipe(
      distinctUntilChanged(),
      map((offset: number) => ({ offset: `${offset}` }))
    ),
    this.route.queryParams.pipe(
      map((params: Params) => ({
        ...params
      }))
    )
  ).pipe(
    scan((acc: RequestCriteria, value: RequestCriteria) => ({
      ...acc,
      ...value
    })),
    shareReplay(1)
  );

  selection = new SelectionModel<Entity>(true, []);

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router
  ) {
    this.commands$.subscribe((criteria: RequestCriteria) => {
      this.criteria.emit(criteria);
      this.router.navigate([], {
        relativeTo: route,
        queryParams: criteria
      });
    });
  }

  ngOnInit() {}

  ngAfterContentInit(): void {
    this.columns.forEach((column: MatColumnDef) =>
      this.table.addColumnDef(column)
    );
  }

  trackByFn(index: number, item: Entity): string {
    return `${item.id}`;
  }

  onFilter(value: string) {
    this.search$.next(value);
  }

  onLoadMore() {
    this.commands$
      .pipe(take(1))
      .subscribe((criteria: RequestCriteria) =>
        this.limit$.next(parseInt(criteria.limit, 10) * 2)
      );
  }

  // Selection

  /**
   * Whether the number of selected elements matches the total number of rows.
   */
  isAllSelected(): boolean {
    const numSelected = this.selection.selected.length;
    const numRows = this.dataSource.list.length;
    return numSelected === numRows;
  }

  /**
   * Selects all rows if they are not all selected; otherwise clear selection.
   */
  masterToggle() {
    this.isAllSelected()
      ? this.selection.clear()
      : this.dataSource.list.forEach(row => this.selection.select(row));
  }

  onSelectionChange(element: Entity) {
    this.selection.toggle(element);
    this.selected.emit(this.selection.selected);
  }

  onPage(event: PageEvent) {
    this.offset$.next(event.pageIndex * event.pageSize);
  }
}
