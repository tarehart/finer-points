package com.nodestand.util;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Collectors;

@Component
public class AliasGenerator {

    private ArrayList<String> adjectives;
    private ArrayList<String> animals;
    private Random random;

    public AliasGenerator() throws IOException {
        adjectives = readLines("/dictionary/adjectives.txt");
        animals = readLines("/dictionary/animals.txt");
        random = new Random();
    }

    public String generateAlias() {
        return randomFrom(adjectives) + " " + randomFrom(animals);
    }

    private String randomFrom(ArrayList<String> list) {
        return list.get(random.nextInt(list.size()));
    }
    private ArrayList<String> readLines(String filename) throws IOException {
        try (InputStream resource = AliasGenerator.class.getResourceAsStream(filename)) {
            return new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8))
                    .lines().collect(Collectors.toCollection(ArrayList::new));
        }
    }
}
