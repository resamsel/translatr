import { ChangeDetectionStrategy, Component } from '@angular/core';
import { BUILD_INFO } from '@dev/translatr-model';

@Component({
  standalone: false,
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-footer',
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.scss']
})
export class FooterComponent {
  buildInfo = BUILD_INFO;
}
