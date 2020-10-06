import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';

export interface RestRequest {
  name: string;
  params: any[];
  method: 'get' | 'post' | 'put' | 'patch' | 'delete';
  path: string;
}

@Injectable({
  providedIn: 'root'
})
export class ErrorHandler {
  handleError(error: HttpErrorResponse, request?: RestRequest): Observable<any> {
    return throwError(error);
  }
}
