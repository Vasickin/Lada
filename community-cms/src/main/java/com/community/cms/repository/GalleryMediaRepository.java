package com.community.cms.repository;

import com.community.cms.model.GalleryMedia;
import com.community.cms.model.MediaType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с медиафайлами галереи
 * Repository for gallery media files operations
 *
 * Обеспечивает доступ к данным о медиафайлах, связанных с элементами галереи
 * Provides data access for media files associated with gallery items
 *
 * @author Vasickin
 * @version 1.0
 * @since 2025
 * @see GalleryMedia
 * @see MediaType
 */
@Repository
public interface GalleryMediaRepository extends JpaRepository<GalleryMedia, Long> {

    /**
     * Находит все медиафайлы для элемента галереи, отсортированные по порядку
     * Finds all media files for gallery item, ordered by sort order
     *
     * @param galleryItemId ID элемента галереи / gallery item ID
     * @return список медиафайлов / list of media files
     */
    List<GalleryMedia> findByGalleryItemIdOrderBySortOrderAsc(Long galleryItemId);

    /**
     * Находит медиафайлы определенного типа для элемента галереи
     * Finds media files of specific type for gallery item
     *
     * @param galleryItemId ID элемента галереи / gallery item ID
     * @param mediaType тип медиа / media type
     * @return список медиафайлов / list of media files
     */
    List<GalleryMedia> findByGalleryItemIdAndMediaTypeOrderBySortOrderAsc(Long galleryItemId, MediaType mediaType);

    /**
     * Находит основной медиафайл для элемента галереи
     * Finds primary media file for gallery item
     *
     * @param galleryItemId ID элемента галереи / gallery item ID
     * @return Optional с основным медиафайлом / Optional with primary media file
     */
    Optional<GalleryMedia> findByGalleryItemIdAndIsPrimaryTrue(Long galleryItemId);

    /**
     * Находит все НЕ основные медиафайлы для элемента галереи
     * Finds all non-primary media files for gallery item
     *
     * @param galleryItemId ID элемента галереи / gallery item ID
     * @return список медиафайлов / list of media files
     */
    List<GalleryMedia> findByGalleryItemIdAndIsPrimaryFalseOrderBySortOrderAsc(Long galleryItemId);

    /**
     * Считает количество медиафайлов для элемента галереи
     * Counts media files for gallery item
     *
     * @param galleryItemId ID элемента галереи / gallery item ID
     * @return количество медиафайлов / count of media files
     */
    long countByGalleryItemId(Long galleryItemId);

    /**
     * Считает количество медиафайлов определенного типа для элемента галереи
     * Counts media files of specific type for gallery item
     *
     * @param galleryItemId ID элемента галереи / gallery item ID
     * @param mediaType тип медиа / media type
     * @return количество медиафайлов / count of media files
     */
    long countByGalleryItemIdAndMediaType(Long galleryItemId, MediaType mediaType);

    /**
     * Находит основной медиафайл для элемента галереи (JPQL версия)
     * Finds primary media file for gallery item (JPQL version)
     *
     * @param itemId ID элемента галереи / gallery item ID
     * @return Optional с основным медиафайлом / Optional with primary media file
     */
    @Query("SELECT gm FROM GalleryMedia gm WHERE gm.galleryItem.id = :itemId AND gm.isPrimary = true")
    Optional<GalleryMedia> findPrimaryMediaForItem(@Param("itemId") Long itemId);

    /**
     * Находит все медиафайлы для элемента с правильной сортировкой
     * Finds all media files for item with proper ordering
     *
     * @param itemId ID элемента галереи / gallery item ID
     * @return отсортированный список медиафайлов / sorted list of media files
     */
    @Query("SELECT gm FROM GalleryMedia gm WHERE gm.galleryItem.id = :itemId ORDER BY gm.isPrimary DESC, gm.sortOrder ASC")
    List<GalleryMedia> findAllByGalleryItemIdOrdered(@Param("itemId") Long itemId);

    /**
     * Находит медиафайлы по типу контента
     * Finds media files by content type
     *
     * @param fileType тип файла / file type (MIME)
     * @return список медиафайлов / list of media files
     */
    List<GalleryMedia> findByFileTypeContaining(String fileType);

    /**
     * Находит все изображения (фото) для элемента галереи
     * Finds all photos for gallery item
     *
     * @param galleryItemId ID элемента галереи / gallery item ID
     * @return список изображений / list of photos
     */
    @Query("SELECT gm FROM GalleryMedia gm WHERE gm.galleryItem.id = :itemId AND gm.mediaType = 'PHOTO' ORDER BY gm.sortOrder ASC")
    List<GalleryMedia> findPhotosByGalleryItemId(@Param("itemId") Long galleryItemId);

