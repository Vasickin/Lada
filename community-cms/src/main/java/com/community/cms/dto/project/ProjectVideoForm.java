package com.community.cms.dto.project;

import com.community.cms.model.project.Project;
import com.community.cms.model.project.ProjectVideo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object (DTO) для формы создания и редактирования видео проекта.
 *
 * <p>Используется в административной панели для управления видео проектов.
 * Видео хранятся только как ссылки на внешние видеохостинги (YouTube, Vimeo).
 * Поддерживает установку основного видео для проекта.</p>
 *
 * @author Community CMS
 * @version 1.0
 * @since 2025
 * @see ProjectVideo
 */
public class ProjectVideoForm {

    private Long id;

    /**
     * Название видео.
     * Отображается в интерфейсе пользователя.
     */
    @NotBlank(message = "Название видео обязательно")
    @Size(min = 3, max = 255, message = "Название должно быть от 3 до 255 символов")
    private String title;

    /**
     * Описание видео (опционально).
     * Может содержать дополнительную информацию о видео.
     */
    @Size(max = 1000, message = "Описание не должно превышать 1000 символов")
    private String description;

    /**
     * URL видео на внешнем видеохостинге.
     * Только YouTube или Vimeo.
     */
    @NotBlank(message = "URL видео обязателен")
    @Pattern(
            regexp = "^(https?://)?(www\\.)?(youtube\\.com|youtu\\.be|vimeo\\.com)/.+$",
            message = "Поддерживаются только ссылки на YouTube и Vimeo"
    )
    private String youtubeUrl;

    /**
     * ID проекта, к которому относится видео.
     * Используется для привязки видео к проекту.
     */
    @NotNull(message = "Проект обязателен")
    private Long projectId;

    /**
     * Флаг основного видео проекта.
     * Основное видео отображается в начале страницы проекта.
     */
    private boolean isMain = false;

    /**
     * Длительность видео в секундах (опционально).
     * Используется для отображения длительности в интерфейсе.
     */
    private Integer durationSeconds;

    /**
     * Порядок сортировки в списке видео.
     * Меньшее значение = выше в списке.
     */
    @NotNull(message = "Порядок сортировки обязателен")
    private Integer sortOrder = 0;

    // ================== КОНСТРУКТОРЫ ==================

    /**
     * Конструктор по умолчанию.
     * Инициализирует значения по умолчанию.
     */
    public ProjectVideoForm() {
        this.sortOrder = 0;
        this.isMain = false;
    }

    /**
     * Конструктор на основе существующего видео проекта.
     * Используется для редактирования видео.
     *
     * @param video существующее видео проекта
     */
    public ProjectVideoForm(ProjectVideo video) {
        this();
        this.id = video.getId();
        this.title = video.getTitle();
        this.description = video.getDescription();
        this.youtubeUrl = video.getYoutubeUrl();
        this.projectId = video.getProject() != null ? video.getProject().getId() : null;
        this.isMain = video.isMain();
        this.durationSeconds = video.getDurationSeconds();
        this.sortOrder = video.getSortOrder();
    }

