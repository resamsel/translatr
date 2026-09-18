import { ChangeDetectionStrategy, Component, HostBinding, Input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterModule } from '@angular/router';
import { User } from '@dev/translatr-model';
import { TranslocoModule } from '@jsverse/transloco';
import { GravatarModule } from 'ngx-gravatar';

@Component({
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-auth-bar-item',
  templateUrl: './auth-bar-item.component.html',
  styleUrls: ['./auth-bar-item.component.scss'],
  imports: [
    RouterModule,
    MatButtonModule,
    MatMenuModule,
    MatIconModule,
    MatTooltipModule,
    MatDividerModule,
    TranslocoModule,
    GravatarModule
  ]
})
export class AuthBarItemComponent {
  @Input() me: User;
  @Input() endpointUrl: string;

  @HostBinding('class') protected readonly clazz = 'auth-bar-item';
}
