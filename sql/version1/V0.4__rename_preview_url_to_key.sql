-- ============================================================
-- V0.4 重命名 preview_url → preview_key
-- 说明：数据库存储的是 MinIO 对象 Key，不是 URL，统一命名
-- ============================================================

-- 1. t_biz_photo: preview_url → preview_key
ALTER TABLE t_biz_photo CHANGE preview_url preview_key VARCHAR(255) NOT NULL COMMENT '预览图OSS Key';

-- 2. t_biz_order: photo_preview_url → photo_preview_key
ALTER TABLE t_biz_order CHANGE photo_preview_url photo_preview_key VARCHAR(255) NOT NULL COMMENT '预览图OSS Key（快照）';

-- 3. t_biz_license: photo_preview_url → photo_preview_key
ALTER TABLE t_biz_license CHANGE photo_preview_url photo_preview_key VARCHAR(255) NOT NULL COMMENT '预览图OSS Key（冗余）';
