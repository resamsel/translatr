import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppComponent } from './app.component';
import { RouterModule } from '@angular/router';
import { MatToolbarModule } from '@angular/material';
import { SearchFieldPageModule } from './pages/search-field-page/search-field-page.module';

@NgModule({
  declarations: [AppComponent],
  imports: [
    BrowserModule,
    RouterModule.forRoot([], { initialNavigation: 'enabled' }),
    MatToolbarModule,
    SearchFieldPageModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {
}
