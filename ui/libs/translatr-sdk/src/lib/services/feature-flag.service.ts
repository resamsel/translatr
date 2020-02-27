import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AbstractService } from './abstract.service';
import { FeatureFlag, FeatureFlagCriteria } from '@dev/translatr-model';
import { ErrorHandler } from './error-handler';

@Injectable({
  providedIn: 'root'
})
export class FeatureFlagService extends AbstractService<FeatureFlag, FeatureFlagCriteria> {
  constructor(
    http: HttpClient,
    errorHandler: ErrorHandler
  ) {
    super(
      http,
      errorHandler,
      () => '/api/featureflags',
      '/api/featureflag'
    );
  }
}
