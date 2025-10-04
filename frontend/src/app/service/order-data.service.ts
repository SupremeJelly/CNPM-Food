import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { Order } from '../dto/order/order-response';

@Injectable({
  providedIn: 'root'
})
export class OrderDataService {
  private orderSource = new BehaviorSubject<Order | null>(null);
  order$ = this.orderSource.asObservable();

  setOrder(order: Order) {
    this.orderSource.next(order);
  }

  clearOrder() {
    this.orderSource.next(null);
  }
}