import * as fromRouter from '@ngrx/router-store';
import { createFeatureSelector } from '@ngrx/store';

export const ROUTER_FEATURE_KEY = 'router';

interface RouterPartialState {
  readonly [ROUTER_FEATURE_KEY]?: fromRouter.RouterReducerState<any>;
}

const selectRouter = createFeatureSelector<RouterPartialState, fromRouter.RouterReducerState<any>>(
  ROUTER_FEATURE_KEY
);

export const routerQuery = fromRouter.getSelectors(selectRouter);
