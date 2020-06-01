import { Directive, Input, NgModule } from '@angular/core';

@Directive({
  selector: '[disableControl]'
})
export class MockDisableControlDirective {
  @Input() disableControl: boolean;
}

@NgModule({
  declarations: [MockDisableControlDirective],
  exports: [MockDisableControlDirective]
})
export class DisableControlTestingModule {
}
