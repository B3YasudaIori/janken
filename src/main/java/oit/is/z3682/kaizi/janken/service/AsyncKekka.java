package oit.is.z3682.kaizi.janken.service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import oit.is.z3682.kaizi.janken.model.Match;

@Service
public class AsyncKekka {
  @Autowired
  private JdbcTemplate jdbc;

  public int latestMatchId() {
    return jdbc.queryForObject("select coalesce(max(id), 0) from matches", Integer.class);
  }

  public Match findResult(int playerId, int opponentId, int afterId, int matchId) {
    String sql;
    Object[] params;
    if (matchId > 0) {
      // 後手は成立させた試合を取得する。先手が表示済みでも取得できる。
      sql = "select * from matches where id = ? and user1 = ? and user2 = ?";
      params = new Object[] { matchId, opponentId, playerId };
    } else {
      // 先手は、この待機画面を開いた後に成立した有効な試合を監視する。
      sql = "select * from matches where user1 = ? and user2 = ? and id > ?"
          + " and isActive = true order by id limit 1";
      params = new Object[] { playerId, opponentId, afterId };
    }
    List<Match> results = jdbc.query(sql, (rs, rowNum) -> {
      Match match = new Match();
      match.setId(rs.getInt("id"));
      match.setUser1(rs.getInt("user1"));
      match.setUser2(rs.getInt("user2"));
      match.setUser1Hand(rs.getString("user1Hand"));
      match.setUser2Hand(rs.getString("user2Hand"));
      match.setIsActive(rs.getBoolean("isActive"));
      return match;
    }, params);
    return results.isEmpty() ? null : results.get(0);
  }

  // 先手の画面から表示完了の通知が来たら、有効フラグを解除する。
  public void acknowledge(int matchId, int playerId) {
    jdbc.update("update matches set isActive = false where id = ? and user1 = ? and isActive = true",
        matchId, playerId);
  }

  @Async
  public void asyncShowResult(SseEmitter emitter, int playerId, int opponentId,
      int afterId, int matchId) {
    AtomicBoolean closed = new AtomicBoolean(false);
    emitter.onCompletion(() -> closed.set(true));
    emitter.onTimeout(() -> closed.set(true));
    emitter.onError(error -> closed.set(true));
    long deadline = System.nanoTime() + 55_000_000_000L;

    try {
      while (!closed.get() && System.nanoTime() < deadline) {
        Match result = findResult(playerId, opponentId, afterId, matchId);
        if (result != null) {
          emitter.send(SseEmitter.event().name("result").data(result));
          emitter.complete();
          return;
        }
        emitter.send(SseEmitter.event().comment("waiting"));
        Thread.sleep(1000);
      }
      if (!closed.get()) {
        emitter.complete(); // 結果未着ならEventSourceが再接続する。
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      emitter.complete();
    } catch (IOException | IllegalStateException e) {
      // 切断時には未表示の結果を処理済みにしない。
      closed.set(true);
    } catch (Exception e) {
      emitter.completeWithError(e);
    }
  }
}
