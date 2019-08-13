import { ChangeDetectionStrategy, Component, Input } from '@angular/core';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'tile',
  templateUrl: './tile.component.html',
  styleUrls: ['./tile.component.scss'],
  host: {
    class: 'tile'
  }
})
export class TileComponent {
  @Input() value: any;
  @Input() name: string;
  @Input() icon: string;
}
