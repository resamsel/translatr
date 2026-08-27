import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  notify(message: string, _action?: string, _config?: any): void {
    console.warn(message);
  }
}
