import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  notify(message: string, action?: string, config?: any): void {
    // tslint:disable-next-line:no-console
    console.log(message);
  }
}
