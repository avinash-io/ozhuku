package io.github.avinashio.ozhuku.storage;

import io.github.avinashio.ozhuku.domain.identity.SourceIdentity;
import io.github.avinashio.ozhuku.domain.resource.Resource;
import java.io.IOException;

public interface SourceIdentityProvider {

    SourceIdentity identify(
            Resource resource) throws IOException;
}