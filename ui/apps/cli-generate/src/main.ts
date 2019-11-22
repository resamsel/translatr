import { GenerateCommand } from './commands';

GenerateCommand.run()
  .then(require('@oclif/command/flush'), require('@oclif/errors/handle'));
