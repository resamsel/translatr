import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { FeatureFlagCriteria, ResolvedFeature, UserFeatureFlag } from '@dev/translatr-model';
import { Observable } from 'rxjs';
import { AbstractService } from './abstract.service';
import { ErrorHandler } from './error-handler';
import { LanguageProvider } from './language-provider';

@Injectable({
  providedIn: 'root'
})
export class FeatureFlagService extends AbstractService<UserFeatureFlag, FeatureFlagCriteria> {
  constructor(http: HttpClient, errorHandler: ErrorHandler, languageProvider: LanguageProvider) {
    super(http, errorHandler, languageProvider, () => '/api/featureflags', '/api/featureflag');
  }

  /** override → global → default detail for the current user, one entry per feature. */
  resolved(): Observable<ResolvedFeature[]> {
    return this.http.get<ResolvedFeature[]>('/api/featureflags/resolved');
  }
}
