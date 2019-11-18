import * as fromRouter from '@ngrx/router-store';
import { createFeatureSelector } from '@ngrx/store';
import { AppState } from './app.reducer';

const selectRouter = createFeatureSelector<AppState,
  fromRouter.RouterReducerState<any>>('router');

const {
  selectQueryParams,
  selectRouteParams
} = fromRouter.getSelectors(selectRouter);

export const routerQuery = {
  selectQueryParams,
  selectRouteParams
};
