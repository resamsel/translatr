import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { provideSvgIcons } from '@ngneat/svg-icon';
import {
  appChat,
  appDocument,
  appEducation,
  appPulse,
  appThumbsUp,
  appUser,
  appWrite
} from '../../../../assets';
import { MainPageComponent } from './main-page.component';

const routes: Routes = [
  {
    path: '',
    component: MainPageComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
  providers: [
    provideSvgIcons([appPulse, appUser, appThumbsUp, appEducation, appChat, appWrite, appDocument])
  ]
})
export class MainPageRoutingModule {}
