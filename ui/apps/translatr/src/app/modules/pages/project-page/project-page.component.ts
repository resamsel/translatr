import {Component, OnDestroy, OnInit} from '@angular/core';
import {Project} from "../../../shared/project";
import {ActivatedRoute, ParamMap, Router} from "@angular/router";
import {ProjectFacade} from "./+state/project.facade";
import {Observable} from "rxjs";
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {filter, switchMapTo, take, tap} from "rxjs/operators";
import {MatSnackBar} from '@angular/material';

@Component({
  selector: 'app-project-page',
  templateUrl: './project-page.component.html',
  styleUrls: ['./project-page.component.scss']
})
export class ProjectPageComponent implements OnInit, OnDestroy {

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
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly snackBar: MatSnackBar,
    private readonly projectFacade: ProjectFacade) {
  }

  ngOnInit(): void {
    this.route.paramMap.subscribe((params: ParamMap) => {
      this.projectFacade.loadProject(params.get('username'), params.get('projectName'));
    });
    this.project$
      .pipe(filter(project => !!project), take(1))
      .subscribe(project => this.nameFormControl.setValue(project.name));
  }

  ngOnDestroy(): void {
    this.projectFacade.unloadProject();
  }

  onSaveName() {
    console.log('save');
    this.project$
      .pipe(
        take(1),
        tap(project => this.projectFacade.save({...project, name: this.nameFormControl.value as string})),
        switchMapTo(this.projectFacade.project$.pipe(
          filter(project => project.name === this.nameFormControl.value),
          take(1)
        ))
      )
      .subscribe(project => {
        this.router.navigate([project.ownerUsername, project.name])
          .then(() => this.snackBar.open(
            'Name has been updated',
            'Dismiss',
            {duration: 2000}
            )
          );
      });
  }
}
