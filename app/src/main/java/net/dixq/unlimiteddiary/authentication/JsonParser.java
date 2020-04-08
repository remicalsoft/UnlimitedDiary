package net.dixq.unlimiteddiary.authentication;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.dixq.unlimiteddiary.utils.Lg;

import java.io.IOException;

public class JsonParser {
    public String getFolderId(String fullJson){
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode node = mapper.readTree(fullJson);
            int size = node.get("albums").size();
            for(int i = 0; i<size; i++) {
                String title = node.get("albums").get(i).get("title").asText();
                if(title.equals("UnlimitedDiary")){
                    return node.get("albums").get(i).get("id").asText();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
