import { Component, Input, NgModule } from '@angular/core';
import { User } from '@dev/translatr-model';

@Component({
  selector: 'user-card',
  template: ''
})
class MockUserCardComponent {
  @Input() user: User;
}

@NgModule({
  declarations: [MockUserCardComponent],
  exports: [MockUserCardComponent]
})
export class UserCardTestingModule {
}
