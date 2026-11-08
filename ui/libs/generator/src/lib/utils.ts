import { ErrorHandler, errorMessage } from '@dev/translatr-sdk';
import { EMPTY, Observable, throwError, timer } from 'rxjs';
import { catchError, mergeMap, retryWhen, tap } from 'rxjs/operators';
import { Persona } from './personas/persona';
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

export const executePersona = (
  persona: Persona,
  errorHandler: ErrorHandler,
  options: { maxRetryAttempts: number; retryScalingDelay: number }
): Observable<string> => {
  const startedMillis = new Date().getTime();

  return persona.execute().pipe(
    tap(message => {
      console.log(`${persona.name}: ${message} in ${new Date().getTime() - startedMillis}ms`);
    }),
    retryWhen(
      genericRetryStrategy({
        maxRetryAttempts: options.maxRetryAttempts,
        scalingDuration: options.retryScalingDelay,
        prefix: `${persona.name}: `
      })
    ),
    catchError(error => {
      console.error(
        `${persona.name}: ${errorMessage(error)} in ${new Date().getTime() - startedMillis}ms`
      );
      // errorHandler.handleError() always returns an observable that re-throws (it exists for
      // callers that want the error propagated); here we only need its side-effect logging, so
      // the re-thrown error is swallowed to keep the load generator running for the next persona.
      return errorHandler.handleError(error).pipe(catchError(() => EMPTY));
    })
  );
};
