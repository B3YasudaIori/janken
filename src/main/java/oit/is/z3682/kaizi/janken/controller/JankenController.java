package oit.is.z3682.kaizi.janken.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import oit.is.z3682.kaizi.janken.model.*;
import oit.is.z3682.kaizi.janken.service.AsyncKekka;

@Controller
public class JankenController {

  @Autowired
  private UserMapper userMapper;

  @Autowired
  private MatchMapper matchMapper;

  @Autowired
  private MatchInfoMapper matchInfoMapper;

  @Autowired
  private AsyncKekka asyncKekka;

  /**
   * 認証済みのユーザ名と、じゃんけんの結果を画面に渡します。
   */
  @GetMapping("/sec/janken")
  public String ShowJanken(
      @RequestParam(name = "hand", required = false) String hand,
      Principal principal,
      Model model) {
    if (principal != null) {
      String userName = principal.getName();
      model.addAttribute("userName", userName);
    }

    ArrayList<User> users = userMapper.selectAllUsers();
    model.addAttribute("users", users);

    ArrayList<Match> matches = matchMapper.selectAllMatches();
    model.addAttribute("matches", matches);

    ArrayList<MatchInfo> activeMatchInfos = matchInfoMapper.selectActiveMatchInfos();
    model.addAttribute("matchInfos", activeMatchInfos);

    return "janken.html";
  }

  /**
   * 選択した対戦相手との試合ページを表示する
   */
  @GetMapping("/sec/match")
  public String ShowMatch(
      @RequestParam("id") int id,
      Principal principal,
      Model model) {

    User opponent = userMapper.selectById(id);

    if (opponent != null) {
      String userName = principal.getName();
      model.addAttribute("userName", userName);
      model.addAttribute("opponent", opponent);
    }

    return "match.html";
  }

  /**
   * 相手が待機中なら試合を成立させ、それ以外は自分の手を登録する。
   */
  @GetMapping("/sec/wait")
  @Transactional
  public String Wait(
      @RequestParam("id") int id,
      @RequestParam("hand") String hand,
      Principal principal,
      Model model) {
    User player = userMapper.selectByName(principal.getName());
    User opponent = userMapper.selectById(id);

    if (player == null || opponent == null || player.getId() == id) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "対戦相手が不正です。");
    }

    String playerHand = switch (hand) {
      case "rock" -> "Gu";
      case "scissors" -> "Choki";
      case "paper" -> "Pa";
      default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "不正な手です。");
    };

    int afterId = asyncKekka.latestMatchId();
    int matchId = 0;
    MatchInfo waiting = matchInfoMapper.selectWaitingMatchInfo(opponent.getId(), player.getId());
    if (waiting == null) {
      MatchInfo matchInfo = new MatchInfo();
      matchInfo.setUser1(player.getId());
      matchInfo.setUser2(opponent.getId());
      matchInfo.setUser1Hand(playerHand);
      matchInfo.setIsActive(true);
      matchInfoMapper.insertMatchInfo(matchInfo);
    } else {
      Match match = new Match();
      match.setUser1(waiting.getUser1());
      match.setUser2(waiting.getUser2());
      match.setUser1Hand(waiting.getUser1Hand());
      match.setUser2Hand(playerHand);
      match.setIsActive(true);
      matchMapper.insertMatch(match);
      matchId = match.getId();

      if (matchInfoMapper.finishMatchInfo(waiting.getId()) != 1) {
        throw new IllegalStateException("待機情報を更新できませんでした。");
      }
    }
    model.addAttribute("userName", principal.getName());
    model.addAttribute("playerId", player.getId());
    model.addAttribute("opponentId", opponent.getId());
    model.addAttribute("opponentName", opponent.getUserName());
    model.addAttribute("afterId", afterId);
    model.addAttribute("matchId", matchId);
    return "wait.html";
  }

  @GetMapping(value = "/sec/results", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter results(@RequestParam("opponentId") int opponentId,
      @RequestParam("afterId") int afterId, @RequestParam("matchId") int matchId,
      Principal principal) {
    User player = userMapper.selectByName(principal.getName());
    if (player == null || userMapper.selectById(opponentId) == null
        || player.getId() == opponentId || afterId < 0 || matchId < 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }
    SseEmitter emitter = new SseEmitter(60000L);
    asyncKekka.asyncShowResult(emitter, player.getId(), opponentId, afterId, matchId);
    return emitter;
  }

  @PostMapping("/sec/results/ack")
  public ResponseEntity<Void> acknowledge(@RequestParam("id") int id, Principal principal) {
    User player = userMapper.selectByName(principal.getName());
    if (player == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }
    asyncKekka.acknowledge(id, player.getId());
    return ResponseEntity.noContent().build();
  }

  /**
   * 勝敗判定を行い、試合情報をDBへ登録して結果を表示する
   */
  @GetMapping("/sec/fight")
  @Transactional
  public String Fight(
      @RequestParam("id") int id,
      @RequestParam("hand") String hand,
      Principal principal,
      Model model) {

    User player = userMapper.selectByName(principal.getName());
    User opponent = userMapper.selectById(id);

    String playerHand;
    int playerIndex = -1;

    switch (hand) {
      case "rock":
        playerHand = "Gu";
        playerIndex = 0;
        break;
      case "scissors":
        playerHand = "Choki";
        playerIndex = 1;
        break;
      case "paper":
        playerHand = "Pa";
        playerIndex = 2;
        break;
      default:
        throw new IllegalArgumentException("不正な手です。");
    }

    String[] hands = { "Gu", "Choki", "Pa" };
    Random rand = new Random();
    int opponentIndex = rand.nextInt(3);
    String opponentHand = hands[opponentIndex];

    String result;
    if (playerIndex == opponentIndex) {
      result = "Draw";
    } else if ((playerIndex + 1) % 3 == opponentIndex) {
      result = "Win!";
    } else {
      result = "Lose...";
    }

    Match match = new Match();
    match.setUser1(player.getId());
    match.setUser2(opponent.getId());
    match.setUser1Hand(playerHand);
    match.setUser2Hand(opponentHand);

    matchMapper.insertMatch(match);

    model.addAttribute("userName", player.getUserName());
    model.addAttribute("opponent", opponent);
    model.addAttribute("playerHand", playerHand);
    model.addAttribute("opponentHand", opponentHand);
    model.addAttribute("result", result);

    return "match.html";
  }
}
