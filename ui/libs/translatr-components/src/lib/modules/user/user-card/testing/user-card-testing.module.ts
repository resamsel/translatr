import { Component, Input, NgModule } from '@angular/core';
import { User } from '@dev/translatr-model';

@Component({
  standalone: false,
  selector: 'user-card',
  template: ''
})
export class MockUserCardComponent {
  @Input() user: User;
}

@Component({
  standalone: false,
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
export class UserCardTestingModule {}
