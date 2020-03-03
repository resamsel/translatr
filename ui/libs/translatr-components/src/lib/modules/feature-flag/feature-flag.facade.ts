import { FeatureFlag } from '@dev/translatr-model';
import { Observable, of } from 'rxjs';

export class FeatureFlagFacade {
  hasFlags$(featureFlags: FeatureFlag | FeatureFlag[]): Observable<boolean> {
    return of(false);
  }
}
