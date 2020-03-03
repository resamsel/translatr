import { Directive, ElementRef, Input, OnInit, Renderer2 } from '@angular/core';
import { FeatureFlag } from '@dev/translatr-model';
import { filter, take } from 'rxjs/operators';
import { FeatureFlagFacade } from './feature-flag.facade';

@Directive({
  selector: '[featureFlagClass]'
})
export class FeatureFlagClassDirective implements OnInit {

  @Input() featureFlagClass: Record<string, FeatureFlag>;

  constructor(
    private readonly renderer: Renderer2,
    private readonly el: ElementRef,
    private readonly facade: FeatureFlagFacade
  ) {
  }

  ngOnInit(): void {
    Object.keys(this.featureFlagClass)
      .forEach(className =>
        this.facade.hasFlags$(this.featureFlagClass[className])
          .pipe(take(1), filter(enabled => enabled))
          .subscribe(() =>
            this.renderer.addClass(this.el.nativeElement, className)));
  }
}
