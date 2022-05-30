package com.leyou.user.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.util.Date;

@TableName("tb_user")
@Data
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    @Length(min = 4,max=16,message ="用户名长度在4-16位之间" )
    private String username;
    @Length(min = 4,max=12,message ="密码长度在4-12位之间" )
    private String password;
    private String phone;
    private Date createTime;
    private Date updateTime;
}