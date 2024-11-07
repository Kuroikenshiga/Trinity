package com.example.trinity.preferecesConfig;

import android.content.Context;
import android.content.SharedPreferences;

public final class ConfigClass {
    /*
    * Atenção!! essa classe é voltada apenas para indexação de chaves de configurações utilizadas no sharePreferences,
    * logo não é uma classe que deva ser extendida ou instanciada.
    * */
    private ConfigClass(){}
    public final static  String TAG_PREFERENCE = "ARQUIVO_DE_PREFERENCIA_DE_LEITURA";
    public static void startupValues(Context context){
        new Thread(){
            @Override
            public void run(){
                SharedPreferences sharedPreferences = context.getSharedPreferences(TAG_PREFERENCE,Context.MODE_PRIVATE);
                ConfigClass.ConfigReader.setAlphaConfigValue(sharedPreferences.getFloat(ConfigReader.ALPHA_CONFIG,1));
                ConfigReader.setReadDirectionValue(sharedPreferences.getInt(ConfigReader.READ_DIRECTION,1));

            }

        }.start();

    }
    public static final class ConfigReader{
        private ConfigReader(){}
        public final static String ALPHA_CONFIG = "alpha";
        public final static String READ_DIRECTION = "readDirection";
        public final static String ALWAYS_CASCADE_WHEN_LONG_STRIP = "alwaysCascadeWhenLongStrip";
        private static Float ALPHA_CONFIG_VALUE;
        private static Integer READ_DIRECTION_VALUE;

        public static float getValueAlpha(){
            return ALPHA_CONFIG_VALUE;
        }
        public static int getValueDirection(){
            return READ_DIRECTION_VALUE;
        }

        public static void setReadDirectionValue(int readDirectionValue) {
            READ_DIRECTION_VALUE = readDirectionValue;
        }

        public static void setAlphaConfigValue(float alphaConfigValue) {
            ALPHA_CONFIG_VALUE = alphaConfigValue;
        }
    }
    public static final class ConfigContent{
        private ConfigContent(){}
        public final static String IMAGE_QUALITY = "imageQuality";
        public final static String ALREDY_LOADED_TAGS = "alredyLoadedeTags";
    }
    public static final class ConfigUpdates{
        private ConfigUpdates(){}
        public final static String LAST_UPDATE = "lastUpdate";
    }
    public static final class ConfigLogoMigration{
        private ConfigLogoMigration(){}
        public final static String ALREDY_MIGRATED = "AlredyMigrated";
    }
}
