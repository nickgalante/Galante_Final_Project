package edu.temple.Stock_Information_App;


public class News {
    private String title;
    private String link;

    public News(){
        this.title = "";
        this.link = "";

    }

    public String getTitle(){
        return title;
    }
    public void setTitle(String title){
        this.title = title;
    }

    public String getLink(){
        return link;
    }
    public void setLink(String link){
        this.link = link;
    }
}
