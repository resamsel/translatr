import { Router } from '@angular/router';
import { ErrorHandler } from '@angular/core';

export class DefaultErrorHandler implements ErrorHandler {
  constructor(
    private readonly router: Router,
    private readonly loginUrl: string
  ) {
  }

  handleError(err: any): void {
    console.log('handleError', err, this.loginUrl);
    if (this.router !== undefined
      && this.loginUrl !== undefined
      && this.loginUrl !== null) {
      this.router.navigate([this.loginUrl])
        .then((navigated: boolean) => {
          if (!navigated) {
            window.location.href = `${this.loginUrl}?redirect_uri=${window.location.href}`;
          }
        });
    }
  }
}
