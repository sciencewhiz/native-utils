package edu.wpi.first.nativeutils.tasks;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.inject.Inject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

public class LinkerSourceLinkGenerationTask extends DefaultTask {
    private final ListProperty<File> inputFiles;
    private final RegularFileProperty sourceLinkFile;

    @InputFiles
    public ListProperty<File> getInputFiles() {
        return inputFiles;
    }

    @OutputFile
    public RegularFileProperty getSourceLinkFile() {
        return sourceLinkFile;
    }

    @Inject
    public LinkerSourceLinkGenerationTask() {
        ObjectFactory objects = getProject().getObjects();
        inputFiles = objects.listProperty(File.class);
        sourceLinkFile = objects.fileProperty();
    }

    @TaskAction
    public void execute() throws IOException {
        SortedMap<String, String> sortedMap = new TreeMap<>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o2.length() - o1.length();
            }
        });
        Gson gson = new Gson();
        // Read all
        for (File file : inputFiles.get()) {
            if (!file.getName().equals("SourceLink.json")) {
                // See if file in same directory
                file = new File(file.getParentFile(), "SourceLink.json");
                if (!file.exists()) {
                    continue;
                }
            }

            try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {
                Map<String, String> input = gson.fromJson(reader, Map.class);
                sortedMap.putAll(input);
            }
        }
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        String json = builder.create().toJson(sortedMap);
        List<String> jsonList = new ArrayList<>();
        jsonList.add(json);
        Files.write(sourceLinkFile.get().getAsFile().toPath(), jsonList, Charset.defaultCharset(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}