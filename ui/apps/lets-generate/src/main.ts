import { LoadGenerateCommand } from './commands';

LoadGenerateCommand.run().then(require('@oclif/command/flush'), require('@oclif/errors/handle'));
