package pt.rfernandes.bubbletweet.data.remote;

public interface RequestCallBack {
  void success();
  void failure(String error);
}
