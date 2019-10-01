import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AuthProvider } from '@dev/translatr-model';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthProviderService {
  constructor(private readonly http: HttpClient) {
  }

  find(): Observable<Array<AuthProvider>> {
    return this.http.get<Array<AuthProvider>>('/api/authproviders');
  }
}
