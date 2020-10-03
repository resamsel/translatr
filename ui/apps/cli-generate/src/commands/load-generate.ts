require('@momothepug/tsmodule-alias').play('./');

import Command, { flags } from '@oclif/command';
import { LoadGenerator } from '@translatr/generator';

export class LoadGenerateCommand extends Command {
  static description = 'Generate data (users, projects, locales, keys) by using the API';

  static flags = {
    endpoint: flags.string({
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
    users: flags.integer({
      description: 'The number of users per minute.',
      char: 'u',
      env: 'USERS',
      default: 60
    })
  };

  async run() {
    const command = this.parse(LoadGenerateCommand);

    await new LoadGenerator({
      baseUrl: command.flags.endpoint,
      accessToken: command.flags['access-token'],
      requestsPerMinute: command.flags.users
    }).execute();
  }
}
