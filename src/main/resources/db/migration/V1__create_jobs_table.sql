CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE IF NOT EXISTS jobs (
    id                UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    status            VARCHAR(20)  NOT NULL DEFAULT 'RECEBIDO',
    s3_key            VARCHAR(512) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    file_type         VARCHAR(10)  NOT NULL,
    file_size_bytes   BIGINT       NOT NULL,
    description       VARCHAR(500),
    error_message     TEXT,
    report_id         UUID,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT jobs_status_check CHECK (
        status IN ('RECEBIDO', 'EM_PROCESSAMENTO', 'ANALISADO', 'ERRO')
    ),
    CONSTRAINT jobs_file_type_check CHECK (
        file_type IN ('PDF', 'PNG', 'JPG', 'JPEG')
    ),
    CONSTRAINT jobs_file_size_check CHECK (
        file_size_bytes > 0 AND file_size_bytes <= 10485760
    )
);

CREATE INDEX IF NOT EXISTS idx_jobs_status     ON jobs (status);
CREATE INDEX IF NOT EXISTS idx_jobs_created_at ON jobs (created_at DESC);

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trg_jobs_updated_at
    BEFORE UPDATE ON jobs
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
