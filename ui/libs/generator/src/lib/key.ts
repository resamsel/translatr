import { Injector } from '@angular/core';
import { AccessToken, Key } from '@dev/translatr-model';
import { Observable, of } from 'rxjs';
import { KeyService } from '@dev/translatr-sdk';
import { cartesianProduct } from '@translatr/utils';
import { State } from '@translatr/generator';

export const featureNames = ['user', 'project', 'locale', 'key', 'message', 'accessToken', 'member', 'admin'];
export const keySuffixes = ['title', 'header', 'description', 'list', 'get', 'create', 'update', 'delete', 'permission', 'find', 'search', 'sell'];
export const keyNames = cartesianProduct([featureNames, keySuffixes]).map((values: string[]) => values.join('.'));

export const createKey = (injector: Injector, key: Key, accessToken: AccessToken): Observable<Key> => {
  return injector.get(KeyService).create(key, {params: {access_token: accessToken.key}});
};

export const createRandomKey = (injector: Injector): Observable<Partial<State>> => {
  return of({message: '+++ Create Random Key +++'});
};

export const deleteRandomKey = (injector: Injector): Observable<Partial<State>> => {
  return of({message: '+++ Delete Random Key +++'});
};
