-- ============================================================
-- Миграция V3: Добавление дополнительных ограничений и индексов
-- для системы проектов
-- ============================================================

-- Добавление CHECK ограничений (если поддерживается версией MySQL)
-- Для MySQL 8.0.16+ можно использовать CHECK ограничения

-- Проверка корректности статуса проекта
ALTER TABLE projects
ADD CONSTRAINT chk_projects_status
CHECK (status IN ('ACTIVE', 'ANNUAL', 'PLANNED', 'ARCHIVE', 'PAUSED', 'CANCELLED'));

-- Проверка корректности категории проекта
ALTER TABLE projects
ADD CONSTRAINT chk_projects_category
CHECK (category IN ('FESTIVAL', 'CONTEST', 'CHARITY', 'THEATER', 'MUSIC', 'LITERARY', 'EXHIBITION', 'CHILDREN', 'FOLK', 'INTERNATIONAL', 'OTHER'));

-- Проверка корректности категории члена команды
ALTER TABLE team_members
ADD CONSTRAINT chk_team_members_category
CHECK (category IN ('MANAGEMENT', 'COORDINATOR', 'TEAM', 'VOLUNTEER', 'PARTNER', 'CONSULTANT'));

-- Проверка корректности типа партнерства
ALTER TABLE project_partners
ADD CONSTRAINT chk_project_partners_type
CHECK (partnership_type IN ('GENERAL_SPONSOR', 'SPONSOR', 'PARTNER', 'INFO_PARTNER', 'TECH_PARTNER', 'MEDIA_PARTNER', 'VOLUNTEER', 'OTHER'));

-- Проверка дат проекта (начало <= окончание)
ALTER TABLE projects
ADD CONSTRAINT chk_projects_dates
CHECK (start_date <= end_date OR end_date IS NULL);

-- Дополнительные индексы для сложных запросов

-- Индекс для поиска по описанию (используется префикс для TEXT полей)
CREATE FULLTEXT INDEX idx_projects_description_search
ON projects(title, short_description, full_description);

-- Индекс для поиска членов команды по полному имени и должности
CREATE FULLTEXT INDEX idx_team_members_search
ON team_members(last_name, first_name, middle_name, position, bio);

-- Индекс для поиска по местоположению проектов
CREATE INDEX idx_projects_location_full
ON projects(location(100));

-- Составной индекс для фильтрации по дате и статусу
CREATE INDEX idx_projects_status_date
ON projects(status, start_date, end_date);

-- Индекс для подсчета статистики
CREATE INDEX idx_projects_created_status
ON projects(created_at, status);

-- Индекс для связи проектов с галереей
CREATE INDEX idx_projects_gallery
ON projects(gallery_id) WHERE gallery_id IS NOT NULL;

-- Индекс для связи проектов с обложкой
CREATE INDEX idx_projects_cover
ON projects(cover_image_id) WHERE cover_image_id IS NOT NULL;

-- Триггер для автоматического обновления updated_at
DELIMITER $$

CREATE TRIGGER before_projects_update
BEFORE UPDATE ON projects
FOR EACH ROW
BEGIN
    SET NEW.updated_at = NOW();
END$$

CREATE TRIGGER before_team_members_update
BEFORE UPDATE ON team_members
FOR EACH ROW
BEGIN
    SET NEW.updated_at = NOW();
END$$

CREATE TRIGGER before_project_videos_update
BEFORE UPDATE ON project_videos
FOR EACH ROW
BEGIN
    SET NEW.updated_at = NOW();
END$$

CREATE TRIGGER before_project_partners_update
BEFORE UPDATE ON project_partners
FOR EACH ROW
BEGIN
    SET NEW.updated_at = NOW();
END$$

DELIMITER ;

-- Представления для упрощения запросов

-- Представление для активных проектов
CREATE OR REPLACE VIEW active_projects AS
SELECT
    p.id,
    p.title,
    p.slug,
    p.category,
    p.status,
    p.short_description,
    p.start_date,
    p.end_date,
    p.location,
    COUNT(DISTINCT v.id) as video_count,
    COUNT(DISTINCT pr.id) as partner_count,
    COUNT(DISTINCT ptm.team_member_id) as team_count
FROM projects p
LEFT JOIN project_videos v ON p.id = v.project_id
LEFT JOIN project_partners pr ON p.id = pr.project_id AND pr.is_active = TRUE
LEFT JOIN project_team_members ptm ON p.id = ptm.project_id
WHERE p.status = 'ACTIVE'
    AND (p.end_date IS NULL OR p.end_date >= CURDATE())
GROUP BY p.id;

-- Представление для проектов с детальной информацией
CREATE OR REPLACE VIEW project_details AS
SELECT
    p.*,
    g.title as gallery_title,
    mf.file_name as cover_image_name,
    mf.file_path as cover_image_path
FROM projects p
LEFT JOIN photo_gallery g ON p.gallery_id = g.id
LEFT JOIN media_file mf ON p.cover_image_id = mf.id;

-- Процедура для автоматического архивирования завершенных проектов
DELIMITER $$

CREATE PROCEDURE archive_completed_projects()
BEGIN
    UPDATE projects
    SET status = 'ARCHIVE'
    WHERE status = 'ACTIVE'
        AND end_date < CURDATE()
        AND end_date IS NOT NULL;
END$$

DELIMITER ;

-- Событие для автоматического архивирования (раз в день)
CREATE EVENT IF NOT EXISTS auto_archive_projects
ON SCHEDULE EVERY 1 DAY
STARTS CURRENT_TIMESTAMP
DO
    CALL archive_completed_projects();