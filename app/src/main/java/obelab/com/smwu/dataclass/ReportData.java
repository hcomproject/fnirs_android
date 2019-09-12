package obelab.com.smwu.dataclass;

public class ReportData {
    private final String KEY;
    private final String DATE_INFO;
    private final String TIME_INFO;
    private final String STUDY_TIME;
    private final String FOCUSED_TIME;
    private final String FOCUSED_RATIO;
    private final int SCORE;
    private final String GRAPH_ANALYSIS;
    private final String SCORE_IMG;

    public ReportData(String key, String date_info, String time_info, String study_time, String focused_time,
                      String focused_ratio, int score, String graph_analysis, String score_img){
        this.KEY = key;
        this.DATE_INFO = date_info;
        this.TIME_INFO = time_info;
        this.STUDY_TIME = study_time;
        this.FOCUSED_TIME = focused_time;
        this.FOCUSED_RATIO = focused_ratio;
        this.SCORE = score;
        this.GRAPH_ANALYSIS = graph_analysis;
        this.SCORE_IMG = score_img;
    }

    public String getKey(){
        return KEY;
    }

    public String getDate_info(){
        return DATE_INFO;
    }

    public String getTime_info(){
        return TIME_INFO;
    }

    public String getStudy_time(){return  STUDY_TIME;}

    public String getFocused_time(){
        return FOCUSED_TIME;
    }

    public String getFocused_ratio(){
        return FOCUSED_RATIO;
    }

    public int getScore(){
        return SCORE;
    }

    public String getGraph_analysis(){
        return GRAPH_ANALYSIS;
    }

    public String getScore_img(){return SCORE_IMG;}
}