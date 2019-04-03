import {Component, OnInit} from '@angular/core';
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {ProjectFacade} from "../+state/project.facade";
import {filter, switchMapTo, take, tap} from "rxjs/operators";
import {Router} from "@angular/router";

@Component({
  selector: 'app-project-settings',
  templateUrl: './project-settings.component.html',
  styleUrls: ['./project-settings.component.scss']
})
export class ProjectSettingsComponent implements OnInit {

  project$ = this.projectFacade.project$;

  form = new FormGroup({
    'name': new FormControl('', [
      Validators.required,
      Validators.pattern('[^\\s/]+')
    ])
  });

  public get nameFormControl() {
    return this.form.get('name');
  }

  constructor(
    private readonly projectFacade: ProjectFacade,
    private readonly router: Router) {
  }

  ngOnInit() {
    this.project$
      .pipe(filter(project => !!project))
      .subscribe(project => this.nameFormControl.setValue(project.name));
  }

  onSaveName() {
    this.project$
      .pipe(
        take(1),
        tap(project => this.projectFacade.save({...project, name: this.nameFormControl.value as string})),
        switchMapTo(this.projectFacade.project$)
      )
      .subscribe(project => {
        this.router.navigate([project.ownerUsername, project.name, 'settings']);
      });
  }
}
