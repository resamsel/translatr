import { Directive, Input, OnInit, TemplateRef, ViewContainerRef } from '@angular/core';
import { filter, take } from 'rxjs/operators';
import { FeatureFlagFacade } from './feature-flag.facade';
import { FeatureFlag } from '@dev/translatr-model';

@Directive({
  selector: '[featureFlag]'
})
export class FeatureFlagDirective implements OnInit {

  @Input() featureFlag: FeatureFlag | FeatureFlag[];

  constructor(
    private readonly viewContainerRef: ViewContainerRef,
    private readonly templateRef: TemplateRef<any>,
    private readonly facade: FeatureFlagFacade
  ) {
  }

  ngOnInit(): void {
    this.facade.hasFlags$(this.featureFlag)
      .pipe(take(1), filter(enabled => enabled))
      .subscribe(() =>
        this.viewContainerRef.createEmbeddedView(this.templateRef));
  }
}
