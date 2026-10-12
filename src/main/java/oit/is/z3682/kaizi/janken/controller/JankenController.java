package oit.is.z3682.kaizi.janken.controller;

import oit.is.z3682.kaizi.janken.model.Janken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class JankenController {
  /**
   * 画面を表示し、手が指定された場合はCPUとの対戦結果を渡します。
   */
  @GetMapping("/janken")
  public String ShowJanken(
      @RequestParam(name = "hand", required = false) String hand,
      Model model) {
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

    return "janken";
  }

  /**
   * 参加フォームから受け取ったユーザ名を画面に渡します。
   */
  @PostMapping("/janken")
  public String JoinJanken(
      @RequestParam(name = "userName", required = false) String userName,
      Model model) {
    if (userName == null || userName.isBlank()) {
      return "redirect:/";
    }

    model.addAttribute("userName", userName);
    return "janken";
  }
}
