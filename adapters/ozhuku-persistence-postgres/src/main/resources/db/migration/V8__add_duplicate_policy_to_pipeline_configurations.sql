ALTER TABLE pipeline_configurations
    ADD COLUMN duplicate_policy VARCHAR(32) NOT NULL
        DEFAULT 'SKIP_IF_PROCESSED';

ALTER TABLE pipeline_configurations
    ADD CONSTRAINT chk_pipeline_configurations_duplicate_policy
        CHECK (
            duplicate_policy IN (
                                 'SKIP_IF_PROCESSED',
                                 'REPROCESS_IF_CHANGED',
                                 'ALWAYS_PROCESS',
                                 'FAIL_IF_DUPLICATE'
                )
            );