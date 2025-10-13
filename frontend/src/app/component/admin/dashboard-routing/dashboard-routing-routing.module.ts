import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DashboardComponent } from '../dashboard/dashboard.component';
import { UserManagementComponent } from '../../admin/user-management/user-management.component';
import { RestaurantManagementComponent } from '../../admin/restaurant-management/restaurant-management.component';
import { OrderManagementComponent } from '../../admin/order-management/order-management.component';

const routes: Routes = [
  {
    path: '',                      // /dashboard
    component: DashboardComponent,  // layout + sidebar + <router-outlet>
    children: [
      { path: '', redirectTo: '', pathMatch: 'full' },  // redirect /dashboard -> /dashboard/users
      { path: 'users', component: UserManagementComponent },
      { path: 'restaurants', component: RestaurantManagementComponent },
      { path: 'orders', component: OrderManagementComponent },
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DashboardRoutingRoutingModule {}