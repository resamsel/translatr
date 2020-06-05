import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AuthProvider } from '@dev/translatr-model';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthProviderService {
  constructor(private readonly http: HttpClient) {}

  find(): Observable<AuthProvider[]> {
    return this.http.get<AuthProvider[]>('/api/authproviders');
  }
}
