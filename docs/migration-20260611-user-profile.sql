ALTER TABLE admin_user
  ADD COLUMN call_sign VARCHAR(32) NULL COMMENT '呼号' AFTER username,
  ADD COLUMN email VARCHAR(128) NULL COMMENT '电子邮箱' AFTER call_sign,
  ADD COLUMN mailing_address VARCHAR(500) NULL COMMENT '邮寄地址' AFTER email,
  ADD COLUMN phone VARCHAR(32) NULL COMMENT '联系电话' AFTER mailing_address,
  ADD COLUMN recipient VARCHAR(128) NULL COMMENT '收件人' AFTER phone,
  ADD COLUMN postal_code VARCHAR(32) NULL COMMENT '邮编' AFTER recipient;
