package dev.jshfx.j.util.json;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ForkJoinPool;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.JsonbException;

public final class JsonUtils {

    private static Jsonb JSONB;
    private static Jsonb JSONB_FORMATTING;
    
    private Jsonb jsonb;

    private JsonUtils(Jsonb jsonb) {
    	this.jsonb = jsonb; 
    }

    public static JsonUtils get() {
    	if (JSONB == null) {
    		JSONB = JsonbBuilder.create();
    	}
    	
    	return new JsonUtils(JSONB);
    }
    
    public static JsonUtils getWithFormatting() {
    	if (JSONB_FORMATTING == null) {
    		JSONB_FORMATTING = JsonbBuilder.create(new JsonbConfig().withFormatting(true));
    	}
    	return new JsonUtils(JSONB_FORMATTING);
    }
    
    public String toJson(Object object) {
    	return jsonb.toJson(object);
    }
    
    public void toJson(Object obj, Path path) {

        Runnable task = () -> {
            synchronized (path) {
                try (var f = Files.newBufferedWriter(path)) {
                	jsonb.toJson(obj, f);

                } catch (JsonbException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        ForkJoinPool.commonPool().execute(task);
    }

    public  <T> T fromJson(String string, Type type) {
    	T result = jsonb.fromJson(string, type);
    	
    	return result;
    }
    
    public <T> T fromJson(Path path, Type type, T defaultObj) {
        T result = defaultObj;

        if (Files.exists(path)) {
            synchronized (path) {
                try (var f = Files.newBufferedReader(path)) {

                    result = jsonb.fromJson(f, type);

                } catch (JsonbException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return result;
    }
}
