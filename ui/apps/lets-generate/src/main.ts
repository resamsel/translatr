import flush from '@oclif/command/flush';
import handle from '@oclif/errors/handle';
import { LetsGenerateCommand } from './commands';

LetsGenerateCommand.run().then(flush, handle);
