import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {UpdateComponent} from "./update/update.component";
import {AppComponent} from "./app.component";
import {ListComponent} from "./list/list.component";


const routes: Routes = [
  { path: 'update/:id', component: UpdateComponent },
  { path: '', component: ListComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
