import { Component, Input, NgModule } from '@angular/core';
import { User } from '@dev/translatr-model';

@Component({
  selector: 'user-card',
  template: ''
})
export class MockUserCardComponent {
  @Input() user: User;
}

@Component({
  selector: 'app-user-card-link',
  template: ''
})
export class MockUserCardLinkComponent {
  @Input() user: User;
}

@NgModule({
  declarations: [MockUserCardComponent, MockUserCardLinkComponent],
  exports: [MockUserCardComponent, MockUserCardLinkComponent]
})
export class UserCardTestingModule {
}
