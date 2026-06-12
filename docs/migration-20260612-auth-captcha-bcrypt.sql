USE qsl_tracker;

ALTER TABLE sys_user
  MODIFY COLUMN password_hash VARCHAR(100) NOT NULL,
  MODIFY COLUMN password_salt VARCHAR(64) NULL,
  ADD COLUMN must_change_password TINYINT(1) NOT NULL DEFAULT 0 AFTER password_salt;

-- After this migration, start the application once with qsl.user.init-enabled=true.
-- The startup migration resets a legacy MD5 admin to qsl.user.password and requires
-- an immediate password change. Other legacy MD5 users are disabled.
