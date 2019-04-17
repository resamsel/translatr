import { Component } from '@angular/core';
import { AppFacade } from "../../../../+state/app.facade";
import { RequestCriteria, User } from "@dev/translatr-sdk";
import {debounceTime, distinctUntilChanged, map, mapTo, shareReplay, startWith, take, tap} from "rxjs/operators";
import { UserDeleted, UserDeleteError } from "../../../../+state/app.actions";
import {
  UserEditDialogComponent,
  UserEditDialogConfig
} from "@dev/translatr-components/src/lib/modules/user/user-edit-dialog/user-edit-dialog.component";
import { MatDialog } from "@angular/material";
import { filter } from "rxjs/internal/operators/filter";
import { UserRole } from "@dev/translatr-sdk/src/lib/shared/user-role";
import { merge, Observable, Subject } from "rxjs";
import { FormControl } from "@angular/forms";
import { scan } from "rxjs/internal/operators/scan";

export const isAdmin = (user?: User): boolean => user !== undefined && user.role === UserRole.Admin;

export const hasCreateUserPermission = () => map(isAdmin);

const hasEditUserPermission = (user: User) => map((me?: User) =>
  (me !== undefined && me.id === user.id) || isAdmin(me));

const hasDeleteUserPermission = (user: User) => map((me?: User) =>
  (me !== undefined && me.id !== user.id) && isAdmin(me));

export const mapToAllowedRoles = () => map((me?: User): UserRole[] =>
  [UserRole.User, ...isAdmin(me) ? [UserRole.Admin] : []]);

@Component({
  selector: 'dev-dashboard-users',
  templateUrl: './dashboard-users.component.html',
  styleUrls: ['./dashboard-users.component.css']
})
export class DashboardUsersComponent {

  readonly displayedColumns = ['name', 'username', 'email', 'when_created', 'role', 'actions'];

  me$ = this.facade.me$;
  search$ = new Subject<string>();
  limit$ = new Subject<number>();
  reload$ = new Subject<void>();
  commands$ = merge(
    this.search$.asObservable().pipe(
      distinctUntilChanged(),
      debounceTime(200),
      map((search: string) => ({search}))
    ),
    this.limit$.asObservable().pipe(
      distinctUntilChanged(),
      map((limit: number) => ({limit: `${limit}`}))
    ),
    this.reload$.asObservable().pipe(mapTo({}))
  )
    .pipe(
      startWith({limit: '20', search: '', order: 'name asc'}),
      scan((acc: RequestCriteria, value: RequestCriteria) => ({...acc, ...value})),
      shareReplay(1)
    );
  users$ = this.facade.users$;
  userDeleted$ = this.facade.userDeleted$;
  allowCreate$ = this.me$.pipe(hasCreateUserPermission());
  search = new FormControl();

  constructor(private readonly facade: AppFacade, private readonly dialog: MatDialog) {
    this.commands$.subscribe((criteria: RequestCriteria) => this.facade.loadUsers(criteria));
  }

  trackByFn(index: number, item: { id: string }): string {
    return item.id;
  }

  allowEdit$(user: User): Observable<boolean> {
    return this.me$.pipe(hasEditUserPermission(user));
  }

  allowDelete$(user: User): Observable<boolean> {
    return this.me$.pipe(hasDeleteUserPermission(user));
  }

  onCreate() {
    this.me$.pipe(take(1), mapToAllowedRoles())
      .subscribe((allowedRoles: UserRole[]) => {
        this.dialog
          .open(UserEditDialogComponent, {
            data: {
              type: 'create',
              allowedRoles,
              onSubmit: (user: User) => this.facade.createUser(user),
              success$: this.facade.userCreated$,
              error$: this.facade.userCreateError$
            } as UserEditDialogConfig
          })
      });
  }

  onEdit(user: User) {
    this.me$.pipe(take(1), mapToAllowedRoles())
      .subscribe((allowedRoles: UserRole[]) => {
        this.dialog
          .open(UserEditDialogComponent, {
            data: {
              type: 'update',
              allowedRoles,
              user,
              onSubmit: (user: User) => this.facade.updateUser(user),
              success$: this.facade.userUpdated$,
              error$: this.facade.userUpdateError$
            }
          })
          .afterClosed()
          .pipe(filter(x => !!x))
          .subscribe((user: User) => this.facade.updateUser(user));
      });
  }

  onDelete(user: User) {
    this.userDeleted$
      .pipe(take(1))
      .subscribe((action: UserDeleted | UserDeleteError) => {
        if (action instanceof UserDeleted) {
          console.log(`User ${action.payload.username} has been deleted`);
          this.reload$.next();
        } else {
          console.warn(`User ${action.payload.error.error} could not be deleted`);
        }
      });
    this.facade.deleteUser(user);
  }

  onFilter(value: string) {
    this.search$.next(value);
  }

  onLoadMore() {
    this.commands$
      .pipe(tap(console.log), take(1))
      .subscribe((criteria: RequestCriteria) =>
        this.limit$.next(parseInt(criteria.limit, 10) * 2));
  }
}
