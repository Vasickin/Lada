package com.community.cms.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * Валидатор для проверки ссылок на видео.
 * Поддерживает YouTube, Vimeo и Rutube.
 *
 * @since 2025
 */
public class VideoUrlValidator implements ConstraintValidator<VideoUrl, String> {

    // YouTube: https://www.youtube.com/watch?v=VIDEO_ID
    //         https://youtu.be/VIDEO_ID
    //         https://www.youtube.com/embed/VIDEO_ID
    private static final Pattern YOUTUBE_PATTERN = Pattern.compile(
            "^(https?://)?(www\\.)?(youtube\\.com/watch\\?v=|youtu\\.be/|youtube\\.com/embed/|youtube\\.com/v/)[a-zA-Z0-9_-]{11}(&[a-zA-Z0-9_=-]*)*$",
            Pattern.CASE_INSENSITIVE
    );

    // Vimeo: https://vimeo.com/VIDEO_ID
    private static final Pattern VIMEO_PATTERN = Pattern.compile(
            "^(https?://)?(www\\.)?vimeo\\.com/\\d+([?&][a-zA-Z0-9_=-]*)*$",
            Pattern.CASE_INSENSITIVE
    );

    // Rutube: https://rutube.ru/video/VIDEO_ID/
    //        https://rutube.ru/play/embed/VIDEO_ID
    private static final Pattern RUTUBE_PATTERN = Pattern.compile(
            "^(https?://)?(www\\.)?rutube\\.ru/(video|play/embed)/[a-zA-Z0-9_-]+([?&][a-zA-Z0-9_=-]*)*/?$",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Инициализация валидатора.
     */
    @Override
    public void initialize(VideoUrl constraintAnnotation) {
        // Дополнительная инициализация не требуется
    }

    /**
     * Проверяет валидность ссылки на видео.
     *
     * @param value проверяемая строка
     * @param context контекст валидации
     * @return true если ссылка валидна или пуста, иначе false
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Пустые значения разрешены (видео необязательно)
        if (value == null || value.trim().isEmpty()) {
            return true;
        }

        String url = value.trim();

        // Проверяем что это HTTP/HTTPS URL
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return false;
        }

        // Проверяем максимальную длину (дополнительно к @Size)
        if (url.length() > 2000) {
            return false;
        }

        // Проверяем соответствие одному из паттернов
        boolean isValid = YOUTUBE_PATTERN.matcher(url).matches() ||
                VIMEO_PATTERN.matcher(url).matches() ||
                RUTUBE_PATTERN.matcher(url).matches();

        // Дополнительные проверки
        if (isValid) {
            // Проверяем что нет опасных символов (XSS защита)
            if (url.contains("<") || url.contains(">") || url.contains("\"") || url.contains("'")) {
                return false;
            }

            // Проверяем YouTube ID (должен быть 11 символов)
            if (url.contains("youtube.com") || url.contains("youtu.be")) {
                String videoId = extractYouTubeId(url);
                if (videoId == null || videoId.length() != 11) {
                    return false;
                }
            }
        }

        return isValid;
    }

    /**
     * Извлекает ID видео из YouTube ссылки.
     */
    private String extractYouTubeId(String url) {
        // youtube.com/watch?v=ID
        if (url.contains("youtube.com/watch?v=")) {
            String[] parts = url.split("v=");
            if (parts.length > 1) {
                String id = parts[1];
                // Убираем параметры после &
                if (id.contains("&")) {
                    id = id.substring(0, id.indexOf("&"));
                }
                return id;
            }
        }
        // youtu.be/ID
        else if (url.contains("youtu.be/")) {
            String[] parts = url.split("youtu.be/");
            if (parts.length > 1) {
                String id = parts[1];
                // Убираем параметры после ?
                if (id.contains("?")) {
                    id = id.substring(0, id.indexOf("?"));
                }
                // Убираем слеш в конце
                if (id.endsWith("/")) {
                    id = id.substring(0, id.length() - 1);
                }
                return id;
            }
        }
        // youtube.com/embed/ID
        else if (url.contains("youtube.com/embed/")) {
            String[] parts = url.split("embed/");
            if (parts.length > 1) {
                String id = parts[1];
                // Убираем параметры после ?
                if (id.contains("?")) {
                    id = id.substring(0, id.indexOf("?"));
                }
                return id;
            }
        }

        return null;
    }
}