package com.fer.hr.springsecurityjwtdemo.mapper;

import com.fer.hr.springsecurityjwtdemo.dto.response.UserResponseDto;
import com.fer.hr.springsecurityjwtdemo.entity.User;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponseDto toDto(User user);

    User toEntity(UserResponseDto userDTO);
}
