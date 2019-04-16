import {Injector} from "@angular/core";
import {exhaustMap, switchMap} from "rxjs/operators";
import {Action, Command, State} from "./state";
import {interval, Observable, of} from "rxjs";
import {createRandomUser, deleteRandomUser, me, updateRandomUser} from "./user";
import {UserService} from "@dev/translatr-sdk";
import {createRandomProject, deleteRandomProject, updateRandomProject} from "./project";

export const handleCommand = (injector: Injector) => switchMap(([command, state]: [Command, State]): Observable<Partial<State>> => {
  switch (command.type) {
    case Action.ShowConfig:
      return of({message: `Config: ${JSON.stringify(state.config)}`});

    case Action.Me:
      return me(injector.get(UserService));
    case Action.CreateRandomUser:
      return createRandomUser(injector.get(UserService), state);
    case Action.UpdateRandomUser:
      return updateRandomUser(injector.get(UserService), state);
    case Action.DeleteRandomUser:
      return deleteRandomUser(injector.get(UserService), state);

    case Action.CreateRandomProject:
      return createRandomProject(injector, state);
    case Action.UpdateRandomProject:
      return updateRandomProject(injector, state);
    case Action.DeleteRandomProject:
      return deleteRandomProject(injector, state);

    default:
      return of({type: command.type, message: `Unknown command ${command.type}`});
  }
});

