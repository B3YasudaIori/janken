package oit.is.z3682.kaizi.janken.model;

import java.util.ArrayList;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MatchInfoMapper {
  @Select("select id, user1, user2, user1Hand, isActive from matchInfo")
  ArrayList<MatchInfo> selectAllMatchInfos();

  @Select("select id, user1, user2, user1Hand, isActive from matchInfo where isActive = true order by id")
  ArrayList<MatchInfo> selectActiveMatchInfos();

  @Select("select id, user1, user2, user1Hand, isActive from matchInfo where user1 = #{opponentID} and user2 = #{playerID} and isActive = true order by id")
  MatchInfo selectWaitingMatchInfo(@Param("opponentID") int opponentID, @Param("playerID") int playerID);

  @Update("update matchinfo set isActive = false where id = #{id} and isActive = true")
  int finishMatchInfo(@Param("id") int id);

  @Insert("insert into matchInfo(user1, user2, user1Hand, isActive) values (#{user1}, #{user2}, #{user1Hand}, #{isActive})")
  @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
  void insertMatchInfo(MatchInfo matchInfo);
}
