import { Directive, ElementRef, Input, OnInit, Renderer2 } from '@angular/core';
import { Feature, FeatureFlagFacade } from '@dev/translatr-model';
import { filter, take } from 'rxjs/operators';

@Directive({
  selector: '[featureFlagClass]'
})
export class FeatureFlagClassDirective implements OnInit {
  @Input() featureFlagClass: Record<string, Feature>;

  constructor(
    private readonly renderer: Renderer2,
    private readonly el: ElementRef,
    private readonly facade: FeatureFlagFacade
  ) {}

  ngOnInit(): void {
    Object.keys(this.featureFlagClass).forEach((className) =>
      this.facade
        .hasFeatures$(this.featureFlagClass[className])
        .pipe(
          take(1),
          filter((enabled) => enabled)
        )
        .subscribe(() => this.renderer.addClass(this.el.nativeElement, className))
    );
  }
}
