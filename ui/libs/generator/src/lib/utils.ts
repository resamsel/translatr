import { Observable, throwError, timer } from 'rxjs';
import { mergeMap } from 'rxjs/operators';
import { WeightedPersonaFactory } from './weighted-persona-factory';

export const selectPersonaFactory = (
  personas: WeightedPersonaFactory[],
  totalWeight: number
): WeightedPersonaFactory => {
  const threshold = Math.floor(Math.random() * totalWeight);

  let total = 0;
  for (const persona of personas) {
    total += persona.weight;

    if (total >= threshold) {
      return persona;
    }
  }

  return personas[0];
};

export const genericRetryStrategy = ({
  maxRetryAttempts = 3,
  scalingDuration = 1000,
  excludedStatusCodes = [],
  prefix = ''
}: {
  maxRetryAttempts?: number;
  scalingDuration?: number;
  excludedStatusCodes?: number[];
  prefix?: string;
} = {}) => (attempts: Observable<any>) => {
  return attempts.pipe(
    mergeMap((error, i) => {
      const retryAttempt = i + 1;
      // if maximum number of retries have been met
      // or response is a status code we don't wish to retry, throw error
      if (retryAttempt > maxRetryAttempts || excludedStatusCodes.find(e => e === error.status)) {
        return throwError(error);
      }
      const delay = retryAttempt * retryAttempt * scalingDuration;
      console.log(`${prefix}attempt ${retryAttempt} failed - retrying in ${delay}ms`);
      // retry after 1s, 2s, etc...
      return timer(delay);
    })
  );
};
