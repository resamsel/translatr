import { Generator, GeneratorConfig } from '@translatr/generator';

describe('Generator', () => {
  it('should create', () => {
    // given
    const config: GeneratorConfig = {
      baseUrl: '',
      accessToken: '',
      intervals: {
        stressFactor: 1,

        // every minute
        me: 1,

        // every ten minutes
        createUser: 1,
        // every five minutes
        updateUser: 1,
        // every hour
        deleteUser: 1,

        // every five minutes
        createProject: 1,
        // every fifteen minutes
        updateProject: 1,
        // every hour
        deleteProject: 1,

        // every hour
        createLocale: 1,
        // every two hours
        deleteLocale: 1,

        // every minute
        createKey: 1,
        // every hour
        deleteKey: 1
      }
    };

    // when
    const generator = new Generator(config);

    // then
    expect(generator).toBeDefined();
  });
});
