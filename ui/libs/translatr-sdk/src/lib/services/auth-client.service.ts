import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AuthClient } from '@dev/translatr-model';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthClientService {
  constructor(private readonly http: HttpClient) {}

  find(): Observable<AuthClient[]> {
    return this.http.get<AuthClient[]>('/api/authclients');
  }
}
