package oit.is.z3682.kaizi.janken.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class JankenController {
  /**
   * 直接アクセスした場合は、挨拶なしで画面を表示します。
   */
  @GetMapping("/janken")
  public String ShowJanken() {
    return "janken.html";
  }

  /**
   * 参加フォームから受け取ったユーザ名を画面に渡します。
   */
  @PostMapping("/janken")
  public String JoinJanken(
      @RequestParam(name = "userName", required = false) String userName,
      ModelMap model) {
    if (userName == null || userName.isBlank()) {
      return "redirect:/";
    }

    model.addAttribute("userName", userName);
    return "janken.html";
  }
}
