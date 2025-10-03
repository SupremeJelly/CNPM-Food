import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { OrderService } from '../../service/order.service';
import { OrderRequest } from '../../dto/order/order-request';
// import { CartService } from '../../service/cart.service';
// import { CartItem } from '../../dto/CartItem';
import { Observable, take } from 'rxjs';
import { environment } from '../../../environments/enviroment';
import { UserService } from '../../service/user.service';
import { AuthService } from '../../service/auth.service';


@Component({
  selector: 'app-shipping',
  templateUrl: './payment.component.html',
  styleUrl: './payment.component.css'
})
export class PaymentComponent implements OnInit {

  // cartItems$: Observable<CartItem[]> | undefined;
  total$: Observable<number> | undefined;
  baseUrl = environment.baseUrl;
  userId! : number ;
  paymentError : string ='';
  constructor(
    private router: Router,            // nếu cần điều hướng
    private orderService: OrderService, // nếu cần gọi API tạo đơn hàng hoặc lấy lịch sử đơn hàng
    // private cartService: CartService,   // để lấy giỏ hàng và tổng tiền
    private userService: UserService,   // để lấy thông tin user
    private authService : AuthService,
  ) { }

  ngOnInit(): void {
    this.getUserId();
    // this.cartItems$ = this.cartService.getCartItems();
    // this.total$ = this.cartItems$.pipe(
    //   map(items => items.reduce((total, item) => total + item.menuItem.price * item.quantity, 0))
    // );
  }

  // getUserId() {
  //   this.userService.user$.subscribe(user => {
  //     this.userId = user?.userId!;
  //     console.log(this.userId);
  //   });

  // }

  getUserId() {
    this.userService.user$.pipe(take(1)).subscribe(user => {
      this.userId = user?.userId!;
      console.log(this.userId);
    });
  }

  // payment(): void {
  //   if(this.authService.isLoggedIn()) {
  //     this.router.navigate(['/payment']);
  //   }else{
  //     this.router.navigate(['/login'], { queryParams: { returnUrl: '/payment' } });
  //   }
  // }
}
