import { interval, merge, Observable, of, Subject } from 'rxjs';
import { mapTo, scan, startWith, withLatestFrom } from 'rxjs/operators';
import { Injector } from '@angular/core';
import { Action, Command, createInjector, GeneratorConfig, GeneratorIntervals, handleCommand, State } from '.';
import * as dateformat from 'dateformat';

export class Generator {
  readonly stateCommand$ = new Subject<Partial<State>>();
  readonly state$: Observable<State> = this.stateCommand$.asObservable().pipe(
    startWith({ config: this.config }),
    scan((acc: State, next: State) => ({ ...acc, ...next }))
  );

  readonly commands$: Observable<Command> = merge(
    of({ type: Action.ShowConfig }),

    interval(this.intervals.me / this.intervals.stressFactor).pipe(
      mapTo({ type: Action.Me })
    ),
    interval(this.intervals.createUser / this.intervals.stressFactor).pipe(
      mapTo({ type: Action.CreateRandomUser })
    ),
    interval(this.intervals.updateUser / this.intervals.stressFactor).pipe(
      mapTo({ type: Action.UpdateRandomUser })
    ),
    interval(this.intervals.deleteUser / this.intervals.stressFactor).pipe(
      mapTo({ type: Action.DeleteRandomUser })
    ),

    // TODO: add 1+ contributors
    interval(this.intervals.createProject / this.intervals.stressFactor).pipe(
      mapTo({ type: Action.CreateRandomProject })
    ),
    interval(this.intervals.updateProject / this.intervals.stressFactor).pipe(
      mapTo({ type: Action.UpdateRandomProject })
    ),
    interval(this.intervals.deleteProject / this.intervals.stressFactor).pipe(
      mapTo({ type: Action.DeleteRandomProject })
    ),

    // TODO
    // create locale every hour
    interval(this.intervals.createLocale / this.intervals.stressFactor).pipe(
      mapTo({ type: Action.CreateRandomLocale })
    ),
    // delete locale every two hours
    interval(this.intervals.deleteLocale / this.intervals.stressFactor).pipe(
      mapTo({ type: Action.DeleteRandomLocale })
    )

    // TODO
    // create key every minute
    // interval(this.intervals.createKey / this.intervals.stressFactor).pipe(mapTo({type: Action.CreateRandomKey})),
    // delete key every hour
    // interval(this.intervals.deleteKey / this.intervals.stressFactor).pipe(mapTo({type: Action.DeleteRandomKey})),

    // add translation (incl. locale and key, if not existing) every minute
    // change translation (incl. locale and key, if not existing) every minute
    // add contributor every minute
    // change contributor mode every hour
    // delete contributor every hour
  );

  get intervals(): GeneratorIntervals {
    return this.config.intervals;
  }

  constructor(public readonly config: GeneratorConfig) {}

  execute() {
    const injector: Injector = createInjector(
      this.config.baseUrl,
      this.config.accessToken
    );

    this.commands$
      .pipe(
        withLatestFrom(this.state$),
        handleCommand(injector)
        //  tap((state: State) => stateCommand$.next(state))
      )
      .subscribe(
        (state: State) =>
          console.log(
            `${dateformat('yyyy-mm-dd hh:MM:ss.l')}: ${state.message}`
          ),
        (state: State) =>
          console.error(
            `${dateformat('yyyy-mm-dd hh:MM:ss.l')}: ${
              state.message
            } (state: ${state})`
          )
      );
  }
}
