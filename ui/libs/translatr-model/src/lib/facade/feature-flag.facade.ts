import { Feature } from '@dev/translatr-model';
import { Observable, of } from 'rxjs';

export class FeatureFlagFacade {
  hasFeatures$(featureFlags: Feature | Feature[]): Observable<boolean> {
    return of(false);
  }
}
