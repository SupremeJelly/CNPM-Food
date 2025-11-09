import { Component, OnInit } from '@angular/core';import { Component, OnInit } from '@angular/core';import { Component, Input, OnInit } from '@angular/core';

import { RestaurantDTO } from '../../../dto/restaurant/RestaurantDTO';

import { MenuItemDTO } from '../../../dto/restaurant/MenuItemDTO';import { MenuItem } from '../../../dto/MenuItem';import { MenuItem } from '../../dto/MenuItem';

import { RestaurantService } from '../../../service/restaurant.service';

import { AuthService } from '../../../service/auth.service';import { MenuItemDTO } from '../../../dto/restaurant/MenuItemDTO';import { MenuItemDTO } from '../../dto/restaurant/MenuItemDTO';

import { Router } from '@angular/router';

import { RestaurantDTO } from '../../../dto/restaurant/RestaurantDTO';import { RestaurantService } from '../../service/restaurant.service';

@Component({

  selector: 'app-my-restaurant',import { RestaurantService } from '../../../service/restaurant.service';import { ActivatedRoute } from '@angular/router';

  templateUrl: './my-restaurant.component.html',

  styleUrl: './my-restaurant.component.css'import { AuthService } from '../../../service/auth.service';import { CartItem } from '../../dto/CartItem';

})

export class MyRestaurantComponent implements OnInit {import { Router } from '@angular/router';import { CartService } from '../../service/cart.service';



  restaurant: RestaurantDTO | null = null;

  menuItems: MenuItemDTO[] = [];

  restaurantId!: number;@Component({@Component({



  // Edit mode flags  selector: 'app-my-restaurant',  selector: 'app-restaurant-detail',

  isEditingRestaurant = false;

  isEditingMenuItem = false;  templateUrl: './my-restaurant.component.html',  templateUrl: './restaurant-detail.component.html',

  editingItem: MenuItemDTO = {} as MenuItemDTO;

  styleUrl: './my-restaurant.component.css'  styleUrl: './restaurant-detail.component.css'

  // Form data for restaurant

  restaurantForm: any = {};})})



