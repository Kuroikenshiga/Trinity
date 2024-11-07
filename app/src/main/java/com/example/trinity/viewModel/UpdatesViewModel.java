package com.example.trinity.viewModel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.trinity.fragments.UpdatesFragment;
import com.example.trinity.valueObject.ChapterManga;
import com.example.trinity.valueObject.ChapterUpdated;

import java.util.ArrayList;

public class UpdatesViewModel extends ViewModel {
    private MutableLiveData<ArrayList<ChapterUpdated>>chapterUpdatedLiveData = new MutableLiveData<>();
    private MutableLiveData<ChapterUpdated> item = new MutableLiveData<>();
//    private int v = 1;
    public UpdatesViewModel(){
        this.item.setValue(new ChapterUpdated(null,null));
        this.chapterUpdatedLiveData.setValue(new ArrayList<ChapterUpdated>());

    }

    public MutableLiveData<ArrayList<ChapterUpdated>> getChapterUpdatedLiveData() {
        return chapterUpdatedLiveData;
    }
    public void addChapterInLiveData(ChapterUpdated c){
        this.item.setValue(c);

        if(this.chapterUpdatedLiveData.getValue().isEmpty()){
            this.chapterUpdatedLiveData.getValue().add(null);
            this.chapterUpdatedLiveData.getValue().add(this.item.getValue());
        }
//        else if(this.chapterUpdatedLiveData.getValue().get(this.chapterUpdatedLiveData.getValue().size() - 1) == null) {
//            this.chapterUpdatedLiveData.getValue().add(this.item.getValue());
//        }
        else if(this.chapterUpdatedLiveData.getValue().get(this.chapterUpdatedLiveData.getValue().size() - 1).getChapterManga().returnTimeReleased().equals(this.item.getValue().getChapterManga().returnTimeReleased())){
            this.chapterUpdatedLiveData.getValue().add(this.item.getValue());
        }
        else{
            this.chapterUpdatedLiveData.getValue().add(null);
            this.chapterUpdatedLiveData.getValue().add(this.item.getValue());
        }

//        System.out.println("Chamou "+v+"°");
//        v++;
//        System.out.println(this.chapterUpdatedLiveData.getValue().size());

    }

    public MutableLiveData<ChapterUpdated> getItem() {
        return item;
    }

    public void setChapterUpdatedLiveData(ArrayList<ChapterUpdated> array){
        this.chapterUpdatedLiveData.setValue(array);
    }
}
