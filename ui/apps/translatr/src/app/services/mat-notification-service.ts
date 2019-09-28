import { NotificationService } from '@dev/translatr-sdk';
import { MatSnackBar } from '@angular/material';

export class MatNotificationService extends NotificationService {
  constructor(private readonly snackBar: MatSnackBar) {
    super();
  }

  notify(message: string): void {
    this.snackBar.open(
      message,
      'Dismiss',
      { duration: 10000 }
    );
  }
}