    // ================== ГЕТТЕРЫ И СЕТТЕРЫ ==================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public boolean isMain() {
        return isMain;
    }

    public void setMain(boolean main) {
        isMain = main;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    // ================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==================

    /**
     * Определяет тип видеохостинга по URL.
     *
     * @return тип видеохостинга или null если не поддерживается
     */
    public ProjectVideo.VideoType getVideoType() {
        if (youtubeUrl == null) {
            return null;
        }
        return ProjectVideo.VideoType.fromUrl(youtubeUrl);
    }

    /**
     * Извлекает ID видео из URL.
     *
     * @return ID видео или null если не удалось извлечь
     */
    public String getVideoId() {
        if (youtubeUrl == null) {
            return null;
        }
        return ProjectVideo.VideoType.extractVideoId(youtubeUrl);
    }

    /**
     * Проверяет валидность URL видео.
     *
     * @return true если URL валиден и поддерживается, иначе false
     */
    public boolean isValidVideoUrl() {
        return getVideoType() != null && getVideoId() != null;
    }

    /**
     * Проверяет является ли видео с YouTube.
     *
     * @return true если видео с YouTube, иначе false
     */
    public boolean isYouTubeVideo() {
        ProjectVideo.VideoType type = getVideoType();
        return type != null && type == ProjectVideo.VideoType.YOUTUBE;
    }

    /**
     * Проверяет является ли видео с Vimeo.
     *
     * @return true если видео с Vimeo, иначе false
     */
    public boolean isVimeoVideo() {
        ProjectVideo.VideoType type = getVideoType();
        return type != null && type == ProjectVideo.VideoType.VIMEO;
    }

    /**
     * Получает отформатированную длительность видео.
     * Пример: 125 → "2:05"
     *
     * @return отформатированная длительность или пустая строка
     */
    public String getFormattedDuration() {
        if (durationSeconds == null || durationSeconds <= 0) {
            return "";
        }

        int hours = durationSeconds / 3600;
        int minutes = (durationSeconds % 3600) / 60;
        int seconds = durationSeconds % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%d:%02d", minutes, seconds);
        }
    }

    /**
     * Получает короткий URL (для отображения в интерфейсе).
     *
     * @return короткая версия URL
     */
    public String getShortUrl() {
        if (youtubeUrl == null) {
            return "";
        }

        if (youtubeUrl.length() > 50) {
            return youtubeUrl.substring(0, 47) + "...";
        }
        return youtubeUrl;
    }

    /**
     * Преобразует ProjectVideoForm в сущность ProjectVideo.
     * Проект не устанавливается (только projectId).
     *
     * @return сущность ProjectVideo с заполненными базовыми полями
     */
    public ProjectVideo toEntity() {
        ProjectVideo video = new ProjectVideo();
        video.setId(this.id);
        video.setTitle(this.title);
        video.setDescription(this.description);
        video.setYoutubeUrl(this.youtubeUrl);
        video.setMain(this.isMain);
        video.setDurationSeconds(this.durationSeconds);
        video.setSortOrder(this.sortOrder);

        // Проект устанавливается отдельно в сервисе по projectId

        return video;
    }

    /**
     * Обновляет существующую сущность ProjectVideo данными из формы.
     *
     * @param video сущность для обновления
     */
    public void updateEntity(ProjectVideo video) {
        video.setTitle(this.title);
        video.setDescription(this.description);
        video.setYoutubeUrl(this.youtubeUrl);
        video.setMain(this.isMain);
        video.setDurationSeconds(this.durationSeconds);
        video.setSortOrder(this.sortOrder);

        // Проект обновляется отдельно в сервисе по projectId
    }

    /**
     * Генерирует embed код для вставки видео в HTML.
     *
     * @return HTML embed код или пустую строку
     */
    public String getEmbedCode() {
        String videoId = getVideoId();
        ProjectVideo.VideoType videoType = getVideoType();

        if (videoId == null || videoType == null) {
            return "";
        }

        if (videoType == ProjectVideo.VideoType.YOUTUBE) {
            return String.format(
                    "<iframe width=\"560\" height=\"315\" " +
                            "src=\"https://www.youtube.com/embed/%s\" " +
                            "title=\"%s\" " +
                            "frameborder=\"0\" " +
                            "allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture\" " +
                            "allowfullscreen></iframe>",
                    videoId, title
            );
        } else if (videoType == ProjectVideo.VideoType.VIMEO) {
            return String.format(
                    "<iframe src=\"https://player.vimeo.com/video/%s\" " +
                            "width=\"560\" height=\"315\" " +
                            "frameborder=\"0\" " +
                            "allow=\"autoplay; fullscreen; picture-in-picture\" " +
                            "allowfullscreen " +
                            "title=\"%s\"></iframe>",
                    videoId, title
            );
        }

        return "";
    }

    /**
     * Получает URL для превью видео (миниатюры).
     *
     * @return URL превью или null если не поддерживается
     */
    public String getThumbnailUrl() {
        String videoId = getVideoId();
        ProjectVideo.VideoType videoType = getVideoType();

        if (videoId == null || videoType == null) {
            return null;
        }

        if (videoType == ProjectVideo.VideoType.YOUTUBE) {
            return String.format("https://img.youtube.com/vi/%s/hqdefault.jpg", videoId);
        }

        // Для Vimeo нужно API запрос, возвращаем заглушку
        return null;
    }

    @Override
    public String toString() {
        return "ProjectVideoForm{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", projectId=" + projectId +
                ", isMain=" + isMain +
                ", sortOrder=" + sortOrder +
                '}';
    }
}
