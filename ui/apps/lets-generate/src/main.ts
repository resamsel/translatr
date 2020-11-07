import { LetsGenerateCommand } from './commands';

LetsGenerateCommand.run().then(require('@oclif/command/flush'), require('@oclif/errors/handle'));
