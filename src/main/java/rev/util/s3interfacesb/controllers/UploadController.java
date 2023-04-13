package rev.util.s3interfacesb.controllers;

import com.amazonaws.services.s3.transfer.Upload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rev.util.s3interfacesb.models.ResponseData;
import rev.util.s3interfacesb.models.UploadResponse;
import rev.util.s3interfacesb.services.S3Services;

import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/{prefix}/upload")
public class UploadController {
    private final S3Services s3Services;
    private final String baseUrl;
    private final String servingDir;

    UploadController(
        @Autowired S3Services s3Services,
        @Value("${web.base_url}") String baseUrl,
        @Value("${web.serving_dir}") String servingDir
    ) {
        // remove / at the end of URL if any
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        this.baseUrl = baseUrl;
        this.servingDir = servingDir;
        this.s3Services = s3Services;
    }
    @GetMapping
    public ResponseEntity<Map<String, String>>uploadGET(@PathVariable String prefix) {
        return ResponseEntity.ok(Map.of("message", prefix + " upload service OK"));
    }

    // accept multipart/form-data with UTF8
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<ResponseData<UploadResponse>> uploadPOST(
            @RequestParam("file") MultipartFile file,
            @PathVariable String prefix) {
        try {
            // if file not exists
            if (file.isEmpty()) {
                ResponseData<UploadResponse> responseData = new ResponseData<>(false, "File not found", null);
                return new ResponseEntity<>(responseData, HttpStatus.BAD_REQUEST);
            }
            // check if type jpg / jpeg / png
            if (!Objects.equals(file.getContentType(), "image/jpeg") && !Objects.equals(file.getContentType(), "image/png")) {
                ResponseData<UploadResponse> responseData = new ResponseData<>(false, "File type not supported", null);
                return new ResponseEntity<>(responseData, HttpStatus.BAD_REQUEST);
            }
//            log.debug("File size: " + is.available());
            InputStreamResource isr = new InputStreamResource(file.getInputStream());
            // generate filename with timestamp
            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String finalfilename = s3Services.uploadWithPrefix(prefix, filename, isr, file.getContentType(), file.getInputStream().available());
            ResponseData<UploadResponse> responseData = new ResponseData<>(true,
        "File uploaded successfully",
                new UploadResponse(baseUrl + "/" + prefix + "/" + servingDir + "/" + finalfilename)
            );
            return new ResponseEntity<>(responseData, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error uploading file", e);
            ResponseData<UploadResponse> responseData = new ResponseData<>(false, "Error uploading file", null);
            return new ResponseEntity<>(responseData, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
