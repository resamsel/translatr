import { MatDialogModule } from '@angular/material/dialog';
import { RouterTestingModule } from '@angular/router/testing';
import { HotkeysModule } from '@ngneat/hotkeys';
import { createComponentFactory, mockProvider } from '@ngneat/spectator/jest';
import { TranslocoTestingModule } from '@ngneat/transloco';
import { mockObservable } from '@translatr/utils/testing';
import { AppFacade } from './+state/app.facade';
import { AppComponent } from './app.component';

describe('AppComponent', () => {
  const createComponent = createComponentFactory({
    component: AppComponent,
    imports: [RouterTestingModule, TranslocoTestingModule, HotkeysModule, MatDialogModule],
    declarations: [AppComponent],
    providers: [
      mockProvider(AppFacade, {
        me$: mockObservable()
      })
    ]
  });

  it('should create the app', () => {
    // given, when
    const spectator = createComponent();

    // then
    expect(spectator.component).toBeTruthy();
  });

  it('should invoke loadMe on the facade', () => {
    // given, when
    const spectator = createComponent();
    const facade = spectator.inject(AppFacade);

    // then
    expect(facade.loadMe.mock.calls.length).toEqual(1);
  });

  it('should include router-outlet', () => {
    // given, when
    const spectator = createComponent();

    // then
    expect(spectator.query('router-outlet')).toExist();
  });
});
