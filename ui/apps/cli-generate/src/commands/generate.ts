import Command, { flags } from '@oclif/command';
import { Generator, GeneratorConfig } from '@translatr/generator';

export class GenerateCommand extends Command {
  static description = 'Generate data (users, projects, locales, keys) by using the API';

  static flags = {
    'endpoint': flags.string({
      description: 'The endpoint of the REST API to communicate with.',
      char: 'e',
      env: 'ENDPOINT',
      default: 'http://localhost:9000'
    }),
    'access-token': flags.string({
      description: 'The access token to use when using the REST API.',
      char: 'a',
      env: 'ACCESS_TOKEN',
      default: '-'
    }),

    // intervals
    'stress-factor': flags.integer({
      description: 'The stress factor to multiply the intervals with.',
      char: 'f',
      env: 'STRESS_FACTOR',
      default: 1
    }),

    // every minute
    'me-interval': flags.integer({
      description: 'Interval in millis',
      env: 'ME_INTERVAL',
      default: 10 * 60 * 1000
    }),

    // every ten minutes
    'create-user-interval': flags.integer({
      description: 'Interval in millis',
      env: 'CREATE_USER_INTERVAL',
      default: 10 * 60 * 1000
    }),
    // every five minutes
    'update-user-interval': flags.integer({
      description: 'Interval in millis',
      env: 'UPDATE_USER_INTERVAL',
      default: 5 * 60 * 1000
    }),
    // every hour
    'delete-user-interval': flags.integer({
      description: 'Interval in millis',
      env: 'DELETE_USER_INTERVAL',
      default: 60 * 60 * 1000
    }),

    // every five minutes
    'create-project-interval': flags.integer({
      description: 'Interval in millis',
      env: 'CREATE_PROJECT_INTERVAL',
      default: 5 * 60 * 1000
    }),
    // every fifteen minutes
    'update-project-interval': flags.integer({
      description: 'Interval in millis',
      env: 'UPDATE_PROJECT_INTERVAL',
      default: 15 * 60 * 1000
    }),
    // every hour
    'delete-project-interval': flags.integer({
      description: 'Interval in millis',
      env: 'DELETE_PROJECT_INTERVAL',
      default: 60 * 60 * 1000
    }),

    // every hour
    'create-locale-interval': flags.integer({
      description: 'Interval in millis',
      env: 'CREATE_LOCALE_INTERVAL',
      default: 60 * 60 * 1000
    }),
    // every two hours
    'delete-locale-interval': flags.integer({
      description: 'Interval in millis',
      env: 'DELETE_LOCALE_INTERVAL',
      default: 2 * 60 * 60 * 1000
    }),

    // every minute
    'create-key-interval': flags.integer({
      description: 'Interval in millis',
      env: 'CREATE_KEY_INTERVAL',
      default: 60 * 1000
    }),
    // every hour
    'delete-key-interval': flags.integer({
      description: 'Interval in millis',
      env: 'DELETE_KEY_INTERVAL',
      default: 60 * 60 * 1000
    })
  };

  async run() {
    const command = this.parse(GenerateCommand);

    const config: GeneratorConfig = {
      baseUrl: command.flags['endpoint'],
      accessToken: command.flags['access-token'],
      intervals: {
        stressFactor: command.flags['stress-factor'],

        // every minute
        me: command.flags['me-interval'],

        // every ten minutes
        createUser: command.flags['create-user-interval'],
        // every five minutes
        updateUser: command.flags['update-user-interval'],
        // every hour
        deleteUser: command.flags['delete-user-interval'],

        // every five minutes
        createProject: command.flags['create-project-interval'],
        // every fifteen minutes
        updateProject: command.flags['update-project-interval'],
        // every hour
        deleteProject: command.flags['delete-project-interval'],

        // every hour
        createLocale: command.flags['create-locale-interval'],
        // every two hours
        deleteLocale: command.flags['delete-locale-interval'],

        // every minute
        createKey: command.flags['create-key-interval'],
        // every hour
        deleteKey: command.flags['delete-key-interval']
      }
    };

    new Generator(config).execute();
  }
}
