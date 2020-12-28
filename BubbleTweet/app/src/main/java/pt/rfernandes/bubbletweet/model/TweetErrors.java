package pt.rfernandes.bubbletweet.model;

public class TweetErrors {
  private int errors;
  private String message;

  public TweetErrors(int errors, String message) {
    this.errors = errors;
    this.message = message;
  }

  public int getErrors() {
    return errors;
  }

  public void setErrors(int errors) {
    this.errors = errors;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
