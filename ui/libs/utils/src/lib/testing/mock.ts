import { Observable, Subject } from 'rxjs';

export const mockObservable = <T>(): Observable<T> => new Subject();
