package android.os;

public class AsyncTask<Params, Progress, Result> {
  public AsyncTask() {
  }

  public void execute(Params... form) {
    doInBackground(form);
  }

  protected Result doInBackground(Params... form) {
    throw new UnsupportedOperationException();
  }

  protected void onPostExecute(Result v) {
  }
}
