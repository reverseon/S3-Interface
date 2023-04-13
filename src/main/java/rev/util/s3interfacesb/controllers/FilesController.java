package rev.util.s3interfacesb.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rev.util.s3interfacesb.services.S3Services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@Slf4j
@RestController
public class FilesController {
    private final S3Services s3Services;
    FilesController(@Autowired S3Services s3Services) {
        this.s3Services = s3Services;
    }
    @GetMapping("/{prefix}/${web.serving_dir}/{filename}")
    public ResponseEntity<Resource> getFiles(@PathVariable String prefix, @PathVariable String filename) {
        try {
            InputStream is = s3Services.retrieveFromPrefix(prefix, filename).getInputStream();
            // buffer the inputstream to avoid connection reset
            ByteArrayOutputStream ostream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                ostream.write(buffer, 0, len);
            }
            is.close();
            byte[] imageBytes = ostream.toByteArray();
            return ResponseEntity.ok()
                    .contentType(s3Services.getContentType(prefix, filename))
                    .contentLength(s3Services.getContentLength(prefix, filename))
                    .body(
                            new InputStreamResource(
                                    new ByteArrayInputStream(imageBytes)
                            )
                    );
        } catch (Exception e) {
            log.error("Error retrieving file", e);
            return ResponseEntity.notFound().build();
        }
    }

}
