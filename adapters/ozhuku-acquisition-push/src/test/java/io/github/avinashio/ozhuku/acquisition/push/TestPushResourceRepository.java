package io.github.avinashio.ozhuku.acquisition.push;

import io.github.avinashio.ozhuku.acquisition.PushResource;
import io.github.avinashio.ozhuku.acquisition.PushResourceRepository;
import io.github.avinashio.ozhuku.acquisition.PushResourceStatus;
import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of {@link PushResourceRepository} for testing.
 */
final class TestPushResourceRepository implements PushResourceRepository {

    private final Map<String, PushResource> store;
    boolean saved;

    TestPushResourceRepository() {
        this.store = new ConcurrentHashMap<>();
        this.saved = false;
    }

    @Override
    public void save(final PushResource resource) {
        store.put(resource.resourceId().value(), resource);
        saved = true;
    }

    @Override
    public Optional<PushResource> findById(final ResourceId resourceId) {
        return Optional.ofNullable(
                store.get(resourceId.value()));
    }

    @Override
    public List<PushResource> findAvailable() {
        final List<PushResource> result = new ArrayList<>();
        for (final PushResource resource : store.values()) {
            if (resource.status() == PushResourceStatus.AVAILABLE) {
                result.add(resource);
            }
        }
        return result;
    }

    @Override
    public void updateStatus(
            final ResourceId resourceId,
            final PushResourceStatus status) throws IOException {
        // No-op for testing: store-based tests don't exercise status transitions
    }
}
