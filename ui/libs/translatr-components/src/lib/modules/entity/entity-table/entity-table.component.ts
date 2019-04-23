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
import { SelectionModel } from "@angular/cdk/collections";
import { MatColumnDef, MatTable } from "@angular/material";
import { PagedList } from "@dev/translatr-model";

export interface Entity {
  id: string;
}

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'entity-table',
  templateUrl: './entity-table.component.html',
  styleUrls: ['./entity-table.component.css']
})
export class EntityTableComponent implements OnInit, AfterContentInit {

  @Input() dataSource: PagedList<Entity>;
  private _displayedColumns: string[];

  @Input() set displayedColumns(displayedColumns: string[]) {
    this._displayedColumns = displayedColumns.indexOf('selection') >= 0
      ? displayedColumns
      : ['selection', ...displayedColumns];
  }

  get displayedColumns(): string[] {
    return this._displayedColumns;
  }

  @Output() readonly selected = new EventEmitter<Entity[]>();
  @Output() readonly filter = new EventEmitter<string>();
  @Output() readonly more = new EventEmitter<number>();

  @ViewChild(MatTable) private table: MatTable<Entity>;

  @ContentChildren(MatColumnDef) protected columns: QueryList<MatColumnDef>;

  selection = new SelectionModel<Entity>(true, []);

  constructor() {
  }

  ngOnInit() {
  }

  ngAfterContentInit(): void {
    console.log('columns after view init', this.columns);
    this.columns.forEach((column: MatColumnDef) => this.table.addColumnDef(column));
  }

  trackByFn(index: number, item: Entity): string {
    return item.id;
  }

  onFilter(value: string) {
    this.filter.emit(value);
  }

  onLoadMore() {
    this.more.emit(this.dataSource.limit * 2);
  }

  // Selection

  /**
   * Whether the number of selected elements matches the total number of rows.
   */
  isAllSelected(): boolean {
    const numSelected = this.selection.selected.length;
    const numRows = this.dataSource.list.length;
    return numSelected == numRows;
  }

  /**
   * Selects all rows if they are not all selected; otherwise clear selection.
   */
  masterToggle() {
    this.isAllSelected() ?
      this.selection.clear() :
      this.dataSource.list.forEach(row => this.selection.select(row))
  }

  onSelectionChange(element: Entity) {
    this.selection.toggle(element);
    this.selected.emit(this.selection.selected);
  }
}
