package obelab.com.smwu.Data;

public class ResultOverviewData {
    private String result_id;
    private String date;
    private String timer;
    private String real_timer;

    public String getResult_id() {
        return result_id;
    }

    public ResultOverviewData(String result_id, String date, String timer, String real_timer, int score) {
        this.result_id = result_id;
        this.date = date;
        this.timer = timer;
        this.real_timer = real_timer;
        this.score = score;
    }

    public void setResult_id(String result_id) {
        this.result_id = result_id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTimer() {
        return timer;
    }

    public void setTimer(String timer) {
        this.timer = timer;
    }

    public String getReal_timer() {
        return real_timer;
    }

    public void setReal_timer(String real_timer) {
        this.real_timer = real_timer;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    private int score;

}
