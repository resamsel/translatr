import {
  GlobalFeatureFlagsLoaded,
  LoggedInUserLoaded,
  ResolvedFeaturesLoaded
} from './app.actions';
import { appReducer, initialState } from './app.reducer';

describe('Admin Reducer', () => {
  describe('valid actions ', () => {
    it('should include given user on meLoaded', () => {
      // given
      const payload = { id: '1', name: 'user', username: 'username' };
      const action = new LoggedInUserLoaded(payload);

      // when
      const actual = appReducer(initialState, action);

      // then
      expect(actual.me).toBeDefined();
    });
  });

  describe('feature flags', () => {
    it('stores resolved features on ResolvedFeaturesLoaded', () => {
      const state = appReducer(
        initialState,
        new ResolvedFeaturesLoaded([{ feature: 'header-graphic' } as any])
      );

      expect(state.resolvedFeatures).toEqual([{ feature: 'header-graphic' }]);
    });

    it('stores global feature flags on GlobalFeatureFlagsLoaded', () => {
      const state = appReducer(
        initialState,
        new GlobalFeatureFlagsLoaded([
          { id: 'g1', feature: 'header-graphic', enabled: true } as any
        ])
      );

      expect(state.globalFeatureFlags).toEqual([
        { id: 'g1', feature: 'header-graphic', enabled: true }
      ]);
    });
  });

  describe('unknown action', () => {
    it('should return the initial state', () => {
      const action = {} as any;
      const result = appReducer(initialState, action);

      expect(result).toEqual(initialState);
    });
  });
});
