const TSModuleAlias = require("@momothepug/tsmodule-alias");
// Path from package.json to your tsconfig.json file
const tsconfigToReadFromRoot = "./";
// Makes it work with play method
const aliasRegister = TSModuleAlias.play(tsconfigToReadFromRoot);

import Command, { flags } from "@oclif/command";
import { Generator, GeneratorConfig } from "@translatr/generator";
import { envAsNumber, envAsString } from "@translatr/node-utils";

// Alias map loaded to nodejs from typescript paths (optional)
//console.log('aliasMap', aliasRegister.nodeRegister.aliasMap);
// Displays root module and typescript project path (optional)
//console.log('env', aliasRegister.currentEnvironmentData);

export class GenerateCommand extends Command {
  static flags = {
    "endpoint": flags.string({
      description: 'The endpoint of the REST API to communicate with.',
      char: 'e',
      env: "ENDPOINT",
      default: "http://localhost:9000"
    }),
    "access-token": flags.string({
      description: 'The access token to use when using the REST API.',
      char: 'a',
      env: "ACCESS_TOKEN",
      default: "-"
    }),

    // intervals
    "stress-factor": flags.integer({
      description: 'The stress factor to multiply the intervals with.',
      char: 'f',
      env: "STRESS_FACTOR",
      default: 1
    }),

    // every minute
    'me-interval': flags.integer({
      description: 'Interval in millis',
      env: "ME_INTERVAL",
      default: 10 * 60 * 1000
    }),

    // every ten minutes
    'create-user-interval': flags.integer({
      description: 'Interval in millis',
      env: "CREATE_USER_INTERVAL",
      default: 10 * 60 * 1000
    }),
    // every five minutes
    'update-user-interval': flags.integer({
      description: 'Interval in millis',
      env: "UPDATE_USER_INTERVAL",
      default: 5 * 60 * 1000
    }),
    // every hour
    'delete-user-interval': flags.integer({
      description: 'Interval in millis',
      env: "DELETE_USER_INTERVAL",
      default: 60 * 60 * 1000
    }),

    // every five minutes
    'create-project-interval': flags.integer({
      description: 'Interval in millis',
      env: "CREATE_PROJECT_INTERVAL",
      default: 5 * 60 * 1000
    }),
    // every fifteen minutes
    'update-project-interval': flags.integer({
      description: 'Interval in millis',
      env: "UPDATE_PROJECT_INTERVAL",
      default: 15 * 60 * 1000
    }),
    // every hour
    'delete-project-interval': flags.integer({
      description: 'Interval in millis',
      env: "DELETE_PROJECT_INTERVAL",
      default: 60 * 60 * 1000
    }),

    // every hour
    'create-locale-interval': flags.integer({
      description: 'Interval in millis',
      env: "CREATE_LOCALE_INTERVAL",
      default: 60 * 60 * 1000
    }),
    // every two hours
    'delete-locale-interval': flags.integer({
      description: 'Interval in millis',
      env: "DELETE_LOCALE_INTERVAL",
      default: 2 * 60 * 60 * 1000
    }),

    // every minute
    'create-key-interval': flags.integer({
      description: 'Interval in millis',
      env: "CREATE_KEY_INTERVAL",
      default: 60 * 1000
    }),
    // every hour
    'delete-key-interval': flags.integer({
      description: 'Interval in millis',
      env: "DELETE_KEY_INTERVAL",
      default: 60 * 60 * 1000
    })
  };

  async run() {
    const { flags } = this.parse(GenerateCommand);

    const config: GeneratorConfig = {
      baseUrl: flags["endpoint"],
      accessToken: flags["access-token"],
      intervals: {
        stressFactor: flags["stress-factor"],

        // every minute
        me: flags['me-interval'],

        // every ten minutes
        createUser: flags['create-user-interval'],
        // every five minutes
        updateUser: flags['update-user-interval'],
        // every hour
        deleteUser: flags['delete-user-interval'],

        // every five minutes
        createProject: flags['create-project-interval'],
        // every fifteen minutes
        updateProject: flags['update-project-interval'],
        // every hour
        deleteProject: flags['delete-project-interval'],

        // every hour
        createLocale: flags['create-locale-interval'],
        // every two hours
        deleteLocale: flags['delete-locale-interval'],

        // every minute
        createKey: flags['create-key-interval'],
        // every hour
        deleteKey: flags['delete-key-interval']
      }
    };

    new Generator(config).execute();
  }
}
