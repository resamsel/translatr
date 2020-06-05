import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { FeatureFlagCriteria, UserFeatureFlag } from '@dev/translatr-model';
import { AbstractService } from './abstract.service';
import { ErrorHandler } from './error-handler';

@Injectable({
  providedIn: 'root'
})
export class FeatureFlagService extends AbstractService<UserFeatureFlag, FeatureFlagCriteria> {
  constructor(http: HttpClient, errorHandler: ErrorHandler) {
    super(http, errorHandler, () => '/api/featureflags', '/api/featureflag');
  }
}
