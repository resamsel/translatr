import { Injector } from "@angular/core";
import { Action, Command, State } from "./state";
import { Observable, of } from "rxjs";
import { createRandomUser, deleteRandomUser, me, updateRandomUser } from "./user";
import { UserService } from "@dev/translatr-sdk";
import { createRandomProject, deleteRandomProject, updateRandomProject } from "./project";
import { concatMap } from "rxjs/internal/operators/concatMap";

export const handleCommand = (injector: Injector) => concatMap(([command, state]: [Command, State]): Observable<Partial<State>> => {
  switch (command.type) {
    case Action.ShowConfig:
      return of({message: `Config: ${JSON.stringify(state.config)}`});

    case Action.Me:
      return me(injector.get(UserService));
    case Action.CreateRandomUser:
      return createRandomUser(injector.get(UserService));
    case Action.UpdateRandomUser:
      return updateRandomUser(injector.get(UserService));
    case Action.DeleteRandomUser:
      return deleteRandomUser(injector.get(UserService));

    case Action.CreateRandomProject:
      return createRandomProject(injector);
    case Action.UpdateRandomProject:
      return updateRandomProject(injector);
    case Action.DeleteRandomProject:
      return deleteRandomProject(injector);

    default:
      return of({type: command.type, message: `Unknown command ${command.type}`});
  }
});

