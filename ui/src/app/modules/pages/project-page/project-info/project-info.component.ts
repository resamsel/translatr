import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {Project} from "../../../../shared/project";
import {ActivatedRoute} from "@angular/router";
import {Locale} from "../../../../shared/locale";
import {Key} from "../../../../shared/key";

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-project-info',
  templateUrl: './project-info.component.html',
  styleUrls: ['./project-info.component.scss']
})
export class ProjectInfoComponent implements OnInit {

  project: Project;

  constructor(private readonly route: ActivatedRoute) {
  }

  ngOnInit() {
    this.route.parent.data
      .subscribe((data: { project: Project }) => {
        this.project = data.project;
      });
  }

  latestLocales(): Array<Locale> {
    if (!this.project || !this.project.locales) {
      return [];
    }

    const locales = this.project.locales;

    return locales
      .sort((a: Locale, b: Locale) => b.whenUpdated.getTime() - a.whenUpdated.getTime())
      .slice(0, 3);
  }

  latestKeys(): Array<Key> {
    if (!this.project || !this.project.keys) {
      return [];
    }

    const keys = this.project.keys;

    return keys
      .sort((a: Key, b: Key) => b.whenUpdated.getTime() - a.whenUpdated.getTime())
      .slice(0, 3);
  }
}
