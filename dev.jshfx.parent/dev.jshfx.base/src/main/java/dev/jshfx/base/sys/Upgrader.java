package dev.jshfx.base.sys;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;

import org.apache.commons.io.FileUtils;

class Upgrader {
    
    void upgrade() throws IOException {
        @SuppressWarnings({ "unchecked", "rawtypes" })
        Optional<Path> previousVersion = Files.list(FileManager.SYS_HOME_DIR)
                .max(Comparator.<Path, Comparable>comparing(p -> RepositoryManager.get().toVersion(p.getFileName().toString())));
        
        if (previousVersion.isPresent()) {
            upgrade(previousVersion.get());
        }
    }
    
    private void upgrade(Path previous) throws IOException {
        FileUtils.copyDirectory(previous.resolve(FileManager.CONF).toFile(), FileManager.USER_CONF_DIR.toFile());
    }
}
