package net.dixq.unlimiteddiary.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.dixq.unlimiteddiary.top.DiaryData;

import java.io.IOException;

public class JsonParser {

//    public static DiaryData decodeJsonToDiaryData(String json){
//        ObjectMapper mapper = new ObjectMapper();
//        DiaryData diary = null;
//        try {
//            diary = mapper.readValue(json, DiaryData.class);
//        } catch (IOException e) {}
//        return diary;
//    }

    public static String encodeJson(DiaryData diary){
        ObjectMapper mapper = new ObjectMapper();
        String json = null;
        try {
            json = mapper.writeValueAsString(diary);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return json;
    }

}
