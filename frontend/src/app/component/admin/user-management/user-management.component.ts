import { Component, OnInit } from '@angular/core';
import { UserService } from '../../../../app/service/user.service';
import { Page } from '../../../dto/Page';
import { UserDTO, UserUpdateDTO } from '../../../../app/dto/auth/UserDTO';
import { EMPTY } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';

@Component({
  selector: 'app-user-management',
  templateUrl: './user-management.component.html',
  styleUrls: ['./user-management.component.css']
})
export class UserManagementComponent implements OnInit {

  users: UserDTO[] = [];
  isLoading: boolean = true;
  errorMessage: string | null = null;

  constructor(private userService: UserService) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.isLoading = true;
    this.errorMessage = null;

    this.userService.getAllUsers(0, 10).subscribe({
      next: (data: Page<UserDTO>) => {
        this.users = data.content;
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Lỗi khi tải user!';
        this.isLoading = false;
        console.error(err);
      }
    });
  }

  viewUserDetails(userId: number): void {
    console.log('View user details:', userId);
    // TODO: Navigate to user details page
  }

  deleteUser(userId: number): void {
    if (confirm('Bạn có chắc chắn muốn vô hiệu hóa người dùng này?')) {
      this.isLoading = true;
      this.errorMessage = null;
      
      this.userService.deactivateUser(userId).subscribe({
        next: () => {
          this.loadUsers(); // Reload the user list
          alert('Đã vô hiệu hóa người dùng thành công');
        },
        error: (err: any) => {
          this.errorMessage = 'Lỗi khi vô hiệu hóa người dùng!';
          this.isLoading = false;
          console.error(err);
        }
      });
    }
  }

  editUser(userId: number): void {
    // Get the current user data using getAllUsers instead of getUsers
    this.userService.getAllUsers(0, 10).pipe(
      tap((page: Page<UserDTO>) => {
        const user = page.content.find((u: UserDTO) => u.userId === userId);
        if (user) {
          // Create update object from current data
          const updateData: UserUpdateDTO = {
            username: user.username,
            email: user.email,
            address: user.address,
            roles: user.roles
          };

          // Here you might want to open a dialog/modal to edit the data
          // For now we'll just prompt for a new username as an example
          const newUsername = prompt('Nhập tên người dùng mới:', user.username);
          if (newUsername && newUsername !== user.username) {
            updateData.username = newUsername;
            
            this.userService.updateUser(userId, updateData).subscribe({
              next: () => {
                this.loadUsers(); // Reload the user list
                alert('Cập nhật thông tin người dùng thành công');
              },
              error: (err: any) => {
                this.errorMessage = 'Lỗi khi cập nhật thông tin người dùng!';
                console.error(err);
              }
            });
          }
        } else {
          this.errorMessage = 'Không tìm thấy thông tin người dùng!';
        }
      }),
      catchError((err: any) => {
        this.errorMessage = 'Lỗi khi tải thông tin người dùng!';
        console.error(err);
        return EMPTY;
      })
    ).subscribe();
}}
