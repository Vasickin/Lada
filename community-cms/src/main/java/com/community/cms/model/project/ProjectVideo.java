package com.community.cms.model.project;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

//Описание файла:
//Сущность для хранения видео проектов с поддержкой YouTube и Vimeo
//Аннотации валидации для URL и полей
//Индексы для оптимизации запросов
//Методы для получения ID видео из URL
//Методы для получения embed URL и миниатюр
//Поддержка сортировки и флага основного видео
//Форматирование длительности и количества просмотров

/**
 * Сущность видео проекта организации "ЛАДА".
 * Хранит ссылки на внешние видеохостинги (YouTube, Vimeo) и метаданные видео.
 *
 * @author Vasickin
 * @since 1.0
 */
@Entity
@Table(name = "project_videos",
        indexes = {
                @Index(columnList = "project_id", name = "idx_project_videos_project"),
                @Index(columnList = "is_main", name = "idx_project_videos_main"),
                @Index(columnList = "sort_order", name = "idx_project_videos_order"),
                @Index(columnList = "created_at", name = "idx_project_videos_created_at")
        })
public class ProjectVideo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Связь с проектом, к которому относится видео
     */
    @NotNull(message = "Проект обязателен для видео")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_project_videos_project"))
    private Project project;

    /**
     * Название видео
     */
    @NotBlank(message = "Название видео не может быть пустым")
    @Size(min = 3, max = 200, message = "Название видео должно содержать от 3 до 200 символов")
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * Описание видео
     */
    @Size(max = 1000, message = "Описание видео не должно превышать 1000 символов")
    @Column(name = "description", length = 1000)
    private String description;

    /**
     * URL видео на YouTube
     */
    @NotBlank(message = "URL видео не может быть пустым")
    @Pattern(regexp = "^(https?://)?(www\\.)?(youtube\\.com|youtu\\.be)/.+$",
            message = "URL видео должен быть корректной ссылкой на YouTube")
    @Column(name = "youtube_url", nullable = false, length = 500)
    private String youtubeUrl;

    /**
     * URL видео на Vimeo (альтернатива YouTube)
     */
    @Pattern(regexp = "^(https?://)?(www\\.)?vimeo\\.com/.+$",
            message = "URL видео должен быть корректной ссылкой на Vimeo")
    @Column(name = "vimeo_url", length = 500)
    private String vimeoUrl;

    /**
     * Длительность видео в секундах
     */
    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    /**
     * Является ли видео основным для проекта
     */
    @Column(name = "is_main", nullable = false)
    private boolean isMain = false;

    /**
     * Порядок сортировки видео в списке
     */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    /**
     * Количество просмотров видео (если доступно)
     */
    @Column(name = "view_count")
    private Long viewCount;

    /**
     * Дата публикации видео на YouTube/Vimeo
     */
    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    /**
     * Миниатюра видео (может быть загружена локально)
     */
    @Column(name = "thumbnail_path", length = 500)
    private String thumbnailPath;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // === КОНСТРУКТОРЫ ===

    /**
     * Конструктор по умолчанию (требуется JPA)
     */
    public ProjectVideo() {
        // JPA требует пустого конструктора
    }

    /**
     * Конструктор с основными полями для YouTube видео
     *
     * @param project Проект, к которому относится видео
     * @param title Название видео
     * @param youtubeUrl URL видео на YouTube
     */
    public ProjectVideo(Project project, String title, String youtubeUrl) {
        this.project = project;
        this.title = title;
        this.youtubeUrl = youtubeUrl;
    }

    /**
     * Конструктор с основными полями для Vimeo видео
     *
     * @param project Проект, к которому относится видео
     * @param title Название видео
     * @param vimeoUrl URL видео на Vimeo
     */
    public ProjectVideo(Project project, String title, String vimeoUrl, boolean isVimeo) {
        this.project = project;
        this.title = title;
        if (isVimeo) {
            this.vimeoUrl = vimeoUrl;
        } else {
            this.youtubeUrl = vimeoUrl;
        }
    }

    // === ГЕТТЕРЫ И СЕТТЕРЫ ===

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getYoutubeUrl() {
        return youtubeUrl;
    }

    public void setYoutubeUrl(String youtubeUrl) {
        this.youtubeUrl = youtubeUrl;
    }

    public String getVimeoUrl() {
        return vimeoUrl;
    }

    public void setVimeoUrl(String vimeoUrl) {
        this.vimeoUrl = vimeoUrl;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public boolean isMain() {
        return isMain;
    }

    public void setMain(boolean main) {
        isMain = main;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Long getViewCount() {
        return viewCount;
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // === ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ===

    /**
     * Получить URL видео (приоритет: YouTube, затем Vimeo)
     *
     * @return URL видео или null, если не указан
     */
    public String getVideoUrl() {
        if (youtubeUrl != null && !youtubeUrl.isEmpty()) {
            return youtubeUrl;
        } else if (vimeoUrl != null && !vimeoUrl.isEmpty()) {
            return vimeoUrl;
        }
        return null;
    }

    /**
     * Получить идентификатор YouTube видео
     *
     * @return YouTube ID или null, если URL не YouTube или не распознан
     */
    public String getYoutubeId() {
        if (youtubeUrl == null || youtubeUrl.isEmpty()) {
            return null;
        }

        try {
            // Обработка различных форматов YouTube URL
            String url = youtubeUrl.trim();

            // Формат: https://www.youtube.com/watch?v=VIDEO_ID
            if (url.contains("youtube.com/watch")) {
                String[] parts = url.split("v=");
                if (parts.length > 1) {
                    String videoId = parts[1];
                    // Удалить дополнительные параметры
                    int ampersandIndex = videoId.indexOf('&');
                    if (ampersandIndex != -1) {
                        videoId = videoId.substring(0, ampersandIndex);
                    }
                    return videoId;
                }
            }

            // Формат: https://youtu.be/VIDEO_ID
            if (url.contains("youtu.be/")) {
                String[] parts = url.split("youtu.be/");
                if (parts.length > 1) {
                    String videoId = parts[1];
                    // Удалить дополнительные параметры
                    int questionMarkIndex = videoId.indexOf('?');
                    if (questionMarkIndex != -1) {
                        videoId = videoId.substring(0, questionMarkIndex);
                    }
                    return videoId;
                }
            }

            // Формат: https://www.youtube.com/embed/VIDEO_ID
            if (url.contains("youtube.com/embed/")) {
                String[] parts = url.split("embed/");
                if (parts.length > 1) {
                    String videoId = parts[1];
                    // Удалить дополнительные параметры
                    int slashIndex = videoId.indexOf('/');
                    if (slashIndex != -1) {
                        videoId = videoId.substring(0, slashIndex);
                    }
                    int questionMarkIndex = videoId.indexOf('?');
                    if (questionMarkIndex != -1) {
                        videoId = videoId.substring(0, questionMarkIndex);
                    }
                    return videoId;
                }
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Получить идентификатор Vimeo видео
     *
     * @return Vimeo ID или null, если URL не Vimeo или не распознан
     */
    public String getVimeoId() {
        if (vimeoUrl == null || vimeoUrl.isEmpty()) {
            return null;
        }

        try {
            String url = vimeoUrl.trim();

            // Формат: https://vimeo.com/VIDEO_ID
            if (url.contains("vimeo.com/")) {
                String[] parts = url.split("vimeo.com/");
                if (parts.length > 1) {
                    String videoId = parts[1];
                    // Удалить дополнительные параметры
                    int slashIndex = videoId.indexOf('/');
                    if (slashIndex != -1) {
                        videoId = videoId.substring(0, slashIndex);
                    }
                    int questionMarkIndex = videoId.indexOf('?');
                    if (questionMarkIndex != -1) {
                        videoId = videoId.substring(0, questionMarkIndex);
                    }
                    return videoId;
                }
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Получить URL для встраивания видео (embed URL)
     *
     * @return Embed URL или null, если видео не поддерживается
     */
    public String getEmbedUrl() {
        String youtubeId = getYoutubeId();
        if (youtubeId != null) {
            return "https://www.youtube.com/embed/" + youtubeId;
        }

        String vimeoId = getVimeoId();
        if (vimeoId != null) {
            return "https://player.vimeo.com/video/" + vimeoId;
        }

        return null;
    }

    /**
     * Получить URL миниатюры видео
     *
     * @return URL миниатюры или стандартную миниатюру, если не указана
     */
    public String getThumbnailUrl() {
        // Если указан путь к локальной миниатюре
        if (thumbnailPath != null && !thumbnailPath.isEmpty()) {
            return "/uploads/videos/thumbnails/" + thumbnailPath;
        }

        // YouTube миниатюра
        String youtubeId = getYoutubeId();
        if (youtubeId != null) {
            return "https://img.youtube.com/vi/" + youtubeId + "/hqdefault.jpg";
        }

        // Vimeo миниатюра (требует API, поэтому возвращаем null)
        return null;
    }

    /**
     * Получить форматированную длительность видео (MM:SS)
     *
     * @return Форматированная строка длительности или null, если длительность не указана
     */
    public String getFormattedDuration() {
        if (durationSeconds == null) {
            return null;
        }

        int minutes = durationSeconds / 60;
        int seconds = durationSeconds % 60;

        return String.format("%d:%02d", minutes, seconds);
    }

    /**
     * Получить тип видеохостинга
     *
     * @return "youtube", "vimeo" или null
     */
    public String getVideoProvider() {
        if (youtubeUrl != null && !youtubeUrl.isEmpty()) {
            return "youtube";
        } else if (vimeoUrl != null && !vimeoUrl.isEmpty()) {
            return "vimeo";
        }
        return null;
    }

    /**
     * Проверить, является ли видео доступным (имеет URL)
     *
     * @return true, если видео имеет URL
     */
    public boolean isAvailable() {
        return (youtubeUrl != null && !youtubeUrl.isEmpty()) ||
                (vimeoUrl != null && !vimeoUrl.isEmpty());
    }

    /**
     * Получить отформатированное количество просмотров
     *
     * @return Форматированная строка просмотров (например, "1,234") или null, если не указано
     */
    public String getFormattedViewCount() {
        if (viewCount == null) {
            return null;
        }

        return String.format("%,d", viewCount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectVideo that = (ProjectVideo) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(youtubeUrl, that.youtubeUrl) &&
                Objects.equals(vimeoUrl, that.vimeoUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, youtubeUrl, vimeoUrl);
    }

    @Override
    public String toString() {
        return "ProjectVideo{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", youtubeUrl='" + youtubeUrl + '\'' +
                ", vimeoUrl='" + vimeoUrl + '\'' +
                ", isMain=" + isMain +
                '}';
    }
}
