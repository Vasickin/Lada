package com.community.cms.util;

import com.community.cms.domain.model.people.Partner;

/**
 * Утилиты для безопасной работы с PartnerType.
 * Обрабатывает возможные опечатки и несоответствия значений из базы данных.
 */
public class PartnerTypeUtils {

    /**
     * Безопасно преобразует строку в PartnerType.
     * Если значение не найдено в enum или есть опечатка, возвращает PartnerType.OTHER.
     */
    public static Partner.PartnerType safeValueOf(String value) {
        if (value == null || value.trim().isEmpty()) {
            return Partner.PartnerType.OTHER;
        }

        String normalizedValue = value.trim().toUpperCase();

        try {
            return Partner.PartnerType.valueOf(normalizedValue);
        } catch (IllegalArgumentException e) {
            // Логируем проблемное значение для отладки
            System.err.println("WARNING: Unknown PartnerType value in DB: '" + value + "' (normalized: '" + normalizedValue + "')");

            // Попробуем найти по похожим значениям
            for (Partner.PartnerType type : Partner.PartnerType.values()) {
                if (type.name().equalsIgnoreCase(normalizedValue) ||
                        type.getNameRu().equalsIgnoreCase(value) ||
                        type.getNameEn().replace(" ", "_").equalsIgnoreCase(normalizedValue)) {
                    return type;
                }
            }

            // Попробуем исправить распространенные опечатки
            return handleCommonTypos(normalizedValue);
        }
    }

    /**
     * Обрабатывает распространенные опечатки в значениях PartnerType.
     */
    private static Partner.PartnerType handleCommonTypos(String normalizedValue) {
        switch (normalizedValue) {
            case "SPONCER":
            case "SPONCERO":
            case "SPONSER":
            case "SPONSORR":
            case "SPONSORS":
                return Partner.PartnerType.SPONSOR;

            case "INFO_PARTNER":
            case "INFO":
            case "INFORMATION":
            case "INFORMATION_PARTNERS":
            case "INFO_PARTNERS":
                return Partner.PartnerType.INFORMATION_PARTNER;

            case "ORGANIZATION_PARTNER":
            case "ORGANIZER":
            case "ORGANIZATIONAL":
            case "ORGANIZATIONAL_PARTNERS":
                return Partner.PartnerType.ORGANIZATIONAL_PARTNER;

            case "TECH":
            case "TECHNICAL":
            case "TECHNICAL_PARTNERS":
                return Partner.PartnerType.TECHNICAL_PARTNER;

            case "GENERAL":
            case "MAIN_PARTNER":
            case "GENERAL_PARTNERS":
                return Partner.PartnerType.GENERAL_PARTNER;

            case "OTHER_PARTNER":
            case "DEFAULT":
            case "MISC":
            case "MISCELLANEOUS":
                return Partner.PartnerType.OTHER;

            default:
                // Если ничего не подошло, возвращаем OTHER
                return Partner.PartnerType.OTHER;
        }
    }

    /**
     * Проверяет является ли строка валидным PartnerType.
     */
    public static boolean isValidPartnerType(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        String normalizedValue = value.trim().toUpperCase();

        try {
            Partner.PartnerType.valueOf(normalizedValue);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Получает русское название типа партнерства по строковому значению.
     * Безопасно обрабатывает невалидные значения.
     */
    public static String getPartnerTypeNameRu(String value) {
        Partner.PartnerType type = safeValueOf(value);
        return type.getNameRu();
    }

    /**
     * Получает английское название типа партнерства по строковому значению.
     * Безопасно обрабатывает невалидные значения.
     */
    public static String getPartnerTypeNameEn(String value) {
        Partner.PartnerType type = safeValueOf(value);
        return type.getNameEn();
    }

    /**
     * Преобразует PartnerType в строку для сохранения в базу данных.
     */
    public static String toString(Partner.PartnerType type) {
        return type != null ? type.name() : Partner.PartnerType.OTHER.name();
    }
}