  constructor(export class MyRestaurantComponent implements OnInit {export class RestaurantDetailComponent implements OnInit {

    private restaurantService: RestaurantService,

    private authService: AuthService,

    private router: Router

  ) { }  restaurant: RestaurantDTO | null = null;  menuItems: MenuItemDTO[] = [];



  ngOnInit(): void {  menuItems: MenuItemDTO[] = [];  restaurantId!: number;

    // Get restaurant ID from current user

    const currentUser = this.authService.getCurrentUser();  restaurantId!: number;

    

    if (!currentUser || !currentUser.restaurantId) {    constructor(

      alert('Bạn không có quyền quản lý nhà hàng');

      this.router.navigate(['/login']);  // Edit mode flags    private restaurantService: RestaurantService, 

      return;

    }  isEditingRestaurant = false;    private route: ActivatedRoute,



    this.restaurantId = currentUser.restaurantId;  isEditingMenuItem = false;    private cartService : CartService) { }

    this.loadRestaurant();

    this.loadMenuItems();  editingItem: MenuItemDTO = {} as MenuItemDTO;

  }

    ngOnInit(): void {

  loadRestaurant(): void {

    this.restaurantService.getRestaurantById(this.restaurantId)  // Form data for restaurant    this.route.params.subscribe(params => {

      .subscribe({

        next: (data) => {  restaurantForm: any = {};      this.restaurantId = params['id']; 

          this.restaurant = data;

          this.restaurantForm = { ...data };      this.loadMenuItems(this.restaurantId);

        },

        error: (err) => {  constructor(    });

          console.error('Error loading restaurant:', err);

          alert('Không thể tải thông tin nhà hàng');    private restaurantService: RestaurantService,  }

        }

      });    private authService: AuthService,

  }

    private router: Router  loadMenuItems(restaurantId: number): void {

  loadMenuItems(): void {

    this.restaurantService.getMenuItemsByRestaurantId(this.restaurantId)  ) { }    this.restaurantService.getMenuItemsByRestaurantId(restaurantId)

      .subscribe({

        next: (data) => {    .subscribe((response: MenuItemDTO[]) => {

          this.menuItems = data;

        },  ngOnInit(): void {      this.menuItems = response;

        error: (err) => {

          console.error('Error loading menu items:', err);    // Get restaurant ID from current user    });

        }

      });    const currentUser = this.authService.getCurrentUser();  }

  }

    

  // Restaurant info management

  editRestaurant(): void {    if (!currentUser || !currentUser.restaurantId) {  // addToCart(menuItem: MenuItem) {

    this.isEditingRestaurant = true;

    this.restaurantForm = { ...this.restaurant };      alert('Bạn không có quyền quản lý nhà hàng');  //   const cart = localStorage.getItem('cart');

  }

      this.router.navigate(['/login']);  //   const cartItems: CartItem[] = cart ? JSON.parse(cart) : [];

  cancelEditRestaurant(): void {

    this.isEditingRestaurant = false;      return;    

    this.restaurantForm = { ...this.restaurant };

  }    }  //   const existingItem = cartItems.find(cartItem => cartItem.menuItem.menuItemId === menuItem.menuItemId);



  saveRestaurant(): void {      //   if (existingItem) {

    const formData = new FormData();

    formData.append('restaurant', JSON.stringify({    this.restaurantId = currentUser.restaurantId;  //       existingItem.quantity++; // Increase quantity by 1

      name: this.restaurantForm.name,

      address: this.restaurantForm.address,    this.loadRestaurant();  //   } else {

      phone: this.restaurantForm.phone,

      description: this.restaurantForm.description    this.loadMenuItems();  //       cartItems.push({ menuItem: menuItem, quantity: 1 }); // Add new item with quantity 1

    }));

  }  //   }

    this.restaurantService.updateRestaurantInfo(this.restaurantId, formData)

      .subscribe({

        next: (data) => {

          this.restaurant = data;  loadRestaurant(): void {  //   localStorage.setItem('cart', JSON.stringify(cartItems));

          this.isEditingRestaurant = false;

          alert('Cập nhật thông tin nhà hàng thành công!');    this.restaurantService.getRestaurantById(this.restaurantId)  // }

        },

        error: (err) => {      .subscribe((response: RestaurantDTO) => {

          console.error('Error updating restaurant:', err);

          alert('Lỗi khi cập nhật thông tin nhà hàng');        this.restaurant = response;  addToCart(menuItemDto: MenuItemDTO) {

        }

      });        this.restaurantForm = { ...response }; // Copy for editing    // Map MenuItemDTO -> MenuItem shape expected by CartItem

  }

      });    const menuItem: MenuItem = {

  // Menu item management

  addNewMenuItem(): void {  }      menuItemId: menuItemDto.menuItemId,

    this.isEditingMenuItem = true;

    this.editingItem = {      name: menuItemDto.name,

      menuItemId: 0,

      name: '',  loadMenuItems(): void {      price: menuItemDto.price,

      price: 0,

      stock: 0,    this.restaurantService.getMenuItemsByRestaurantId(this.restaurantId)      restaurantId: menuItemDto.restaurantId,

      imageUrl: '',

      restaurantId: this.restaurantId      .subscribe((response: MenuItemDTO[]) => {      stock: menuItemDto.stock ?? 0,

    } as MenuItemDTO;

  }        this.menuItems = response;      imageUrl: menuItemDto.imageUrl



  editMenuItem(item: MenuItemDTO): void {      });    };

    this.isEditingMenuItem = true;

    this.editingItem = { ...item };  }

  }

    const cartItem: CartItem = {

  cancelEditMenuItem(): void {

    this.isEditingMenuItem = false;  // ===== Restaurant Info Management =====      menuItem: menuItem,

    this.editingItem = {} as MenuItemDTO;

  }        quantity: 1



  saveMenuItem(): void {  startEditRestaurant(): void {    };

    if (this.editingItem.menuItemId === 0) {

      // Add new menu item    this.isEditingRestaurant = true;

      this.restaurantService.addNewMenuItem(this.editingItem)

        .subscribe({    this.restaurantForm = { ...this.restaurant };    this.cartService.addToCart(cartItem);

          next: (data) => {

            this.menuItems.push(data);  }  }

            this.isEditingMenuItem = false;

            this.editingItem = {} as MenuItemDTO;}

            alert('Thêm món ăn mới thành công!');

          },  cancelEditRestaurant(): void {

          error: (err) => {    this.isEditingRestaurant = false;

            console.error('Error adding menu item:', err);    this.restaurantForm = { ...this.restaurant };

            alert('Lỗi khi thêm món ăn');  }

          }

        });  saveRestaurant(): void {

    } else {    this.restaurantService.updateRestaurantInfo(this.restaurantId, this.restaurantForm)

      // Update existing menu item      .subscribe({

      this.restaurantService.updateExistingMenuItem(this.editingItem.menuItemId!, this.editingItem)        next: (response) => {

        .subscribe({          this.restaurant = response;

          next: (data) => {          this.isEditingRestaurant = false;

            const index = this.menuItems.findIndex(item => item.menuItemId === data.menuItemId);          alert('Cập nhật thông tin nhà hàng thành công!');

            if (index !== -1) {        },

              this.menuItems[index] = data;        error: (error) => {

            }          console.error('Error updating restaurant:', error);

            this.isEditingMenuItem = false;          alert('Có lỗi xảy ra khi cập nhật thông tin nhà hàng');

            this.editingItem = {} as MenuItemDTO;        }

            alert('Cập nhật món ăn thành công!');      });

          },  }

          error: (err) => {

            console.error('Error updating menu item:', err);  // ===== Menu Item Management =====

            alert('Lỗi khi cập nhật món ăn');  

          }  startAddMenuItem(): void {

        });    this.editingItem = {

    }      menuItemId: 0,

  }      name: '',

      description: '',

  deleteMenuItem(menuItemId: number): void {      price: 0,

    if (!confirm('Bạn có chắc chắn muốn xóa món ăn này?')) {      imageUrl: '',

      return;      restaurantId: this.restaurantId,

    }      stock: 0,

      category: '',

    this.restaurantService.deleteExistingMenuItem(menuItemId)      available: true

      .subscribe({    };

        next: () => {    this.isEditingMenuItem = true;

          this.menuItems = this.menuItems.filter(item => item.menuItemId !== menuItemId);  }

          alert('Xóa món ăn thành công!');

        },  startEditMenuItem(item: MenuItemDTO): void {

        error: (err) => {    this.editingItem = { ...item };

          console.error('Error deleting menu item:', err);    this.isEditingMenuItem = true;

          alert('Lỗi khi xóa món ăn');  }

        }

      });  cancelEditMenuItem(): void {

  }    this.isEditingMenuItem = false;

}    this.editingItem = {} as MenuItemDTO;

  }

  saveMenuItem(): void {
    if (this.editingItem.menuItemId === 0) {
      // Create new
      this.restaurantService.addNewMenuItem(this.editingItem)
        .subscribe({
          next: () => {
            this.loadMenuItems();
            this.cancelEditMenuItem();
            alert('Thêm món ăn thành công!');
          },
          error: (error) => {
            console.error('Error creating menu item:', error);
            alert('Có lỗi xảy ra khi thêm món ăn');
          }
        });
    } else {
      // Update existing
      this.restaurantService.updateExistingMenuItem(this.editingItem.menuItemId, this.editingItem)
        .subscribe({
          next: () => {
            this.loadMenuItems();
            this.cancelEditMenuItem();
            alert('Cập nhật món ăn thành công!');
          },
          error: (error) => {
            console.error('Error updating menu item:', error);
            alert('Có lỗi xảy ra khi cập nhật món ăn');
          }
        });
    }
  }

  deleteMenuItem(itemId: number): void {
    if (confirm('Bạn có chắc chắn muốn xóa món ăn này?')) {
      this.restaurantService.deleteExistingMenuItem(itemId)
        .subscribe({
          next: () => {
            this.loadMenuItems();
            alert('Xóa món ăn thành công!');
          },
          error: (error) => {
            console.error('Error deleting menu item:', error);
            alert('Có lỗi xảy ra khi xóa món ăn');
          }
        });
    }
  }
}
