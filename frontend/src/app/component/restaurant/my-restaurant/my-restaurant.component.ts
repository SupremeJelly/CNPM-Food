import { Component, OnInit } from '@angular/core';import { Component, OnInit } from '@angular/core';import { Component, OnInit } from '@angular/core';import { Component, Input, OnInit } from '@angular/core';

import { RestaurantDTO } from '../../../dto/restaurant/RestaurantDTO';

import { MenuItemDTO } from '../../../dto/restaurant/MenuItemDTO';import { RestaurantDTO } from '../../../dto/restaurant/RestaurantDTO';

import { RestaurantService } from '../../../service/restaurant.service';

import { AuthService } from '../../../service/auth.service';import { MenuItemDTO } from '../../../dto/restaurant/MenuItemDTO';import { MenuItem } from '../../../dto/MenuItem';import { MenuItem } from '../../dto/MenuItem';

import { Router } from '@angular/router';

import { RestaurantService } from '../../../service/restaurant.service';

@Component({

  selector: 'app-my-restaurant',import { AuthService } from '../../../service/auth.service';import { MenuItemDTO } from '../../../dto/restaurant/MenuItemDTO';import { MenuItemDTO } from '../../dto/restaurant/MenuItemDTO';

  templateUrl: './my-restaurant.component.html',

  styleUrl: './my-restaurant.component.css'import { Router } from '@angular/router';

})

export class MyRestaurantComponent implements OnInit {import { RestaurantDTO } from '../../../dto/restaurant/RestaurantDTO';import { RestaurantService } from '../../service/restaurant.service';



  restaurant: RestaurantDTO | null = null;@Component({

  menuItems: MenuItemDTO[] = [];

  restaurantId!: number;  selector: 'app-my-restaurant',import { RestaurantService } from '../../../service/restaurant.service';import { ActivatedRoute } from '@angular/router';



  // Edit mode flags  templateUrl: './my-restaurant.component.html',

  isEditingRestaurant = false;

  isEditingMenuItem = false;  styleUrl: './my-restaurant.component.css'import { AuthService } from '../../../service/auth.service';import { CartItem } from '../../dto/CartItem';

  editingItem: MenuItemDTO = {} as MenuItemDTO;

})

  // Form data for restaurant

