CREATE TABLE destination_commits (
                                     execution_id VARCHAR(255) NOT NULL,
                                     resource_id VARCHAR(255) NOT NULL,
                                     status VARCHAR(32) NOT NULL,
                                     committed_at TIMESTAMP WITH TIME ZONE,

                                     CONSTRAINT pk_destination_commits
                                         PRIMARY KEY (
                                                      execution_id,
                                                      resource_id
                                             ),

                                     CONSTRAINT chk_destination_commits_status
                                         CHECK (
                                             status IN (
                                                        'NOT_COMMITTED',
                                                        'COMMITTED',
                                                        'UNKNOWN'
                                                 )
                                             ),

                                     CONSTRAINT chk_destination_commits_committed_at
                                         CHECK (
                                             (
                                                 status = 'COMMITTED'
                                                     AND committed_at IS NOT NULL
                                                 )
                                                 OR
                                             (
                                                 status IN ('NOT_COMMITTED', 'UNKNOWN')
                                                     AND committed_at IS NULL
                                                 )
                                             )
);