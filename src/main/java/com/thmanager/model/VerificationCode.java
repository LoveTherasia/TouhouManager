package com.thmanager.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

//邮箱验证码实体类
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("verification_code")
public class VerificationCode {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String email;
    private String code;
    private String type;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
