import { LoadGeneratorConfig } from '@translatr/generator';
import { LoadGenerator } from './load-generator';

describe('LoadGenerator', () => {
  it('should create', () => {
    // given
    const config: LoadGeneratorConfig = {
      baseUrl: '',
      accessToken: '',
      usersPerMinute: 0,
      includePersonas: []
    };

    // when
    const actual = new LoadGenerator(config);

    // then
    expect(actual).toBeDefined();
  });
});
