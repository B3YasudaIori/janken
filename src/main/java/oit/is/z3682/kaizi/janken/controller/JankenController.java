package oit.is.z3682.kaizi.janken.controller;

import java.security.Principal;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import oit.is.z3682.kaizi.janken.model.Janken;
import oit.is.z3682.kaizi.janken.model.UserMapper;
import oit.is.z3682.kaizi.janken.model.*;

@RequestMapping("/sec")
@Controller
public class JankenController {

  @Autowired
  private UserMapper userMapper;

  /**
   * 認証済みのユーザ名と、じゃんけんの結果を画面に渡します。
   */
  @GetMapping("/janken")
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

    if (hand != null) {
      try {
        Janken janken = new Janken(hand);
        model.addAttribute("playerHand", janken.GetPlayerHand());
        model.addAttribute("computerHand", janken.GetComputerHand());
        model.addAttribute("result", janken.GetResult());
      } catch (IllegalArgumentException exception) {
        model.addAttribute("errorMessage", "グー・チョキ・パーのリンクから選択してください。");
      }
    }

    return "janken.html";
  }

  /**
   * 参加フォームから受け取ったユーザ名を画面に渡します。
   *
   */
  @PostMapping("/janken")
  public String JoinJanken(
      @RequestParam(name = "userName", required = false) String userName,
      Principal principal,
      Model model) {
    if (principal != null) {
      String name = principal.getName();
      model.addAttribute("userName", name);
    }

    ArrayList<User> users = userMapper.selectAllUsers();
    model.addAttribute("users", users);
    return "janken.html";
  }
}
