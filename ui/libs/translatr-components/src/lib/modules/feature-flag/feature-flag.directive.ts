import { Directive, Input, OnInit, TemplateRef, ViewContainerRef } from '@angular/core';
import { Feature, FeatureFlagFacade } from '@dev/translatr-model';
import { filter, take } from 'rxjs/operators';

@Directive({
  selector: '[featureFlag]'
})
export class FeatureFlagDirective implements OnInit {
  @Input() featureFlag: Feature | Feature[];

  constructor(
    private readonly viewContainerRef: ViewContainerRef,
    private readonly templateRef: TemplateRef<any>,
    private readonly facade: FeatureFlagFacade
  ) {}

  ngOnInit(): void {
    this.facade
      .hasFeatures$(this.featureFlag)
      .pipe(
        take(1),
        filter(enabled => enabled)
      )
      .subscribe(() => this.viewContainerRef.createEmbeddedView(this.templateRef));
  }
}
