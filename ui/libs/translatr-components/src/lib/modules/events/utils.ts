import { Action } from '@ngrx/store';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Observable } from 'rxjs';

export const notifyEvent = <O extends Action, E extends Action>(
  snackBar: MatSnackBar,
  observable: Observable<O | E>,
  okType: string,
  okMessage: (action: O) => string,
  errorMessage: (action: E) => string
) => {
  observable.subscribe((action: O | E) => {
    if (action.type === okType) {
      snackBar.open(okMessage(action as O), 'Dismiss', { duration: 3000 });
      // this.reload$.next();
    } else {
      snackBar.open(errorMessage(action as E), 'Dismiss', { duration: 8000 });
    }
  });
};
