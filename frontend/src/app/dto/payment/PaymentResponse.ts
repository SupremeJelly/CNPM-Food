export interface PaymentResponse {
  transactionId: string;    // Mã giao dịch từ backend trả về
  orderId: number;
  userId: number;
  amount: number;
  method: string;
  status: PaymentStatus;    // Enum trạng thái
  message: string;          // Thông báo kết quả (VD: "Payment successful")
  timestamp: string;        // ISO datetime (VD: "2025-10-04T12:00:00Z")
}

export enum PaymentStatus {
  SUCCESS = 'SUCCESS',
  FAILED = 'FAILED',
  PENDING = 'PENDING'
}