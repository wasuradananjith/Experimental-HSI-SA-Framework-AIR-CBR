package com.example.mapswarm.model;

public class Question {

    private Integer questionId;
    private String questionContent;
    private Integer isDrawing;
    private String mcqAnswer1;
    private String mcqAnswer2;
    private String mcqAnswer3;
    private String mcqAnswer4;
    private Integer saLevel;
    private String mcqAnswer;
    private byte[] drawingAnswer;
    private int numberOfMarkedCells;
    private String markedCells;
    private Integer count;
    private long elapsedTime = 0;
    private long questionRoundTime;

    public Question(String questionContent, Integer isDrawing, String mcqAnswer1, String mcqAnswer2,
                    String mcqAnswer3, String mcqAnswer4, Integer saLevel, Integer  questionId,
                    Integer count) {
        this.questionContent = questionContent;
        this.isDrawing = isDrawing;
        this.mcqAnswer1 = mcqAnswer1;
        this.mcqAnswer2 = mcqAnswer2;
        this.mcqAnswer3 = mcqAnswer3;
        this.mcqAnswer4 = mcqAnswer4;
        this.saLevel = saLevel;
        this.questionId = questionId;
        this.count = count;
    }

    public Integer getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }

    public String getQuestionContent() {
        return questionContent;
    }

    public void setQuestionContent(String questionContent) {
        this.questionContent = questionContent;
    }

    public Integer isDrawing() {
        return isDrawing;
    }

    public void setDrawing(Integer drawing) {
        isDrawing = drawing;
    }

    public String getMcqAnswer1() {
        return mcqAnswer1;
    }

    public void setMcqAnswer1(String mcqAnswer1) {
        this.mcqAnswer1 = mcqAnswer1;
    }

    public String getMcqAnswer2() {
        return mcqAnswer2;
    }

    public void setMcqAnswer2(String mcqAnswer2) {
        this.mcqAnswer2 = mcqAnswer2;
    }

    public String getMcqAnswer3() {
        return mcqAnswer3;
    }

    public void setMcqAnswer3(String mcqAnswer3) {
        this.mcqAnswer3 = mcqAnswer3;
    }

    public String getMcqAnswer4() {
        return mcqAnswer4;
    }

    public void setMcqAnswer4(String mcqAnswer4) {
        this.mcqAnswer4 = mcqAnswer4;
    }

    public String getMcqAnswer() {
        return mcqAnswer;
    }

    public void setMcqAnswer(String mcqAnswer) {
        this.mcqAnswer = mcqAnswer;
    }

    public byte[] getDrawingAnswer() {
        return drawingAnswer;
    }

    public void setDrawingAnswer(byte[] drawingAnswer) {
        this.drawingAnswer = drawingAnswer;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getSaLevel() {
        return saLevel;
    }

    public void setSaLevel(Integer saLevel) {
        this.saLevel = saLevel;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public int getNumberOfMarkedCells() {
        return numberOfMarkedCells;
    }

    public void setNumberOfMarkedCells(int numberOfMarkedCells) {
        this.numberOfMarkedCells = numberOfMarkedCells;
    }

    public String getMarkedCells() {
        return markedCells;
    }

    public void setMarkedCells(String markedCells) {
        this.markedCells = markedCells;
    }

    public long getQuestionRoundTime() {
        return questionRoundTime;
    }

    public void setQuestionRoundTime(long questionRoundTime) {
        this.questionRoundTime = questionRoundTime;
    }
}

