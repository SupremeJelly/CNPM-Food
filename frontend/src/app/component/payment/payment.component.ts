  import { Component, OnInit } from '@angular/core';
  import { Router } from '@angular/router';
  import { OrderService } from '../../service/order.service';
  // import { OrderRequest } from '../../dto/order/order-request';
  // import { CartService } from '../../service/cart.service';
  // import { CartItem } from '../../dto/CartItem';
  import { FormBuilder, FormGroup, Validators } from '@angular/forms';
  import { Observable, take } from 'rxjs';
  import { environment } from '../../../environments/enviroment';
  import { UserService } from '../../service/user.service';
  // import { AuthService } from '../../service/auth.service';
  import { ActivatedRoute } from '@angular/router';
  import { OrderDataService } from '../../service/order-data.service';
  import { Order } from '../../dto/order/order-response';
  import { PaymentService } from '../../service/payment.service';
  import { PaymentMethod } from '../../dto/payment/PaymentRequest';

  @Component({
    selector: 'app-shipping',
    templateUrl: './payment.component.html',
    styleUrl: './payment.component.css'
  })
  export class PaymentComponent implements OnInit {

    // cartItems$: Observable<CartItem[]> | undefined;
    // orderId!: number;
    // total!: number;
    // status!: string;
    order!: Order | null;
    // total$: Observable<number> | undefined;
    baseUrl = environment.baseUrl;
    userId! : number ;
    paymentError : string ='';
    paymentForm!: FormGroup;

    constructor(
      private fb: FormBuilder,
      private router: Router,            // nếu cần điều hướng
      private orderService: OrderService, // nếu cần gọi API tạo đơn hàng hoặc lấy lịch sử đơn hàng
      // private cartService: CartService,   // để lấy giỏ hàng và tổng tiền
      private userService: UserService,   // để lấy thông tin user
      // private authService : AuthService,
      private route: ActivatedRoute,
      private orderDataService: OrderDataService,
      private paymentService: PaymentService,
    ) { }

    ngOnInit(): void {
      this.orderDataService.order$.pipe(take(1)).subscribe(order => {
        this.order = order;
        console.log('Received order:', order);
      });

      this.paymentForm = this.fb.group({
        paymentMethod: ['', Validators.required],
        paymentProvider: ['']
      });

      // Nếu chọn “card” → yêu cầu chọn provider
      this.paymentForm.get('paymentMethod')?.valueChanges.subscribe(method => {
        const providerControl = this.paymentForm.get('paymentProvider');
        if (method === 'card') {
          providerControl?.setValidators(Validators.required);
        } else {
          providerControl?.clearValidators();
        }
        providerControl?.updateValueAndValidity();
      });

      // Lấy user id nếu cần
      this.getUserId();
    }

    getUserId() {
      this.userService.user$.pipe(take(1)).subscribe(user => {
        this.userId = user?.userId!;
        console.log(this.userId);
      });
    }

    onSubmit(): void {
      if (!this.order) return;

      const selectedMethod = this.mapPaymentMethod();
      const paymentData = {
        userId: this.order.userId,
        orderId: this.order.orderId,
        amount: this.order.totalAmount,
        method: selectedMethod
      };

       console.log('Payment payload:', paymentData);

      this.paymentService.processPayment(paymentData).subscribe({
        next: res => {
          alert('Payment created successfully!');
          console.log(res);
        },
        error: err => {
          console.error(err);
          alert('Payment failed.');
        }
      });
    }

    // map từ radio button sang enum backend
    mapPaymentMethod(): PaymentMethod {
      const paymentMethod = this.paymentForm.value.paymentMethod;
      const provider = this.paymentForm.value.paymentProvider;

      // if (paymentMethod === 'cod') return 'CASH_ON_DELIVERY';
      // if (paymentMethod === 'card') {
      //   switch (provider) {
      //     case 'zalopay': return 'ZALOPAY';
      //     case 'paypal': return 'PAYPAL';
      //     case 'bank': return 'BANK_TRANSFER';
      //     default: return 'CREDIT_CARD';
      //   }
      // }

      if (paymentMethod === 'cod') return PaymentMethod.CASH_ON_DELIVERY;
      if (paymentMethod === 'card') return PaymentMethod.CREDIT_CARD;
      return PaymentMethod.CASH_ON_DELIVERY;
    }

    // // map từ radio button sang enum backend
    // mapPaymentMethod(): string {
    //   const paymentMethod = this.paymentForm.value.paymentMethod;
    //   const provider = this.paymentForm.value.paymentProvider;

    //   if (paymentMethod === 'cod') return 'CASH_ON_DELIVERY';
    //   if (paymentMethod === 'card') {
    //     switch (provider) {
    //       case 'zalopay': return 'ZALOPAY';
    //       case 'paypal': return 'PAYPAL';
    //       case 'bank': return 'BANK_TRANSFER';
    //       default: return 'CREDIT_CARD';
    //     }
    //   }
    //   return 'CASH_ON_DELIVERY';
    // }

  }


    // ngOnInit(): void {
    //   this.getUserId();
    //   // this.cartItems$ = this.cartService.getCartItems();
    //   // this.total$ = this.cartItems$.pipe(
    //   //   map(items => items.reduce((total, item) => total + item.menuItem.price * item.quantity, 0))
    //   // );
    // }


    // ngOnInit(): void {
    //   this.route.queryParams.subscribe(params => {
    //     this.orderId = params['orderId'];
    //     this.total = params['total'];
    //     this.status = params['status'];
    //     console.log("Received order:", this.orderId, this.total, this.status);
    //   });
    // }

    // getUserId() {
    //   this.userService.user$.subscribe(user => {
    //     this.userId = user?.userId!;
    //     console.log(this.userId);
    //   });

    // }

      // payment(): void {
    //   if(this.authService.isLoggedIn()) {
    //     this.router.navigate(['/payment']);
    //   }else{
    //     this.router.navigate(['/login'], { queryParams: { returnUrl: '/payment' } });
    //   }
    // }
