package com.example.trinity.utilities;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public abstract class SortUtilities {

    public static void dinamicSort(String classToUse, String method, ArrayList array,OrderEnum order) throws NoSuchMethodException, ClassNotFoundException, IllegalAccessException, InvocationTargetException {
        int orderChap = order == OrderEnum.DECRESCENTE?1:-1;
        Class<?> clazz = Class.forName(classToUse);

        Method methodToUse = clazz.getMethod(method, new Class[]{});

//        System.out.println(methodToUse.invoke(array.get(0)));

        for(int i = 0;i < array.size() - 1;i++){
            for(int k = i+1;k < array.size();k++){
                if(orderChap*(Double.parseDouble(methodToUse.invoke(array.get(i)).toString().isEmpty()?"0":methodToUse.invoke(array.get(i)).toString())) < (orderChap*Double.parseDouble(methodToUse.invoke(array.get(k)).toString().isEmpty()?"0":methodToUse.invoke(array.get(k)).toString()))){
                    array.add(i,array.get(k));
                    array.remove(k+1);
                }
            }
        }


    }
}
