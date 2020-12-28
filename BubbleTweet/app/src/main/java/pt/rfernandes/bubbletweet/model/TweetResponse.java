package pt.rfernandes.bubbletweet.model;

import java.util.List;

public class TweetResponse {
  private String text;
  private List<TweetErrors> errors;

  public TweetResponse(String text, List<TweetErrors> errors) {
    this.text = text;
    this.errors = errors;
  }

  public TweetResponse(List<TweetErrors> errors) {
    this.errors = errors;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public List<TweetErrors> getErrors() {
    return errors;
  }

  public void setErrors(List<TweetErrors> errors) {
    this.errors = errors;
  }
}
