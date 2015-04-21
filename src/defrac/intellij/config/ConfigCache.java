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

package defrac.intellij.config;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.io.Closeables;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileAdapter;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.concurrent.ExecutionException;

/**
 *
 */
final class ConfigCache {
  @NotNull
  private static final ConfigCache INSTANCE = new ConfigCache();

  @NotNull
  public static ConfigCache getInstance() {
    return INSTANCE;
  }

  @NotNull
  private static JsonReader lenientReader(@NotNull final Reader reader) {
    final JsonReader jsonReader = new JsonReader(reader);
    jsonReader.setLenient(true);
    return jsonReader;
  }

  @NotNull
  private final LoadingCache<String, DefracConfig> cache =
      CacheBuilder.
          newBuilder().
          maximumSize(100).
          build(new CacheLoader<String, DefracConfig>() {
            @Override
            public DefracConfig load(final String url) throws Exception {
              final VirtualFile file =
                  VirtualFileManager.getInstance().findFileByUrl(url);

              if(file == null) {
                throw new IOException("No such file: " + url);
              }

              return ApplicationManager.getApplication().runReadAction(
                  new ThrowableComputable<DefracConfig, Exception>() {
                    @Override
                    public DefracConfig compute() throws Exception {
                      final Gson gson = new Gson();
                      Reader reader = null;

                      try {
                        reader = new InputStreamReader(file.getInputStream());
                        return gson.fromJson(lenientReader(reader), DefracConfig.class);
                      } finally {
                        Closeables.closeQuietly(reader);
                      }
                    }
                  });
            }
          });


  public void invalidate() {
    cache.invalidateAll();
    cache.cleanUp();
  }

  public DefracConfig get(@NotNull final PsiFile file) throws IOException {
    return get(file.getVirtualFile());
  }

  public DefracConfig get(@NotNull final VirtualFile file) throws IOException {
    return get(file.getUrl());
  }

  public DefracConfig get(@NotNull final String url) throws IOException {
    try {
      return cache.get(url);
    } catch(final ExecutionException executionException) {
      final Throwable cause = executionException.getCause();

      if(cause instanceof IOException) {
        throw (IOException)cause;
      } else {
        throw new RuntimeException(cause);
      }
    }
  }

  private void evict(@NotNull final String url) {
    cache.invalidate(url);
  }

  private ConfigCache() {
    VirtualFileManager.getInstance().addVirtualFileListener(new VirtualFileAdapter() {
      @Override
      public void fileDeleted(@NotNull final VirtualFileEvent event) {
        evict(event.getFile().getUrl());
      }

      @Override
      public void contentsChanged(@NotNull final VirtualFileEvent event) {
        evict(event.getFile().getUrl());
      }
    });
  }
}
