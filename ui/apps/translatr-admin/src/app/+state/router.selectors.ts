import { getRouterSelectors, RouterReducerState } from '@ngrx/router-store';
import { createFeatureSelector } from '@ngrx/store';

export const ROUTER_FEATURE_KEY = 'router';

interface RouterPartialState {
  readonly [ROUTER_FEATURE_KEY]?: RouterReducerState<any>;
}

const selectRouter = createFeatureSelector<RouterPartialState, RouterReducerState<any>>(
  ROUTER_FEATURE_KEY
);

export const routerQuery = getRouterSelectors(selectRouter);
