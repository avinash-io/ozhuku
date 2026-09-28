package io.github.avinashio.ozhuku.storage;

import io.github.avinashio.ozhuku.domain.identity.SourceFingerprint;
import io.github.avinashio.ozhuku.domain.resource.Resource;
import java.io.IOException;

public interface SourceFingerprintProvider {

    SourceFingerprint fingerprint(
            Resource resource) throws IOException;
}