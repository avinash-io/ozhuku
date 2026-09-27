CREATE TABLE pipeline_configurations (
                                         pipeline_id VARCHAR(255) NOT NULL,
                                         pipeline_version BIGINT NOT NULL,
                                         description VARCHAR(4000) NOT NULL,

                                         flow_id VARCHAR(255) NOT NULL,
                                         flow_name VARCHAR(255) NOT NULL,
                                         flow_mode VARCHAR(32) NOT NULL,

                                         source_resource_id VARCHAR(255) NOT NULL,
                                         source_location VARCHAR(2048) NOT NULL,

                                         destination_resource_id VARCHAR(255) NOT NULL,
                                         destination_location VARCHAR(2048) NOT NULL,

                                         conflict_behavior VARCHAR(32) NOT NULL,

                                         CONSTRAINT pk_pipeline_configurations
                                             PRIMARY KEY (
                                                          pipeline_id,
                                                          pipeline_version
                                                 ),

                                         CONSTRAINT chk_pipeline_configurations_version
                                             CHECK (
                                                 pipeline_version > 0
                                                 ),

                                         CONSTRAINT chk_pipeline_configurations_flow_mode
                                             CHECK (
                                                 flow_mode IN (
                                                               'RESOURCE_TRANSFER',
                                                               'RECORD_PROCESSING',
                                                               'RESOURCE_PROCESSING'
                                                     )
                                                 ),

                                         CONSTRAINT chk_pipeline_configurations_conflict_behavior
                                             CHECK (
                                                 conflict_behavior IN (
                                                                       'FAIL',
                                                                       'REPLACE',
                                                                       'SKIP',
                                                                       'VERSION'
                                                     )
                                                 )
);