  restaurantForm: any = {};export class MyRestaurantComponent implements OnInit {import { Router } from '@angular/router';import { CartService } from '../../service/cart.service';



  constructor(

    private restaurantService: RestaurantService,

    private authService: AuthService,  restaurant: RestaurantDTO | null = null;

    private router: Router

  ) { }  menuItems: MenuItemDTO[] = [];



  ngOnInit(): void {  restaurantId!: number;@Component({@Component({

    const currentUser = this.authService.getCurrentUser();

    

    if (!currentUser || !currentUser.restaurantId) {

      alert('Bạn không có quyền quản lý nhà hàng');  // Edit mode flags  selector: 'app-my-restaurant',  selector: 'app-restaurant-detail',

      this.router.navigate(['/login']);

      return;  isEditingRestaurant = false;

    }

  isEditingMenuItem = false;  templateUrl: './my-restaurant.component.html',  templateUrl: './restaurant-detail.component.html',

    this.restaurantId = currentUser.restaurantId;

    this.loadRestaurant();  editingItem: MenuItemDTO = {} as MenuItemDTO;

    this.loadMenuItems();

  }  styleUrl: './my-restaurant.component.css'  styleUrl: './restaurant-detail.component.css'



  loadRestaurant(): void {  // Form data for restaurant

    this.restaurantService.getRestaurantById(this.restaurantId)

      .subscribe({  restaurantForm: any = {};})})

        next: (data) => {

          this.restaurant = data;

          this.restaurantForm = { ...data };

        },  constructor(export class MyRestaurantComponent implements OnInit {export class RestaurantDetailComponent implements OnInit {

        error: (err) => {

          console.error('Error loading restaurant:', err);    private restaurantService: RestaurantService,

          alert('Không thể tải thông tin nhà hàng');

        }    private authService: AuthService,

      });

  }    private router: Router



  loadMenuItems(): void {  ) { }  restaurant: RestaurantDTO | null = null;  menuItems: MenuItemDTO[] = [];

    this.restaurantService.getMenuItemsByRestaurantId(this.restaurantId)

      .subscribe({

        next: (data) => {

          this.menuItems = data;  ngOnInit(): void {  menuItems: MenuItemDTO[] = [];  restaurantId!: number;

        },

        error: (err) => {    // Get restaurant ID from current user

          console.error('Error loading menu items:', err);

        }    const currentUser = this.authService.getCurrentUser();  restaurantId!: number;

      });

  }    



  // Restaurant info management    if (!currentUser || !currentUser.restaurantId) {    constructor(

  editRestaurant(): void {

    this.isEditingRestaurant = true;      alert('Bạn không có quyền quản lý nhà hàng');

    this.restaurantForm = { ...this.restaurant };

  }      this.router.navigate(['/login']);  // Edit mode flags    private restaurantService: RestaurantService, 



  cancelEditRestaurant(): void {      return;

    this.isEditingRestaurant = false;

    this.restaurantForm = { ...this.restaurant };    }  isEditingRestaurant = false;    private route: ActivatedRoute,

  }



  saveRestaurant(): void {

    const formData = new FormData();    this.restaurantId = currentUser.restaurantId;  isEditingMenuItem = false;    private cartService : CartService) { }

    formData.append('restaurant', JSON.stringify({

      name: this.restaurantForm.name,    this.loadRestaurant();

      address: this.restaurantForm.address,

      phone: this.restaurantForm.phone,    this.loadMenuItems();  editingItem: MenuItemDTO = {} as MenuItemDTO;

      description: this.restaurantForm.description

    }));  }



    this.restaurantService.updateRestaurantInfo(this.restaurantId, formData)    ngOnInit(): void {

      .subscribe({

        next: (data) => {  loadRestaurant(): void {

          this.restaurant = data;

          this.isEditingRestaurant = false;    this.restaurantService.getRestaurantById(this.restaurantId)  // Form data for restaurant    this.route.params.subscribe(params => {

          alert('Cập nhật thông tin nhà hàng thành công!');

        },      .subscribe({

        error: (err) => {

          console.error('Error updating restaurant:', err);        next: (data) => {  restaurantForm: any = {};      this.restaurantId = params['id']; 

          alert('Lỗi khi cập nhật thông tin nhà hàng');

        }          this.restaurant = data;

      });

  }          this.restaurantForm = { ...data };      this.loadMenuItems(this.restaurantId);



  // Menu item management        },

  addNewMenuItem(): void {

    this.isEditingMenuItem = true;        error: (err) => {  constructor(    });

    this.editingItem = {

      menuItemId: 0,          console.error('Error loading restaurant:', err);

      name: '',

      price: 0,          alert('Không thể tải thông tin nhà hàng');    private restaurantService: RestaurantService,  }

      stock: 0,

      imageUrl: '',        }

      restaurantId: this.restaurantId

    } as MenuItemDTO;      });    private authService: AuthService,

  }

  }

  editMenuItem(item: MenuItemDTO): void {

    this.isEditingMenuItem = true;    private router: Router  loadMenuItems(restaurantId: number): void {

    this.editingItem = { ...item };

  }  loadMenuItems(): void {



  cancelEditMenuItem(): void {    this.restaurantService.getMenuItemsByRestaurantId(this.restaurantId)  ) { }    this.restaurantService.getMenuItemsByRestaurantId(restaurantId)

    this.isEditingMenuItem = false;

    this.editingItem = {} as MenuItemDTO;      .subscribe({

  }

        next: (data) => {    .subscribe((response: MenuItemDTO[]) => {

  saveMenuItem(): void {

    if (this.editingItem.menuItemId === 0) {          this.menuItems = data;

      // Add new menu item

      this.restaurantService.addNewMenuItem(this.editingItem)        },  ngOnInit(): void {      this.menuItems = response;

        .subscribe({

          next: (data) => {        error: (err) => {

            this.menuItems.push(data);

            this.isEditingMenuItem = false;          console.error('Error loading menu items:', err);    // Get restaurant ID from current user    });

            this.editingItem = {} as MenuItemDTO;

            alert('Thêm món ăn mới thành công!');        }

          },

          error: (err) => {      });    const currentUser = this.authService.getCurrentUser();  }

            console.error('Error adding menu item:', err);

            alert('Lỗi khi thêm món ăn');  }

          }

        });    

    } else {

      // Update existing menu item  // Restaurant info management

      this.restaurantService.updateExistingMenuItem(this.editingItem.menuItemId!, this.editingItem)

        .subscribe({  editRestaurant(): void {    if (!currentUser || !currentUser.restaurantId) {  // addToCart(menuItem: MenuItem) {

          next: (data) => {

            const index = this.menuItems.findIndex(item => item.menuItemId === data.menuItemId);    this.isEditingRestaurant = true;

            if (index !== -1) {

              this.menuItems[index] = data;    this.restaurantForm = { ...this.restaurant };      alert('Bạn không có quyền quản lý nhà hàng');  //   const cart = localStorage.getItem('cart');

            }

            this.isEditingMenuItem = false;  }

            this.editingItem = {} as MenuItemDTO;

            alert('Cập nhật món ăn thành công!');      this.router.navigate(['/login']);  //   const cartItems: CartItem[] = cart ? JSON.parse(cart) : [];

          },

          error: (err) => {  cancelEditRestaurant(): void {

            console.error('Error updating menu item:', err);

            alert('Lỗi khi cập nhật món ăn');    this.isEditingRestaurant = false;      return;    

          }

        });    this.restaurantForm = { ...this.restaurant };

    }

  }  }    }  //   const existingItem = cartItems.find(cartItem => cartItem.menuItem.menuItemId === menuItem.menuItemId);



  deleteMenuItem(menuItemId: number): void {

    if (!confirm('Bạn có chắc chắn muốn xóa món ăn này?')) {

      return;  saveRestaurant(): void {      //   if (existingItem) {

    }

    const formData = new FormData();

    this.restaurantService.deleteExistingMenuItem(menuItemId)

      .subscribe({    formData.append('restaurant', JSON.stringify({    this.restaurantId = currentUser.restaurantId;  //       existingItem.quantity++; // Increase quantity by 1

        next: () => {

          this.menuItems = this.menuItems.filter(item => item.menuItemId !== menuItemId);      name: this.restaurantForm.name,

          alert('Xóa món ăn thành công!');

        },      address: this.restaurantForm.address,    this.loadRestaurant();  //   } else {

        error: (err) => {

          console.error('Error deleting menu item:', err);      phone: this.restaurantForm.phone,

          alert('Lỗi khi xóa món ăn');

        }      description: this.restaurantForm.description    this.loadMenuItems();  //       cartItems.push({ menuItem: menuItem, quantity: 1 }); // Add new item with quantity 1

      });

  }    }));

}

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
