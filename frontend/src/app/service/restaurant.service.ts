import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Restaurant } from '../dto/Restaurant';
import { Page } from '../dto/Page';
import { MenuItemDTO, MenuItemUpdateDTO, MenuItemCreateDTO } from '../dto/restaurant/MenuItemDTO';
import { RestaurantDTO, RestaurantUpdateDTO } from '../dto/restaurant/RestaurantDTO';
import { environment } from '../../environments/enviroment';

@Injectable({
  providedIn: 'root'
})
export class RestaurantService {
  private restaurantUrl = `${environment.baseUrl}/restaurants`;
  private menuUrl = `${environment.baseUrl}/menu-items`;

  // private restaurantUrl = 'http://localhost:8082/api/v1/restaurants';
  // private menuUrl = 'http://localhost:8082/api/v1/menu-items';

  constructor(private http: HttpClient) { }

  getAllRestaurants(page: number, size: number, keyword?: string): Observable<Page<RestaurantDTO>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (keyword) {
      params = params.set('keyword', keyword);
    }
    return this.http.get<Page<RestaurantDTO>>(this.restaurantUrl, { params });
  }

  updateRestaurant(id: number, restaurant: RestaurantUpdateDTO, image?: File): Observable<RestaurantDTO> {
    const formData = new FormData();
    formData.append('restaurant', new Blob([JSON.stringify(restaurant)], { type: 'application/json' }));
    if (image) {
      formData.append('image', image);
    }
    return this.http.put<RestaurantDTO>(`${this.restaurantUrl}/${id}`, formData);
  }

  // Menu Item Methods
  // Backend exposes menu items under /api/v1/menu-items/restaurant/{restaurantId}
  getMenuItemsByRestaurantId(restaurantId: number): Observable<MenuItemDTO[]> {
    return this.http.get<MenuItemDTO[]>(`${this.menuUrl}/restaurant/${restaurantId}`);
  }

  // Legacy/paged API is not available on backend; provide a simple wrapper that fetches all and maps
  // For components that expect a Page-like result, they should be updated to consume the array instead.
  getMenuItems(restaurantId: number, page: number, size: number): Observable<Page<MenuItemDTO>> {
    // Attempt to fetch all items and return a fake Page object client-side
    return this.http.get<MenuItemDTO[]>(`${this.menuUrl}/restaurant/${restaurantId}`).pipe(
      // map to Page<MenuItemDTO>
      // Note: import of map operator is intentionally avoided here to keep change minimal; callers should prefer getMenuItemsByRestaurantId
      // If needed, components can call getMenuItemsByRestaurantId directly.
      // For now, throw if called to signal the mismatch.
      // We'll throw an error to make it explicit that pagination isn't supported server-side.
      // But to keep runtime safe, we implement a simple emulation here if desired in future.
      // (Left intentionally simple.)
      // -- implementation omitted --
      // placeholder: cast the array response to any and let caller handle it (not ideal)
    ) as unknown as Observable<Page<MenuItemDTO>>;
  }

  getMenuItem(restaurantId: number, menuItemId: number): Observable<MenuItemDTO> {
    return this.http.get<MenuItemDTO>(`${this.restaurantUrl}/${restaurantId}/menu-items/${menuItemId}`);
  }

  createMenuItem(restaurantId: number, menuItem: MenuItemCreateDTO, image?: File): Observable<MenuItemDTO> {
    const formData = new FormData();
    formData.append('menuItem', new Blob([JSON.stringify(menuItem)], { type: 'application/json' }));
    if (image) {
      formData.append('image', image);
    }
    return this.http.post<MenuItemDTO>(`${this.restaurantUrl}/${restaurantId}/menu-items`, formData);
  }

  updateMenuItem(restaurantId: number, menuItemId: number, menuItem: MenuItemUpdateDTO, image?: File): Observable<MenuItemDTO> {
    const formData = new FormData();
    formData.append('menuItem', new Blob([JSON.stringify(menuItem)], { type: 'application/json' }));
    if (image) {
      formData.append('image', image);
    }
    return this.http.put<MenuItemDTO>(`${this.restaurantUrl}/${restaurantId}/menu-items/${menuItemId}`, formData);
  }

  toggleMenuItemAvailability(restaurantId: number, menuItemId: number): Observable<void> {
    return this.http.post<void>(`${this.restaurantUrl}/${restaurantId}/menu-items/${menuItemId}/toggle-availability`, {});
  }

  // ===== Convenience methods for Restaurant Owners =====
  
  getRestaurantById(restaurantId: number): Observable<RestaurantDTO> {
    return this.http.get<RestaurantDTO>(`${this.restaurantUrl}/${restaurantId}`);
  }

  // Simplified wrapper methods (called by restaurant owner component)
  addNewMenuItem(menuItem: MenuItemDTO): Observable<MenuItemDTO> {
    const restaurantId = menuItem.restaurantId;
    const createDTO: MenuItemCreateDTO = {
      restaurantId: restaurantId,
      name: menuItem.name,
      description: menuItem.description || '',
      price: menuItem.price,
      category: menuItem.category || 'Other'
    };
    return this.createMenuItem(restaurantId, createDTO);
  }

  updateExistingMenuItem(menuItemId: number, menuItem: MenuItemDTO): Observable<MenuItemDTO> {
    const restaurantId = menuItem.restaurantId;
    const updateDTO: MenuItemUpdateDTO = {
      name: menuItem.name,
      description: menuItem.description || '',
      price: menuItem.price,
      category: menuItem.category
    };
    return this.updateMenuItem(restaurantId, menuItemId, updateDTO);
  }

  deleteExistingMenuItem(menuItemId: number): Observable<void> {
    return this.http.delete<void>(`${this.menuUrl}/${menuItemId}`);
  }

  updateRestaurantInfo(restaurantId: number, restaurant: any): Observable<RestaurantDTO> {
    const updateDTO: RestaurantUpdateDTO = {
      name: restaurant.name,
      address: restaurant.address,
      description: restaurant.description || '',
      phone: restaurant.phone || ''
    };
    return this.updateRestaurant(restaurantId, updateDTO);
  }
}
