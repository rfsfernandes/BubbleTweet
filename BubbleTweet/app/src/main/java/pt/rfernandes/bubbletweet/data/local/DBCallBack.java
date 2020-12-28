package pt.rfernandes.bubbletweet.data.local;

public interface DBCallBack<T> {
  void returnDB(T object);
}
