/*
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package demo.app.plugin.utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class SpringCloudStreamPluginUtils {

    private SpringCloudStreamPluginUtils() {
        //prevents instantiation
    }

    public static void cleanupGenProjHome(File genProjecthome) throws IOException {
        FileUtils.cleanDirectory(genProjecthome);
        FileUtils.deleteDirectory(genProjecthome);
    }

    public static void ignoreUnitTestGeneratedByInitializer(String generatedAppHome) throws IOException {
        Collection<File> files = FileUtils.listFiles(new File(generatedAppHome, "src/test/java"), null, true);
        Optional<File> first = files.stream()
                .filter(f -> f.getName().endsWith("ApplicationTests.java"))
                .findFirst();

        if (first.isPresent()) {
            StringBuilder sb = new StringBuilder();
            File f1 = first.get();
            Files.readAllLines(f1.toPath()).forEach(l -> {
                if (l.startsWith("import") && !sb.toString().contains("import org.junit.Ignore")) {
                    sb.append("import org.junit.Ignore;\n");
                } else if (l.startsWith("@RunWith") && !sb.toString().contains("@Ignore")) {
                    sb.append("@Ignore\n");
                }
                sb.append(l);
                sb.append("\n");
            });
            Files.write(f1.toPath(), sb.toString().getBytes());
        }
    }

    public static void addExtraTestConfig(String generatedAppHome, String clazzInfo) throws IOException {
        Collection<File> files = FileUtils.listFiles(new File(generatedAppHome, "src/test/java"), null, true);
        Optional<File> first = files.stream()
                .filter(f -> f.getName().endsWith("ApplicationTests.java"))
                .findFirst();

        if (first.isPresent()) {
            StringBuilder sb = new StringBuilder();
            File f1 = first.get();
            Files.readAllLines(f1.toPath()).forEach(l -> {
                if (l.startsWith("@SpringApplicationConfiguration")) {
                    sb.append("@SpringApplicationConfiguration(").append(clazzInfo).append(")");
                } else if (l.startsWith("@SpringBootTest")) {
                    sb.append("@SpringBootTest(").append(clazzInfo).append(")");
                } else {
                    sb.append(l);
                }
                sb.append("\n");
            });
            Files.write(f1.toPath(), sb.toString().getBytes());
        }
    }

    public static void addAutoConfigImport(String generatedAppHome, String autoConfigClazz, String... annotationImports) throws IOException {
        Collection<File> files = FileUtils.listFiles(new File(generatedAppHome, "src/main/java"), null, true);
        Optional<File> first = files.stream()
                .filter(f -> f.getName().endsWith("Application.java"))
                .findFirst();

        if (first.isPresent()) {
            StringBuilder sb = new StringBuilder();
            File f1 = first.get();
            List<String> annotations = new ArrayList<>();
            Files.readAllLines(f1.toPath()).forEach(l -> {
                if (l.startsWith("import org.springframework.boot.autoconfigure.SpringBootApplication;")) {
                    sb.append(l).append("\n").append("import org.springframework.context.annotation.Import;\n");
                    if (annotationImports != null && annotationImports.length > 0) {
                        for (String annotationsImport : annotationImports) {
                            sb.append(annotationsImport).append(";\n");
                            annotations.add("@" + annotationsImport.substring(annotationsImport.lastIndexOf('.') + 1));
                        }
                    }
                } else if (l.startsWith("@SpringBootApplication")) {
                    sb.append(l);
                    if (!annotations.isEmpty()) {
                        for (String annotation : annotations) {
                            sb.append("\n").append(annotation);
                        }
                    }
                    sb.append("\n").append("@Import(").append(autoConfigClazz).append(")");
                } else {
                    sb.append(l);
                }
                sb.append("\n");
            });
            Files.write(f1.toPath(), sb.toString().getBytes());
        }
    }
}
