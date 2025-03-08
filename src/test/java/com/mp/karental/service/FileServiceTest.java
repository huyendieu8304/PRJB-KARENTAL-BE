package com.mp.karental.service;

import com.mp.karental.exception.AppException;
import com.mp.karental.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is a class used to test FileService, service used with AWS S3
 *
 * @author DieuTTH4
 *
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private MultipartFile file;
    @Mock
    private S3Presigner s3Presigner;

    @InjectMocks
    private FileService fileService;

    private static String bucketName;
    private static String key;


    @BeforeEach
    public void setUp() {
        bucketName = "test-bucket";
        key = "test-file.txt";
    }

    @Test
    public void testUploadFile_Success() throws IOException {
        byte[] fileBytes = "Test file content".getBytes();
        when(file.getBytes()).thenReturn(fileBytes);

        // Use ArgumentCaptor to catch PutObjectRequest, which create internal in the method
        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);

        // Mock s3Client.putObject to check parameter
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(null);

        // Call the tested method
        fileService.uploadFile(file, key);

        // Assert
        // check s3Client.putObject is called?
        verify(s3Client, times(1)).putObject(captor.capture(), any(RequestBody.class));


        //check value of PutObjectRequest catched
        PutObjectRequest capturedRequest = captor.getValue();
//        assertEquals(key, capturedRequest.key());
    }

    @Test
    public void testUploadFile_Failure() throws IOException {
        // Arrange
        when(file.getBytes()).thenThrow(new IOException("Test exception"));

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> fileService.uploadFile(file, key));
        assertEquals(ErrorCode.UPLOAD_OBJECT_TO_S3_FAIL, exception.getErrorCode());
    }

    @Test
    public void testGetPresignedUrl_Success() throws MalformedURLException {

        String expectedUrl = "http://example.com/signed-url";

        PresignedGetObjectRequest presignedRequest = mock(PresignedGetObjectRequest.class);
        when(presignedRequest.url()).thenReturn(new URL(expectedUrl));

        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presignedRequest);

        // Act
        String actualUrl = fileService.getFileUrl(key);

        // Assert: Kiểm tra URL trả về có bằng expectedUrl không
        assertEquals(expectedUrl, actualUrl);

        //Check parameter in GetObjectPresignRequest
        ArgumentCaptor<GetObjectPresignRequest> captor = ArgumentCaptor.forClass(GetObjectPresignRequest.class);
        verify(s3Presigner).presignGetObject(captor.capture());
        GetObjectPresignRequest capturedRequest = captor.getValue();

        // duration of the url
        assertEquals(Duration.ofMinutes(30), capturedRequest.signatureDuration());

        GetObjectRequest getObjectRequest = capturedRequest.getObjectRequest();
        assertEquals(key, getObjectRequest.key());
    }

    @Test
    void getFileExtension_ShouldReturnExtension_WhenFileNameHasExtension() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("example.jpg");

        String extension = fileService.getFileExtension(file);

        assertEquals(".jpg", extension);
    }

    @Test
    void getFileExtension_ShouldReturnEmptyString_WhenFileNameHasNoExtension() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("example");

        String extension = fileService.getFileExtension(file);

        assertEquals("", extension);
    }

    @Test
    void getFileExtension_ShouldReturnEmptyString_WhenFileNameIsNull() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn(null);

        String extension = fileService.getFileExtension(file);

        assertEquals("", extension);
    }
}