    /**
     * Находит все видео для элемента галереи
     * Finds all videos for gallery item
     *
     * @param galleryItemId ID элемента галереи / gallery item ID
     * @return список видео / list of videos
     */
    @Query("SELECT gm FROM GalleryMedia gm WHERE gm.galleryItem.id = :itemId AND gm.mediaType = 'VIDEO' ORDER BY gm.sortOrder ASC")
    List<GalleryMedia> findVideosByGalleryItemId(@Param("itemId") Long galleryItemId);

    /**
     * Удаляет все медиафайлы для элемента галереи
     * Deletes all media files for gallery item
     *
     * @param galleryItemId ID элемента галереи / gallery item ID
     */
    @Modifying
    @Query("DELETE FROM GalleryMedia gm WHERE gm.galleryItem.id = :itemId")
    void deleteByGalleryItemId(@Param("itemId") Long galleryItemId);

    /**
     * Удаляет медиафайлы определенного типа для элемента галереи
     * Deletes media files of specific type for gallery item
     *
     * @param galleryItemId ID элемента галереи / gallery item ID
     * @param mediaType тип медиа / media type
     */
    @Modifying
    @Query("DELETE FROM GalleryMedia gm WHERE gm.galleryItem.id = :itemId AND gm.mediaType = :mediaType")
    void deleteByGalleryItemIdAndMediaType(@Param("itemId") Long galleryItemId, @Param("mediaType") MediaType mediaType);

    /**
     * Обновляет порядок сортировки для медиафайла
     * Updates sort order for media file
     *
     * @param mediaId ID медиафайла / media file ID
     * @param sortOrder новый порядок сортировки / new sort order
     */
    @Modifying
    @Query("UPDATE GalleryMedia gm SET gm.sortOrder = :sortOrder WHERE gm.id = :mediaId")
    void updateSortOrder(@Param("mediaId") Long mediaId, @Param("sortOrder") Integer sortOrder);

    /**
     * Устанавливает основной медиафайл для элемента галереи
     * Sets primary media file for gallery item
     *
     * @param mediaId ID медиафайла / media file ID
     */
    @Modifying
    @Query("UPDATE GalleryMedia gm SET gm.isPrimary = true WHERE gm.id = :mediaId")
    void setAsPrimary(@Param("mediaId") Long mediaId);

    /**
     * Снимает статус основного со всех медиафайлов элемента галереи
     * Removes primary status from all media files of gallery item
     *
     * @param galleryItemId ID элемента галереи / gallery item ID
     */
    @Modifying
    @Query("UPDATE GalleryMedia gm SET gm.isPrimary = false WHERE gm.galleryItem.id = :itemId")
    void clearPrimaryStatus(@Param("itemId") Long galleryItemId);

    /**
     * Находит дубликаты файлов по имени и пути
     * Finds duplicate files by name and path
     *
     * @param fileName имя файла / file name
     * @param filePath путь к файлу / file path
     * @return список дубликатов / list of duplicates
     */
    List<GalleryMedia> findByFileNameAndFilePath(String fileName, String filePath);

    /**
     * Находит медиафайлы по размеру (больше указанного)
     * Finds media files by size (larger than specified)
     *
     * @param minSize минимальный размер / minimum size
     * @return список медиафайлов / list of media files
     */
    @Query("SELECT gm FROM GalleryMedia gm WHERE gm.fileSize > :minSize ORDER BY gm.fileSize DESC")
    List<GalleryMedia> findLargeFiles(@Param("minSize") Long minSize);

    /**
     * Получает статистику по типам медиа для элемента галереи
     * Gets media type statistics for gallery item
     *
     * @param galleryItemId ID элемента галереи / gallery item ID
     * @return массив с количеством [фото, видео] / array with counts [photos, videos]
     */
    @Query("SELECT " +
            "SUM(CASE WHEN gm.mediaType = 'PHOTO' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN gm.mediaType = 'VIDEO' THEN 1 ELSE 0 END) " +
            "FROM GalleryMedia gm WHERE gm.galleryItem.id = :itemId")
    Object[] getMediaTypeStatistics(@Param("itemId") Long galleryItemId);

    /**
     * Находит последние добавленные медиафайлы
     * Finds recently added media files
     *
     * @param limit ограничение количества / limit
     * @return список последних медиафайлов / list of recent media files
     */
    @Query("SELECT gm FROM GalleryMedia gm ORDER BY gm.createdAt DESC LIMIT :limit")
    List<GalleryMedia> findRecentMediaFiles(@Param("limit") int limit);

    /**
     * Считает количество медиафайлов по типу медиа
     * Counts media files by media type
     *
     * @param mediaType тип медиа / media type
     * @return количество медиафайлов / count of media files
     */
    long countByMediaType(MediaType mediaType);
}
