package com.example.trinity.viewModel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.trinity.valueObject.Manga;

public class MangaDataViewModel extends ViewModel {
    private MutableLiveData<Manga> manga = new MutableLiveData<>();
    private MutableLiveData<String> idChap = new MutableLiveData<>();


    public void setManga(Manga manga) {
        this.manga.setValue(manga);
    }
    public Manga getManga(){
        return this.manga.getValue();
    }

    public void setIdChap(String id){
        this.idChap.setValue(id);
    }
    public String getIdChap(){
        return this.idChap.getValue();
    }
}
