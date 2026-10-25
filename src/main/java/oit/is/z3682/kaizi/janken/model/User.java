package oit.is.z3682.kaizi.janken.model;

/**
 * usersテーブルの1行分のユーザー情報を保持します。
 */
public class User {
  private int id;
  private String userName;

  /**
   * ユーザーIDを取得します。
   *
   */
  public int getId() {
    return id;
  }

  /**
   * ユーザーIDを設定します。
   *
   */
  public void setId(int id) {
    this.id = id;
  }

  /**
   * ユーザー名を取得します。
   *
   */
  public String getUserName() {
    return userName;
  }

  /**
   * ユーザー名を設定します。
   *
   */
  public void setUserName(String userName) {
    this.userName = userName;
  }
}
