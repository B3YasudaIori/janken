package oit.is.z3682.kaizi.janken.model;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

/**
 * アプリケーション全体でエントリーユーザを管理します。
 */
@Component
public class Entry {
  private ArrayList<String> users = new ArrayList<>();

  public void AddUser(String userName) {
    for (String user : this.users) {
      if (user.equals(userName)) {
        return;
      }
    }
    this.users.add(userName);
  }

  public ArrayList<String> getUsers() {
    return users;
  }
}
