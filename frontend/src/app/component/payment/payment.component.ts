import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { OrderService } from '../../service/order.service';
import { OrderRequest } from '../../dto/order/order-request';
import { CartService } from '../../service/cart.service';
import { CartItem } from '../../dto/CartItem';
import { Observable, map, switchMap, take } from 'rxjs';
import { environment } from '../../../environments/enviroment';
import { UserService } from '../../service/user.service';

@Component({
  selector: 'app-shipping',
  templateUrl: './shipping.component.html',
  styleUrl: './shipping.component.css'
})
export class PaymentComponent implements OnInit {

  cartItems$: Observable<CartItem[]> | undefined;
  total$: Observable<number> | undefined;
  baseUrl = environment.baseUrl;
  userId! : number ;
  constructor(
    private router: Router,            // nếu cần điều hướng
    private orderService: OrderService, // nếu cần gọi API tạo đơn hàng hoặc lấy lịch sử đơn hàng
    private cartService: CartService,   // để lấy giỏ hàng và tổng tiền
    private userService: UserService,   // để lấy thông tin user
  ) { }

  ngOnInit(): void {
    this.getUserId();
    this.cartItems$ = this.cartService.getCartItems();
    this.total$ = this.cartItems$.pipe(
      map(items => items.reduce((total, item) => total + item.menuItem.price * item.quantity, 0))
    );
  }

  // onSubmit() {
  //   if (this.shippingForm.valid) {
  //     this.cartItems$?.pipe(
  //       take(1), // Only take the first emission from cartItems$
  //       map(items => items.map(cartItem => ({  // Convert CartIteam from local to OrderItem
  //         menuItemId: cartItem.menuItem.menuItemId,
  //         quantity: cartItem.quantity
  //       }))),
  //       switchMap(orderItems => {  // Switch to the orderService.createOrder Observable
  //         const orderRequest: OrderRequest = {
  //           userId: this.userId,
  //           ...this.shippingForm.value,
  //           items: orderItems
  //         };
  //         return this.orderService.createOrder(orderRequest); // This returns the Observable from the orderService
  //       })
  //     ).subscribe({
  //       next: (order) => {
  //         this.router.navigate(['/order-confirmation', order.orderId]);
  //         this.cartService.clearCart();
  //       },
  //       error: (err) => {
  //         console.error("Error creating order:", err); // Handle errors gracefully
  //       }
  //     });
  //   }
  // }

  getUserId() {
    this.userService.user$.subscribe(user => {
      this.userId = user?.userId!;
      console.log(this.userId);
    });
  }

}
