package oit.is.z3682.kaizi.janken.model;

public class Janken {
  private static final String COMPUTER_HAND = "グー";

  private final String _playerHand;
  private final String _result;

  /**
   * プレイヤーの手を検証し、グー固定のCPUとの勝敗を判定します。
   *
   * @param hand rock、scissors、paperのいずれか
   * @throws IllegalArgumentException 手が未指定または不正な場合
   */
  public Janken(String hand) {
    if (hand == null) {
      throw new IllegalArgumentException("手を指定してください。");
    }

    switch (hand) {
      case "rock":
        _playerHand = "グー";
        _result = "あいこ";
        break;
      case "scissors":
        _playerHand = "チョキ";
        _result = "負け";
        break;
      case "paper":
        _playerHand = "パー";
        _result = "勝ち";
        break;
      default:
        throw new IllegalArgumentException("不正な手です。");
    }
  }

  /**
   * プレイヤーの手を返します。
   */
  public String GetPlayerHand() {
    return _playerHand;
  }

  /**
   * CPUの手を返します。
   */
  public String GetComputerHand() {
    return COMPUTER_HAND;
  }

  /**
   * プレイヤーから見た勝敗を返します。
   */
  public String GetResult() {
    return _result;
  }
}
