package com.example.trinity.Interfeces;

import android.os.Handler;

import com.example.trinity.valueObject.Manga;



import java.util.ArrayList;

public interface Extensions {
    /*Ao implementar esta interface, lembre-se de criar uma váriavel para a WebView
    * nos atributos da classe implementadora.
    */
   // public void updates( Handler h);
    public void loadMangaLogo(Handler h, ArrayList<Manga> mangaArrayList);


}
