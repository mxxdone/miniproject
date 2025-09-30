package com.mxxdone.miniproject.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignUpRequestDto(
        @NotBlank(message = "아이디는 필수 항목입니다.")
        @Size(min = 4, max = 12, message = "아이디는 4자 이상 12자 이하로 입력해주세요.")
        @Pattern(regexp = "^[a-z0-9]+$", message = "아이디는 영문 소문자와 숫자만 사용할 수 있습니다.")
        String username,

        @NotBlank(message = "비밀번호는 필수 항목입니다.")
        @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요.")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
                message = "비밀번호는 영문, 숫자, 특수문자를 각각 하나 이상 포함해야 합니다.")
        String password,

        @NotBlank(message = "닉네임은 필수 항목입니다.")
        @Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하로 입력해주세요.")
        @Pattern(regexp = "^[a-zA-Z0-9가-힣]+$", message = "닉네임은 한글, 영문, 숫자만 사용할 수 있습니다.")
        String nickname,

        @NotBlank(message = "이메일은 필수 항목입니다.")
        @Email(message = "유효한 이메일 형식이 아닙니다.")
        String email
){
}
