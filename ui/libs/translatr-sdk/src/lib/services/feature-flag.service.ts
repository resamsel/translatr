import { HttpClient, HttpContext } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { FeatureFlagCriteria, ResolvedFeature, UserFeatureFlag } from '@dev/translatr-model';
import { Observable } from 'rxjs';
import { UserFeatureFlagsService } from '../generated/api/userFeatureFlags.service';
import { FeatureFlagDto } from '../generated/model/featureFlagDto';
import { AbstractService, PagedListLike } from './abstract.service';
import { ErrorHandler } from './error-handler';
import { LanguageProvider } from './language-provider';

@Injectable({
  providedIn: 'root',
})
export class FeatureFlagService extends AbstractService<UserFeatureFlag, FeatureFlagCriteria> {
  constructor(
    http: HttpClient,
    errorHandler: ErrorHandler,
    languageProvider: LanguageProvider,
    client: UserFeatureFlagsService,
  ) {
    super(http, errorHandler, languageProvider, {
      entityPath: '/api/featureflag',
      listPath: () => '/api/featureflags',
      list: (c: FeatureFlagCriteria | undefined, context?: HttpContext) =>
        client.findUserFeatureFlags(
          c?.search,
          c?.offset,
          c?.limit,
          c?.order,
          c?.fetch,
          c?.userId,
          undefined,
          'body',
          false,
          { context },
        ) as unknown as Observable<PagedListLike<UserFeatureFlag>>,
      get: (id: string | number, context?: HttpContext) =>
        client.getUserFeatureFlag(String(id), 'body', false, {
          context,
        }) as unknown as Observable<UserFeatureFlag>,
      create: (dto: UserFeatureFlag, context?: HttpContext) =>
        client.createUserFeatureFlag(dto as unknown as FeatureFlagDto, 'body', false, {
          context,
        }) as unknown as Observable<UserFeatureFlag>,
      update: (dto: Partial<UserFeatureFlag>, context?: HttpContext) =>
        client.updateUserFeatureFlag(dto as unknown as FeatureFlagDto, 'body', false, {
          context,
        }) as unknown as Observable<UserFeatureFlag>,
      delete: (id: string | number, context?: HttpContext) =>
        client.deleteUserFeatureFlag(String(id), 'body', false, {
          context,
        }) as unknown as Observable<UserFeatureFlag>,
    });
  }

  /**
   * override → global → default detail for `userId`, one entry per feature. Omit `userId` to
   * resolve for the caller; resolving for anyone else requires Admin.
   */
  resolved(userId?: string): Observable<ResolvedFeature[]> {
    return this.http.get<ResolvedFeature[]>('/api/featureflags/resolved', {
      context: this.authContext,
      params: userId ? { userId } : {},
    });
  }
}
