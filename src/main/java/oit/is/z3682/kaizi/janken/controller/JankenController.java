package oit.is.z3682.kaizi.janken.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.transaction.annotation.Transactional;

import oit.is.z3682.kaizi.janken.model.*;

@Controller
public class JankenController {

  @Autowired
  private UserMapper userMapper;

  @Autowired
  private MatchMapper matchMapper;

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
