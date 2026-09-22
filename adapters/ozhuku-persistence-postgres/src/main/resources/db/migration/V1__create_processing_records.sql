CREATE TABLE processing_records (
                                    source_identity VARCHAR(512) NOT NULL,
                                    pipeline_id VARCHAR(255) NOT NULL,
                                    pipeline_version BIGINT NOT NULL,
                                    status VARCHAR(32) NOT NULL,
                                    source_fingerprint VARCHAR(1024),
                                    processed_at TIMESTAMP WITH TIME ZONE,

                                    CONSTRAINT pk_processing_records
                                        PRIMARY KEY (
                                                     source_identity,
                                                     pipeline_id,
                                                     pipeline_version
                                            ),

                                    CONSTRAINT chk_processing_records_status
                                        CHECK (
                                            status IN ('NOT_PROCESSED', 'PROCESSED')
                                            ),

                                    CONSTRAINT chk_processing_records_processed_state
                                        CHECK (
                                            (
                                                status = 'PROCESSED'
                                                    AND source_fingerprint IS NOT NULL
                                                    AND processed_at IS NOT NULL
                                                )
                                                OR
                                            (
                                                status = 'NOT_PROCESSED'
                                                    AND source_fingerprint IS NULL
                                                    AND processed_at IS NULL
                                                )
                                            )
);