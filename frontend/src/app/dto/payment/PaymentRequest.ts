export interface PaymentRequest {
  orderId: number;
  userId: number;
  amount: number;           // Dùng number thay vì BigDecimal
  method: PaymentMethod;    // Enum cho loại thanh toán
}

export enum PaymentMethod {
  CREDIT_CARD = 'CREDIT_CARD',
  BANK_TRANSFER = 'BANK_TRANSFER',
  CASH_ON_DELIVERY = 'CASH_ON_DELIVERY'
}