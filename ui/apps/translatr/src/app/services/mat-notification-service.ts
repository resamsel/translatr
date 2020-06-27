import { MatSnackBar, MatSnackBarConfig } from '@angular/material/snack-bar';
import { NotificationService } from '@dev/translatr-sdk';

export class MatNotificationService extends NotificationService {
  constructor(private readonly snackBar: MatSnackBar) {
    super();
  }

  notify(message: string, action?: string, config: MatSnackBarConfig = {}): void {
    this.snackBar.open(message, action || 'Dismiss', {
      duration: 10000,
      ...config
    });
  }
}
