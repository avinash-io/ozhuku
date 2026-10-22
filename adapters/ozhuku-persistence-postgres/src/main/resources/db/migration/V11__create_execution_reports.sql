CREATE TABLE execution_reports (
                                 report_id VARCHAR(255) NOT NULL,
                                 generated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                 pipeline_id VARCHAR(255) NOT NULL,
                                 execution_id VARCHAR(255) NOT NULL,
                                 organization_id VARCHAR(255) NOT NULL,
                                 status VARCHAR(32) NOT NULL,
                                 start_time TIMESTAMP WITH TIME ZONE,
                                 end_time TIMESTAMP WITH TIME ZONE,
                                 files_discovered INTEGER NOT NULL DEFAULT 0,
                                 files_processed INTEGER NOT NULL DEFAULT 0,
                                 files_failed INTEGER NOT NULL DEFAULT 0,
                                 files_skipped INTEGER NOT NULL DEFAULT 0,
                                 destination_outcomes_json JSONB,
                                 overall_status VARCHAR(64) NOT NULL,
                                 errors TEXT,
                                 audit_event_id VARCHAR(255),

                                 CONSTRAINT pk_execution_reports
                                     PRIMARY KEY (report_id),

                                 CONSTRAINT chk_execution_reports_status
                                     CHECK (
                                         status IN (
                                                          'PENDING',
                                                          'RUNNING',
                                                          'COMPLETED',
                                                          'FAILED',
                                                          'CANCELLED'
                                                      )
                                     )
);