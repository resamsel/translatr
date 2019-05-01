import { Component } from "@angular/core";
import { AppFacade } from "../../../+state/app.facade";
import { User, UserRole } from "@dev/translatr-model";
import { environment } from "../../../../environments/environment";

@Component({
  selector: "app-main-page",
  templateUrl: "./main-page.component.html",
  styleUrls: ["./main-page.component.scss"]
})
export class MainPageComponent {

  me$ = this.facade.me$;

  constructor(private readonly facade: AppFacade) {
  }

  isAdmin(me: User | undefined): boolean {
    return !!me && me.role === UserRole.Admin;
  }

  adminRoute(): string {
    return environment.adminUrl;
  }
}
