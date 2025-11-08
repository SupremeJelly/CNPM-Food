import { Component } from '@angular/core';
import { Observable, switchMap } from 'rxjs';
import { Order } from '../../dto/order/order-response';
import { ActivatedRoute } from '@angular/router';
import { Router } from '@angular/router';
import { OrderService } from '../../service/order.service';
import { OrderDataService } from '../../service/order-data.service';


@Component({
  selector: 'app-order-confirmation',
  templateUrl: './order-confirmation.component.html',
  styleUrl: './order-confirmation.component.css'
})
export class OrderConfirmationComponent {
  order$ : Observable<Order> | undefined;

  constructor(
    private orderService : OrderService,
    private route : ActivatedRoute,
    private router: Router,
    private orderDataService: OrderDataService

  ) { 
  }
  ngOnInit(): void {
    this.order$ = this.route.paramMap.pipe(
      switchMap(params => {
        const id = Number(params.get('orderId'));
        return this.orderService.getOrder(id);
      })
    );
  }

  goToPayment(order: Order) {
      this.orderDataService.setOrder(order);
      this.router.navigate(['/payment']);
  }

  cancelOrder(order: Order): void {
    if (confirm('Bạn có chắc chắn muốn hủy đơn hàng này?')) {
      this.orderService.cancelOrder(order.orderId).subscribe({
        next: () => {
          alert('Đã hủy đơn hàng thành công!');
          this.router.navigate(['/']);
        },
        error: (error) => {
          console.error('Error canceling order:', error);
          alert('Có lỗi xảy ra khi hủy đơn hàng. Vui lòng thử lại!');
        }
      });
    }
  }
}
