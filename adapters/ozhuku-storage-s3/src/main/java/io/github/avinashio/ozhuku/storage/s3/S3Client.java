package io.github.avinashio.ozhuku.storage.s3;

import java.io.IOException;
import java.io.InputStream;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

/**
 * Minimal S3 operations interface for Ozhuku storage adapters.
 *
 * <p>This interface abstracts the subset of S3 operations needed
 * by the storage adapter. It allows the adapter to work with any
 * S3-compatible client, including test doubles, without requiring
 * a full {@link software.amazon.awssdk.services.s3.S3Client}
 * implementation.</p>
 */
public interface S3Client {

    /**
     * Reads an S3 object and returns its content as a stream.
     *
     * @param request the get object request
     * @return a streaming response containing the object content
     * @throws software.amazon.awssdk.services.s3.model.NoSuchKeyException
     *         if the object does not exist
     */
    ResponseInputStream<GetObjectResponse> getObject(
            GetObjectRequest request);

    /**
     * Checks whether an S3 object exists and retrieves its metadata.
     *
     * @param request the head object request
     * @return the object metadata
     * @throws software.amazon.awssdk.services.s3.model.NoSuchKeyException
     *         if the object does not exist
     */
    HeadObjectResponse headObject(
            HeadObjectRequest request);

    /**
     * Writes an S3 object.
     *
     * @param request the put object request
     * @param content the object content provider
     * @return the put object result
     */
    PutObjectResponse putObject(
            PutObjectRequest request,
            InputStream content);
}
