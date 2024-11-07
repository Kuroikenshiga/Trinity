package com.example.trinity.valueObject;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ChapterManga implements Parcelable {

    private  String id;
    public String title;
    private String chapter;
    private String scan;
    private Calendar data;
    private String dateRFC3339;
    private boolean alredyRead;
    private int currentPage;
    private boolean isDownloaded;
    public boolean isSelected = false;
    public ChapterManga(String id, String title, String chapter, String scan, Calendar data) {
        this.id = id;
        this.title = title;
        this.chapter = chapter;
        this.scan = scan;
        this.data = data;
        this.alredyRead = false;
        currentPage = 0;
        this.isDownloaded = false;
        this.validateNumChapter();
    }
    public ChapterManga(String id, String title, String chapter, String scan,String date_RFC3339,boolean alredyRead) {
        this.id = id;
        this.title = title;
        this.chapter = chapter;
        this.scan = scan;
        this.dateRFC3339 = date_RFC3339;
        this.alredyRead = alredyRead;
        currentPage = 0;
        this.isDownloaded = false;
        this.validateNumChapter();
    }
    public ChapterManga(String id, String title, String chapter, String scan,String date_RFC3339,boolean alredyRead,int currentPage,boolean isDownloaded) {
        this.id = id;
        this.title = title;
        this.chapter = chapter;
        this.scan = scan;
        this.dateRFC3339 = date_RFC3339;
        this.alredyRead = alredyRead;
        this.currentPage = currentPage;
        this.isDownloaded = isDownloaded;
        this.validateNumChapter();
    }

    protected ChapterManga(Parcel in) {
        id = in.readString();
        title = in.readString();
        chapter = in.readString();
        scan = in.readString();
        dateRFC3339 = in.readString();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            alredyRead = in.readBoolean();
        }
        currentPage = in.readInt();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            isDownloaded = in.readBoolean();
        }
    }

    public static final Creator<ChapterManga> CREATOR = new Creator<ChapterManga>() {
        @Override
        public ChapterManga createFromParcel(Parcel in) {
            return new ChapterManga(in);
        }

        @Override
        public ChapterManga[] newArray(int size) {
            return new ChapterManga[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getChapter() {
        return chapter;
    }

    public void setChapter(String chapter) {
        this.chapter = chapter;
    }

    public String getScan() {
        return scan;
    }

    public void setScan(String scan) {
        this.scan = scan;
    }

    public Calendar getData() {
        return data;
    }

    public void setData(Calendar data) {
        this.data = data;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDateRFC3339() {
        return dateRFC3339;
    }

    public boolean isAlredyRead() {
        return alredyRead;
    }

    public void setAlredyRead(boolean alredyRead) {
        this.alredyRead = alredyRead;
    }

    public void setDateRFC3339(String dateRFC3339) {
        this.dateRFC3339 = dateRFC3339;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public boolean isDownloaded() {
        return isDownloaded;
    }

    public void setDownloaded(boolean downloaded) {
        isDownloaded = downloaded;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(chapter);
        dest.writeString(scan);
        dest.writeString(dateRFC3339);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            dest.writeBoolean(alredyRead);
        }
        dest.writeInt(currentPage);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            dest.writeBoolean(isDownloaded);
        }
    }

    public String returnTimeReleased(){
        long dateReleased = Instant.parse(this.dateRFC3339).getEpochSecond();
        long currentDate = Instant.now().getEpochSecond();
        long time = currentDate - dateReleased;

        long year = time / 31556926;
        time = year > 0?time % 31556926:time;
        long month = time / 2592000;
        time = month > 0?time % 2592000:time;
        long day = time/86000;
        time = day > 0?time%86000:time;
        long hour = time / 3600;
        time  = hour > 0?time%hour:time;
        long min = time/60;
        time = min > 0?time%60:time;
        long sec = time;

        if(year > 0){
            return "Há "+year+(year > 1?" anos atrás":" ano atrás");
        }
        if(month > 0){
            return "Há "+month+(month > 1?" meses atrás":" mês atrás");
        }
        if(day > 0){
            return "Há "+day+(day > 1?" dias atrás":" dia atrás");
        }
        if(hour > 0){
            return "Há "+hour+(hour > 1?" horas atrás":" hora atrás");
        }
        if(min > 0){
            return "Há "+min+(min > 1?" minutos atrás":" minuto atrás");
        }
        return "Há "+sec+(sec > 1?" segundos atrás":" segundo atrás");
    }
    private boolean isChapterValid() {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(this.chapter);
        return matcher.matches();
    }

    private void validateNumChapter() {
        if (this.chapter.isEmpty()) {
            this.chapter = "0";
            return;
        }
        if (!isChapterValid()) {
            StringBuilder newStringChapter = new StringBuilder();
            for (int i = 0; i < this.chapter.length(); i++) {
                if(this.chapter.charAt(i) == '.'){
                    newStringChapter.append(""+this.chapter.charAt(i));
                    continue;
                }
                String stringChar = ""+chapter.charAt(i);

                Pattern pattern = Pattern.compile("\\d+");
                Matcher matcher = pattern.matcher(stringChar);

                if (!matcher.matches()) {
                    break;
                }
                newStringChapter.append(stringChar);
            }
            this.chapter = String.valueOf(newStringChapter);
        }
    }
}
