import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { environment } from '../../environments/enviroment';
import { PaymentRequest } from '../dto/payment/PaymentRequest';
import { PaymentResponse } from '../dto/payment/PaymentResponse';

@Injectable({
  providedIn: 'root'
})
export class PaymentService {
  private baseUrl = environment.baseUrl + '/payments';

  constructor(private http: HttpClient) {}

  // Gửi thông tin thanh toán lên backend
  processPayment(paymentData: PaymentRequest): Observable<PaymentResponse> {
    return this.http.post<PaymentResponse>(`${this.baseUrl}`, paymentData)
      .pipe(
        catchError(this.handleError)
      );
  }

  // Hàm xử lý lỗi tương tự AuthService
  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'Payment failed.';
    if (error.error && error.error.message) {
      errorMessage = error.error.message;
    }
    return throwError(() => new Error(errorMessage));
  }
}
