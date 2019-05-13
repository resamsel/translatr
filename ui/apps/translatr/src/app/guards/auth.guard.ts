import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot } from '@angular/router';
import { Observable } from 'rxjs';
import { AppFacade } from '../+state/app.facade';
import { map, tap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  constructor(private readonly facade: AppFacade, private readonly router: Router) {
  }

  canActivate(
    next: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean> | Promise<boolean> | boolean {
    return this.facade.me$.pipe(
      map(x => !!x),
      tap((authenticated: boolean) => {
        if (!authenticated) {
          this.router.navigate(['/']);
        }
      })
    );
  }
}
