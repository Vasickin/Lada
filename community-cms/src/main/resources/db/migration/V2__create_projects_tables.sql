-- ============================================================
-- Миграция V2: Создание таблиц для системы проектов
-- Организация: Русская община "ЛАДА"
-- Автор: Vasickin
-- Дата: 2024
-- Описание: Создание таблиц для управления проектами организации
-- ============================================================

-- Таблица проектов организации
CREATE TABLE IF NOT EXISTS projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Уникальный идентификатор проекта',

    -- Основная информация
    title VARCHAR(200) NOT NULL COMMENT 'Название проекта',
    slug VARCHAR(200) NOT NULL UNIQUE COMMENT 'URL-идентификатор проекта (ЧПУ)',
    category VARCHAR(50) NOT NULL COMMENT 'Категория проекта (фестиваль, конкурс и т.д.)',
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Статус проекта (активный, архивный и т.д.)',

    -- Описание
    short_description VARCHAR(500) COMMENT 'Краткое описание для карточек',
    full_description TEXT COMMENT 'Полное описание проекта',
    goals TEXT COMMENT 'Цели и задачи проекта',

    -- Организация
    location VARCHAR(200) COMMENT 'Место проведения',
    start_date DATE COMMENT 'Дата начала',
    end_date DATE COMMENT 'Дата окончания',
    curator_contacts VARCHAR(500) COMMENT 'Контакты куратора',
    participation_info TEXT COMMENT 'Информация об участии',

    -- Связи с существующими таблицами
    gallery_id BIGINT COMMENT 'Ссылка на галерею фотографий',
    cover_image_id BIGINT COMMENT 'Ссылка на изображение обложки',

    -- Настройки отображения секций
    show_description BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Показывать секцию описания',
    show_photos BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Показывать секцию фотогалереи',
    show_videos BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Показывать секцию видео',
    show_team BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Показывать секцию команды',
    show_participation BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Показывать секцию участия',
    show_partners BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Показывать секцию партнеров',
    show_related BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Показывать секцию похожих проектов',
    sections_order TEXT COMMENT 'Порядок отображения секций (JSON)',

    -- SEO оптимизация
    meta_title VARCHAR(200) COMMENT 'SEO заголовок (meta title)',
    meta_description VARCHAR(500) COMMENT 'SEO описание (meta description)',
    meta_keywords VARCHAR(500) COMMENT 'Ключевые слова (meta keywords)',

    -- Технические поля
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT 'Дата создания',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL COMMENT 'Дата обновления',

    -- Внешние ключи
    FOREIGN KEY (gallery_id) REFERENCES photo_gallery(id) ON DELETE SET NULL,
    FOREIGN KEY (cover_image_id) REFERENCES media_file(id) ON DELETE SET NULL,

    -- Индексы для оптимизации
    INDEX idx_projects_slug (slug),
    INDEX idx_projects_category (category),
    INDEX idx_projects_status (status),
    INDEX idx_projects_start_date (start_date),
    INDEX idx_projects_end_date (end_date),
    INDEX idx_projects_created_at (created_at),
    INDEX idx_projects_location (location),
    INDEX idx_projects_category_status (category, status),
    INDEX idx_projects_date_range (start_date, end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Проекты организации';

-- Таблица членов команды организации
CREATE TABLE IF NOT EXISTS team_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Уникальный идентификатор члена команды',

    -- Личная информация
    first_name VARCHAR(50) NOT NULL COMMENT 'Имя',
    last_name VARCHAR(50) NOT NULL COMMENT 'Фамилия',
    middle_name VARCHAR(50) COMMENT 'Отчество',

    -- Профессиональная информация
    position VARCHAR(100) NOT NULL COMMENT 'Должность в организации',
    bio TEXT COMMENT 'Биография',
    education VARCHAR(500) COMMENT 'Образование',
    experience VARCHAR(1000) COMMENT 'Опыт работы',
    achievements TEXT COMMENT 'Достижения и награды',

    -- Контактная информация
    email VARCHAR(100) NOT NULL UNIQUE COMMENT 'Email для связи',
    phone VARCHAR(20) COMMENT 'Телефон',

    -- Изображения
    avatar_id BIGINT COMMENT 'Ссылка на аватар',

    -- Социальные сети
    social_link VARCHAR(500) COMMENT 'Ссылка на основную социальную сеть',
    social_type VARCHAR(20) COMMENT 'Тип социальной сети',
    additional_socials TEXT COMMENT 'Дополнительные социальные сети (JSON)',

    -- Классификация
    category VARCHAR(50) NOT NULL DEFAULT 'TEAM' COMMENT 'Категория (руководство, волонтер и т.д.)',
    access_level VARCHAR(20) COMMENT 'Уровень доступа в системе',

    -- Настройки отображения
    sort_order INT NOT NULL DEFAULT 0 COMMENT 'Порядок сортировки',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Активен ли член команды',
    show_contacts BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Показывать контакты публично',
    show_in_team_list BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Показывать в списке команды',

    -- Дополнительная информация
    notes TEXT COMMENT 'Примечания (внутренняя информация)',

    -- Технические поля
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT 'Дата создания',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL COMMENT 'Дата обновления',

    -- Внешние ключи
    FOREIGN KEY (avatar_id) REFERENCES media_file(id) ON DELETE SET NULL,

    -- Индексы для оптимизации
    INDEX idx_team_members_name (last_name, first_name),
    INDEX idx_team_members_position (position),
    INDEX idx_team_members_category (category),
    INDEX idx_team_members_active (is_active),
    INDEX idx_team_members_email (email),
    INDEX idx_team_members_sort_order (sort_order),
    INDEX idx_team_members_full_name (last_name, first_name, middle_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Члены команды организации';

-- Таблица видео проектов
CREATE TABLE IF NOT EXISTS project_videos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Уникальный идентификатор видео',
    project_id BIGINT NOT NULL COMMENT 'Ссылка на проект',

    -- Основная информация
    title VARCHAR(200) NOT NULL COMMENT 'Название видео',
    description VARCHAR(1000) COMMENT 'Описание видео',

    -- Ссылки на видео
    youtube_url VARCHAR(500) COMMENT 'Ссылка на YouTube',
    vimeo_url VARCHAR(500) COMMENT 'Ссылка на Vimeo',

    -- Метаданные
    duration_seconds INT COMMENT 'Длительность в секундах',
    is_main BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Является ли основным видео проекта',
    sort_order INT NOT NULL DEFAULT 0 COMMENT 'Порядок сортировки',
    view_count BIGINT COMMENT 'Количество просмотров',
    published_at TIMESTAMP COMMENT 'Дата публикации на платформе',
    thumbnail_path VARCHAR(500) COMMENT 'Путь к миниатюре',

    -- Технические поля
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT 'Дата создания',
    updated_at TIMESTAMP COMMENT 'Дата обновления',

    -- Внешние ключи
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,

    -- Индексы для оптимизации
    INDEX idx_project_videos_project (project_id),
    INDEX idx_project_videos_main (is_main),
    INDEX idx_project_videos_order (sort_order),
    INDEX idx_project_videos_created (created_at),
    INDEX idx_project_videos_youtube (youtube_url(100)),
    INDEX idx_project_videos_vimeo (vimeo_url(100)),
    UNIQUE INDEX uk_project_videos_url (project_id, youtube_url(100), vimeo_url(100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Видео проектов';

-- Таблица партнеров проектов
CREATE TABLE IF NOT EXISTS project_partners (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Уникальный идентификатор партнера',
    project_id BIGINT NOT NULL COMMENT 'Ссылка на проект',

    -- Основная информация
    name VARCHAR(200) NOT NULL COMMENT 'Название партнера/организации',
    description VARCHAR(1000) COMMENT 'Описание партнера',
    partnership_type VARCHAR(50) COMMENT 'Тип партнерства (спонсор, медиа-партнер и т.д.)',

    -- Изображения и ссылки
    logo_id BIGINT COMMENT 'Ссылка на логотип',
    website_url VARCHAR(500) COMMENT 'Ссылка на веб-сайт',

    -- Контактная информация
    email VARCHAR(100) COMMENT 'Email для связи',
    phone VARCHAR(20) COMMENT 'Телефон',
    contact_person VARCHAR(100) COMMENT 'Контактное лицо',
    additional_info TEXT COMMENT 'Дополнительная информация',

    -- Настройки
    sort_order INT NOT NULL DEFAULT 0 COMMENT 'Порядок сортировки',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Активен ли партнер',
    is_main BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Является ли главным партнером',

    -- Технические поля
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT 'Дата создания',
    updated_at TIMESTAMP COMMENT 'Дата обновления',

    -- Внешние ключи
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (logo_id) REFERENCES media_file(id) ON DELETE SET NULL,

    -- Индексы для оптимизации
    INDEX idx_project_partners_project (project_id),
    INDEX idx_project_partners_name (name),
    INDEX idx_project_partners_type (partnership_type),
    INDEX idx_project_partners_active (is_active),
    INDEX idx_project_partners_order (sort_order),
    INDEX idx_project_partners_created (created_at),
    UNIQUE INDEX uk_project_partners_name (project_id, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Партнеры проектов';

-- Таблица связи проектов с членами команды (многие-ко-многим с дополнительным атрибутом)
CREATE TABLE IF NOT EXISTS project_team_members (
    project_id BIGINT NOT NULL COMMENT 'Ссылка на проект',
    team_member_id BIGINT NOT NULL COMMENT 'Ссылка на члена команды',
    project_role VARCHAR(100) NOT NULL DEFAULT 'Участник' COMMENT 'Роль в проекте',

    -- Технические поля
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT 'Дата назначения',

    -- Первичный ключ
    PRIMARY KEY (project_id, team_member_id),

    -- Внешние ключи
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (team_member_id) REFERENCES team_members(id) ON DELETE CASCADE,

    -- Индексы для оптимизации
    INDEX idx_project_team_members_member (team_member_id),
    INDEX idx_project_team_members_role (project_role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Связь проектов с членами команды';

-- ============================================================
-- ДАННЫЕ ДЛЯ ТЕСТИРОВАНИЯ (опционально)
-- ============================================================

-- Вставка тестовых категорий членов команды
INSERT IGNORE INTO team_members (id, first_name, last_name, position, email, category, is_active) VALUES
(1, 'Иван', 'Петров', 'Директор проектов', 'ivan.petrov@lada.org', 'MANAGEMENT', TRUE),
(2, 'Мария', 'Сидорова', 'Координатор мероприятий', 'maria.sidorova@lada.org', 'COORDINATOR', TRUE),
(3, 'Алексей', 'Иванов', 'Волонтер', 'alexey.ivanov@lada.org', 'VOLUNTEER', TRUE),
(4, 'Елена', 'Кузнецова', 'PR-менеджер', 'elena.kuznetsova@lada.org', 'TEAM', TRUE),
(5, 'Дмитрий', 'Смирнов', 'Технический специалист', 'dmitry.smirnov@lada.org', 'TEAM', TRUE);

-- Вставка тестовых проектов
INSERT IGNORE INTO projects (id, title, slug, category, status, short_description, start_date, end_date, location) VALUES
(1, 'Снегурочка года', 'snegurochka-goda', 'CONTEST', 'ACTIVE', 'Ежегодный конкурс красоты и талантов "Снегурочка года"', '2024-12-01', '2024-12-25', 'Москва, Концертный зал "Россия"'),
(2, 'Детские новогодние спектакли', 'detskie-novogodnie-spektakli', 'THEATER', 'ACTIVE', 'Новогодние представления для детей от молодежного театра "Отрада"', '2024-12-15', '2024-12-30', 'Москва, Театр "Отрада"'),
(3, 'Вечер друзей «Рождественский круг»', 'rozhdestvenskiy-krug', 'MUSIC', 'PLANNED', 'Музыкальный вечер в кругу друзей с рождественскими песнями', '2024-12-24', '2024-12-24', 'Москва, Культурный центр "ЛАДА"'),
(4, 'Благотворительная акция «Дайте мне шанс»', 'dayte-mne-shans', 'CHARITY', 'ACTIVE', 'Сбор средств для детей из малообеспеченных семей', '2024-01-15', '2024-12-31', 'По всей России'),
(5, 'Музыкальные вечера', 'muzykalnye-vechera', 'MUSIC', 'ANNUAL', 'Регулярные музыкальные вечера с участием талантливых исполнителей', '2024-03-01', '2024-11-30', 'Москва, Концертный зал филармонии');

-- Вставка тестовых видео для проектов
INSERT IGNORE INTO project_videos (id, project_id, title, youtube_url, is_main, sort_order) VALUES
(1, 1, 'Финал конкурса Снегурочка года 2023', 'https://www.youtube.com/watch?v=abc123', TRUE, 1),
(2, 1, 'Интервью с участницами', 'https://www.youtube.com/watch?v=def456', FALSE, 2),
(3, 2, 'Отрывок из спектакля "Новогодние чудеса"', 'https://www.youtube.com/watch?v=ghi789', TRUE, 1);

-- Вставка тестовых партнеров
INSERT IGNORE INTO project_partners (id, project_id, name, partnership_type, website_url, is_main, is_active) VALUES
(1, 1, 'Московский Дом Моды', 'GENERAL_SPONSOR', 'https://moda-house.ru', TRUE, TRUE),
(2, 1, 'Газета "Культура"', 'MEDIA_PARTNER', 'https://kultura-news.ru', FALSE, TRUE),
(3, 2, 'Театральный союз России', 'PARTNER', 'https://teatr-union.ru', TRUE, TRUE),
(4, 4, 'Фонд "Помощь детям"', 'SPONSOR', 'https://fond-detyam.ru', TRUE, TRUE),
(5, 5, 'Радио "Классика"', 'MEDIA_PARTNER', 'https://radio-klassika.ru', FALSE, TRUE);

-- Вставка тестовых связей проектов с командой
INSERT IGNORE INTO project_team_members (project_id, team_member_id, project_role) VALUES
(1, 1, 'Руководитель проекта'),
(1, 2, 'Координатор конкурса'),
(1, 3, 'Волонтер сопровождения'),
(2, 2, 'Режиссер-постановщик'),
(2, 4, 'PR-менеджер'),
(3, 1, 'Организатор мероприятия'),
(3, 5, 'Технический директор'),
(4, 4, 'Координатор акции'),
(5, 1, 'Арт-директор'),
(5, 4, 'PR-менеджер');

-- ============================================================
-- КОММЕНТАРИИ К ТАБЛИЦАМ (для документации)
-- ============================================================

-- projects:
-- Основная таблица проектов. Содержит всю информацию о мероприятии, включая настройки отображения.
-- Связана с таблицами: photo_gallery (галерея), media_file (обложка).

-- team_members:
-- Таблица членов команды организации. Может быть связана с пользователями системы (поле access_level).
-- Используется как для отображения на сайте, так и для внутреннего учета.

-- project_videos:
-- Видео проектов. Хранит ссылки на внешние видеохостинги (YouTube, Vimeo).
-- Поддерживается флаг основного видео (is_main) и порядок сортировки (sort_order).

-- project_partners:
-- Партнеры проектов. Могут иметь различные типы партнерства (спонсоры, медиа-партнеры и т.д.).
-- Поддерживается загрузка логотипов через связь с media_file.

-- project_team_members:
-- Таблица связи многие-ко-многим между проектами и членами команды.
-- Включает дополнительный атрибут - роль в конкретном проекте.

-- ============================================================
-- ВАЖНЫЕ ЗАМЕЧАНИЯ:
-- ============================================================

-- 1. Используется кодировка utf8mb4 для поддержки всех символов Unicode, включая эмодзи.
-- 2. Двигатель InnoDB для поддержки транзакций и внешних ключей.
-- 3. Все внешние ключи имеют соответствующие действия при удалении (CASCADE или SET NULL).
-- 4. Созданы индексы для оптимизации наиболее частых запросов.
-- 5. Тестовые данные вставлены с использованием INSERT IGNORE для избежания дублирования.
-- 6. Поля с типом TEXT не индексируются полностью, для них используются префиксные индексы где необходимо.
-- 7. Для полей URL созданы индексы с ограничением длины (100 символов) для экономии места.