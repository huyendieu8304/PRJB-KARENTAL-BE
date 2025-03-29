package com.mp.karental.service;

import com.mp.karental.exception.AppException;
import com.mp.karental.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;

/**
 * This is the service handle upload file to s3 and get url of the file
 *
 * @author DieuTTH4
 *
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class FileService {
    S3Client s3Client;
    S3Presigner s3Presigner;

    @NonFinal
    @Value("${cloud.aws.s3.buckets.name}")
    String bucketName;

    /**
     * Uploads a file to the specified S3 bucket with the given key.
     *
     * @param file the file to be uploaded (as MultipartFile)
     * @param key  the key (path/filename) under which the file will be stored in the S3 bucket
     * @throws AppException if there is an error during the file upload process
     * @return true if successfully upload file
     */
    public boolean uploadFile(MultipartFile file, String key) {
        //upload object to s3
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
            log.info("Upload file {} to S3 successful", key);
            return true;
        } catch (IOException e) {
            log.info("Upload file {} to S3 failed", key);
            throw new AppException(ErrorCode.UPLOAD_OBJECT_TO_S3_FAIL);
        }
    }

    /**
     * Generates a presigned URL for accessing a file stored in the S3 bucket.
     * This URL is temporary and valid for 30 minutes.
     *
     * @param uri the key (path/filename) of the file stored in the S3 bucket
     * @return the presigned URL as a String
     */
    public String getFileUrl(String uri) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(uri)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(30)) //allow this url to be access in 30
                .getObjectRequest(getObjectRequest)
                .build();
        log.info("Get url of the file with the key={} successful", uri);
        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    /**
     * Get the file extension (.png, .jpg .pdf ...)
     * @param file the file
     * @return the file extension
     */
    public String getFileExtension(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        //File name exist
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
        }
        return ""; // return empty string if the file doesn't has the extension
    }
}
