import { Component, OnInit } from '@angular/core';
import { RestaurantService } from '../../../../app/service/restaurant.service';
import { MenuItemDTO, MenuItemUpdateDTO } from '../../../../app/dto/restaurant/MenuItemDTO';
import { RestaurantDTO } from '../../../../app/dto/restaurant/RestaurantDTO';
import { Page } from '../../../dto/Page';

@Component({
  selector: 'app-menu-management',
  templateUrl: './menu-management.component.html',
  styleUrls: ['./menu-management.component.css']
})
export class MenuManagementComponent implements OnInit {
  restaurants: RestaurantDTO[] = [];
  menuItems: MenuItemDTO[] = [];
  selectedRestaurant: RestaurantDTO | null = null;
  selectedMenuItem: MenuItemDTO | null = null;
  selectedFile: File | null = null;
  isLoading: boolean = true;
  errorMessage: string | null = null;

  constructor(private restaurantService: RestaurantService) {}

  ngOnInit(): void {
    this.loadRestaurants();
  }

  loadRestaurants(): void {
    this.isLoading = true;
    this.restaurantService.getAllRestaurants(0, 100).subscribe({
      next: (data: Page<RestaurantDTO>) => {
        this.restaurants = data.content;
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Lỗi khi tải danh sách nhà hàng!';
        this.isLoading = false;
        console.error(err);
      }
    });
  }

  loadMenuItems(restaurantId: number): void {
    this.isLoading = true;
    this.restaurantService.getMenuItemsByRestaurantId(restaurantId).subscribe({
      next: (items: MenuItemDTO[]) => {
        this.menuItems = items;
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Lỗi khi tải danh sách món ăn!';
        this.isLoading = false;
        console.error(err);
      }
    });
  }

  onRestaurantSelect(value: string): void {
    // value is the option value (index as string) or empty string
    if (!value) {
      this.selectedRestaurant = null;
      this.menuItems = [];
      return;
    }

    const idx = Number(value);
    const restaurant = this.restaurants[idx];
    if (restaurant) {
      this.selectedRestaurant = restaurant;
      this.loadMenuItems(restaurant.restaurantId);
    } else {
      this.errorMessage = 'Không tìm thấy nhà hàng được chọn';
    }
  }

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;
    }
  }

  editMenuItem(menuItem: MenuItemDTO): void {
    if (!this.selectedRestaurant) return;

    this.selectedMenuItem = menuItem;
    // Trong thực tế, bạn sẽ mở một dialog/modal để chỉnh sửa
    const updateData: MenuItemUpdateDTO = {
      name: prompt('Tên món:', menuItem.name) || menuItem.name,
      description: prompt('Mô tả:', menuItem.description) || menuItem.description,
      price: Number(prompt('Giá:', menuItem.price.toString())) || menuItem.price,
      category: prompt('Danh mục:', menuItem.category) || menuItem.category
    };

    if (updateData.name !== menuItem.name ||
        updateData.description !== menuItem.description ||
        updateData.price !== menuItem.price ||
        updateData.category !== menuItem.category) {

      this.restaurantService.updateMenuItem(
        this.selectedRestaurant.restaurantId,
        menuItem.menuItemId,
        updateData,
        this.selectedFile || undefined
      ).subscribe({
        next: () => {
          this.loadMenuItems(this.selectedRestaurant!.restaurantId);
          alert('Cập nhật món ăn thành công!');
          this.selectedMenuItem = null;
          this.selectedFile = null;
        },
        error: (err) => {
          this.errorMessage = 'Lỗi khi cập nhật món ăn!';
          console.error(err);
        }
      });
    }
  }

  toggleMenuItemAvailability(menuItem: MenuItemDTO): void {
    if (!this.selectedRestaurant) return;

    this.restaurantService.toggleMenuItemAvailability(
      this.selectedRestaurant.restaurantId,
      menuItem.menuItemId
    ).subscribe({
      next: () => {
        // Availability UI removed — no frontend state to toggle.
        // If backend returns changes you can refresh the list instead.
        this.loadMenuItems(this.selectedRestaurant!.restaurantId);
      },
      error: (err) => {
        this.errorMessage = 'Lỗi khi thay đổi trạng thái món ăn!';
        console.error(err);
      }
    });
  }
}