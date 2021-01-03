package pt.rfernandes.bubbletweet.custom.utils;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import pt.rfernandes.bubbletweet.custom.CallBackNetwork;

public class CheckInternet {

  private Consumer mConsumer;

  public interface Consumer {
    void accept(Boolean internet);
  }

  public CheckInternet(Consumer consumer) {
    mConsumer = consumer;
    getInternet();
  }

  public void getInternet (){
    new Thread(() -> {
      try {
        Socket sock = new Socket();
        sock.connect(new InetSocketAddress("8.8.8.8", 53), 1500);
        sock.close();
        mConsumer.accept(true);
      } catch (IOException e) {
        mConsumer.accept(false);
      }
    }).start();
  }

}