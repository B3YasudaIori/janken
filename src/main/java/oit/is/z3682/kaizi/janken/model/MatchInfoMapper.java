package oit.is.z3682.kaizi.janken.model;

import java.util.ArrayList;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface MatchInfoMapper {
  @Select("select id, user1, user2, user1Hand, isActive from matchInfo")
  ArrayList<MatchInfo> selectAllMatchInfos();

  @Select("select id, user1, user2, user1Hand, isActive from matchInfo where isActive = true order by id")
  ArrayList<MatchInfo> selectActiveMatchInfos();

  @Insert("insert into matchInfo(user1, user2, user1Hand, isActive) values (#{user1}, #{user2}, #{user1Hand}, #{isActive})")
  @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
  void insertMatchInfo(MatchInfo matchInfo);
}
