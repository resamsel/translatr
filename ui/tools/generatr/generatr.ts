import { Generator, GeneratorConfig } from '@translatr/generator';
import { envAsNumber, envAsString } from '@translatr/node-utils';

const config: GeneratorConfig = {
  baseUrl: envAsString('ENDPOINT', 'http://localhost:9000'),
  accessToken: envAsString('ACCESS_TOKEN', ''),
  intervals: {
    stressFactor: envAsNumber('STRESS_FACTOR', 1),

    // every minute
    me: envAsNumber('ME_INTERVAL', 10 * 60 * 1000),

    // every ten minutes
    createUser: envAsNumber('CREATE_USER_INTERVAL', 10 * 60 * 1000),
    // every five minutes
    updateUser: envAsNumber('UPDATE_USER_INTERVAL', 5 * 60 * 1000),
    // every hour
    deleteUser: envAsNumber('DELETE_USER_INTERVAL', 60 * 60 * 1000),

    // every five minutes
    createProject: envAsNumber('CREATE_PROJECT_INTERVAL', 5 * 60 * 1000),
    // every fifteen minutes
    updateProject: envAsNumber('UPDATE_PROJECT_INTERVAL', 15 * 60 * 1000),
    // every hour
    deleteProject: envAsNumber('DELETE_PROJECT_INTERVAL', 60 * 60 * 1000),

    // every hour
    createLocale: envAsNumber('CREATE_LOCALE_INTERVAL', 60 * 60 * 1000),
    // every two hours
    deleteLocale: envAsNumber('DELETE_LOCALE_INTERVAL', 2 * 60 * 60 * 1000),

    // every minute
    createKey: envAsNumber('CREATE_KEY_INTERVAL', 60 * 1000),
    // every hour
    deleteKey: envAsNumber('DELETE_KEY_INTERVAL', 60 * 60 * 1000)
  }
};

new Generator(config).execute();
