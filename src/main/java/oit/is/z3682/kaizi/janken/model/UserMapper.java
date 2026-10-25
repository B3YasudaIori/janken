package oit.is.z3682.kaizi.janken.model;

import java.lang.reflect.Array;
import java.util.ArrayList;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
  @Select("select id, username from users")
  ArrayList<User> selectAllUsers();

  @Select("select id, username from users where id = #{id}")
  User selectById(int id);
}
