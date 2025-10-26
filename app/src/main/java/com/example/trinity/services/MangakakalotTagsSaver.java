package com.example.trinity.services;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.example.trinity.preferecesConfig.ConfigClass;
import com.example.trinity.valueObject.TagManga;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.Comparator;

public class MangakakalotTagsSaver {

    private Context context;
    private SharedPreferences preferences;
    private final String json = "[\n" +
            "  \"Newest\",\n" +
            "  \"Latest\",\n" +
            "  \"Top read\",\n" +
            "  \"All\",\n" +
            "  \"Completed\",\n" +
            "  \"Ongoing\",\n" +
            "  \"Comedy\",\n" +
            "  \"Supernatural\",\n" +
            "  \"Drama\",\n" +
            "  \"Fantasy\",\n" +
            "  \"Action\",\n" +
            "  \"Josei\",\n" +
            "  \"Adventure\",\n" +
            "  \"Romance\",\n" +
            "  \"Smut\",\n" +
            "  \"Manhwa\",\n" +
            "  \"Tragedy\",\n" +
            "  \"Slice of life\",\n" +
            "  \"School life\",\n" +
            "  \"Seinen\",\n" +
            "  \"Historical\",\n" +
            "  \"Harem\",\n" +
            "  \"Horror\",\n" +
            "  \"Psychological\",\n" +
            "  \"Mystery\",\n" +
            "  \"Shounen\",\n" +
            "  \"Martial arts\",\n" +
            "  \"Manhua\",\n" +
            "  \"Shoujo\",\n" +
            "  \"Isekai\",\n" +
            "  \"Erotica\",\n" +
            "  \"Gender bender\",\n" +
            "  \"Mature\",\n" +
            "  \"Webtoons\",\n" +
            "  \"Shoujo ai\",\n" +
            "  \"Yaoi\",\n" +
            "  \"Yuri\",\n" +
            "  \"Medical\",\n" +
            "  \"Mecha\",\n" +
            "  \"Shounen ai\",\n" +
            "  \"Sports\",\n" +
            "  \"Cooking\",\n" +
            "  \"Sci fi\",\n" +
            "  \"One shot\",\n" +
            "  \"Ecchi\",\n" +
            "  \"Adult\",\n" +
            "  \"Pornographic\",\n" +
            "  \"Doujinshi\",\n" +
            "  \"Long Strip\",\n" +
            "  \"Survival\",\n" +
            "  \"Adaptation\",\n" +
            "  \"Official Colored\",\n" +
            "  \"Wuxia\",\n" +
            "  \"Thriller\",\n" +
            "  \"Web Comic\",\n" +
            "  \"Full Color\",\n" +
            "  \"Reincarnation\",\n" +
            "  \"Monsters\",\n" +
            "  \"Military\",\n" +
            "  \"Philosophical\",\n" +
            "  \"Gyaru\",\n" +
            "  \"Bloody\",\n" +
            "  \"Demons\",\n" +
            "  \"System\",\n" +
            "  \"Loli\",\n" +
            "  \"Ninja\",\n" +
            "  \"Incest\",\n" +
            "  \"Crime\",\n" +
            "  \"Office Workers\",\n" +
            "  \"Sexual Violence\",\n" +
            "  \"Crossdressing\",\n" +
            "  \"Gore\",\n" +
            "  \"Delinquents\",\n" +
            "  \"Shota\",\n" +
            "  \"Police\",\n" +
            "  \"Manga\",\n" +
            "  \"Time Travel\",\n" +
            "  \"Monster Girls\",\n" +
            "  \"Anthology\",\n" +
            "  \"4-Koma\",\n" +
            "  \"Oneshot\",\n" +
            "  \"Animals\",\n" +
            "  \"Heartwarming\",\n" +
            "  \"Superhero\",\n" +
            "  \"Magic\",\n" +
            "  \"Genderswap\",\n" +
            "  \"Post-Apocalyptic\",\n" +
            "  \"Music\",\n" +
            "  \"Self-Published\",\n" +
            "  \"Aliens\",\n" +
            "  \"Villainess\",\n" +
            "  \"Virtual Reality\",\n" +
            "  \"Ghosts\",\n" +
            "  \"Award Winning\",\n" +
            "  \"Video Games\",\n" +
            "  \"Magical Girls\",\n" +
            "  \"Reverse Harem\",\n" +
            "  \"Fan Colored\",\n" +
            "  \"Zombies\",\n" +
            "  \"Mafia\",\n" +
            "  \"Webtoon\",\n" +
            "  \"Royal family\",\n" +
            "  \"Manhwa Hot\",\n" +
            "  \"Traditional Games\",\n" +
            "  \"Magical\",\n" +
            "  \"Vampires\",\n" +
            "  \"Revenge\",\n" +
            "  \"ecchi\",\n" +
            "  \"Samurai\",\n" +
            "  \"Yaoi\",\n" +
            "  \"Monster\",\n" +
            "  \"Super Power\",\n" +
            "  \"Animal\",\n" +
            "  \"Game\",\n" +
            "  \"Comic\",\n" +
            "  \"Science fiction\",\n" +
            "  \"Office\",\n" +
            "  \"School\",\n" +
            "  \"Parody\",\n" +
            "  \"Iyashikei\",\n" +
            "  \"Girls Love\",\n" +
            "  \"Boys Love\",\n" +
            "  \"Mahou Shoujo\",\n" +
            "  \"Suspense\",\n" +
            "  \"Vampire\",\n" +
            "  \"Kids\",\n" +
            "  \"Space\",\n" +
            "  \"Gourmet\",\n" +
            "  \"Soft Yaoi\",\n" +
            "  \"Avant Garde\",\n" +
            "  \"cartoon\",\n" +
            "  \"violence\",\n" +
            "  \"imageset\",\n" +
            "  \"teacher_student\",\n" +
            "  \"cultivation\",\n" +
            "  \"death_game\",\n" +
            "  \"degeneratemc\",\n" +
            "  \"cars\",\n" +
            "  \"showbiz\",\n" +
            "  \"blackmail\",\n" +
            "  \"western\",\n" +
            "  \"xianxia\",\n" +
            "  \"fetish\",\n" +
            "  \"netorare\",\n" +
            "  \"age_gap\",\n" +
            "  \"ai_art\",\n" +
            "  \"master_servant\",\n" +
            "  \"college_life\",\n" +
            "  \"childhood_friends\",\n" +
            "  \"non_human\",\n" +
            "  \"dementia\",\n" +
            "  \"Informative\",\n" +
            "  \"Graphic Novel\",\n" +
            "  \"Royalty\",\n" +
            "  \"Liexing\",\n" +
            "  \"Ping Ping Jun\",\n" +
            "  \"Josei\",\n" +
            "  \"Shoujo\",\n" +
            "  \"Reverse\",\n" +
            "  \"artbook\",\n" +
            "  \"omegaverse\",\n" +
            "  \"cheating_infidelity\",\n" +
            "  \"sm_bdsm\",\n" +
            "  \"bodyswap\",\n" +
            "  \"netori\",\n" +
            "  \"old_people\",\n" +
            "  \"beasts\",\n" +
            "  \"Seinen\",\n" +
            "  \"Shounen\",\n" +
            "  \"Creators\",\n" +
            "  \"Others\",\n" +
            "  \"step_family\",\n" +
            "  \"brocon_siscon\",\n" +
            "  \"Korean\"\n" +
            "]";

