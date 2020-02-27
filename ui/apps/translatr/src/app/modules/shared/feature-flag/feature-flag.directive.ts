import { Directive, Input, OnInit, TemplateRef, ViewContainerRef } from '@angular/core';
import { AppFacade } from '../../../+state/app.facade';
import { filter, take } from 'rxjs/operators';

@Directive({
  selector: '[featureFlag]'
})
export class FeatureFlagDirective implements OnInit {

  @Input() featureFlag: string | string[];

  constructor(
    private readonly viewContainerRef: ViewContainerRef,
    private readonly templateRef: TemplateRef<any>,
    private readonly facade: AppFacade
  ) {
  }

  ngOnInit(): void {
    this.facade.hasFlags$(this.featureFlag)
      .pipe(take(1), filter(enabled => enabled))
      .subscribe(enabled =>
        this.viewContainerRef.createEmbeddedView(this.templateRef));
  }
}
