CREATE TABLE leases (
                     lease_id VARCHAR(255) NOT NULL,
                     execution_id VARCHAR(255) NOT NULL,
                     source_identity VARCHAR(512) NOT NULL,
                     worker_id VARCHAR(255) NOT NULL,
                     status VARCHAR(32) NOT NULL,
                     fencing_token BIGINT NOT NULL,
                     claimed_at TIMESTAMP WITH TIME ZONE NOT NULL,
                     expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
                     revoked_at TIMESTAMP WITH TIME ZONE,

                     CONSTRAINT pk_leases
                         PRIMARY KEY (lease_id),

                     CONSTRAINT chk_leases_status
                         CHECK (
                             status IN (
                                           'CLAIMED',
                                           'EXPIRED',
                                           'REVOKED'
                                       )
                             ),

                     CONSTRAINT chk_leases_fencing_token
                         CHECK (
                             fencing_token >= 0
                             ),

                     CONSTRAINT chk_leases_expiry
                         CHECK (
                             status = 'CLAIMED'
                                 AND expires_at > claimed_at
                             OR
                             status = 'EXPIRED'
                                 AND expires_at > claimed_at
                             OR
                             status = 'REVOKED'
                                 AND revoked_at IS NOT NULL
                             )
);
