CREATE TABLE audit_events (
                                 event_id VARCHAR(255) NOT NULL,
                                 timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
                                 organization_id VARCHAR(255) NOT NULL,
                                 actor VARCHAR(255) NOT NULL,
                                 event_type VARCHAR(64) NOT NULL,
                                 resource_type VARCHAR(255) NOT NULL,
                                 resource_id VARCHAR(255) NOT NULL,
                                 pipeline_id VARCHAR(255),
                                 execution_id VARCHAR(255),
                                 source_identity VARCHAR(255),
                                 previous_state TEXT,
                                 new_state TEXT,
                                 reason TEXT,
                                 outcome VARCHAR(64) NOT NULL,
                                 correlation_id VARCHAR(255) NOT NULL,
                                 metadata_json JSONB,

                                 CONSTRAINT pk_audit_events
                                     PRIMARY KEY (event_id),

                                 CONSTRAINT chk_audit_events_type
                                     CHECK (
                                         event_type IN (
                                                          'REPROCESSING',
                                                          'DUPLICATE_DETECTION',
                                                          'SOURCE_CHANGE',
                                                          'SOURCE_DELETION',
                                                          'DESTINATION_REPLACEMENT',
                                                          'MANUAL_RETRY',
                                                          'CONFIGURATION_CHANGE',
                                                          'PIPELINE_PUBLISHING',
                                                          'CONNECTION_CHANGE',
                                                          'SECRET_ROTATION',
                                                          'AUTHORIZATION_FAILURE',
                                                          'WORKER_FENCING',
                                                          'LEASE_TAKEOVER',
                                                          'RECOVERY',
                                                          'DIAGNOSTICS',
                                                          'TEST_EMAIL_OPERATION'
                                                      )
                                     )
);