import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Feature, GlobalFeatureFlag } from '@dev/translatr-model';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class GlobalFeatureFlagService {
  private readonly listPath = '/api/featureflags/global';
  private readonly entityPath = '/api/featureflag/global';

  constructor(private readonly http: HttpClient) {}

  list(): Observable<GlobalFeatureFlag[]> {
    return this.http.get<GlobalFeatureFlag[]>(this.listPath);
  }

  set(feature: Feature, enabled: boolean): Observable<GlobalFeatureFlag> {
    return this.http.post<GlobalFeatureFlag>(this.entityPath, { feature, enabled });
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.entityPath}/${id}`);
  }
}
