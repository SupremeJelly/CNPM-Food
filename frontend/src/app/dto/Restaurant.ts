export interface Restaurant {
    restaurantId: number;
    name: string;
    address: string;
    image?: string;
    imageUrl?: string;  // Thêm field này
    description?: string;  // Thêm field này
    phone?: string;  // Thêm field này
    cuisine?: string;  // Thêm field này (loại ẩm thực)
  }