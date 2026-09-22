CREATE TABLE flow_executions (
                                 execution_id VARCHAR(255) NOT NULL,
                                 flow_id VARCHAR(255) NOT NULL,
                                 status VARCHAR(32) NOT NULL,
                                 started_at TIMESTAMP WITH TIME ZONE,
                                 completed_at TIMESTAMP WITH TIME ZONE,

                                 CONSTRAINT pk_flow_executions
                                     PRIMARY KEY (
                                                  execution_id,
                                                  flow_id
                                         ),

                                 CONSTRAINT chk_flow_executions_status
                                     CHECK (
                                         status IN (
                                                    'PENDING',
                                                    'RUNNING',
                                                    'COMPLETED',
                                                    'FAILED',
                                                    'CANCELLED'
                                             )
                                         ),

                                 CONSTRAINT chk_flow_executions_started_at
                                     CHECK (
                                         status = 'PENDING'
                                             OR started_at IS NOT NULL
                                             OR status = 'CANCELLED'
                                         ),

                                 CONSTRAINT chk_flow_executions_completed_at
                                     CHECK (
                                         status IN ('PENDING', 'RUNNING')
                                             OR completed_at IS NOT NULL
                                         ),

                                 CONSTRAINT chk_flow_executions_timestamp_order
                                     CHECK (
                                         started_at IS NULL
                                             OR completed_at IS NULL
                                             OR completed_at >= started_at
                                         )
);