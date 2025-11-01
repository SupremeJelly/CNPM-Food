import { Component, OnInit } from '@angular/core';
import { OrderService } from '../../../service/order.service';
import { Page } from '../../../dto/Page';
import { Order } from '../../../dto/order/order-response';

@Component({
  selector: 'app-order-management',
  templateUrl: './order-management.component.html',
  styleUrl: './order-management.component.css'
})
export class OrderManagementComponent implements OnInit {
  orders: Order[] = [];
  isLoading: boolean = true;
  errorMessage: string | null = null;

  constructor(private orderService: OrderService) {}

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.isLoading = true;
    this.errorMessage = null;

    this.orderService.getAllOrders(0, 20).subscribe({
      next: (data: Page<Order>) => {
        this.orders = data.content;
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = 'Failed to load orders';
        this.isLoading = false;
        console.error('Error loading orders:', error);
      }
    });
  }

  getStatusColor(status: string): string {
    const colors: {[key: string]: string} = {
      'PENDING': 'bg-yellow-100 text-yellow-800',
      'CONFIRMED': 'bg-blue-100 text-blue-800',
      'PREPARING': 'bg-purple-100 text-purple-800',
      'DELIVERING': 'bg-indigo-100 text-indigo-800',
      'DELIVERED': 'bg-green-100 text-green-800',
      'CANCELLED': 'bg-red-100 text-red-800'
    };
    return colors[status] || 'bg-gray-100 text-gray-800';
  }

  getPaymentStatusColor(status: string): string {
    const colors: {[key: string]: string} = {
      'PENDING': 'bg-yellow-100 text-yellow-800',
      'PAID': 'bg-green-100 text-green-800',
      'FAILED': 'bg-red-100 text-red-800'
    };
    return colors[status] || 'bg-gray-100 text-gray-800';
  }

  viewOrderDetails(orderId: number): void {
    console.log('View order details:', orderId);
    // TODO: Navigate to order details page
  }

  updateOrderStatus(orderId: number): void {
    console.log('Update order status:', orderId);
    // TODO: Open status update modal
    alert('Update order status functionality will be implemented soon');
  }

  cancelOrder(orderId: number): void {
    if (confirm('Are you sure you want to cancel this order?')) {
      console.log('Cancel order:', orderId);
      // TODO: Call cancel order API
      alert('Cancel order functionality will be implemented soon');
    }
  }
}
