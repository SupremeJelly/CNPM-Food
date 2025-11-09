import { Component, OnInit } from '@angular/core';
import { RestaurantDTO } from '../../../dto/restaurant/RestaurantDTO';
import { MenuItemDTO } from '../../../dto/restaurant/MenuItemDTO';
import { RestaurantService } from '../../../service/restaurant.service';
import { AuthService } from '../../../service/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-my-restaurant',
  templateUrl: './my-restaurant.component.html',
  styleUrl: './my-restaurant.component.css'
})
export class MyRestaurantComponent implements OnInit {
  restaurant: RestaurantDTO | null = null;
  menuItems: MenuItemDTO[] = [];
  restaurantId!: number;
  isEditingRestaurant = false;
  isEditingMenuItem = false;
  editingItem: MenuItemDTO = {} as MenuItemDTO;
  restaurantForm: any = {};

  constructor(
    private restaurantService: RestaurantService,
    private authService: AuthService,
    private router: Router
  ) { }

  ngOnInit(): void {
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser || !currentUser.restaurantId) {
      alert('Bạn không có quyền quản lý nhà hàng');
      this.router.navigate(['/login']);
      return;
    }
    this.restaurantId = currentUser.restaurantId;
    this.loadRestaurant();
    this.loadMenuItems();
  }

  loadRestaurant(): void {
    this.restaurantService.getRestaurantById(this.restaurantId).subscribe({
      next: (data) => { this.restaurant = data; this.restaurantForm = { ...data }; },
      error: (err) => { console.error('Error:', err); alert('Không thể tải thông tin nhà hàng'); }
    });
  }

  loadMenuItems(): void {
    this.restaurantService.getMenuItemsByRestaurantId(this.restaurantId).subscribe({
      next: (data) => { this.menuItems = data; },
      error: (err) => { console.error('Error:', err); }
    });
  }

  startEditRestaurant(): void {
    this.isEditingRestaurant = true;
    this.restaurantForm = { ...this.restaurant };
  }

  cancelEditRestaurant(): void {
    this.isEditingRestaurant = false;
  }

  saveRestaurant(): void {
    const formData = new FormData();
    formData.append('restaurant', JSON.stringify({ name: this.restaurantForm.name, address: this.restaurantForm.address, phone: this.restaurantForm.phone, description: this.restaurantForm.description }));
    this.restaurantService.updateRestaurantInfo(this.restaurantId, formData).subscribe({
      next: (data) => { this.restaurant = data; this.isEditingRestaurant = false; alert('Cập nhật thành công!'); },
      error: (err) => { console.error('Error:', err); alert('Lỗi khi cập nhật'); }
    });
  }

  startAddMenuItem(): void {
    this.isEditingMenuItem = true;
    this.editingItem = { menuItemId: 0, name: '', price: 0, stock: 0, imageUrl: '', restaurantId: this.restaurantId } as MenuItemDTO;
  }

  startEditMenuItem(item: MenuItemDTO): void {
    this.isEditingMenuItem = true;
    this.editingItem = { ...item };
  }

  cancelEditMenuItem(): void {
    this.isEditingMenuItem = false;
  }

  saveMenuItem(): void {
    if (this.editingItem.menuItemId === 0) {
      this.restaurantService.addNewMenuItem(this.editingItem).subscribe({
        next: (data) => { this.menuItems.push(data); this.cancelEditMenuItem(); alert('Thêm món thành công!'); },
        error: (err) => { console.error('Error:', err); alert('Lỗi'); }
      });
    } else {
      this.restaurantService.updateExistingMenuItem(this.editingItem.menuItemId!, this.editingItem).subscribe({
        next: (data) => { const i = this.menuItems.findIndex(x => x.menuItemId === data.menuItemId); if (i !== -1) this.menuItems[i] = data; this.cancelEditMenuItem(); alert('Cập nhật thành công!'); },
        error: (err) => { console.error('Error:', err); alert('Lỗi'); }
      });
    }
  }

  deleteMenuItem(itemId: number): void {
    if (confirm('Bạn có chắc chắn muốn xóa món ăn này?')) {
      this.restaurantService.deleteExistingMenuItem(itemId).subscribe({
        next: () => { this.menuItems = this.menuItems.filter(x => x.menuItemId !== itemId); alert('Xóa thành công!'); },
        error: (err) => { console.error('Error:', err); alert('Lỗi'); }
      });
    }
  }
}