    public MangakakalotTagsSaver(@NonNull Context c) {
        this.context = c;
        preferences = context.getSharedPreferences(ConfigClass.TAG_PREFERENCE,Context.MODE_PRIVATE);
    }
    public boolean tagsAlredySaved(){
        return preferences.contains(ConfigClass.ConfigTagMangakakalot.KEY_NAME_EXTENSION_TAG);
    }
    @SuppressLint("ApplySharedPref")
    @WorkerThread
    public void saveIfNotExistsTags(){
        if(this.tagsAlredySaved())return;
        preferences.edit().putString(ConfigClass.ConfigTagMangakakalot.KEY_NAME_EXTENSION_TAG,json).commit();
    }
    @WorkerThread
    public ArrayList<TagManga> getTags(){
        Gson gson = new Gson();
        ArrayList<TagManga> tagMangas = new ArrayList<>();
        JsonArray jsonArray = gson.fromJson(preferences.getString(ConfigClass.ConfigTagMangakakalot.KEY_NAME_EXTENSION_TAG,"[]"),JsonElement.class).getAsJsonArray();

        for(JsonElement e:jsonArray){
            if(e.toString().equals("\"Newest\"") || e.toString().equals("\"Top read\"") || e.toString().equals("\"Completed\"") || e.toString().equals("\"Ongoing\"") || e.toString().equals("\"All\"") || e.toString().equals("\"Latest\""))continue;
            tagMangas.add(new TagManga("",e.toString().replace("\"","")));
        }

        tagMangas.sort(new Comparator<TagManga>() {
            @Override
            public int compare(TagManga o1, TagManga o2) {
                return o1.getNome().compareTo(o2.getNome());
            }
        });
        return tagMangas;
    }

}
