// The generator library depends on partially-compiled Angular packages (@angular/core,
// @angular/common/http) that it consumes via a manually-created Injector rather than a
// platform bootstrap. The esbuild-based build does not run the Angular Linker, so the
// JIT compiler must be present as a fallback for `ɵɵngDeclareFactory` at runtime.
import '@angular/compiler';
import flush from '@oclif/command/flush';
import handle from '@oclif/errors/handle';
import { LetsGenerateCommand } from './commands';

LetsGenerateCommand.run().then(flush, handle);
