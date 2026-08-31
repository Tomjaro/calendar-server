package com.fiveelements.calendar.file;

import static org.assertj.core.api.Assertions.*;

import com.fiveelements.calendar.file.infrastructure.LocalPrivateObjectStorage;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalPrivateObjectStorageTest {
  @TempDir Path directory;

  @Test
  void storesReadsAndDeletesPrivateObject() throws Exception {
    var storage = new LocalPrivateObjectStorage(directory.toString());
    byte[] content = {1, 2, 3};
    storage.put("10/image.jpg", content, "image/jpeg");
    assertThat(storage.exists("10/image.jpg")).isTrue();
    assertThat(storage.get("10/image.jpg")).containsExactly(content);
    storage.delete("10/image.jpg");
    assertThat(storage.exists("10/image.jpg")).isFalse();
  }

  @Test
  void rejectsPathTraversal() throws Exception {
    var storage = new LocalPrivateObjectStorage(directory.toString());
    assertThatThrownBy(() -> storage.put("../escape.jpg", new byte[] {1}, "image/jpeg"))
        .isInstanceOf(IllegalStateException.class)
        .hasRootCauseInstanceOf(IllegalArgumentException.class);
  }
}
