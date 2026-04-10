import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  notify(message: string, action?: string, config?: any): void {
    console.log(message);
  }
}
