ALTER TABLE app_version
  ADD COLUMN channel VARCHAR(32) NOT NULL DEFAULT 'OFFICIAL' AFTER platform,
  ADD COLUMN rollout_percent INT NOT NULL DEFAULT 100 AFTER force_update,
  ADD COLUMN package_sha256 CHAR(64) DEFAULT NULL AFTER rollout_percent,
  ADD COLUMN min_supported_version_code INT NOT NULL DEFAULT 0 AFTER package_sha256;

CREATE INDEX idx_app_version_release ON app_version(platform, channel, status, version_code);
ALTER TABLE app_version DROP INDEX uk_platform_version_code,
  ADD UNIQUE KEY uk_platform_channel_version_code(platform, channel, version_code);
