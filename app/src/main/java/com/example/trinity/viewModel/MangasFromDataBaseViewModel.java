package com.example.trinity.viewModel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.trinity.valueObject.Manga;

import java.util.ArrayList;

public class MangasFromDataBaseViewModel extends ViewModel {
    private MutableLiveData<ArrayList<Manga>> mangaMutableLiveData = new MutableLiveData<>();
    public MangasFromDataBaseViewModel(){
        mangaMutableLiveData.setValue(new ArrayList<>());
    }
    public void setMangas(ArrayList<Manga> dataSet){
        this.mangaMutableLiveData.setValue(dataSet);
    }
    public ArrayList<Manga> getMangas(){
        return this.mangaMutableLiveData.getValue();
    }
    public void addManga(Manga m){
        this.mangaMutableLiveData.getValue().add(m);
    }

    public MutableLiveData<ArrayList<Manga>> getMangaMutableLiveData() {
        return mangaMutableLiveData;
    }
}
