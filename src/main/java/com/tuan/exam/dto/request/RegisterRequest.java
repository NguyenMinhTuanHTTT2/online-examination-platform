package com.tuan.exam.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class RegisterRequest {
    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 4 , max = 50, message = "Tên đăng nhập phải 4 đến 50 ký tự")
    private  String username;

    @NotBlank(message = "Email không được để trống")
    @Email(
            regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
            message = "Email không đúng định dạng"
    )
    private String email;

    @NotBlank(message = "Mật khẩu không được trống")
    @Size(min = 7, message = "Mật khẩu phải có ít nhất 7 ký tự ")
    private String password;

  /*  @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^\\d{10,11}$", message = "Số điện thoại phải là số và có độ dài từ 10 đến 11 chữ số")
    private String phone;*/

    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;
}
