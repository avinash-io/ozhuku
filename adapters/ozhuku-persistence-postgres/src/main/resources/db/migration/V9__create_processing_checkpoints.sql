CREATE TABLE processing_checkpoints (
                                        execution_id VARCHAR(255) NOT NULL,
                                        resource_id VARCHAR(255) NOT NULL,
                                        record_sequence BIGINT NOT NULL,
                                        checkpointed_at TIMESTAMP WITH TIME ZONE NOT NULL,

                                        CONSTRAINT pk_processing_checkpoints
                                            PRIMARY KEY (
                                                         execution_id,
                                                         resource_id
                                                ),

                                        CONSTRAINT chk_processing_checkpoints_record_sequence
                                            CHECK (
                                                record_sequence >= 0
                                                )
);