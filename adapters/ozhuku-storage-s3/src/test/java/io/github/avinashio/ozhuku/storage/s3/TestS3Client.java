package io.github.avinashio.ozhuku.storage.s3;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Map;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

/**
 * Minimal in-memory test S3 client.
 */
public class TestS3Client implements S3Client {

    private final Map<String, byte[]> objects;

    public TestS3Client(final Map<String, byte[]> objects) {
        this.objects = objects;
    }

    @Override
    public ResponseInputStream<GetObjectResponse> getObject(
            final GetObjectRequest request) {

        final String key = request.bucket() + ":" + request.key();
        final byte[] data = objects.get(key);

        if (data == null) {
            throw NoSuchKeyException.builder()
                    .message("No such key: " + key)
                    .build();
        }

        final GetObjectResponse response = GetObjectResponse.builder()
                .contentLength((long) data.length)
                .lastModified(Instant.now())
                .eTag("test-etag-" + key)
                .build();

        return new ResponseInputStream<>(
                response,
                new ByteArrayInputStream(data));
    }

    @Override
    public HeadObjectResponse headObject(
            final HeadObjectRequest request) {

        final String key = request.bucket() + ":" + request.key();
        final byte[] data = objects.get(key);

        if (data == null) {
            throw NoSuchKeyException.builder()
                    .message("No such key: " + key)
                    .build();
        }

        return HeadObjectResponse.builder()
                .contentLength((long) data.length)
                .lastModified(Instant.now())
                .eTag("test-etag-" + key)
                .versionId("v1")
                .build();
    }

    @Override
    public PutObjectResponse putObject(
            final PutObjectRequest request,
            final InputStream content) {

        final String key = request.bucket() + ":" + request.key();

        final byte[] data;
        try {
            data = content.readAllBytes();
        } catch (final IOException exception) {
            throw new RuntimeException(exception);
        }

        objects.put(key, data);

        return PutObjectResponse.builder()
                .eTag("test-etag-" + key)
                .build();
    }
}
