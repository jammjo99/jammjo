package com.calendar.service;

import com.calendar.model.DayData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class DataStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type MAP_TYPE = new TypeToken<Map<String, DayData>>() {}.getType();

    private final Path dataFile;
    private final Map<String, DayData> cache = new HashMap<>();

    public DataStore() {
        String home = System.getProperty("user.home");
        Path dir = Path.of(home, ".checklist-calendar");
        dataFile = dir.resolve("data.json");
        load();
    }

    public DayData get(LocalDate date) {
        return cache.computeIfAbsent(DayData.dateKey(date), k -> new DayData());
    }

    public void save(LocalDate date, DayData data) {
        cache.put(DayData.dateKey(date), data);
        persist();
    }

    public Map<String, DayData> getAll() {
        return cache;
    }

    private void load() {
        if (!Files.exists(dataFile)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(dataFile)) {
            Map<String, DayData> loaded = GSON.fromJson(reader, MAP_TYPE);
            if (loaded != null) {
                cache.putAll(loaded);
            }
        } catch (IOException e) {
            System.err.println("데이터 로드 실패: " + e.getMessage());
        }
    }

    private void persist() {
        try {
            Files.createDirectories(dataFile.getParent());
            try (Writer writer = Files.newBufferedWriter(dataFile)) {
                GSON.toJson(cache, writer);
            }
        } catch (IOException e) {
            System.err.println("데이터 저장 실패: " + e.getMessage());
        }
    }
}
