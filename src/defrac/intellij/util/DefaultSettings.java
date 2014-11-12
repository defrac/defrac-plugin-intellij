/*
 * Copyright 2014 defrac inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package defrac.intellij.util;

import defrac.intellij.DefracPlatform;
import com.google.common.base.Charsets;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

/**
 *
 */
public final class DefaultSettings {
  public static void write(@NotNull final String name,
                           @NotNull final String package$,
                           @NotNull final String identifier,
                           @NotNull final String version,
                           @NotNull final Set<DefracPlatform> platforms,
                           @NotNull final File file) throws IOException {
    PrintWriter writer = null;

    try {
      writer = new PrintWriter(Files.newWriter(file, Charsets.UTF_8));

      writer.println("{\n" +
          "  \"name\": \""+name+"\",\n" +
          "  \"package\": \""+package$+"\",\n" +
          "  \"identifier\": \""+identifier+"\",\n" +
          "  \"version\": \""+version+"\",\n" +
          "  \"targets\": [");

      for(final DefracPlatform platform : platforms) {
        writer.println(
            "    \""+platform.name+"\",");
      }

      writer.println(
          "  ],\n" +
          "  \"web\": {\n" +
          "    \"js\": \""+identifier+".js\"\n" +
          "  }\n" +
          "}");
    } finally {
      Closeables.close(writer, /*swallowException=*/false);
    }
  }

  private DefaultSettings() {
  }
}
